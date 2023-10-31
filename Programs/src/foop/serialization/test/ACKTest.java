package foop.serialization.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import foop.serialization.ACK;
import foop.serialization.Message;

/**
 * Test ACK method
 * @author Maiqi Hou
 * @version 1.0
 *
 */
class ACKTest {

	/**
	 * Test ACK constructor with invalid message ID
	 * @param id message ID
	 */
	@DisplayName("Test ACK constructor with invalid Msg ID")
	@ParameterizedTest(name = "Msg ID = {0}")
	@ValueSource(ints = {-1, 256, -100, -256})
	void test(int id) {
		Exception e = assertThrows(IllegalArgumentException.class, 
				()->new ACK(id));
		assertEquals("msg ID is out of range", e.getMessage());
	}
	
	/**
	 * Test ACK constructor with valid Message ID and to String
	 * @param id Message ID
	 */
	@DisplayName("Test ACK constructor with valid Msg ID and To String")
	@ParameterizedTest(name = "Msg ID = {0}")
	@ValueSource(ints = {1, 255, 100, 0})
	void test1(int id) {
		ACK ack = new ACK(id);
		String s = "ACK: MsgID="+id;
		String s1 = ack.toString();
		assertEquals(s, s1);
	}

	/**
	 * Testing ACK hashCode and Equals
	 */
	@Test
	@DisplayName("Testing HashCode and Equals")
	void test2() {
		Message m1 = new ACK(0);
		Message m2 = new ACK(0);
		Message m3 = new ACK(1);
		String s = " ";
		assertTrue(m1.equals(m2) && m2.equals(m1));
		assertTrue(m1.hashCode() == m2.hashCode());
		assertFalse(m1.equals(m3) && m3.equals(m1));
		assertFalse(m1.equals(m3) != m3.equals(m1));
		assertFalse(m1.equals(null));
		assertTrue(m1.equals(m1));
		assertFalse(m1.equals(s));
	}
}
