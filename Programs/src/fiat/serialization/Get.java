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
 * Represents a Get and provides serialization/deserialization
 * @author Maiqi Hou
 * @version 1.3 
 * update getRequest() method
 * add getReuqestinfo method, which get all information after the request
 * change the type of Message request
 * fix some small problem, such super setTimestampe
 * fix string request to final string request
 */
public class Get extends Message {
	
	private static final String SP = " "; //string type space
	/**
	 * declare a message request GET
	 */
	public final String request = "GET";
	
	/**
	 * Override Message Request
	 */
	@Override
	public String getRequest() {
		return request;
	}
	
	/**
	 * get request information
	 * @return message request information
	 */
	@Override
	public String getRequestInfo() {
		return SP;
	}
	
	
	/**
	 * Constructs Get using set values
	 * @param messageTimestamp message time stamp
	 * @throws
	 * IllegalArgumentException if validation fails
	 */
	public Get(long messageTimestamp) {
		super.setTimestamp(messageTimestamp);
	}
	
	/**
	 * Returns string of the form
	 */
	@Override
	public String toString() {
		return "GET (TS=" + getTimestamp() + ")";
	}


	/**
	 * HashCode
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(request);
		return result;
	}


	/**
	 * Equals method
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass()) return false;
		Get other = (Get) obj;
		return Objects.equals(request, other.request);
	}
	
	
}
