/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 4
* Class: CSI 4321
*
************************************************/
package foop.serialization;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import fiat.serialization.MealType;

/**
 * Factory to create deserialized and serialized messages
 * @author Maiqi Hou
 * @version 1.1
 * update the addition decode message packet size (small) 
 *
 */
public class MessageFactory {
	// Standard Charsets UTF 8 for message input, message out, decode, and encode
	private static final Charset UTF8  = StandardCharsets.UTF_8; 
	private static final int CURRENTVERSION = 3; //Foop Current Version 
	private static final int ONE    = 1; //Foop Code operation(Item Addition) and constant 1
	//Foop Code operation(Error) and constant 2, and minimum packet size
	private static final int TWO    = 2; 
	private static final int ZERO   = 0; //Foop code operation(Register) and constant 0
	private static final int THREE  = 3; //use to convert little endian to big endian(Address)
	private static final int FOUR   = 4; //use to convert little endian to big endian(Address)
	private static final int FIVE   = 5; //use to convert little endian to big endian(Address)
	private static final int SIX    = 6; //use to convert little endian to big endian(port)
	private static final int SEVEN  = 7; //use to convert little endian to big endian(port)
	private static final int REGISTERPKT = 8;//Register Message packet size
	

	/**
	 * Constructs message through deserialization
	 * @param pkt buffer containing bytes a single message
	 * @return
	 * new deserialization message(Error, ACK, Register, and Addition)
	 * @throws NullPointerException if packet is null
	 * @throws IllegalArgumentException if validation failure, 
	 * 			such invalid message value, packet size problem 
	 */
	public static Message decode(byte [] pkt) {
		Objects.requireNonNull(pkt,"message packet is null");
		if(pkt.length < TWO) {
			throw new IllegalArgumentException("Packet Size small");
		}
		
		int version = high4(pkt[ZERO]);
		int code = low4(pkt[ZERO]);
		int msgID = convert8bitToInt(pkt[ONE]);
		
		checkCode(code);//check code whether valid
		//check version whether valid
		if(version != CURRENTVERSION) {
			throw new IllegalArgumentException("Invalid Foop version");
		}
		if(code == ONE) {//decode for addition item
			return additionMsg(pkt, msgID);
		}
		if(code == TWO) {//decode for Error
			return errorMsg(pkt, msgID);
		}
		if(code == ZERO) {//decode for register
			return registerMsg(pkt, msgID);
		}
		return ackMsg(pkt, msgID);//decode for ACK
		
	}
	
	/**
	 * Serializes message(ACK, Register, Error, Addition)
	 * @param msg message to serialize
	 * @return
	 * serialized byte array
	 */
	public static byte[] encode(Message msg) {
		//check message whether is null
		Objects.requireNonNull(msg, "message is null");
		try {
			return msg.encodeMsg();//decode message 
		} catch (IOException e) {
			throw new IllegalArgumentException("Message encode problem");
		}
	}
	
	
	
	/**
	 * Decode message for Register type
	 * @param pkt message packet will be decoded
	 * @param msgID message ID
	 * @return
	 * a new register 
	 */
	public static Register registerMsg(byte[] pkt, int msgID) {
		//check register whether out of range
		if(pkt.length > REGISTERPKT) {
			throw new IllegalArgumentException("Register packet out of range");
		//check register whether is too small
		}else if (pkt.length < REGISTERPKT) {
			throw new IllegalArgumentException("Register packet is small");
		}
		try {
			//Little endian to big endian Address
			byte [] temp = {pkt[FIVE], pkt[FOUR], pkt[THREE], pkt[TWO]}; 
			//little endian for port
			byte [] temp2 = {pkt[SIX], pkt[SEVEN]};
			Inet4Address address = (Inet4Address) Inet4Address.getByAddress(temp);
			//convert little endian 16 bit to port;
			int port = convert16bitToInt(temp2);
			return new Register(msgID, address, port);
		} catch (UnknownHostException e) {
			//if Host is unknown
			throw new IllegalArgumentException("Host Unkonw");
		}
	}
	
