package tor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RouterServerThread extends Thread{
	
	private ServerSocket routerSocket;
	private HashMap<ConnectionIdentifier, ConnectionIdentifier> routingTable;
	private HashMap<Socket, ConcurrentLinkedQueue<Cell>> bufferTable;
	private HashMap<Socket,ConnectionIdentifier> pendingConnections;
	private int proxyPortNum;
	private ArrayList<Integer> circuitList;
	
	public RouterServerThread(int proxyPortNum,int portNum, HashMap<ConnectionIdentifier, ConnectionIdentifier> routingTable, HashMap<Socket, ConcurrentLinkedQueue<Cell>> bufferTable, HashMap<Socket,ConnectionIdentifier> pendingConnections, ArrayList<Integer> circuitList) {
		try {
            //hard coded host name
			this.proxyPortNum = proxyPortNum;
            this.routerSocket = new ServerSocket(portNum);
            this.pendingConnections = pendingConnections;
            this.routingTable = routingTable;
            this.bufferTable = bufferTable;
            this.circuitList = circuitList;
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
	}
	public void run() {
		System.out.println("Router server socket listening to incoming connections");
		while (true) {
        	try {
	            Socket s = routerSocket.accept();
	            System.out.println("Router server socket just accepted a connection. Creating threads");
	            System.out.println("Router server socket created a new socket with local port: " + s.getLocalPort() + " connected port: " + s.getPort());
	            ConcurrentLinkedQueue<Cell> buffer = new ConcurrentLinkedQueue<Cell>();
	            RouterReadThread rrt = new RouterReadThread(proxyPortNum, s, buffer, routingTable, bufferTable, null, pendingConnections, circuitList);
	            RouterSendThread rst = new RouterSendThread(s, buffer);
	            rrt.start();
	            rst.start();
        	} catch (IOException e) {
    			e.printStackTrace();
        	}
        }
	}
	
	
}
