package tor;

import java.nio.ByteBuffer;

public class Cell {

	private static final int CELL_SIZE = 512;
	private ByteBuffer cell;
	
	
	public Cell(ByteBuffer buf) {
		cell = buf;
	}
	
	public Cell(byte[] buf) {
		cell = ByteBuffer.wrap(buf);
	}
	
	//relay command?
	
	/*cellType:
	 * 1 = create
	 * 2 = created
	 * 3 = relay
	 * 4 = destroy
	 * 5 = open
	 * 6 = opened
	 * 7 = open failed
	 * 8 = create failed
	 */
	public Cell(int cellID,int agentOpenerID, int agentOpenedID, int circuitID, int streamID, int relayCommand, byte[] body) {
		cell = ByteBuffer.allocate(512);
		
		for(int i = 0 ; i < CELL_SIZE;i++) {
			cell.put(i,(byte)0);
		}
		
		
		if(cellID >= 5 && cellID <= 7) {
			cell.put(0,(byte)0);
			cell.put(1,(byte)0);
			setCellID(cellID);
			setAgentOpenerID(agentOpenerID);
			setAgentOpenedID(agentOpenedID);
		} else if(cellID == 1 || cellID == 2 || cellID == 4 || cellID == 8) {
			setCircuitID(circuitID);
			setCellID(cellID);
		} else {
			setCircuitID(circuitID);
			setCellID(cellID);
			setStreamID(streamID);
			if(body != null) {
				int length = body.length;
				setLength(length);
				setBody(body);
			}
			setRelayCommand(relayCommand);
		}
	}
	
	public void setBody(byte[] body) {
		for(int i = 0; i < body.length;i++) {
			cell.put(14+i,body[i]);
		}
	}
	
	public void setRelayCommand(int relayCommand) {
		cell.put(13,(byte)relayCommand);
	}
	
	public void setLength(int length) {
		cell.putShort(11,(short)length);
	}
	
	public void setStreamID(int streamID) {
		cell.putShort(3,(short)streamID);
	}
	
	public void setAgentOpenerID(int agentOpenerID) {
		cell.putInt(3,agentOpenerID);
	}
	
	public void setAgentOpenedID(int agentOpenedID) {
		cell.putInt(7,agentOpenedID);
	}
	
	public void setCircuitID(int circuitID) {
		cell.putShort(0,(short)circuitID);
	}

	public void setCellID(int cellID) {
		cell.put(2,(byte)cellID);
	}
	
	public int getBodyLength() {
		return (int)cell.getShort(11);
	}
	
	public int getCellID() {
		return (int)cell.get(2);
	}
	public int getAgentOpenerID() {
		return cell.getInt(3);
	}
	public int getAgentOpenedID() {
		return cell.getInt(7);
	}
	public int getCircuitID() {
		return (int)cell.getShort(0);
	}
	public int getStreamID() {
		return (int)cell.getShort(3);
	}
	public int getRelayCommand() {
		return (int)cell.get(13);
	}
	public byte[] getBody() {
		byte[] body = new byte[getBodyLength()];
		for(int i = 0; i < getBodyLength();i++) {
			body[i] = cell.get(i + 14);
		}
		return body;
	}
	
	public byte[] getBuffer() {
		return cell.array();
	}
	
	
	
	public String getHostPort() {
		String hostPort = new String(getBody()).split("\0")[0];
		//System.out.println("Host port is: " +hostPort);
		return hostPort;
	}
	public String getAgentID() {
		String agentID = new String(getBody()).split("\0")[1];
		return agentID;
	}
	
}
