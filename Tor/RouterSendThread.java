package tor;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RouterSendThread extends Thread{
	Socket s;
	ConcurrentLinkedQueue<Cell> buffer;
	public RouterSendThread(Socket s, ConcurrentLinkedQueue<Cell> buffer) {
		this.s = s;
		this.buffer = buffer;
	}
	public void run() {
		while(true) {
			if(!buffer.isEmpty()) {
				System.out.println("Sending cell");
				Cell cell = buffer.remove();
				try {
					BufferedOutputStream hostOut = new BufferedOutputStream(s.getOutputStream());
					hostOut.write(cell.getBuffer());
					hostOut.flush();
	                //hostOut.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				
				}
			}
		}
	}
}