package tor;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Node {
	
	private static final int GROUP_NUM = 3716;
	
	private ServerSocket proxySocket;
	private static ConcurrentLinkedQueue<Cell> proxyRouterBuffer;
	private int groupNum;
	private int instanceNum;
	private int portNum;
	
	public Node(int groupNum, int instanceNum,int portNum) {
		proxyRouterBuffer = new ConcurrentLinkedQueue<Cell>();
		this.groupNum = groupNum;
		this.instanceNum = instanceNum;
		this.portNum = portNum;
	}
	
	public int getGroupNum() {
		return groupNum;
	}
	
	public int getInstanceNum() {
		return instanceNum;
	}
	
	public int getPortNum() {
		return portNum;
	}
	
	public static ConcurrentLinkedQueue<Cell> getSharedBuffer() {
		return proxyRouterBuffer;
	}
	
	public static void main(String[] args) {
		
		if (args.length == 3) {
			
			int port, groupNum, instanceNum;
			// address = InetAddress.getLocalHost();
			groupNum = Integer.parseInt(args[0]);
			instanceNum = Integer.parseInt(args[1]);
			port = Integer.parseInt(args[2]);
			Node node = new Node(groupNum,instanceNum,port);
			System.out.println("Starting proxy");
			//TorProxy proxy = new TorProxy(port,proxyRouterBuffer);
			//proxy.start();
			System.out.println("Starting router");
			TorRouter router = new TorRouter(port,GROUP_NUM, instanceNum,proxyRouterBuffer);
		} else {
			System.out.println("Usage: ./run <group number> <instance number> <HTTP Proxy Port>");
		}
	}
}
