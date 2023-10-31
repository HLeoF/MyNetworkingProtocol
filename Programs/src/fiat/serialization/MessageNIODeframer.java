package fiat.serialization;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

/**
 * Non blocking I/O deframer for message. 
 * @author Maiqi Hou
 * @version 1.0
 *
 */
public class MessageNIODeframer {
	private static final int MAXBYTE = 65510;
	private byte[] delimiter;

	/**
	 * Create message deframer with specified byte sequence delimiter
	 * @param delimiter bytes of message delimiters
	 */
	public MessageNIODeframer(byte[] delimiter) {
		this.delimiter = delimiter;
	}
	
	/**
	 * Decode the next message 
	 * @param buffer next bytes of message
	 * @return deframed message or null if fram incomplete
	 * @throws NullPointerException if buffer is null
	 */
	public byte[] nextMsg(byte[] buffer) {
		//check buffer whether is null
		Objects.requireNonNull(buffer, "buffer is null");
		
		ByteBuffer buffer2 = ByteBuffer.allocate(MAXBYTE);
		for(byte a : buffer) {
			//if buffer contain delimiter 
			if(contains(a, this.delimiter)) {
				buffer2.flip();
				byte[] arr = new byte[buffer2.remaining()];
				buffer2.get(arr);
				return arr;
			}
			buffer2.put(a);
		}
		return null;
	}
	
	/**
	 * check whether buffer contain delimiter 
	 * @param source bytes of buffer
	 * @param delimiter delimiter in delimiter array
	 * @return true if buffer contain delimiter, 
	 * 			false if buffer does not contain delimiter
	 */
	public boolean contains(byte source, byte[] delimiter) {
		for(byte b: delimiter) {
			if(source == b) {
				return true;
			}
		}
		return false;
	}

	/**
	 * hashCode method
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(delimiter);
		return result;
	}

	/**
	 * equals method
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MessageNIODeframer other = (MessageNIODeframer) obj;
		return Arrays.equals(delimiter, other.delimiter);
	}
}
