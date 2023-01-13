/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 0
* Class: CSI 4321
*
************************************************/
package fiat.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Serialization output source
 * @author Maiqi Hou
 * @version 2.0
 *
 */
public class MessageOutput {
	private OutputStream out; //for outputStream
	
	/**
	 * Creating Message output from an OutputStream
	 * @param out byte output source
	 */
	public MessageOutput(OutputStream out) {
		Objects.requireNonNull(out, "out is null");
		this.out = out;
	}
	
	/**
	 * Creating a new byte array
	 * @return the current contents of this output stream
	 */
	public byte[] toByteArray() {
		return ((ByteArrayOutputStream) this.out).toByteArray();
	}
	
	/**
	 * writing length bytes from the byte array 
	 * @param b the data
	 * @param off the start offset in the data
	 * @param len the number of bytes to write
	 * @throws IOException  I/O problem
	 */
	public void write(byte[] b, int off, int len) 
			throws IOException {
		this.out.write(b, off, len);
	}
	
	/**
	 * Writing the byte to this byte array output stream
	 * @param b the data
	 * @throws IOException I/O problem
	 */
	public void write(byte[] b) throws IOException {
		this.out.write(b);
	}
	
	/**
	 * flushes output stream and forces any buffer output bytes to be written out
	 * @throws IOException I/O problem
	 */
	public void flush() throws IOException{
		this.out.flush();
	}
	
	/**
	 * Closing the output stream and release any system resource
	 * @throws IOException I/O problem
	 */
	public void close() throws IOException {
		this.out.close();
	}

}
