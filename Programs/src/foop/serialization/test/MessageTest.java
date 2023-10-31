package foop.serialization.test;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


import foop.serialization.Message;

/**
 * Message Test
 * @author Maiqi Hou
 * @version 1.0
 *
 */
class MessageTest {
	
	/**
	 * Test Message ID setter
	 *
	 */
	@DisplayName("Test Message ID setter")
	@Nested
	class setterTest{
		/**
		 * Message ID is invalid
		 * @param id Message ID
		 */
		@DisplayName("Message ID is invalid")
		@ParameterizedTest(name = "Message ID: {0}")
		@ValueSource(ints = {-1, -100, 256, -255})
		void test(int id ) {
			Message msg = new Message() {};
			Exception e = assertThrows(IllegalArgumentException.class, ()->msg.setMsgID(id));
			assertEquals("msg ID is out of range", e.getMessage());
		}
		
		/**
		 * Message ID is valid
		 * @param id Message ID
		 */
		@DisplayName("Message ID is valid")
		@ParameterizedTest(name = "Message ID: {0}")
		@ValueSource(ints = {1, 0, 255, 100})
		void test1(int id) {
			Message msg = new Message() {};
			msg.setMsgID(id);
			assertEquals(id, msg.getMsgID());
		}
		
	}
	
	/**
	 * Test Encode Message Function is equal null
	 * @throws IOException IO problem if write bytes have problems.
	 */
	@Test
	@DisplayName("Test encode Message function return null")
	void test1() throws IOException {
		Message msg = new Message() {};
		assertNull(msg.encodeMsg());
	}
	
	/**
	 * Testing HashCode and Equals
	 */
	@Test
	@DisplayName("Testing HashCode and Equals")
	void test2() {
		Message m1 = new Message() {};
		Message m2 = m1;
		Message m3 = new Message() {};
		String s = " ";
		m1.setMsgID(1);
		m2.setMsgID(1);
		m3.setMsgID(3);
		assertTrue(m1.equals(m2) && m2.equals(m1));
		assertTrue(m1.hashCode() == m2.hashCode());
		assertFalse(m1.equals(m3) && m3.equals(m1));
		assertFalse(m1.equals(m3) != m3.equals(m1));
		assertFalse(m1.equals(null));
		assertTrue(m1.equals(m1));
		assertFalse(m1.equals(s));
	}


}
