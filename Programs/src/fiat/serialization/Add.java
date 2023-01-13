/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 1
* Class: CSI 4321
*
************************************************/
package fiat.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Represents an Add and provides serialization/deserialization
 * @author Maiqi Hou
 * @version 1.3
 * update getRequest()
 * add getReuqestinfo method(), which get all information after the request
 * fix some small problem, such super setTimestampe
 * update string request to final string request
 */
public class Add extends Message {
	
	private static final String SP     = " ";//string type space 
	
	// Standard Charsets UTF 8 for message input, message output, encode, decode
	private static final Charset UTF8  = StandardCharsets.UTF_8; 
	
	
	/**
	 * declare the string request "ADD"
	 */
	public final String request = "ADD";
	
	/**
	 * declare a item 
	 */
	 Item item;
	
	
	
	/**
	 * Returns request
	 * @return request type
	 */
	@Override
	public String getRequest(){
		return request;
	}

	/**
	 * get request information
	 * @return request information
	 */
	@Override
	public String getRequestInfo() {
		MessageOutput o = new MessageOutput(new ByteArrayOutputStream());
		try {
			//encode the item information
			ItemFactory.encode(getItem(), o);
		} catch (IOException e) {}
		byte b[] = o.toByteArray();//item information to byte array
		String item = "";
		item = new String(b,UTF8);
		//get item encode inform
		return SP + item;
	}
	
	/**
	 * Constructs new Add using attribute value
	 * @param messageTimestamp message time stamp
	 * @param item new item
	 * @throws IllegalArgumentException if validation fails 
	 */
	public Add(long messageTimestamp, Item item) {
		super.setTimestamp(messageTimestamp);
		setItem(item);
	}
	
	/**
	 * Returns string of the form
	 */
	@Override
	public String toString() {
		return "ADD (TS=" 
	               + getTimestamp() + ") item=" 
				   + item.toString();
	}
	
	/**
	 * Return item
	 * @return item to add
	 */
	public Item getItem() {
		return this.item;
	}
	
	
	
	/**
	 * Sets item
	 * @param item new item
	 * @return
	 * this object with new value
	 * @throws IllegalArgumentException if null item
	 */
	public Add setItem(Item item) {
		//check if item is null
		if(item == null) {
			throw new IllegalArgumentException("Item is null");
		}
		this.item = item;
		return this;
	}


	/**
	 * HashCode
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(item, request);
		return result;
	}


	/**
	 * equals
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass()) return false;
		Add other = (Add) obj;
		return Objects.equals(item, other.item) && 
				Objects.equals(request, other.request);
	}
	
}
