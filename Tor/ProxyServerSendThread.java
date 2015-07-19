package tor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
public class ProxyServerSendThread extends Thread{
    private Socket s;
    private ConcurrentLinkedQueue<Cell> buffer;
    private ConcurrentLinkedQueue<Cell> routerBuffer;
    
    BufferedInputStream hostIn;
    public ProxyServerSendThread(Socket s, ConcurrentLinkedQueue<Cell> buffer, ConcurrentLinkedQueue<Cell> routerBuffer) {
        this.s = s;
        this.buffer = buffer;
        this.routerBuffer = routerBuffer;
    }
    
    public void run() {
		BufferedOutputStream hostOut;
		try {
			hostOut = new BufferedOutputStream(s.getOutputStream());
			while(true) {
				if(!buffer.isEmpty()) {
					Cell cell = buffer.remove();
					try {
		                hostOut.write(cell.getBody());
		                hostOut.flush();
		                //hostOut.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }



}
