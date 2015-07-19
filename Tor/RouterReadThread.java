package tor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.List;
	
public class RouterReadThread extends Thread {
	private Socket s;
	private ConcurrentLinkedQueue<Cell> buffer;
    private ConcurrentLinkedQueue<Cell> sharedBuffer;
	private ArrayList<Integer> circuitList;
	private ArrayList<Integer> streamList;
	private HashMap<Socket, ConcurrentLinkedQueue<Cell>> bufferTable;
	private HashMap<ConnectionIdentifier, ConnectionIdentifier> routingTable;
	private HashMap<Socket,ConnectionIdentifier> pendingConnections;
	private HashMap<Integer, ConcurrentLinkedQueue<Cell>> streamTable;
	private boolean firstPass;
	private boolean isOdd;
	private String peersList[][];
	private int circLength;
	private int localAgentID;
	private ConnectionIdentifier relayingIdentifier;
	private final int CELL_SIZE = 512;
	private final int MAX_CONNECTIONS = 100;
	private int proxyPortNum;
	
	public RouterReadThread(int proxyPortNum, Socket s, ConcurrentLinkedQueue<Cell> buffer, HashMap<ConnectionIdentifier, ConnectionIdentifier> routingTable, HashMap<Socket, ConcurrentLinkedQueue<Cell>> bufferTable, String [][] peersList, HashMap<Socket,ConnectionIdentifier> pendingConnections, ArrayList<Integer> circuitList) {
		this.s = s;
		this.proxyPortNum = proxyPortNum;
		this.buffer = buffer;
		this.bufferTable = bufferTable;
		this.routingTable = routingTable;
		this.peersList = peersList;
		this.pendingConnections = pendingConnections;
		this.relayingIdentifier = null;
		this.streamTable = new HashMap<Integer, ConcurrentLinkedQueue<Cell>>();
		this.circuitList = circuitList;
        this.sharedBuffer = Node.getSharedBuffer();
		streamList = new ArrayList<Integer>(MAX_CONNECTIONS);
		for(int i = 0; i < MAX_CONNECTIONS;i++) {
			streamList.add(0);
		}
		firstPass = true;
		isOdd = false;
		circLength = 0;
		localAgentID = -1;
		
	}
	
    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

