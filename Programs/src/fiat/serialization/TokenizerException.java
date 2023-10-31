
package fiat.serialization;

/**
 * Exception indicating the token and location where the problem occurred
 * 
 * @author Maiqi Hou
 * @version 1.1
 * Update Check offset value method
 */
public class TokenizerException extends Exception{
	
	/**
	 * create the own exception class
	 * declare a unique exception id
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * //declare a int type for offset in bytes.
	 */
	private int offset;
	
	/**
	 * Token and location validation exception with cause
	 * @param offset offset in bytes of tokenizer problem
	 * @param message exception message
	 * @param cause exception cause
	 */
	public TokenizerException(int offset, String message, Throwable cause) {
		super(message,cause);
		testOffset(offset);
		this.offset = offset;
	}
	
	/**
	 * Token and location validation exception without cause
	 * @param offset  offset in bytes of tokenizer problem
	 * @param message  exception message
	 */
	public TokenizerException(int offset, String message) {
		super(message);
		testOffset(offset);
		this.offset = offset;
	}
	
	/**
	 * Getting offset in bytes start of stream for token that failed validation
	 * @return
	 * offset in bytes from start of stream
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Check offset value whether valid 
	 * @param offset  offset in bytes of tokenizer problem
	 */
	public void testOffset(int offset) {
		if(offset < 0) {
			throw new IllegalArgumentException("Offset Invalid");
		}
	}
}
