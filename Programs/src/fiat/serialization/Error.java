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
 * Represents an Error and provides serialization/deserialization
 * @author Maiqi Hou
 * @version 1.3
 * update getRequest method
 * add getReuqestinfo method, which get all information after the request
 * fix some small problem, such super setTimestampe
 * update string request to final string request
 */
public class Error extends Message{
	private static final String SP = " ";  //string type space
	
	/**
	 * declare a message request ERROR
	 */
	public final String request = "ERROR";
	
	/**
	 * Error Message
	 */
	public String message;
	
	
	/**
	 * Constructs error message using set values
	 * @param messageTimestamp message time stamp
	 * @param errorMessage error message
	 * @throws
	 * IllegalArgumentException if validation fails
	 */
	public Error(long messageTimestamp, String errorMessage) {
		super.setTimestamp(messageTimestamp);
		setMessage(errorMessage);
	}
	
	
	
	/**
	 * Returns request
	 * @return request type
	 */
	@Override
	public String getRequest() {
		return request;
	}
	
	/**
	 * get the requestInfo
	 * @return the message request information
	 */
	@Override
	public String getRequestInfo() {
		int size = getMessage().length();
		return SP + size + SP + getMessage();
	}
	
	/**
	 * Returns string of the form
	 */
	@Override
	public String toString() {
		return "ERROR (TS="
				   + getTimestamp() +") message="
				   + getMessage();
	}
	
	
	/**
	 * Return error message
	 * @return
	 * error message
	 */
	public String getMessage() {
		return this.message;
	}
	
	
	/**
	 * Set error message
	 * @param message new error message
	 * @return
	 * this object with new value
	 * @throws
	 * IllegalArgumentException if invalid error message
	 */
	public Error setMessage(String message) {
		//call item class, use checkUnint and CheckCharList method
		Item item = new Item();
		
		//Check message whther is null
		if(message == null) {
			throw new IllegalArgumentException("message is null");
		}
		//check message character list
		if(item.CheckUnINT(message) == false || item.checkCharList(message)==false) {
			throw new IllegalArgumentException("message is invalid");
		}
		this.message = message;
		return this;
	}
		
	

	/**
	 * HashCode
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(message, request);
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
		Error other = (Error) obj;
		return Objects.equals(message, other.message) 
				&& Objects.equals(request, other.request);
	}
	
	
}
