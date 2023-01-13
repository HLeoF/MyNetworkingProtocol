/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 0
* Class: CSI 4321
*
************************************************/
package fiat.serialization.test;


import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fiat.serialization.MessageOutput;

/**
 * Test Message out methods
 * @author Maiqi Hou
 * @version 1.0
 */
class MessageOutputTest {
	
	/**
	 * Testing Message output when out is not null
	 * @throws IOException I/O problem
	 */
	@Test
	@DisplayName("Test Message output when out is not null")
	void test() throws IOException {
		MessageOutput out = new MessageOutput(new ByteArrayOutputStream());
		out.write("5 FriesB512 5.6 ".getBytes(StandardCharsets.UTF_8));
		out.flush();
		assertArrayEquals("5 FriesB512 5.6 ".getBytes(StandardCharsets.UTF_8),
				out.toByteArray());
		out.close();
	}
 
	/**
	 * Testing Message output when out is empty
	 * @throws IOException I/O problem
	 */
	@Test
	@DisplayName("Test Message output when out is empty")
	void test1() throws IOException{
		MessageOutput out = new MessageOutput(new ByteArrayOutputStream());
		out.write("".getBytes(StandardCharsets.UTF_8), 0, 0);
		out.flush();
		assertArrayEquals("".getBytes(StandardCharsets.UTF_8), 
				out.toByteArray());
		out.close();
	}
	
	/**
	 * Testing Message output when out is null
	 */
	@Test
	@DisplayName("Test Message output when out is null")
	void test2() {
		OutputStream output = null;
		Exception e = Assertions.assertThrows(
		NullPointerException.class, ()->{new MessageOutput(output);});
		Assertions.assertEquals("out is null", e.getMessage());
	}

}
