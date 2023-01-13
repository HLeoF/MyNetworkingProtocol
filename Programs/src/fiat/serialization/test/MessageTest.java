/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 1
* Class: CSI 4321
*
************************************************/
package fiat.serialization.test;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import fiat.serialization.Get;
import fiat.serialization.Message;

/**
 * Test Message class
 * @author Maiqi Hou
 * @version 1.1
 *
 */
class MessageTest {
	
	/**
	 * Testing set time stamp
	 *
	 */
	@DisplayName("Testing Set Time stamp")
	@Nested
	class SetTimeStamp{
		
		/**
		 * Testing Timestamp is invalid
		 * @param timestamp Time Stamp
		 * @throws IllegalArgumentException if validation fails
		 */
		@DisplayName("Testing Timestamp is invalid")
		@ParameterizedTest(name = "Timestamp = {0} + 1")
		@ValueSource(longs = {-11111111, 9223372036854775807L})
		void test(long timestamp) {
			Message message = new Message() {};
			Exception e = Assertions.assertThrows(
				IllegalArgumentException.class, ()->{message.setTimestamp(timestamp+1);});
			Assertions.assertEquals("TimeStamp is invalid", e.getMessage());
		}
		
		/**
		 * Testing Timestamp is valid 
		 * @param timestamp Time Stamp
		 * @throws IllegalArgumentException if validation fails
		 */
		@DisplayName("Testing Timestamp is valid")
		@ParameterizedTest(name = "Timestamp = {0}")
		@ValueSource(longs = {20210524163426L, 20220912192905L, 19990819093000L})
		void test1(long timestamp) {
			Message message = new Message() {};
			message.setTimestamp(timestamp);
			assertEquals(timestamp, message.getTimestamp());
		}
		
	}
	
	/**
	 * Testing get Request method
	 */
	@Test
	@DisplayName("Test get Request method()")
	void test() {
		Message message = new Message() {};
		assertEquals("", message.getRequest());
		assertEquals("", message.getRequestInfo());
	}
	
	
	/**
	 * Testing HashCode and Equals
	 */
	@Test
	@DisplayName("Testing Hashcode and Equals")
	void test1() {
		Message m1 = new Message() {};
		Message m2 = m1;
		Message m3 = new Get(1L);
		String s = " ";
		m1.setTimestamp(20220912192905L);
		m2.setTimestamp(20220912192905L); 
		assertTrue(m1.equals(m2) && m2.equals(m1));
		assertTrue(m1.hashCode() == m2.hashCode());
		assertFalse(m1.equals(m3) && m3.equals(m1));
		assertFalse(m1.equals(m3) != m3.equals(m1));
		assertFalse(m1.equals(null));
		assertTrue(m1.equals(m1));
		assertFalse(m1.equals(s));
		
	}
	
}
