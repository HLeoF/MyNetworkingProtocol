/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 4
* Class: CSI 4321
*
************************************************/
package foop.serialization.test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import foop.serialization.Error;



/**
 * Error Test
 * @author Maiqi Hou
 * @version 1.0
 *
 */
class ErrorTest {

	/**
	 * Test Error Message Constructor
	 *
	 */
	@DisplayName("Test Constructor")
	@Nested
	class Constructor{
		/**
		 * Test Error Constructor with invalid Message ID
		 * @param id Message ID
		 */
		@DisplayName("Test Error Constructor with invalid MsgID")
		@ParameterizedTest(name = "MegID = {0}")
		@ValueSource(ints = {-1, -256, 256, -100})
		void test(int id) {
			Exception e = assertThrows(IllegalArgumentException.class, 
					()->new Error(id, "Error"));
			assertEquals("msg ID is out of range", e.getMessage());
		}
		
		/**
		 * Test Error Constructor with valid Message ID
		 * @param id Message ID
		 */
		@DisplayName("Test Error Constructor with valid MsgID")
		@ParameterizedTest(name = "MegID = {0}")
		@ValueSource(ints = {1, 255, 10, 250})
		void test1(int id) {
			Error error = new Error(id, "Error");
			assertEquals(id, error.getMsgID());
		}
		
		/**
		 * Test Error Message is Null
		 */
		@DisplayName("Test Error Message is Null")
		@Test
		void test2() {
			Exception e = assertThrows(IllegalArgumentException.class, 
					()->new Error(0, null));
			assertEquals("Foop Message is null", e.getMessage());
		}
		
		/**
		 * Test Error Constructor with invalid message
		 * @param s invalid error message
		 */
		@DisplayName("Test Error Constructor with invalid message")
		@ParameterizedTest(name = "Msg = {0}")
		@ValueSource(strings =  {"\r0","W\nning", "No\r\npe", "Err\n"})
		void test3(String s) {
			Exception e = assertThrows(IllegalArgumentException.class, 
					()->new Error(0, s));
			assertEquals("Foop Message is invalid", e.getMessage());
		}
		
		/**
		 * Test Error Constructor with valid message
		 * @param s Error Message
		 */
		@DisplayName("Test Error Constructor with valid message")
		@ParameterizedTest(name = "Msg = {0}")
		@ValueSource(strings = {"0Warning!", "Error~", "Caution!!"})
		void test4(String s) {
			Error error = new Error(0, s);
			assertEquals(s, error.getMessage());
		}
		
		/**
		 * Test Message is too long
		 */
		@Test
		@DisplayName("Test Message is too long")
		void test5() {
			StringBuffer buffer = new StringBuffer();
			for(int i = 0; i < 65506; i++) {
				buffer.append("A");
			}
			String msg = buffer.toString();
			Exception e = assertThrows(IllegalArgumentException.class, 
					()->new Error(0,  msg));
			assertEquals("Error message is too long", e.getMessage());
		}
	}
	
	/**
	 * Test ToString method
	 *
	 */
	@DisplayName("Test toString")
	@Nested
	class toStringTest{
		/**
		 * Test toString 
		 * @param id message id
		 * @param msg error message
		 */
		@DisplayName("Test ToString")
		@ParameterizedTest(name = "ToString with msgID = {0} and msg = {1}")
		@MethodSource("ValidTest")
		void testToString(int id, String msg) {
			Error error = new Error(id, msg);
			String s = "Error: MsgID="+id+" Message="+msg;
			String s1 = error.toString();
			assertEquals(s, s1);
		}
		
		/**
		 * Static stream for to stirng with valid value
		 * @return 
		 * a stream with parameters
		 */
		static Stream<Arguments>ValidTest(){
			return Stream.of(
					Arguments.of(123, "Error!"),
					Arguments.of(0,"Caution!"),
					Arguments.of(255, "Ohh!")
			);
		}
	}
	
	
	/**
	 * Testing HashCode and Equals
	 */
	@Test
	@DisplayName("Testing HashCode and Equals")
	void test() {
		Error e1 = new Error(1, "H");
		Error e2 = new Error(1, "H");
		Error e3 = new Error(0, "H");
		assertTrue(e1.equals(e2) && e2.equals(e1));
		assertTrue(e1.hashCode() == e2.hashCode());
		assertFalse(e1.equals(e3) && e3.equals(e1));
		assertFalse(e1.equals(e3) != e3.equals(e1));
		assertFalse(e1.equals(null));
		assertTrue(e1.equals(e1));
		assertFalse(e1.equals(" "));
	}
}
