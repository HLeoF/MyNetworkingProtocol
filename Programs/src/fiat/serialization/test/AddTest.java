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
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import fiat.serialization.Add;
import fiat.serialization.Item;
import fiat.serialization.ItemFactory;
import fiat.serialization.MealType;
import fiat.serialization.MessageOutput;

/**
 * Add Test
 * @author Maiqi Hou
 * @version 1.0
 *
 */
class AddTest {
	/**
	 * Test Constructor
	 *
	 */
	@DisplayName("Test Constructor")
	@Nested
	class testC{
		
		/**
		 * Test item is null
		 */
		@Test
		@DisplayName("Test itme is null")
		void test() {
			Item i = null;
			Long timestamp = 20220912203100L;
			Exception e = Assertions.assertThrows(
				IllegalArgumentException.class,()->{new Add(timestamp, i);});
			assertEquals("Item is null", e.getMessage());
		}
		
		/**
		 * Test constructor with invalid time stamp
		 * @param time  time stamp
		 */
		@DisplayName("Test constructor with invalid time stamp")
		@ParameterizedTest(name = "Timestamp = {0} + 1")
		@ValueSource(longs = {-11111111, 9223372036854775807L})
		void test1(long time) {
			Item item = new Item("h", MealType.Lunch, 1, 1.1);
			Exception e = Assertions.assertThrows(
					IllegalArgumentException.class,()->{new Add(time+1, item);});
				assertEquals("TimeStamp is invalid", e.getMessage());
		}
		
		/**
		 * Test constructor with valid value
		 * @param item new item
		 * @param timestamp add item time stamp
		 */
		@DisplayName("Test constructor with valid value")
		@ParameterizedTest(name = "Constructor with {0} and {1}")
		@MethodSource("validValue")
		void test2(Item item, long timestamp) {
			Add add = new Add(timestamp, item);
			MessageOutput o = new MessageOutput(new ByteArrayOutputStream());
			try {
				ItemFactory.encode(add.getItem(), o);
			} catch (IOException e) {}
		    String i ="ADD" ;//get item encode inform
			assertTrue("Test item", item.equals(add.getItem()));
			assertTrue("Test timestamp", timestamp == add.getTimestamp());
			assertEquals(i, add.getRequest());
		}
		
		/**
		 * Static stream for constructor with valid value
		 * @return
		 * a stream with parameters
		 */
		static Stream<Arguments> validValue(){
			return Stream.of(
					Arguments.of(new Item("0k", MealType.Lunch, 1, 1.2), 1L),
					Arguments.of(new Item("Frain!",MealType.Breakfast,2,100), 2080L),
					Arguments.of(new Item("Bacon",MealType.Dinner,100,8.92),20211021190200L),
					Arguments.of(new Item("coke",MealType.Snack,2047,100000.0),Long.MAX_VALUE)
			);
		}
		
	}
	
	
	/**
	 * ToSting test
	 *
	 */
	@DisplayName("ToString")
	@Nested
	class ToStringTest{
		
		/**
		 * Test ToString
		 * @param item new item
		 * @param timestamp time stamp
		 */
		@DisplayName("Test ToString")
		@ParameterizedTest(name =  "toString with {0} and {1}")
		@MethodSource("validValue")
		void test(Item item, long timestamp) {
			Add add = new Add(timestamp, item);
			String s ="ADD (TS=" 
		               + timestamp + ") item=" 
					   + item.toString();
			String s1 = add.toString();
			assertEquals(s, s1);
		}
		
		
		
		/**
		 * Static stream for constructor with valid value
		 * @return
		 * a stream with parameters
		 */
		static Stream<Arguments> validValue(){
			return Stream.of(
					Arguments.of(new Item("0k", MealType.Lunch, 1, 1.2), 1L),
					Arguments.of(new Item("Frain!",MealType.Breakfast,2,100), 2080L),
					Arguments.of(new Item("Bacon",MealType.Dinner,100,8.92),20211021190200L),
					Arguments.of(new Item("coke",MealType.Snack,2047,100000.0),Long.MAX_VALUE)
			);
		}
	}
	
	
	/**
	 * Testing HashCode and Equals
	 */
	@Test
	@DisplayName("Testing HashCode and Equals")
	void test() {
		Item item = new Item("0k", MealType.Lunch, 1, 1.2);
		long time = 1L;
		Add a1 = new Add(time, item);
		Add a2 = new Add(time, item);
		Add a3 = new Add(0L, item);
		assertTrue(a1.equals(a2) && a2.equals(a1));
		assertTrue(a1.hashCode() == a2.hashCode());
		assertFalse(a1.equals(a3) && a3.equals(a1));
		assertFalse(a1.equals(a3) != a3.equals(a1));
		assertFalse(a1.equals(null));
		assertTrue(a1.equals(a1));
		assertFalse(a1.equals(time));
	}
}
