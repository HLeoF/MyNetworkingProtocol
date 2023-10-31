package foop.serialization;

import java.io.IOException;
import java.util.Objects;

/**
 * Represents generic portion of message
 * @author Maiqi Hou
 * @version 1.0
 *
 */
public abstract class Message {
	
	private static final int MININT = 0;  //Minimum message id value
	private static final int MAXINT = 255;//maximum message id value
	private int msgID; //Message ID
	
	/**
	 * Set messages ID
	 * @param msgID message ID
	 * @return this object with new value
	 * @throws IllegalArgumentException if message ID is out of range
	 */
	public Message setMsgID(int msgID) {
		//check message ID whether out of range
		if(msgID < MININT || msgID > MAXINT) {
			throw new IllegalArgumentException("msg ID is out of range");
		}
		this.msgID = msgID;
		return this;
	}
	
	/**
	 * Get Message ID
	 * @return message ID
	 */
	public int getMsgID() {
		return this.msgID;
	}

	
	/**
	 * encode Message information
	 * @return
	 * byte array for store message information
	 * @throws IOException if encode message failure
	 */
	public byte[] encodeMsg() throws IOException {
		return null;
	}
	
	/**
	 * Hash Code method
	 */
	@Override
	public int hashCode() {
		return Objects.hash(msgID);
	}

	
	/**
	 * Equals Methods
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Message other = (Message) obj;
		return msgID == other.msgID;
	}
	
	
}
