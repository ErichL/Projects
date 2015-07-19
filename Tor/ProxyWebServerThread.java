package tor;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class ProxyWebServerThread {
    private Socket s;
    private ConcurrentLinkedQueue<Cell> buffer;
    private int circuitID;
    private int streamID;
    BufferedOutputStream hostOut;
    BufferedInputStream hostIn;
    public ProxyWebServerThread(Socket s, ConcurrentLinkedQueue<Cell> buffer, int circID, int streamID) {
        this.s = s;
        this.buffer = buffer;
        circuitID = circID;
        this.streamID = streamID;
        try {
            hostOut = new BufferedOutputStream(s.getOutputStream());
            hostIn = new BufferedInputStream(s.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //The cells will read from buffer
    public void sendToServer(Cell cell) {
        try {
            byte[] toServer = cell.getBody();
            int countbuf, count;
            countbuf = 0;
            count = 0;
            String str, str2;
            hostOut.write(toServer, 0, countbuf);
            hostOut.flush();
            byte[] buf = new byte[s.getReceiveBufferSize()];
            if (count > 0) {
                //sendToRouter(buf);
            }
            
            s.close();
            hostIn.close();
            hostOut.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void sendToRouter(byte[] body) {
        int bodyLength = body.length;
        int pos = 0;
        if (bodyLength > 498) {
            while (bodyLength > 0) {
                if (bodyLength > 498) {
                    Cell cell = new Cell(3, -1, -1, circuitID, streamID, 2, Arrays.copyOfRange(body, pos, pos+498));
                    pos += 498;
                    bodyLength -= 498;
                    buffer.add(cell);
                } else {
                    Cell cell = new Cell(3, -1, -1, circuitID, streamID, 2, Arrays.copyOfRange(body, pos, pos+498));
                    pos += bodyLength;
                    bodyLength -= bodyLength;
                    buffer.add(cell);
                }
            }
        } else {
            Cell cell = new Cell(3, -1, -1, circuitID, streamID, 2, body);
            buffer.add(cell);
        }
    }

}
