package tor;

import java.net.Socket;

public class ConnectionIdentifier {

	private Socket socket;
	private int circuit;
	private boolean isLast;
	private int streamID;
	
	public ConnectionIdentifier(Socket socket,int circuit) {
		this.socket = socket;
		this.circuit = circuit;
		isLast = true;
		this.streamID = -1;
	}
	
	public ConnectionIdentifier(Socket socket,int circuit,boolean isLast) {
		this.socket = socket;
		this.circuit = circuit;
		this.isLast = isLast;
		this.streamID = -1;
	}
	
	public ConnectionIdentifier(int circuit, int streamID) {
		this.circuit = circuit;
		this.streamID = streamID;
		isLast = false;
		socket = null;
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public int getCircuit() {
		return circuit;
	}
	
	public boolean isLast() {
		return isLast;
	}
	
	public int getStreamID() {
		return streamID;
	}
	
	@Override
	public boolean equals(Object ci) {
		return (((ConnectionIdentifier) ci).getCircuit() == circuit && ((ConnectionIdentifier) ci).getSocket().equals(socket)) || (((ConnectionIdentifier) ci).getCircuit() == circuit && (((ConnectionIdentifier) ci).getStreamID() == (streamID)));
	}
	
	@Override
	public int hashCode() {
		return circuit + streamID;
	}
	
	
	
	
}
