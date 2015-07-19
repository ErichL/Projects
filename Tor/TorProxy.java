/* package tor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TorProxy extends Thread{

	private ServerSocket proxySocket;
	private static ConcurrentLinkedQueue<Cell> proxyRouterBuffer;
	private static HashMap<ConnectionIdentifier,Socket> streamTable;
	
	public TorProxy(int portnum, ConcurrentLinkedQueue<Cell> proxyRouterBuffer) {
		//spawn server socket thread that listens for incoming browser connections
		//ProxyBrowserThread pbt = new ProxyBrowserThread(portnum,proxyRouterBuffer);
		//pbt.start();
		this.proxyRouterBuffer = proxyRouterBuffer;
		streamTable = new HashMap<ConnectionIdentifier,Socket>();
	}
	
	public void run() {
		//sits in a while loop that reads from shared buffer between proxy and server and checks stream id
		//if there is no socket bound to stream id, create one and spawn the 2 proxy threads
		//else one of the proxy threads will pull from the buffer instead
		while(true) {
			if(!proxyRouterBuffer.isEmpty()) {
				Cell cell = proxyRouterBuffer.peek();
				int streamID = cell.getStreamID();
				int circuitID = cell.getCircuitID();
				ConnectionIdentifier checker = new ConnectionIdentifier(circuitID,streamID);
				if(!streamTable.containsKey(checker)) {
					//create socket and spawn two threads
					String[] hostPort = cell.getHostPort().split(":");
					String host = hostPort[0];
					int port = Integer.parseInt(hostPort[1]);
					InetAddress address;
					try {
						address = InetAddress.getByName(host);
						Socket socket = new Socket(address,port);
						streamTable.put(checker, socket);
						ProxySendThread pbt = new ProxySendThread(socket,proxyRouterBuffer,checker); //TODO: Need router object to manage stream creation and passing off cells to router
						//ProxyReadThread prt = new ProxyReadThread(socket,checker);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println("Failed to connect to or resolve host");
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static void main(String [] args) {
		
	}
	
	
} */
