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
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * Represents a client registration
 * @author Maiqi Hou
 * @version 1.0
 *
 */
public class Register extends Message {
	private static final int MINPORT = 0; //minimum value of port
	private static final int MAXPORT = 65535; //maximum value of port
	private Inet4Address address;	//declare a private Inet4Address value
	private int port;				//declare a private port value
	
	/**
	 * Constructs register message
	 * @param msgID message id
	 * @param address address to register
	 * @param port port to register
	 * @throws IllegalArgumentException if validation if fails
	 */
	public Register(int msgID, Inet4Address address,int port) {
		super.setMsgID(msgID);
		setAddress(address);
		setPort(port);
	}
	
	
	@Override
	public String toString() {
		return "Register: MsgID=" + getMsgID() 
				+ " Address=" + getAddress().getHostAddress()
				+ " Port=" + getPort();
	}
	
	/**
	 * Get register address
	 * @return 
	 * register address
	 */
	public Inet4Address getAddress() {
		return this.address;
	}
	
	/**
	 * Set register address
	 * @param address register address
	 * @return
	 * address with new value
	 * @throws IllegalArgumentException if address is null, or is multicast, loopback
	 */
	public Register setAddress(Inet4Address address) {
		//check address whether is null
		if(address == null) {
			throw new IllegalArgumentException("IP address is null");
		}
		//check address whether is multicast address
		if(address.isMulticastAddress()) {
			throw new IllegalArgumentException("IP address is Multicast");
		}
		
		//check address whether is loopback address
		//if(address.isLoopbackAddress()) {
			//throw new IllegalArgumentException("IP address is loop back");
		//}
		this.address = address;
		return this;
	}
	
	
	/**
	 * Get register port
	 * @return
	 * register port
	 */
	public int getPort() {
		return this.port;
	}
	
	
	
	/**
	 * Set registration port
	 * @param port registration port
	 * @return
	 * the port with new value
	 * @throws IllegalArgumentException if port is out of range
	 */
	public Register setPort(int port) {
		//check port whether out of range
		if(port < MINPORT || port > MAXPORT) {
			throw new IllegalArgumentException("IP port is out of range");
		}
		this.port = port;
		return this;
	}
	
	/**
	 * Get the register socket address
	 * @return
	 * register socket address
	 */
	public InetSocketAddress getSocketAddress(){
		return new InetSocketAddress(this.address, this.port);
	}

	
	/**
	 * Encode message for Register Type
	 * @throws IOException IO problem if write byte have problem
	 */
	@Override
	public byte[] encodeMsg() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int verison = 3; //get the protocol version 3
		int code = 0;   //register code is 0
		//combine version and code into one byte
		byte t = (byte) (verison << 4 | code);
		out.write(t);
		t = (byte) getMsgID(); //get Message ID byte
		out.write(t);
		byte[] temp = getAddress().getAddress();//get the IP address bytes
		//convert 32 bit IP address
	 	temp = convertLittle(temp);
		out.write(temp);
		//convert covert port to 16 bits
		temp = convertIntto16bit(getPort());
		out.write(temp);
		//combine all bytes into a byte array
		byte [] arr = out.toByteArray();
		return arr;
		
	}
	
	/**
	 * Convert big endian order address to little endian order
	 * @param arr big endian order address
	 * @return
	 * little endian order address
	 */
	public static byte[] convertLittle(byte[] arr) {
		byte[] temp = new byte[arr.length];
		int c = arr.length-1;
		for(int i = 0; i < arr.length; i++) {
			temp[c] = arr[i];
			c--;
		}
		return temp;
	}
	
	/**
	 * Convert Integer value to little endian 16bit 
	 * @param val register port
	 * @return
	 * little endian 16 bit endian
	 */
	public static byte[] convertIntto16bit(int val) {
		return new byte[] {(byte) (val & 0x000000ff), 
				(byte) (val >>> 8 & 0x00000ff)};
	}
	
	
	/**
	 * Register hash code method
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(address, port);
		return result;
	}


	/**
	 * Register Equals method
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass()) return false;
		Register other = (Register) obj;
		return Objects.equals(address, other.address) 
				&& port == other.port;
	}
	
	
}
