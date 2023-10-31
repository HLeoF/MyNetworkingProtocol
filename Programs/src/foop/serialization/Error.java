package foop.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Pattern;


/**
 * Represents error
 * @author Maiqi Hou
 * @version 1.1
 * Update checking message 
 *
 */
public class Error extends Message {
	// Standard Charsets UTF 8 for message input, message out, decode, and encode
	private static final Charset UTF8   = StandardCharsets.UTF_8; 
	private static final String UNICODE = "\\p{N}|\\p{L}|\\p{P}|\\p{S}"; //for check valid unicode categroy
	private static final String SP      = " ";                  //String space
	private static final int MAXSTRING  = 65505; //max error message length
	
	
	private String message; //error message
	
	/**
	 * Constructs from given values
	 * @param msgID message ID
	 * @param errorMessage error message
	 * @throws IllegalArgumentException if validation fails
	 */
	public Error(int msgID, String errorMessage) {
		super.setMsgID(msgID);
		setMessage(errorMessage);
	}
	
	/**
	 * Return string of the form
	 */
	@Override
	public String toString() {
		return "Error: MsgID="+getMsgID()+ " Message="+getMessage();
	}
	
	/**
	 * Set error message
	 * @param message error message
	 * @return this error message with new value
	 * @throws IllegalArgumentException if validation fails
	 */
	public Error setMessage(String message) {
		if(message == null) {
			throw new IllegalArgumentException("Foop Message is null");
		}
		if(message.length() > MAXSTRING) {
			throw new IllegalArgumentException("Error message is too long");
		}
		if(checkCharList(message)==false) {
			throw new IllegalArgumentException("Foop Message is invalid");
		}
		this.message = message;
		return this;
	}
	
	/**
	 * Check error message valid 
	 * @param msg error message
	 * @return if error message valid return true
	 * 		   if error message valid return false
	 */
	private boolean checkCharList(String msg) {
		for(int i = 0; i < msg.length(); i++) {
			String temp = Character.toString(msg.charAt(i));
			//check character meet space, letter, Number, Punctuation, or Symbol Unicode 
			if(Pattern.matches(UNICODE, temp) == false && !temp.equals(SP)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get error message
	 * @return error message
	 */
	public String getMessage() {
		return this.message;
	}

	
	/**
	 * encode Message Error type
	 * @throws IOException  IO problem if write bytes have problem
	 */
	@Override
	public byte[] encodeMsg() throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int verison = 3; //get the protocol version 3
		int code = 2;   //the Error code is 2
		//combine version and code into a byte
		byte t = (byte) (verison << 4 | code);
		out.write(t);
		t = (byte) getMsgID();//get message ID byte
		out.write(t);
		out.write(getMessage().getBytes(UTF8));//get error message bytes
		//combine all bytes to a byte array
		byte [] arr = out.toByteArray();
		return arr;
	}
	
	
	/**
	 * Hash Code method
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(message);
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
		return Objects.equals(message, other.message);
	}
	
	
}
