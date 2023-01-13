/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 4
* Class: CSI 4321
*
************************************************/
package foop.serialization.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import fiat.serialization.MealType;
import foop.serialization.Addition;

/**
 * Test Addition Method
 * @author Maiqi Hou
 * @version 1.0
 *
 */
class AdditionTest {
	
	/**
	 * Testing addition method
	 */
	@DisplayName("Testing constructor and toString")
	@Nested
	class constructorTest{
		
		/**
		 * Constructor with name is null
		 */
		@Test
		@DisplayName("constructor with name is null")
		void test() {
			Exception e = assertThrows(IllegalArgumentException.class, 
					()->{new Addition(0, null, MealType.Breakfast, 0);});
			assertEquals("name is null", e.getMessage());
		}
		
		/**
		 * Constructor with Meal type is null
		 */
		@Test
		@DisplayName("constructor with Mealtype is null")
		void test1() {
			Exception e = assertThrows(IllegalArgumentException.class, 
					()->{new Addition(0, "coke", null, 0);});
			assertEquals("MealType is null", e.getMessage());
		}
		
		/**
		 * Constructor with invalid name
		 * @param name addition item name
		 */
		@DisplayName("Constructor with invalid name")
		@ParameterizedTest(name = "name = {0}")
		@ValueSource(strings = {"\rTT","1\r\nok"})
		void test2(String name) {
			Exception e = assertThrows(IllegalArgumentException.class, 
					()->{new Addition(0, name, MealType.Dinner, 0);});
			assertEquals("name is invalid", e.getMessage());
		}
		
		/**
		 * Constructor with valid name
		 * @param name addition item name
		 */
		@DisplayName("Constructor with valid name")
		@ParameterizedTest(name = "name = {0}")
		@ValueSource(strings = {"1Ok","Fr ies", "Egg", "Noodle","!Ok", "~h"})
		void test3(String name) {
			Addition addition = new Addition(0, name , MealType.Dinner, 0);
			assertEquals(name, addition.getName());
		}
		
		/**
		 * Constructor with valid meal type
		 */
		@Test
		@DisplayName("Constructor with valid MealType")
		void test4() {
			Addition addition = new Addition(255, "coke", MealType.Lunch, 2048);
			assertEquals(MealType.Lunch, addition.getMealType());
		}
		
		/**
		 * Constructor with invalid calories
		 * @param cal addition item calories
		 */
		@DisplayName("Constructor with invalid calories")
		@ParameterizedTest(name = "calories = {0}")
		@ValueSource(ints = {-1, 2049})
		void test5(int cal) {
			Exception e = assertThrows(IllegalArgumentException.class, 
					()->{new Addition(0, "Coke", MealType.Lunch, cal);});
			assertEquals("invalid calories", e.getMessage());
		}
		
		/**
		 * Constructor with valid calories
		 * @param cal addition item calories
		 */
		@DisplayName("Constructor with valid calories")
		@ParameterizedTest(name = "calories = {0}")
		@ValueSource(ints = {0,100,2000,2048})
		void test6(int cal) {
			Addition addition = new Addition(0, "Coke", MealType.Dinner, cal);
			String s = "Addition: MsgID="+addition.getMsgID() + 
						" Name=" + addition.getName() +  " Calories=" +
						addition.getCalories() + " Meal=" + addition.getMealType();
			String s1 = addition.toString();
			assertEquals(s, s1);
		}
		
		/**
		 * Set Name length out of range
		 */
		@Test
		@DisplayName("Set Name out of range")
		void test7() {
			StringBuffer buffer = new StringBuffer();
			for(int i = 0; i < 256; i++) {
				buffer.append("Z");
			}
			String temp = buffer.toString();
			Exception e = assertThrows(IllegalArgumentException.class, 
					()->{new Addition(0, temp, MealType.Lunch, 150);});
			assertEquals("name is invalid", e.getMessage());
		}
		
	}
	
	/**
	 * Test Addition method Hash Code and Equals
	 */
	@Test
	@DisplayName("Addition Hash Code and Equals")
	void test() {
		Addition a1 = new Addition(255, "H", MealType.Dinner, 2048);
		Addition a2 = new Addition(255, "H", MealType.Dinner, 2048);
		Addition a3 = new Addition(0, "A", MealType.Breakfast, 0);
		assertTrue(a1.equals(a2) && a2.equals(a1));
		assertTrue(a1.hashCode() == a2.hashCode());
		assertFalse(a1.equals(a3) && a3.equals(a1));
		assertFalse(a1.equals(a3) != a3.equals(a1));
		assertFalse(a1.equals(null));
		assertTrue(a1.equals(a1));
		assertFalse(a1.equals(" "));
	}
	
}
