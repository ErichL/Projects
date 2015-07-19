package tor;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProxyBrowserThread extends Thread {
	
	private ServerSocket proxySocket;
	private ConcurrentLinkedQueue<Cell> routerBuffer;
    private ArrayList<Integer> streamList;
    private final int MAX_CONNECTIONS = 100;
    private int circuitID;
	
	public ProxyBrowserThread(int portnum, ConcurrentLinkedQueue<Cell> routerBuffer, int circuitID){
		try {
            //hard coded host name
            this.proxySocket = new ServerSocket(portnum);
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
		this.routerBuffer = routerBuffer;
        this.circuitID = circuitID;
        streamList = new ArrayList<>(MAX_CONNECTIONS);
        for(int i = 0; i < MAX_CONNECTIONS;i++) {
            streamList.add(0);
        }
	}
	
	public void run() {
		System.out.println("Proxy browser thread started on socket: " + proxySocket.getLocalPort());
		while (true) {
			// call accept, once it is accepted spawn a thread and pass
			// it socket
			try {
				Socket s = proxySocket.accept();
                int streamID = -1;
                for(int i = 1; i < streamList.size();i++) {
                    if(streamList.get(i) == 0) {
                        streamID = i;
                        streamList.add(i, 1);
                        i = streamList.size();
                    }
                }
                if (streamID == -1) {
                    System.out.println("No streamIDs available");
                }
                AtomicBoolean connected = new AtomicBoolean();
                AtomicBoolean beginFailed = new AtomicBoolean();
                ConnectionIdentifier identifier = new ConnectionIdentifier(circuitID, streamID);
                ProxyReadThread prt = new ProxyReadThread(s, identifier, routerBuffer, connected, beginFailed);
				ProxySendThread pst = new ProxySendThread(s, identifier, connected, beginFailed);
                pst.start();
                prt.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
