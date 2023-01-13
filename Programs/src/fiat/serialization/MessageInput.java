/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 0
* Class: CSI 4321
*
************************************************/
package fiat.serialization;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;




/**
 * Deserialization input source
 * @author Maiqi Hou
 * @version 2.0
 */
public class MessageInput{
	private InputStreamReader reader;
	private static final Charset UTF8 = StandardCharsets.UTF_8; //Standard charsets UTF-8
	/**
	 * Creating Message Input from an InputStream
	 * @param in byte input source
	 * @throws NullPointerException if in is null
	 */
	public MessageInput(InputStream in)  {
		Objects.requireNonNull(in,"in is null");
		this.reader = new InputStreamReader(in, UTF8);
	}
	
	/**
	 * read bytes from the inputStream
	 * @return InputStream read() methods
	 * @throws IOException I/O problesm
	 */
	public int read() throws IOException {
		return this.reader.read();
	}
	
	/**
	 * read ups to length bytes from the inputStream
	 * @param b  buffer into which the data is read
	 * @param off the start offset in the destination array b
	 * @param len the maximum number of bytes read.
	 * @return total number of bytes read into the buffer
	 * @throws IOException I/O problem
	 */
	public int read(char [] b, int off, int len) throws IOException {
		return this.reader.read(b, off, len);
	}
	
	/**
	 * Close input stream
	 * @throws IOException I/O problem
	 */
	public void close() throws IOException {
		this.reader.close();
	}

	
}
