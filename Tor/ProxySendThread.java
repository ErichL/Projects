package tor;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProxySendThread extends Thread{

	private Socket socket;
	private ConcurrentLinkedQueue<Cell> proxyRouterBuffer;
	private ConnectionIdentifier ci;
    private AtomicBoolean connected;
    private AtomicBoolean beginFailed;
	
	public ProxySendThread(Socket socket, ConnectionIdentifier ci, AtomicBoolean connected, AtomicBoolean beginFailed) {
		this.socket = socket;
		this.proxyRouterBuffer = Node.getSharedBuffer();
		this.ci = ci;
        this.connected = connected;
        this.beginFailed = beginFailed;
	}
	
	public void run() {
		System.out.println("Proxy send thread started");
        System.out.flush();
        BufferedOutputStream hostOut;
        try {
            hostOut = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
		while(true) {
			if(!proxyRouterBuffer.isEmpty()) {
				Cell cell = proxyRouterBuffer.peek();
                if (cell == null || socket.isClosed()) {
                    System.out.println("THIS IS NULL");
                    System.out.flush();
                } else if(cell.getCircuitID() == ci.getCircuit() && cell.getStreamID() == ci.getStreamID()) {
                    proxyRouterBuffer.remove();
                    try {
                        if (cell.getRelayCommand() == 3) {
                            hostOut.close();
                            socket.close();
                            break;
                        } else if (cell.getRelayCommand() == 4) {
                            connected.set(true);
                        } else if (cell.getRelayCommand() == 11) {
                            beginFailed.set(true);
                        } else {
                            hostOut.write(cell.getBody());
                            hostOut.flush();
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();

                    }
				}
			}
		}
        System.out.println("sending finished");
        System.out.flush();
	}
	
}
