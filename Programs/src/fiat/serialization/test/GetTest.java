/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 1
* Class: CSI 4321
*
************************************************/
package fiat.serialization.test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import fiat.serialization.Get;



/**
 * Get Test
 * @author Maiqi Hou
 * @version 1.0
 *
 */
class GetTest {
	
	/**
	 * Test Constructor
	 */
	@DisplayName("Test Constructor")
	@Nested
	class testC{
		
		/**
		 * Test constructor with invalid time stamp
		 * @param time  time stamp
		 */
		@DisplayName("Test constructor with invalid time stamp")
		@ParameterizedTest(name = "Timestamp = {0} + 1")
		@ValueSource(longs = {-11111111, 9223372036854775807L})
		void test(long time) {
			Exception e = Assertions.assertThrows(
					IllegalArgumentException.class,()->{new Get(time+1);});
				assertEquals("TimeStamp is invalid", e.getMessage());
		}
		
		/**
		 * Test constructor with valid time stamp
		 * @param time time stamp
		 */
		@DisplayName("Test constructor with valid time stamp")
		@ParameterizedTest(name = "Timestamp = {0}")
		@ValueSource(longs = {9223372036854775807L, 20220925153047L,19990819093057L})
		void test1(long time) {
			Get get = new Get(time);
			assertTrue("Test timestamp", time == get.getTimestamp());
			assertEquals("GET", get.getRequest());
		}
	}
	
	/**
	 * Test toString
	 * @param time time stamp
	 */
	@DisplayName("Test ToString")
	@ParameterizedTest(name = "toString with timestamp = {0}")
	@ValueSource(longs = {9223372036854775807L, 20220925153047L,19990819093057L})
	void test(long time) {
		Get get = new Get(time);
		String s = "GET " + "(TS=" + time +")";
		String s1 = get.toString();
		assertEquals(s, s1);
	}
	
	/**
	 * Testing HashCode and Equals
	 */
	@Test
	@DisplayName("Testing HashCode and Equals")
	void test1() {
		Get g1 = new Get(1L);
		Get g2 = new Get(1L);
		Get g3 = new Get(3L);
		assertTrue(g1.equals(g2) && g2.equals(g1));
		assertTrue(g1.hashCode() == g2.hashCode());
		assertFalse(g1.equals(g3) && g3.equals(g1));
		assertFalse(g1.equals(g3) != g3.equals(g1));
		assertFalse(g1.equals(null));
		assertTrue(g1.equals(g1));
		assertFalse(g1.equals(" "));
	}

}