	public void run() {
		System.out.println("In the router read thread, is the socket still open? " + s.isConnected());
		BufferedInputStream hostIn;
		try {
			hostIn = new BufferedInputStream(s.getInputStream());
			while(true) {
		        byte[] buf = new byte[CELL_SIZE];
		        int bytesRead = 0;
		        while(bytesRead < buf.length) {
		        	System.out.println("Bytes read: " + bytesRead);
		        	int chunk = hostIn.read(buf,bytesRead,buf.length - bytesRead);
		        	if(chunk == -1 ) {
		        		throw new IOException("Didn't get as much data as we should have!");
		        	}
		        	bytesRead += chunk;
		        }
		        Cell cell = new Cell(buf);
		        System.out.println("Cell ID should be 5: " + cell.getCellID());
	        	System.out.println("First two bytes should be 0: " + cell.getCircuitID());
	        	System.out.println("Relay Command: " + cell.getRelayCommand());
	        	System.out.println("From IP: " + s.getInetAddress().getHostAddress() + " Port: " + s.getPort());
	        	System.out.println("Current IP: " + s.getInetAddress().getHostAddress() + " Port: " + s.getLocalPort());
	        	handleCell(cell);
	        	bufferTable.put(s,buffer);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void extend(String ip, String port, int agentID, int circuitID) {
		String body = ip + ":" + port + "\0" + agentID;
		Cell extend = new Cell(3, -1, -1, circuitID, -1, 6, body.getBytes());
		buffer.add(extend);
	}

	/* For handling any cells received */
    public void handleOpen(Cell cell) {
    	localAgentID = cell.getAgentOpenedID();
    	if(firstPass) {
        	isOdd = false;
        	firstPass = false;
        }
    	cell.setCellID(6);
        buffer.add(cell);
        
    }
    public void handleOpened(Cell cell) {
    	localAgentID = cell.getAgentOpenerID();
    	if(firstPass) {
        	isOdd = true;
        	firstPass = false;
        }
    	int circuitID = -1;
    	System.out.println("Is it odd? " + isOdd);
    	for(int i = 0; i < circuitList.size();i++) {
    		if(isOdd && i%2 == 1 && circuitList.get(i) == 0) {
    			circuitID = i;
    			i = circuitList.size();
    		} else if(!isOdd && i%2 == 0 &&circuitList.get(i) == 0) {
    			circuitID = i;
    			i = circuitList.size();
    		}
    	}
    	if(circuitID == -1) {
    		System.out.println("CANT FIND AN OPEN CIRCUIT ID");
    	}
    	System.out.println("Circuit ID chosen: " + circuitID);
        Cell createCell = new Cell(1, -1, -1, circuitID, -1, -1, null);
        buffer.add(createCell);
    }
    public void handleCreate(Cell cell) {
        if(circuitList.get(cell.getCircuitID()) == 1) {
        	cell.setCellID(8);
        } else {
        	circuitList.add(cell.getCircuitID(),1);
        	cell.setCellID(2);
        }
        buffer.add(cell);
        
    }
    public void handleCreated(Cell cell) {
    	ConnectionIdentifier identifier = new ConnectionIdentifier(s, cell.getCircuitID());
		System.out.println("LOCAL SOCKET PORT IP: " + s.getInetAddress().getHostAddress() + " Port: " + s.getPort());
		
    	if(pendingConnections.containsKey(s)) {
    		System.out.println("Had a pending connection");
    		ConnectionIdentifier relayingIdentifier = pendingConnections.get(s);
    		
    		ConnectionIdentifier incomingIdentifier = new ConnectionIdentifier(s,cell.getCircuitID());
    		ConnectionIdentifier outgoingIdentifier = new ConnectionIdentifier(relayingIdentifier.getSocket(),relayingIdentifier.getCircuit());
    		//routingTable.put(relayingIdentifier, newIdentifier);
    		routingTable.put(incomingIdentifier, outgoingIdentifier);
    		routingTable.put(outgoingIdentifier, incomingIdentifier);
    		printRoutingTable();
    		//ConnectionIdentifier relayingThread = routingTable.get(identifier);
    		//send back extended
    		Cell extendedCell = new Cell(3, -1, -1, identifier.getCircuit(), -1, 7, null);
    		ConcurrentLinkedQueue<Cell> nextBuffer = bufferTable.get(identifier.getSocket());
			nextBuffer.add(extendedCell);
			System.out.println("Extending to: " + relayingIdentifier.getSocket().getInetAddress().getHostAddress() + " and IP: " + relayingIdentifier.getSocket().getPort());
			//System.out.println("Relaying identifier port: " + relayingIdentifier.getSocket().getLocalPort());
    	}else if (peersList != null) {
    		System.out.println("peers list is not null");
    		if (circLength < 3) {
    			int circuitID = cell.getCircuitID();
    			int random_router_no = randInt(0, peersList.length - 1);
    			String ip = peersList[random_router_no][0];
    	        String port = peersList[random_router_no][1];
    			//String port = "" + 7997;
    	        int agentID = Integer.parseInt(peersList[random_router_no][1]);
    			extend(ip, port, agentID, circuitID);
    		}
    	} else if(routingTable.containsKey(identifier)) {
			//relay cell by finding correct buffer to send to 
			ConnectionIdentifier nextRouter = routingTable.get(identifier);
			int circuitID = nextRouter.getCircuit();
			cell.setCircuitID(circuitID);
			ConcurrentLinkedQueue<Cell> routerBuffer = bufferTable.get(identifier.getSocket());
			routerBuffer.add(cell);
		}
    }
    public void handleBegin(Cell cell) {
        ConnectionIdentifier identifier = new ConnectionIdentifier(s,cell.getCircuitID());
        if(routingTable.containsKey(identifier)) {
			//relay cell by finding correct buffer to send to 
			ConnectionIdentifier nextRouter = routingTable.get(identifier);
			int circuitID = nextRouter.getCircuit();
			cell.setCircuitID(circuitID);
			ConcurrentLinkedQueue<Cell> routerBuffer = bufferTable.get(identifier.getSocket());
			routerBuffer.add(cell);
		} else {
			String[] hostPort = cell.getHostPort().split(":");
			String routerIP = hostPort[0];
			String routerPort = hostPort[1];
			Socket foreignSocket;
	        try {
	        	foreignSocket = new Socket(routerIP, Integer.parseInt(routerPort));
	        	ConcurrentLinkedQueue<Cell> streamBuffer = new ConcurrentLinkedQueue<Cell>();
	        	streamTable.put(cell.getStreamID(), streamBuffer); 
				Cell connectedCell = new Cell(3, -1, -1, cell.getCircuitID(), -1, 4, null);
				ProxyServerSendThread psrt = new ProxyServerSendThread(foreignSocket, streamBuffer, buffer);
				ProxyServerReadThread psst = new ProxyServerReadThread(foreignSocket,streamBuffer, cell.getCircuitID(), cell.getStreamID());
	            buffer.add(connectedCell);
	            psrt.start();
	            psst.start();
	        } catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Failed to connect to or resolve host");
				e.printStackTrace();
			}
		}
    }
    public void handleData(Cell cell) {
    	ConnectionIdentifier identifier = new ConnectionIdentifier(s,cell.getCircuitID());
        if(routingTable.containsKey(identifier)) {
			//relay cell by finding correct buffer to send to 
			ConnectionIdentifier nextRouter = routingTable.get(identifier);
			int circuitID = nextRouter.getCircuit();
			cell.setCircuitID(circuitID);
			ConcurrentLinkedQueue<Cell> routerBuffer = bufferTable.get(identifier.getSocket());
			routerBuffer.add(cell);
		} else {
			// use streamTable to find socket to write to, write to socket, get data
			// and send back as data stream
			ConcurrentLinkedQueue<Cell> streamBuffer = streamTable.get(cell.getStreamID());
			if (streamBuffer == null) {
				Node.getSharedBuffer().add(cell);
			} else {
				streamBuffer.add(cell);
			}
		}
    }
    public void handleEnd(Cell cell) {
    	ConnectionIdentifier identifier = new ConnectionIdentifier(s,cell.getCircuitID());
        if(routingTable.containsKey(identifier)) {
			//relay cell by finding correct buffer to send to 
			ConnectionIdentifier nextRouter = routingTable.get(identifier);
			int circuitID = nextRouter.getCircuit();
			cell.setCircuitID(circuitID);
			ConcurrentLinkedQueue<Cell> routerBuffer = bufferTable.get(identifier.getSocket());
			routerBuffer.add(cell);
		} else {
			//close tcp connection for streamID, remove streamID, socket pair from streamTable, streamList
			Node.getSharedBuffer().add(cell);
		}
    	
    }
    public void handleConnected(Cell cell) {
    	ConnectionIdentifier identifier = new ConnectionIdentifier(s,cell.getCircuitID());
        if(routingTable.containsKey(identifier)) {
			//relay cell by finding correct buffer to send to 
			ConnectionIdentifier nextRouter = routingTable.get(identifier);
			int circuitID = nextRouter.getCircuit();
			cell.setCircuitID(circuitID);
			ConcurrentLinkedQueue<Cell> routerBuffer = bufferTable.get(identifier.getSocket());
			routerBuffer.add(cell);
		} else {
            Node.getSharedBuffer().add(cell);
		}
    }
	public void handleExtend(Cell cell) {
		ConnectionIdentifier identifier = new ConnectionIdentifier(s,cell.getCircuitID());
		if(routingTable.containsKey(identifier)) {
			//relay cell by finding correct buffer to send to 
			ConnectionIdentifier nextRouter = routingTable.get(identifier);
			int circuitID = nextRouter.getCircuit();
			cell.setCircuitID(circuitID);
			ConcurrentLinkedQueue<Cell> routerBuffer = bufferTable.get(identifier.getSocket());
			routerBuffer.add(cell);
		} else {
			String hostPortFull = cell.getHostPort();
			String[] hostPort = hostPortFull.split(":");
			String routerIP = hostPort[0];
			String routerPort = hostPort[1];
			System.out.println("Extending to: " + routerIP + " Port: " + routerPort);
			int agentID = Integer.parseInt(cell.getAgentID());
	        try {
	        	boolean found = false;
	        	//check if tcp connection already exists, if it does then don't create another socket/threads
	        	for(Socket socket: bufferTable.keySet()) {
	        		System.out.println("IP: " + socket.getInetAddress().getHostAddress() + " Port: " + socket.getPort());
	        		
	        		//NOTE: CHANGE TO !FOUND WHEN TRYING TO FIX
	        		
	        		if(!found && routerPort.equals(""+socket.getPort()) && routerIP.equals(socket.getInetAddress().getHostAddress())) {
	        			int circuitID = -1;
	        			for(int i = 0; i < circuitList.size();i++) {
	        	    		if(isOdd && i%2 == 1 && circuitList.get(i) == 0) {
	        	    			circuitID = i;
	        	    			circuitList.add(i,1);
	        	    			i = circuitList.size();
	        	    		} else if(!isOdd && i%2 == 0 &&circuitList.get(i) == 0) {
	        	    			circuitID = i;
	        	    			circuitList.add(i,2);
	        	    			i = circuitList.size();
	        	    		}
	        	    	}
	        	    	if(circuitID == -1) {
	        	    		System.out.println("CANT FIND AN OPEN CIRCUIT ID");
	        	    	}
	        	        Cell createCell = new Cell(1, -1, -1, circuitID, -1, -1, null);
	        			//Cell open = new Cell(5, localAgentID, agentID, -1, -1, -1, null);
	    	            ConcurrentLinkedQueue<Cell> otherBuffer = bufferTable.get(socket);
	    	            otherBuffer.add(createCell);
	    	            found = true;
	    	            System.out.println("Found an existing TCP connection");
	    	            ConnectionIdentifier newIdentifier = new ConnectionIdentifier(socket,circuitID);
	    	            pendingConnections.put(socket, identifier);
	        		}
	        	}
	        	if(!found) {
		        	Socket routersocket = new Socket(routerIP, Integer.parseInt(routerPort));
		        	ConcurrentLinkedQueue<Cell> routerBuffer = new ConcurrentLinkedQueue<Cell>();
		        	pendingConnections.put(routersocket, identifier);
		        	System.out.println("Creating new connection to: " + routersocket.getPort());
		        	RouterReadThread rrt = new RouterReadThread(proxyPortNum, routersocket, routerBuffer, routingTable, bufferTable, null, pendingConnections, circuitList);
		        	RouterSendThread rst = new RouterSendThread(routersocket, routerBuffer);
		        	rrt.start();
		        	rst.start();
		        	System.out.println("wanting extend port " + s.getLocalPort() + " to: " + routersocket.getPort());
		        	rrt.setRelayingIdentifier(identifier);
		            Cell open = new Cell(5, localAgentID, agentID, -1, -1, -1, null);
		            routerBuffer.add(open);
	        	}
	            
	        } catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Failed to connect to or resolve host");
				e.printStackTrace();
			}
		}
	}
	
	public void setRelayingIdentifier(ConnectionIdentifier identifier) {
		relayingIdentifier = identifier;
	}
	
	public void handleExtended(Cell cell) {
		//printRoutingTable();
		ConnectionIdentifier identifier = new ConnectionIdentifier(s,cell.getCircuitID());
		int circuitID = cell.getCircuitID();
    	if (peersList != null) {
    		circLength++;
    		if (circLength < 3) {

    			int random_router_no = randInt(0, peersList.length-1);
				System.out.println("Extending again");
    			String ip = peersList[random_router_no][0];
    	        String port = peersList[random_router_no][1];
    	        //String port = "" + 7997;
    			int agentID = Integer.parseInt(peersList[random_router_no][1]);
    			extend(ip, port, agentID, circuitID);
    		} else {
    			ProxyBrowserThread pbt = new ProxyBrowserThread(proxyPortNum, buffer,circuitID);
                pbt.start();
    		}
    	} else {
    		System.out.println("Socket port " + s.getLocalPort());
    		System.out.println("Extending identifier: " + identifier.getSocket().getPort() + " Circuit: " + identifier.getCircuit());
			ConnectionIdentifier nextRouter = routingTable.get(identifier);
			System.out.println("RELAY EXTEND IP: " + nextRouter.getSocket().getInetAddress().getHostAddress() + " Port: " + nextRouter.getSocket().getPort());
			circuitID = nextRouter.getCircuit();
			cell.setCircuitID(circuitID);
			ConcurrentLinkedQueue<Cell> routerBuffer = bufferTable.get(nextRouter.getSocket());
			routerBuffer.add(cell);
    	}
	}
    public void handleBeginFailed(Cell cell) {
        ConnectionIdentifier identifier = new ConnectionIdentifier(s,cell.getCircuitID());
        if(routingTable.containsKey(identifier)) {
            //relay cell by finding correct buffer to send to
            ConnectionIdentifier nextRouter = routingTable.get(identifier);
            int circuitID = nextRouter.getCircuit();
            cell.setCircuitID(circuitID);
            ConcurrentLinkedQueue<Cell> routerBuffer = bufferTable.get(identifier.getSocket());
            routerBuffer.add(cell);
        } else {
            Node.getSharedBuffer().add(cell);
        }
    }
    public void handleExtendFailed(Cell cell) {
        ConnectionIdentifier identifier = new ConnectionIdentifier(s,cell.getCircuitID());
        if(routingTable.containsKey(identifier)) {
            //relay cell by finding correct buffer to send to
            ConnectionIdentifier nextRouter = routingTable.get(identifier);
            int circuitID = nextRouter.getCircuit();
            cell.setCircuitID(circuitID);
            ConcurrentLinkedQueue<Cell> routerBuffer = bufferTable.get(identifier.getSocket());
            routerBuffer.add(cell);
        } else {
            String hostPortFull = cell.getHostPort();
            String[] hostPort = hostPortFull.split(":");
            String routerIP = hostPort[0];
            String routerPort = hostPort[1];
            for (int i = 0; i < peersList.length; i++) {
                if (peersList[i][0] == routerIP && peersList[i][1] == routerPort) {
                    peersList[i][0] = "";
                }
            }
            String[][] temp = new String[peersList.length - 1][3];
            for (int i = 0; i < peersList.length; i++) {
                if (peersList[i][0] == "") {
                    i--;
                    continue;
                }
                temp[i][0] = peersList[i][0];
                temp[i][1] = peersList[i][1];
                temp[i][2] = peersList[i][2];
            }
            peersList = temp;

            System.out.println("Extending again");
            int random_router_no = randInt(0, peersList.length - 1);
            String ip = peersList[random_router_no][0];
            String port = peersList[random_router_no][1];
            //String port = "" + 7997;
            int agentID = Integer.parseInt(peersList[random_router_no][1]);
            extend(ip, port, agentID, cell.getCircuitID());
        }
    }
    public void handleDestroy(Cell cell) {
    	// destroy circuit

    }
    public void handleCreateFailed(Cell cell) {
    	// retry create
        localAgentID = cell.getAgentOpenerID();
        if(firstPass) {
            isOdd = true;
            firstPass = false;
        }
        int circuitID = -1;
        System.out.println("Is it odd? " + isOdd);
        for(int i = 0; i < circuitList.size();i++) {
            if(isOdd && i%2 == 1 && circuitList.get(i) == 0) {
                circuitID = i;
                circuitList.add(i,1);
                i = circuitList.size();
            } else if(!isOdd && i%2 == 0 &&circuitList.get(i) == 0) {
                circuitID = i;
                circuitList.add(i,2);
                i = circuitList.size();
            }
        }
        if(circuitID == -1) {
            System.out.println("CANT FIND AN OPEN CIRCUIT ID");
        }
        Cell createCell = new Cell(1, -1, -1, circuitID, -1, -1, null);
        buffer.add(createCell);
    }
    public void handleOpenFailed(Cell cell) {
        // retry open
        cell.setCellID(5);
        buffer.add(cell);
    }

    public void printRoutingTable() {
    	System.out.println("\nRouting Table:");
    	System.out.println("Incoming||Outgoing\nHost|Circuit|||Host|Circuit");
    	for(ConnectionIdentifier ci : routingTable.keySet()) {
    		ConnectionIdentifier value = routingTable.get(ci);
    		System.out.println(ci.getSocket().getPort() + " | " + ci.getCircuit() + " ||| " + value.getSocket().getPort() + " | " + value.getCircuit());
    	}
    	System.out.println();
    }
    
    /* End of Handling Cells Received */

    public void handleCell(Cell cell) {
        if (cell.getCellID() == 1) {
            handleCreate(cell);
        } else if (cell.getCellID() == 2) {
            handleCreated(cell);
        } else if (cell.getCellID() == 4) {
            //handleDestroy(cell);
        } else if (cell.getCellID() == 5) {
            handleOpen(cell);
        } else if (cell.getCellID() == 6) {
        	handleOpened(cell);
        } else if (cell.getCellID() == 7) {
            handleOpenFailed(cell);
        } else if (cell.getCellID() == 8) {
            handleCreateFailed(cell);
        } else if (cell.getCellID() == 3) { 
        	if (cell.getRelayCommand() == 1) {
	            handleBegin(cell);
	        } else if (cell.getRelayCommand() == 2) {
	            handleData(cell);
	        } else if (cell.getRelayCommand() == 3) {
	            handleEnd(cell);
	        } else if (cell.getRelayCommand() == 4) {
	            handleConnected(cell);
	        } else if (cell.getRelayCommand() == 6) {
	            handleExtend(cell);
	        } else if (cell.getRelayCommand() == 7) {
	            handleExtended(cell);
	        } else if (cell.getRelayCommand() == 11) {
	            handleBeginFailed(cell);
	        } else if (cell.getRelayCommand() == 12) {
	            handleExtendFailed(cell);
	        } else {
	            System.out.println("Cell Type Unknown");
	        }
        } else {
            System.out.println("Cell Type Unknown");
        } 
    }
	
	
}
