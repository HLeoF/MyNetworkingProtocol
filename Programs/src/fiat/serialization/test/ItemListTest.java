package fiat.serialization.test;


import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import fiat.serialization.Item;
import fiat.serialization.ItemList;
import fiat.serialization.MealType;

/**
 * Item List Test
 * @author Maiqi Hou
 * @version 1.0
 *
 */
class ItemListTest {
	
	/**
	 * Test ItemList Constructor
	 *
	 */
	@DisplayName("Test ItemList Constructor")
	@Nested
	class testConstructor{
		
		/**
		 * Test ItemLits constructor with invalid timestamp
		 * @param time timestamp
		 */
		@DisplayName("Test ItemList constructor with invalid timestamp")
		@ParameterizedTest(name = "timestamp = {0}")
		@ValueSource(longs = {-11111111, 9223372036854775807L})
		void test1(long time) {
			Exception e = Assertions.assertThrows(
					IllegalArgumentException.class,()->{new ItemList(time+1, 1L);});
				assertEquals("TimeStamp is invalid", e.getMessage());
		}
		
		/**
		 * Test ItemList constructor with valid value
		 * @param time timestamp
		 * @param mt modified timestamp
		 */
		@DisplayName("Test Error constructor with valid value")
		@ParameterizedTest(name = "Constructor: tiemstamp: {0} and mod tiem: {1}")
		@MethodSource("test")
		void test2(long time, long mt) {
			ItemList list = new ItemList(time,mt);
			assertTrue("Test timestamp", time == list.getTimestamp());
			assertTrue("Test mod timestamp", mt == list.getModifiedTimestamp());
			assertEquals("LIST", list.getRequest());
		}
		
		/**
		 * Static stream for constructor with valid value
		 * @return
		 * a stream with parameters
		 */
		static Stream<Arguments>test(){
			return Stream.of(
					Arguments.of(20220913103023L,20221031140913L ),
					Arguments.of(20221031140913L, 2L),
					Arguments.of(19990819233113L,111111111L),
					Arguments.of(20211231235900L,19990819233113L)
			);
		}
	}
	
	
	
	/**
	 * Test Set modifiedTiemstamp
	 *
	 */
	@DisplayName("Test Set modifiedTiemstamp")
	@Nested
	class testMT{
		
		/**
		 * Test invalid modified timestamp
		 * @param time modified time stamp
		 */
		@DisplayName("Test invalid modified timestamp")
		@ParameterizedTest(name = "modified timestamp {0} + 1")
		@ValueSource(longs = {-11111111, 9223372036854775807L})
		void test(long time) {
			Exception e = Assertions.assertThrows(
					IllegalArgumentException.class,()->{new ItemList(1L, time+1);});
			assertEquals("ModifiedTiemStamp is invalid", e.getMessage());
		}
		
		/**
		 * Test valid modified time stamp 
		 * @param time modified time stamp
		 */
		@DisplayName("Test valid modified time stamp")
		@ParameterizedTest(name = "modified timestamp {0}")
		@ValueSource(longs = {1L, 20221221142311L, 9223372036854775807L})
		void test1(long time) {
			ItemList list = new ItemList(1L, time);
			assertTrue("Modified timestamp", time == list.getModifiedTimestamp());
		}
	}
	
	/**
	 * Test list
	 *
	 */
	@DisplayName("Test list")
	@Nested
	class TestList{
		/**
		 * Add item to list, list is null
		 */
		@Test
		@DisplayName("Add item to list, list is null")
		void test() {
			ItemList list = new ItemList(0L, 0L);
			Item item = null;
			Exception e = Assertions.assertThrows(
					IllegalArgumentException.class,()->{list.addItem(item);});
			assertEquals("item is null", e.getMessage());
		}
		
		/**
		 * Add item to list
		 */
		@Test
		@DisplayName("Add item to list")
		void test1() {
			Item item1 = new Item("0k", MealType.Lunch, 1, 1.2);
			Item item2 = new Item("F!",MealType.Dinner,2,100.1);
			ItemList list = new ItemList(1L, 1L);
			list.addItem(item1);
			list.addItem(item2);
			assertAll(
			  ()-> assertEquals("0k", list.getItemList().get(0).getName()),
			  ()-> assertEquals(MealType.Lunch, list.getItemList().get(0).getMealType()),
			  ()-> assertEquals(1, list.getItemList().get(0).getCalories()),
			  ()-> assertEquals(1.2, list.getItemList().get(0).getFat()),
			  ()-> assertEquals("F!",list.getItemList().get(1).getName()),
			  ()-> assertEquals(MealType.Dinner, list.getItemList().get(1).getMealType()),
			  ()->assertEquals(2, list.getItemList().get(1).getCalories()),
			  ()->assertEquals(100.1,list.getItemList().get(1).getFat()));
			 
		}
	}
	
	/**
	 * Test toString
	 *
	 */
	@DisplayName("Test toString")
	@Nested
	class toStringTest{
		
		/**
		 * toString test 1
		 */
		@Test
		@DisplayName("toString test 1")
		void test() {
			ItemList list = new ItemList(1L, 1L);
			Item item1 = new Item("0k", MealType.Lunch, 1, 1.2);
			String s = "LIST (TS=1) last mod=1, list={" +item1.toString()+ "}";
			list.addItem(item1);
			String s1 = list.toString();
			assertEquals(s, s1);
		}
		
		/**
		 * toString test 2
		 */
		@Test
		@DisplayName("toString test 2")
		void test1() {
			ItemList list = new ItemList(1L, 1L);
			Item item1 = new Item("0k", MealType.Lunch, 1, 1.2);
			Item item2 = new Item("F!",MealType.Dinner,2,8.9);
			String s = "LIST (TS=1) last mod=1, list={" 
			           +item1.toString()+","+item2.toString()+ "}";
			list.addItem(item1);
			list.addItem(item2);
			String s1 = list.toString();
			assertEquals(s, s1);
		}
	}
	
	/**
	 * Testing HashCode and Equals
	 */
	@Test
	@DisplayName("Testing Hashcode and Equals")
	void test() {
		ItemList l1 = new ItemList(1L, 1L);
		ItemList l2 = new ItemList(1L, 1L);
		ItemList l3 = new ItemList(1L, 0L);
		assertTrue(l1.equals(l2) && l2.equals(l1));
		assertTrue(l1.hashCode() == l2.hashCode());
		assertFalse(l1.equals(l3) && l3.equals(l1));
		assertFalse(l1.equals(l3) != l3.equals(l1));
		assertFalse(l1.equals(null));
		assertTrue(l1.equals(l1));
		assertFalse(l1.equals(" "));
	}

}
