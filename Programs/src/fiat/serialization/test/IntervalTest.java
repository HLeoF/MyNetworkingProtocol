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

import fiat.serialization.Interval;

/**
 * Interval Test
 * @author Maiqi Hou
 * @version 1.0
 *
 */
class IntervalTest {
	
	/**
	 * Test Interval COnstructor
	 *
	 */
	@DisplayName("Testing Constructor")
	@Nested
	class Contructor{
		
		/**
		 * Test Invertal constructor with invalid timestamp
		 * @param time time stamp
		 */
		@DisplayName("Test Invevtal constructor with invalid timestamp")
		@ParameterizedTest(name = "timestamp = {0}")
		@ValueSource(longs = {-11111111, 9223372036854775807L})
		void test1(long time) {
			Exception e = Assertions.assertThrows(
					IllegalArgumentException.class,()->{new Interval(time+1,1);});
				assertEquals("TimeStamp is invalid", e.getMessage());
		}
		
		/**
		 * Test Invertal consturctor with invalid intervalTime
		 * @param time time stamp
		 */
		@DisplayName("Test Inverval consturctor with invalid intervalTime")
		@ParameterizedTest(name = "intervalTime = {0}")
		@ValueSource(ints = {-1, 2049, 100000, -10000})
		void test2(int time) {
			Exception e = Assertions.assertThrows(
					IllegalArgumentException.class,()->{new Interval(1,time);});
				assertEquals("invalid intervalTime", e.getMessage());
		}
		
		/**
		 * Test Inverval constructor with valid value
		 * @param time time stamp
		 * @param inter interval time
		 */
		@DisplayName("Test Inverval constructor with valid value")
		@ParameterizedTest(name = "Constructor: tiemstamp: {0} and interval: {1}")
		@MethodSource("test")
		void test3(long time, int inter) {
			Interval interval = new Interval(time, inter);
			assertTrue("Test timestamp", time == interval.getTimestamp());
			assertTrue("Interval time", inter == interval.getIntervalTime());
		}
		/**
		 * Static stream for constructor with valid value
		 * @return
		 * a stream with parameters
		 */
		static Stream<Arguments>test(){
			return Stream.of(
					Arguments.of(20220913103023L,1),
					Arguments.of(20221031140913L, 2048),
					Arguments.of(19990819233113L,2047),
					Arguments.of(20211231235900L,100)
			);
		}
		
	}
	
	
	/**
	 * Test Set the interval tiem methdo
	 *
	 */
	@DisplayName("Set the interval time")
	@Nested
	class inter{
		
		/**
		 * Test interval time invalid
		 * @param time interval time
		 */
		@DisplayName("Interval time invalid")
		@ParameterizedTest(name = "interval = {0}")
		@ValueSource(ints = {-1, 2049, 100000, -10000})
		void test4(int time) {
			Interval interval = new Interval(0L, 0);
			Exception e = Assertions.assertThrows(
					IllegalArgumentException.class,()->{interval.setIntervalTime(time);});
				assertEquals("invalid intervalTime", e.getMessage());
		}
		
		/**
		 * Interval time invalid
		 * @param time interval time
		 */
		@DisplayName("Interval time invalid")
		@ParameterizedTest(name = "interval = {0}")
		@ValueSource(ints = {1, 2048, 1000, 2047})
		void test5(int time) {
			Interval interval = new Interval(0L, 0);
			interval.setIntervalTime(time);
			assertTrue("Test Interval time", time == interval.getIntervalTime());
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
		 * Test toString
		 * @param time time stamp
		 * @param inter interval time
		 */
		@DisplayName("Test ToString")
		@ParameterizedTest(name = "toString with timestamp: {0} and interval: {1}")
		@MethodSource("validTest")
		void testToString(long time, int inter) {
			Interval interval = new Interval(time, inter);
			String s = "INTERVAL (TS="+ interval.getTimestamp()
			+") time="+interval.intervalTime;
			String s1 = interval.toString();
			assertEquals(s, s1);
		}
		
		
		/**
		 * Static stream for toString with valid value
		 * @return
		 * a stream with parameters
		 */
		static Stream<Arguments>validTest(){
			return Stream.of(
					Arguments.of(20220913103023L, 1),
					Arguments.of(20221031140913L, 150),
					Arguments.of(19990819233113L, 2048),
					Arguments.of(20211231235900L, 1989)
			);
		}
	}
	
	/**
	 * Testing HashCode and Equals
	 */
	@Test
	@DisplayName("Testing HashCode and Equals")
	void test() {
		Interval i1 = new Interval(1L, 1);
		Interval i2 = new Interval(1L, 1);
		Interval i3 = new Interval(0L, 2);
		assertTrue(i1.equals(i2) && i2.equals(i1));
		assertTrue(i1.hashCode() == i2.hashCode());
		assertFalse(i1.equals(i3) && i3.equals(i1));
		assertFalse(i1.equals(i3) != i3.equals(i1));
		assertFalse(i1.equals(null));
		assertTrue(i1.equals(i1));
		assertFalse(i1.equals(" "));
	}
	
}
