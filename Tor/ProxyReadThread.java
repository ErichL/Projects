package tor;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.*;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProxyReadThread extends Thread {

	private Socket socket;
	private ConnectionIdentifier ci;
	private ConcurrentLinkedQueue<Cell> buffer;
    private final int MAX_DATA_SIZE = 498;
    private AtomicBoolean connected;
    private AtomicBoolean beginFailed;

	public ProxyReadThread(Socket socket,ConnectionIdentifier ci, ConcurrentLinkedQueue<Cell> buffer, AtomicBoolean connected, AtomicBoolean beginFailed) {
		this.socket = socket;
		this.ci = ci;
		this.buffer = buffer;
        this.connected = connected;
        this.beginFailed = beginFailed;
	}

    public String read() {
        String s = null;
        String response = "";
        try {
            BufferedReader browserIn = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            boolean first = true;
            while((s = browserIn.readLine()) != null) {
                if(s.isEmpty()) {
                    break;
                }
                if(s.toLowerCase().startsWith("proxy-connection: keep-alive")) {
                    response += "Proxy-connection: close\r\n";
                } else if(s.toLowerCase().startsWith("connection: keep-alive")) {
                    response += "Connection: close\r\n";
                } else if(s.matches(".*HTTP/1.1.*")) {
                    s = s.replaceAll("HTTP/1.1", "HTTP/1.0");
                    response += s + "\r\n";
                } else {
                    response += s + "\r\n";
                }
                if(first) {
                    first = false;
                }
            }
            response += "\r\n\r\n";
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return response;
    }

    public void send(String response) {
        String host = null;
        int port = 80;
        String[] lines = response.split("\r\n");
        for(int i = 0 ; i < lines.length;i++) {
            if(lines[i].startsWith("CONNECT")) {
                System.out.println("CAN'T HANDLE CONNECT");
                return;
            }
            if(lines[i].startsWith("Host:")) {
                String[] hosts = lines[i].split(" ");
                host = hosts[1];
            }
        }
        if(host.contains(":")) {
            int pos = host.indexOf(":") + 1;
            port = Integer.parseInt(host.substring(pos));
            host = host.substring(0,pos - 1);
        }
        try {
            host = InetAddress.getByName(host).getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String hostIP = host + ":" + port + "\0";
        System.out.println("StreamID: " + ci.getStreamID());
        Cell streamBegin = new Cell(3, -1, -1, ci.getCircuit(), ci.getStreamID(), 1, hostIP.getBytes());
        buffer.add(streamBegin);
        System.out.println(response);
        System.out.flush();
        while (!connected.get()) {
            if (beginFailed.get()) {
                return;
            }
        }
        byte body[] = response.getBytes();
        int bodyLength = body.length;
        if (bodyLength > 498) {
            int pos = 0;
            while (bodyLength > 0) {
                if (bodyLength > 498) {
                    Cell sendRequest = new Cell(3, -1, -1, ci.getCircuit(), ci.getStreamID(), 2, Arrays.copyOfRange(body, pos, pos+498));
                    buffer.add(sendRequest);
                    bodyLength -= 498;
                    pos += 498;
                } else {
                    Cell sendRequest = new Cell(3, -1, -1, ci.getCircuit(), ci.getStreamID(), 2, Arrays.copyOfRange(body, pos, body.length));
                    pos += bodyLength;
                    bodyLength -= bodyLength;
                    buffer.add(sendRequest);
                }
            }
        } else {
            Cell sendRequest = new Cell(3, -1, -1, ci.getCircuit(), ci.getStreamID(), 2, body);
            buffer.add(sendRequest);
        }
    }

    public void run() {
        System.out.println("Proxy read thread started");
        System.out.flush();
        String request = "";
        request = read();
        if(request != "") {
            String[] lines = request.split("\r\n");
            send(request);
        }
	}
}
