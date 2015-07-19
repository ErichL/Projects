package tor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ProxyServerReadThread extends Thread{

	private Socket s;
	private ConcurrentLinkedQueue<Cell> buffer;
	private final int CELL_SIZE = 512;
	private final int MAX_BODY_LENGTH = CELL_SIZE - 14;
	private int circID;
	private int streamID;
	
	public ProxyServerReadThread(Socket s, ConcurrentLinkedQueue<Cell> buffer, int circID, int streamID) {
		this.s = s;
		this.buffer = buffer;
		this.circID = circID;
		this.streamID = streamID;
	}

	public void run() {
		BufferedInputStream hostIn;
		try {
			hostIn = new BufferedInputStream(s.getInputStream());
			while(true) {
		        byte[] buf = new byte[MAX_BODY_LENGTH];
		        int bytesRead = 0;
		        while(bytesRead < buf.length) {
		        	System.out.println("Bytes read: " + bytesRead);
		        	int chunk = hostIn.read(buf,bytesRead,buf.length - bytesRead);
		        	if(chunk == -1 ) {
		        		throw new IOException("Didn't get as much data as we should have!");
		        	}
		        	bytesRead += chunk;
		        }
		        Cell cell = new Cell(3,-1,-1,circID,streamID,2,buf);
		        buffer.add(cell);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
