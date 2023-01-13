/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 0
* Class: CSI 4321
*
************************************************/
package fiat.serialization.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import fiat.serialization.MessageInput;


/**
 * Test Message input methods
 * @author Maiqi Hou
 * @version 1.1
 *
 */
class MessageInputTest {
	
	/**
	 * Testing MessageInput when InputStream is not null
	 * @throws IOException I/O problem
	 */
	@Test
	@DisplayName("Test Message input when in is not null")
	void test() throws IOException {
		byte [] b = "5 FriesB512 5.6 ".getBytes(StandardCharsets.UTF_8);
		InputStream in = new ByteArrayInputStream(b);
		MessageInput input = new MessageInput(in);
		int n;
		StringBuilder strings = new StringBuilder();
		while((n = input.read()) != -1) {
			strings.append((char) n);
		}
		assertEquals("5 FriesB512 5.6 ", strings.toString());
		input.close();
	}
	
	/**
	 * Testing MessageInput when InputStream is empty
	 * @throws IOException I/O problem
	 */
	@Test
	@DisplayName("Test Message in put when in is empty")
	void test1() throws IOException {
		InputStream in = new ByteArrayInputStream("".getBytes("UTF-8"));
		MessageInput input = new MessageInput(in);
		char[] a = new char[1];
		int n = input.read(a, 0, 0);
		assertEquals(0, n);
		input.close();
	}
	
	/**
	 * Testing message Input when InputStream is null
	 */
	@Test
	@DisplayName("Test Message input when in is null")
	void test2() {
		InputStream in = null;
		Exception e = Assertions.assertThrows(
		NullPointerException.class, () ->{new MessageInput(in);});
		Assertions.assertEquals("in is null", e.getMessage());
	}
}