	/**
	 * Decode message for Addition type
	 * @param pkt message packet will be decoded
	 * @param msgID message ID
	 * @return
	 * a new addition message
	 */
	public static Addition additionMsg(byte[] pkt, int msgID) {
		try {
			int len = convert8bitToInt(pkt[TWO]);//get the name length
			int size = SEVEN + len;//the the whole packet size
			
			if(pkt.length > size) {//if packet is out of range
				throw new IllegalArgumentException("Addition Packet out of range");
			}
			if(pkt.length < size) {
				throw new IllegalArgumentException("Addition Packet is small");
			}
			int pos = THREE;//the pointer point to the byte of packet
			//get the name
			byte [] sarr = new byte[len];
			for(int i = 0 ; i < len; i++) {
				sarr[i] = pkt[i+THREE];
				pos++;
			}
			String name = new String(sarr, UTF8);
			char meal = (char)pkt[pos];
			pos += ONE;//skip a empty byte
			
			//check the byte between meal type and calories whether equal to 0
			if(convert8bitToInt(pkt[pos]) != ZERO) {
				throw new IllegalArgumentException("Error byte between meal type and calories");
			}
			
			//get calories
			byte[] carr = {pkt[pos+ONE], pkt[pos+TWO]};
			int calories = convert16bitToInt(carr);
			return new Addition(msgID, name, MealType.getMealType(meal), calories);
		} catch (ArrayIndexOutOfBoundsException e) {
			//if Addition packet size is too small
			throw new IllegalArgumentException("Addition Packet is small");
		}
		
	}
	
	/**
	 * Decode message for Error type
	 * @param pkt message packet will be decoded
	 * @param msgID message ID
	 * @return
	 * a new error message
	 */
	public static Error errorMsg(byte[] pkt, int msgID) {
		//get the Error message
		byte[] temp = new byte[pkt.length-TWO];
		for(int i = 0; i < pkt.length-TWO; i++) {
			temp[i] = pkt[i+TWO];
		}
		return new Error(msgID,new String(temp, UTF8));
	}
	
	/**
	 * Decode message for ACK type
	 * @param pkt message packet will be decode
	 * @param msgID message ID
	 * @return
	 * a new ACK message
	 */
	public static ACK ackMsg(byte[] pkt, int msgID) {
		//check ACK packet whether out of range
		if(pkt.length != TWO) {
			throw new IllegalArgumentException("ACK packet out of range");
		}
		return new ACK(msgID);
	}
	
	/**
	 * Check Message code whether valid
	 * @param code Message code(ACK, Register, Addition, Error)
	 */
	public static void checkCode(int code) {
		if(code != ZERO && code != ONE &&
			code != TWO && code != THREE) {
			throw new IllegalArgumentException("Invalid Foop code: " + code);
		}
	}
	
	/**
	 * Get the Message version
	 * @param b first byte on the message packet
	 * @return
	 * the message version
	 */
	public static int high4(byte b) {
		return ((b & 0xf0) >>> 4);
	}
	
	/**
	 * Get the message code
	 * @param b first byte on the message packet
	 * @return
	 * the message code
	 */
	public static int low4(byte b) {
		return (b & 0x0f);
	}
	
	/**
	 * Convert 8 unsigned bit to the Message ID/ calories(addition) 
	 * @param b the byte for message ID or calories
	 * @return
	 * the value of Message ID or calories
	 */
	public static int convert8bitToInt(byte b) {
		return b & 0xff;
	}
	
	/**
	 * Convert 16 bit unsigned bit to the Register port 
	 * @param b the bytes for Register port(little endian)
	 * @return
	 * the register port
	 */
	public static int convert16bitToInt(byte[] b) {
		return (b[ZERO] & 0x000000ff)|((b[ONE] & 0x000000ff) << 8);
	}
}
