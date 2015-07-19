package tor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TorRouter{
    private static HashMap<ConnectionIdentifier, ConnectionIdentifier> routingTable;
    private static HashMap<Socket, ConcurrentLinkedQueue<Cell>> bufferTable;
    private static HashMap<Socket,ConnectionIdentifier> pendingConnections;
    private static ArrayList<ConnectionIdentifier> connections; // List of ip port the router is connected to
    private ConcurrentLinkedQueue<Cell> proxyRouterBuffer;
    private static ArrayList<Integer> circuitList;
    private static String[][] peersList;
    private static int port = 7997;
    private static int circ_length = 0;
    private static int agent_id;
    private static int proxyPortNum;
	private final int MAX_CONNECTIONS = 100;

    public TorRouter(int proxyPortNum, int groupNum, int instanceNum,ConcurrentLinkedQueue<Cell> proxyRouterBuffer) {
    	System.out.println("TorRouter initializing");
    	this.proxyPortNum = proxyPortNum;
    	circuitList = new ArrayList<Integer>(MAX_CONNECTIONS);
    	for(int i = 0; i < MAX_CONNECTIONS;i++) {
			circuitList.add(0);
		}
    	pendingConnections = new HashMap<Socket,ConnectionIdentifier>();
        routingTable = new HashMap<ConnectionIdentifier, ConnectionIdentifier>();
    	agent_id = (groupNum << 16) | instanceNum;
        this.proxyRouterBuffer = proxyRouterBuffer;
        String routerName = String.format("Tor61Router-%s-%s", groupNum, instanceNum);
        bufferTable = new HashMap<Socket,ConcurrentLinkedQueue<Cell>>();
        RouterServerThread thread = new RouterServerThread(proxyPortNum,port,routingTable,bufferTable,pendingConnections, circuitList);
		thread.start();
		System.out.println("Tor61 Router is up on port " + port);
		register(Integer.toString(port), routerName, Integer.toString(agent_id));
		peersList = getPeers("Tor61Router-0002");
        createCircuit();
    }

    public void register(String port, String name, String data) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python2.7","registration_client.py", port, name, data);
            pb.inheritIO();
            Process p = pb.start();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static String fetch(String name) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python2.7","fetch.py", name);
            //pb.inheritIO();
            Process p = pb.start();
            // read output, line by line
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String output = "";
            String line;
            while ((line = in.readLine()) != null ) {
              output += line + "\n";
            }
            return output;
            //p.waitFor();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    public static String[][] getPeers(String name) {
        String[] peers = fetch(name).split("\n");
        String[][] peers_arr = new String[peers.length][3];
        for (int i = 0; i < peers_arr.length; i++) {
        	System.out.println(peers[i]);
        	String[] data = peers[i].split("\t");
        	peers_arr[i][0] = data[0];
        	peers_arr[i][1] = data[1];
        	peers_arr[i][2] = data[2];
        }
        return peers_arr;

    }


    public static void createCircuit() {
        System.out.println("Creating Circuit");
        int random_router_no = randInt(0, peersList.length - 1);
        String r_ip = peersList[random_router_no][0];
        String r_port = peersList[random_router_no][1];
        //String r_port = "" + port;
        int r_agent_id = Integer.parseInt(peersList[random_router_no][1]);
        Socket routersocket;
        try {
        	System.out.println("Chosen IP: " + r_ip);
        	System.out.println("Chosen port: " + r_port);
			routersocket = new Socket(r_ip, Integer.parseInt(r_port));
			
			System.out.println("router socket is connected: " +routersocket.isConnected() + " with the port: " + routersocket.getLocalPort() + " to this port: " + routersocket.getPort());
        	ConcurrentLinkedQueue<Cell> buffer = new ConcurrentLinkedQueue<Cell>();
        	RouterReadThread rrt = new RouterReadThread(proxyPortNum,routersocket, buffer, routingTable, bufferTable,peersList,pendingConnections, circuitList);
        	RouterSendThread rst = new RouterSendThread(routersocket, buffer);
        	rrt.start();
        	rst.start();
            Cell open = new Cell(5, agent_id, r_agent_id, -1, -1, -1, null);
            buffer.add(open);
        } catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Failed to connect to or resolve host");
			e.printStackTrace();
		}
    }
    public static void main(String[]args) {
    	//System.out.println("Getting peers list");
    	
    }
}