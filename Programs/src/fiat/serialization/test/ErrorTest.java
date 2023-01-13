/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 1
* Class: CSI 4321
*
************************************************/
package fiat.serialization.test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import fiat.serialization.Error;

/**
 * Error Test
 * @author Maiqi Hou
 * @version 1.0
 *
 */
class ErrorTest {
	
	/**
	 * Testing Error Constructor
	 *
	 */
	@DisplayName("Testing Constructor")
	@Nested
	class Constructor{
		/**
		 * Test Error constructor with invalid timestamp
		 * @param time timestamp
		 */
		@DisplayName("Test Error constructor with invalid timestamp")
		@ParameterizedTest(name = "timestamp = {0}")
		@ValueSource(longs = {-11111111, 9223372036854775807L})
		void test1(long time) {
			Exception e = Assertions.assertThrows(
					IllegalArgumentException.class,()->{new Error(time+1, "H");});
				assertEquals("TimeStamp is invalid", e.getMessage());
		}
		
		
		/**
		 * Test Error constructor with valid value
		 * @param time time stamp
		 * @param msg Error message
		 */
		@DisplayName("Test Error constructor with valid value")
		@ParameterizedTest(name = "Constructor: tiemstamp: {0} and Meg: {1}")
		@MethodSource("test")
		void test2(long time, String msg) {
			Error error = new Error(time, msg);
			String s = "ERROR";
			assertTrue("Test timestamp", time == error.getTimestamp());
			assertTrue("Test Message", msg.equals(error.getMessage()));
			
			assertEquals(s, error.getRequest());
		}
		
		/**
		 * Static stream for constructor with valid value
		 * @return
		 * a stream with parameters
		 */
		static Stream<Arguments>test(){
			return Stream.of(
					Arguments.of(20220913103023L, "1Warning"),
					Arguments.of(20221031140913L, "Oops~"),
					Arguments.of(19990819233113L,"Ohh!"),
					Arguments.of(20211231235900L,"Ahh!~")
			);
		}
		
	}
	
	
	
	/**
	 * Testing set Message
	 *
	 */
	@DisplayName("Testing set Message")
	@Nested
	class SetMessageTest{
		
		/**
		 * Message is null
		 */
		@Test
		@DisplayName("Message is null")
		void test() {
			Exception e = assertThrows(
			 IllegalArgumentException.class, ()->{new Error(1L, null);});
			assertEquals("message is null", e.getMessage());
		}
		
		/**
		 * Sub SetMessage testing(Unsigned Integer String)
		 *
		 */
		@DisplayName("Check char count Unsigned Integer String")
		@Nested
		class subTesting{
			
			/**
			 * Test Message is empty
			 */
			@Test
			@DisplayName("Char count -> Message is empty")
			void test() {
				Exception e = assertThrows(
						IllegalArgumentException.class, () ->{new Error(1L, "");});
				assertEquals("message is invalid", e.getMessage());
			}
			
			/**
			 * Test char count of range 2048
			 */
			@Test
			@DisplayName("Char count -> out of 2048")
			void test1() {
				String[] temp = new String[2049];
				for(int i = 0; i < 2049; i++) {
					temp[i] = "a";
				}
				String s = String.join("", temp);
				Exception e = assertThrows(
						IllegalArgumentException.class, () ->{new Error(1L, s);});
				assertEquals("message is invalid", e.getMessage());
			}
			
			
			/**
			 * Test char count equal to 2048;
			 */
			@Test
			@DisplayName("Char count -> equal to 2048")
			void test2() {
				String[] temp = new String[2048];
				for(int i = 0; i < 2048; i++) {
					temp[i] = "X";
				}
				String s = String.join("", temp);
				Error error =new Error(1L, s);
				assertEquals(s, error.getMessage());
			}
			
		}
		
		/**
		 * Check Message character list
		 *
		 */
		@DisplayName("Check Message char list")
		@Nested
		class subTesting2{
			
			/**
			 * Message Char list  first char invalid
			 * @param msg Error message
			 */
			@DisplayName("Message Char list -> first char invalid")
			@ParameterizedTest(name = "Message = {0}")
			@ValueSource(strings = {"!Warig", "*Bad Message", "$No", "()No!","\rNope"})
			void test3(String msg) {
				Exception e = Assertions.assertThrows(
						 IllegalArgumentException.class, () -> {new Error(1L, msg);});
				Assertions.assertEquals("message is invalid", e.getMessage());
			}
			
			/**
			 * Message Char list  first char valid
			 * @param msg Error Message
			 */
			@DisplayName("Message Char list -> first char valid")
			@ParameterizedTest(name = "Message = {0}")
			@ValueSource(strings = {"0Warning","Bad", "9Caution", "No~!", "N*", "N!", "404"})
			void test4(String msg) {
				Error error = new Error(1L, msg);
				assertEquals(msg, error.getMessage());
			}
			
			/**
			 * Invalid Character List
			 * @param msg Error Message
			 */
			@DisplayName("Invalid Character List")
			@ParameterizedTest(name = "Message = {0}")
			@ValueSource(strings = {"0\rCaution", "W\nning", "No\r\npe"})
			void test5(String msg) {
				Exception e = Assertions.assertThrows(
						 IllegalArgumentException.class, () -> {new Error(1L, msg);});
				Assertions.assertEquals("message is invalid", e.getMessage());
			}
			
			/**
			 * Valid Character List
			 * @param msg Error Message
			 */
			@DisplayName("Valid Character List")
			@ParameterizedTest(name = "Message = {0}")
			@ValueSource(strings = {"0Warning", "Oops~","Caution!!!"})
			void test6(String msg) {
				Error error = new Error(1L, msg);
				assertEquals(msg, error.getMessage());
			}
		}
			
	}
	
	/**
	 * ToString test
	 *
	 */
	@DisplayName("ToString")
	@Nested
	class toStringTest{
		
		/**
		 * Test ToString
		 * @param time timestamp
		 * @param msg Error message
		 */
		@DisplayName("Test ToString")
		@ParameterizedTest(name = "toString with timestamp: {0} and meg: {1}")
		@MethodSource("validTest")
		void testTostring(long time, String msg) {
			Error error = new Error(time, msg);
			String s = "ERROR (TS=" +time+") message="
					+ msg;
			String s1 = error.toString();
			assertEquals(s, s1);
		}
		
		/**
		 * Static stream for toString with valid value
		 * @return
		 * a stream with parameters
		 */
		static Stream<Arguments>validTest(){
			return Stream.of(
					Arguments.of(20220913103023L, "1Warning"),
					Arguments.of(20221031140913L, "Oops~"),
					Arguments.of(19990819233113L,"Ohh!"),
					Arguments.of(20211231235900L,"Ahh!~")
			);
		}
	}
	
	/**
	 * Testing HashCode and Equals
	 */
	@Test
	@DisplayName("Testing HashCode and Equals")
	void test() {
		Error e1 = new Error(1L, "H");
		Error e2 = new Error(1L, "H");
		Error e3 = new Error(0L, "H");
		assertTrue(e1.equals(e2) && e2.equals(e1));
		assertTrue(e1.hashCode() == e2.hashCode());
		assertFalse(e1.equals(e3) && e3.equals(e1));
		assertFalse(e1.equals(e3) != e3.equals(e1));
		assertFalse(e1.equals(null));
		assertTrue(e1.equals(e1));
		assertFalse(e1.equals(" "));
	}
	
}
