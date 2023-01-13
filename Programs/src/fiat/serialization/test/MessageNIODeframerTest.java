/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 7
* Class: CSI 4321
*
************************************************/
package fiat.serialization.test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import fiat.serialization.MessageNIODeframer;

/**
 * Message NIO Deframer Test
 * @author Maiqi Hou
 * @version 1.1
 *
 */
class MessageNIODeframerTest {

	/**
	 * Message NIO defreamer Test
	 */
	@DisplayName("Message NIO defreamer Test")
	@Nested
	class MessageNIOTest{
		
		/**
		 * Test buffer is null when decode next Message
		 */
		@Test
		@DisplayName("Test buffer is null when decode next Message")
		void test() {
			
			MessageNIODeframer deframer = new MessageNIODeframer(null);
			Exception e = assertThrows(NullPointerException.class, 
					()->{deframer.nextMsg(null);});
			assertEquals("buffer is null", e.getMessage());
		}
		
		/**
		 * Test decode next Message can return a byte array
		 */
		@Test
		@DisplayName("Test decoed next Message can return a byte array")
		void test1() {
			byte[] delimiter = "\r\n".getBytes(StandardCharsets.UTF_8);
			MessageNIODeframer deframer = new MessageNIODeframer(delimiter);
			byte[] Msg = "FT1.0 1 GET \n".getBytes(StandardCharsets.UTF_8);
			String temp = new String(deframer.nextMsg(Msg), StandardCharsets.UTF_8);
			assertEquals("FT1.0 1 GET ", temp);
			Msg = "FT1.0 1 GET \r".getBytes(StandardCharsets.UTF_8);
			temp = new String(deframer.nextMsg(Msg), StandardCharsets.UTF_8);
			assertEquals("FT1.0 1 GET ", temp);
		}
		
		/**
		 * Test decode next Message can retrun a byte array with \0 delimiter
		 */
		@Test
		@DisplayName("Test decode next Message can return a byte array with \0")
		void test2() {
			byte[] delimiter = "\0".getBytes(StandardCharsets.UTF_8);
			MessageNIODeframer deframer = new MessageNIODeframer(delimiter);
			byte[] Msg = "FT1.0 1 GET \0".getBytes(StandardCharsets.UTF_8);
			System.out.println();
			String temp = new String(deframer.nextMsg(Msg), StandardCharsets.UTF_8);
			assertEquals("FT1.0 1 GET ", temp);
		}
		
		/**
		 * Test decode next message return null
		 */
		@Test
		@DisplayName("Test decode next Message return null")
		void test3() {
			byte[] delimiter = "\r\n".getBytes(StandardCharsets.UTF_8);
			MessageNIODeframer deframer = new MessageNIODeframer(delimiter);
			byte[] Msg = "FT1.0 1 ".getBytes(StandardCharsets.UTF_8);
			assertEquals(null, deframer.nextMsg(Msg));
		}
	}
	
	/**
	 * Testing HashCode and Equals
	 */
	@Test
	@DisplayName("Testing HashCode and Equals")
	void test() {
		byte[] b = "HH".getBytes(StandardCharsets.UTF_8);
		MessageNIODeframer d1 = new MessageNIODeframer(b);
		MessageNIODeframer d2 = new MessageNIODeframer(b);
		MessageNIODeframer d3 = new MessageNIODeframer(null);
		assertTrue(d1.equals(d2) && d2.equals(d1));
		assertTrue(d1.hashCode() == d2.hashCode());
		assertFalse(d1.equals(d3) && d3.equals(d1));
		assertFalse(d1.equals(d3) != d3.equals(d1));
		assertFalse(d1.equals(null));
		assertTrue(d1.equals(d1));
		assertFalse(d1.equals(" "));
	}
	
	

}
