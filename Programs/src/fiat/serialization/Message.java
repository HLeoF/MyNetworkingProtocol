/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 1
* Class: CSI 4321
*
************************************************/
package fiat.serialization;

import java.util.Objects;

/**
 * Represents generic portion message
 * @author Maiqi Hou
 * @version 1.3
 * add getReuqestinfo method(), which get all information after the request
 * Update setTimestamp method(), return message
 * update Message class to Abstract Message
 * fix some small problem, such super setTimestampe
 */
public abstract class Message {
	
	private static final long ZERO    = 0;              //time stamp min value
	private static final long MAXTIME = Long.MAX_VALUE; //time stamp max value

	
	/**
	 * declare the time stamp
	 */
	public long tiemstamp;
	
	/**
	 * Returns time stamp
	 * @return 
	 * message time stamp
	 */
	public long getTimestamp() {
		return this.tiemstamp;
	}
	
	
	/**
	 * set Message Time stamp
	 * @param timestamp Message time stamp
	 * @return Message time stamp
	 */
	public Message setTimestamp(long timestamp) {
		//check message time stamp whether valid
		if(timestamp < ZERO || timestamp > MAXTIME) {
			throw new IllegalArgumentException("TimeStamp is invalid");
		}
		this.tiemstamp = timestamp;
		return this;
	}
	
	
	/**
	 * Returns request
	 * @return message request
	 */
	public String getRequest() {
		return "";
	}


	/**
	 * get request information
	 * @return message request information
	 */
	public String getRequestInfo() {
		return "";
	}


	@Override
	public int hashCode() {
		return Objects.hash(tiemstamp);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Message other = (Message) obj;
		return tiemstamp == other.tiemstamp;
	}
	
}
