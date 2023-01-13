/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 4
* Class: CSI 4321
*
************************************************/
package foop.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * Represents ACK
 * @author Maiqi Hou
 * @version 1.0
 *
 */
public class ACK extends Message {
	
	/**
	 * Constructs from given values
	 * @param msgID message ID
	 */
	public ACK(int msgID) {
		super.setMsgID(msgID);
	}
	
	
	/**
	 * Return string of from ACK
	 */
	@Override
	public String toString() {
		return "ACK: MsgID=" + getMsgID();
	}
	
	/**
	 * Encode Message (ACK);
	 * @throws IOException is IO problem
	 */
	@Override
	public byte[] encodeMsg() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();//write bytes to byte array
		int verison = 3; //get the protocol version
		int code = 3; // get ack code
		byte t = (byte) (verison << 4 | code);//combine version and code into one byte
		out.write(t);
		t = (byte) getMsgID();//get the message ID byte
		out.write(t);
		byte [] arr = out.toByteArray();//combine all information bytes into a byte array
		return arr;
	}
}
