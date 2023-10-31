package fiat.serialization.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.DecimalFormat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import fiat.serialization.Item;
import fiat.serialization.MealType;

/**
 * Item Test
 * @author Maiqi Hou
 * @version 1.0
 *
 */
class ItemTest {
	
	/**
	 * 
	 * Testing constructor and toString
	 *
	 */
	@DisplayName("Testing Constructor and toString")
	@Nested
	class CandTTest{
		/**
		 * Constructor with null
		 */
		@Test
		@DisplayName("constructor with null")
		void test() { 
			assertThrows(IllegalArgumentException.class, 
					() -> {new Item(null, null, 0, 0);});
		}
		
		/**
		 * Constructor with invalid name
		 * @param name name of item
		 */
		@DisplayName("Constructor with invalid name")
		@ParameterizedTest(name = "name = {0}")
		@ValueSource(strings = {"!Ok", "\rTT","1\r\nok", "~h"," sh"})
		void test1(String name) {
			assertThrows(IllegalArgumentException.class, 
					() -> {new Item(name, MealType.Breakfast, 1, 1.0);});
		}
		
		/**
		 * Constructor with valid name
		 * @param name name of item
		 */
		@DisplayName("Constructor with valid name")
		@ParameterizedTest(name = "name = {0}")
		@ValueSource(strings = {"1Ok","Fr ies", "Egg", "Noodle"})
		void test2(String name) {
			Item item = new Item(name, MealType.Snack, 1, 1.0);
			DecimalFormat f = new DecimalFormat("#");
			String s = item.getName() + " with " 
					+ item.getCalories() + " calories and "
					+ f.format(item.getFat()) +"g of fat eaten at "
					+ item.getMealType().toString();
			assertEquals(s, item.toString());
		}
		
		
		
		/**
		 * Constructor with invalid MealType
		 */
		@Test
		@DisplayName("Constructor with invalid MealType")
		void test3() {
			assertThrows(IllegalArgumentException.class, 
					() -> {new Item("Coke",null, 1, 1.0);});
		}
		
		/**
		 * Constructor with valid mealtype
		 */
		@Test
		@DisplayName("Constructor with valid MealType")
		void test4() {
			Item item = new Item("coke", MealType.Snack, 1, 1.0);
			DecimalFormat f = new DecimalFormat("#");
			String s = item.getName() + " with " 
					+ item.getCalories() + " calories and "
					+ f.format(item.getFat()) +"g of fat eaten at "
					+ item.getMealType().toString();
			assertEquals(s, item.toString());
		}
		
		
		/**
		 * Constructor with invalid calories
		 * @param cal  number of calories in item
		 */
		@DisplayName("Constructor with invalid calories")
		@ParameterizedTest(name = "calories = {0}")
		@ValueSource(ints = {-1, 2049})
		void test5(int cal) {
			assertThrows(IllegalArgumentException.class, 
					() -> {new Item("Coke", MealType.Breakfast, cal, 1.0);});
		}
		
		/**
		 * Constructor with valid calories
		 * @param cal number of calories in item
		 */
		@DisplayName("Constructor with valid calories")
		@ParameterizedTest(name = "calories = {0}")
		@ValueSource(ints = {0,100,2000,2048})
		void test6(int cal) {
			Item item = new Item("coke", MealType.Lunch, cal, 1.0);
			DecimalFormat f = new DecimalFormat("#");
			String s = item.getName() + " with " 
					+ item.getCalories() + " calories and "
					+ f.format(item.getFat()) +"g of fat eaten at "
					+ item.getMealType().toString();
			assertEquals(s, item.toString());
		}
		
		/**
		 * Constructor with invalid fat
		 * @param fat gram of fat in item
		 */
		@DisplayName("Constructor with invalid fat")
		@ParameterizedTest(name = "fat = {0}")
		@ValueSource(doubles = {100000.1,-1.0, 100000.001})
		void test7(double fat) {
			assertThrows(IllegalArgumentException.class, 
					() -> {new Item("Coke", MealType.Breakfast, 1, fat);});
		}
		
		/**
		 * Constructor with valid fat
		 * @param fat gram of fat in item
		 */
		@DisplayName("Constructor with valid fat")
		@ParameterizedTest(name = "fat = {0}")
		@ValueSource(doubles = {100000.0, 99999.9, 1.0, 100.1, 0.5})
		void test8(double fat) {
			Item item = new Item("coke", MealType.Dinner, 1, fat);
			double d = Math.round(item.getFat());
			DecimalFormat f = new DecimalFormat("#");
			String s = item.getName() + " with " 
					+ item.getCalories() + " calories and "
					+ f.format(d) +"g of fat eaten at "
					+ item.getMealType().toString();
			assertEquals(s, item.toString());
		}
	}
	
	
	/**
	 * Testing setName
	 */
	@DisplayName("Testing SetName")
	@Nested
	class SetNameTest{
		/**
		 * Test name is null
		 */
		@Test
		@DisplayName("Name is null")
		void test() {
			Item item = new Item();
			Exception e = Assertions.assertThrows(
			 IllegalArgumentException.class, () -> {item.setName(null);});
			Assertions.assertEquals("name is null", e.getMessage());
		}
		
		/**
		 * Sub setName testing (Unsigned Integer String)
		 */
		@DisplayName("Check char count Unsigned Integer String")
		@Nested
		class subTesting{
			
			/**
			 * Test Name is empty
			 */
			@Test
			@DisplayName("Char count -> Name is empty")
			void test() {
				Item item = new Item();
				Exception e = Assertions.assertThrows(
				 IllegalArgumentException.class, () -> {item.setName("");});
				Assertions.assertEquals("name is invalid", e.getMessage());
			}
			
			/**
			 * char count out of range 2048
			 */
			@Test
			@DisplayName("Char count -> out of 2048")
			void test1() {
				String[] temp = new String[2049];
				for(int i = 0; i < 2049; i++) {
					temp[i] = "1";
				}
				String s = String.join("", temp);
				Item item = new Item();
				Exception e = Assertions.assertThrows(
				 IllegalArgumentException.class, () -> {item.setName(s);});
				Assertions.assertEquals("name is invalid", e.getMessage());
			}
			
		}
		
		/**
		 * Check character list
		 */
		@DisplayName("Check char list")
		@Nested
		class subTesting2{
			/**
			 * Check character list first char invalid
			 * @param name for meal name
			 */
			@DisplayName("char lits -> first char invalid")
			@ParameterizedTest(name = "Name = {0}")
			@ValueSource(strings = {"!Ok", "/ri", "*ries", "$hah", " Bread", "()Coke"})
			void test6(String name) {
				Item item = new Item();
				Exception e = Assertions.assertThrows(
				 IllegalArgumentException.class, () -> {item.setName(name);});
				Assertions.assertEquals("name is invalid", e.getMessage());
			}
			
			/**
			 * Check character list first char valid
			 * @param name for meal name
			 */
			@DisplayName("char lits -> first char valid")
			@ParameterizedTest(name = "Name = {0}")
			@ValueSource(strings = {"0Ok", "Ar", "9rses", "aha~", "Z*","z!", "19"})
			void test7(String name) {
				Item item = new Item();
				item.setName(name);
				assertEquals(name, item.getName());
			}

			/**
			 * check character list valid character
			 * @param name for meal name
			 */
			@DisplayName("Invalid character list")
			@ParameterizedTest(name = "Name = {0}")
			@ValueSource(strings = {"0\r\nk", "a\n", "A\rnj"})
			void test8(String name) {
				Item item = new Item();
				Exception e = Assertions.assertThrows(
				 IllegalArgumentException.class, () -> {item.setName(name);});
				Assertions.assertEquals("name is invalid", e.getMessage());
			}
			
			/**
			 * check valid character list
			 * @param name for meal name
			 */
			@DisplayName("Valid character list")
			@ParameterizedTest(name = "Name = {0}")
			@ValueSource(strings = {"0AB", "Ok", "Hha!","Fries~"})
			void test9(String name) {
				Item item = new Item();
				item.setName(name);
				assertEquals(name, item.getName());
			}
		}
	}
	
	
	
	
	/**
	 * Testing setMealType
	 */
	@DisplayName("Testing setMealType")
	@Nested
	class testMealType{
		/**
		 * Test Meal type is null
		 */
		@Test
		@DisplayName("MealType is null")
		void test() {
			Item item = new Item();
			Exception e = Assertions.assertThrows(
			 IllegalArgumentException.class, () -> {item.setMealType(null);});
			Assertions.assertEquals("MealType is null", e.getMessage());
		}
		
		/**
		 * Test meal type is valid
		 * @param type Meal type
		 */
		@DisplayName("Valid MealType")
		@ParameterizedTest(name = "MealType = {0}")
		@ValueSource(chars = {'B','L','D','S'})
		void test1(char type) {
			Item item = new Item();
			item.setMealType(MealType.getMealType(type));
			String t = MealType.getMealType(type).toString();
			assertEquals(t, item.getMealType().toString());
		}
		
		/**
		 * Test invalid MealType
		 * @param type meal type
		 */
		@DisplayName("Invalid MealType")
		@ParameterizedTest(name = "MealType = {0}")
		@ValueSource(chars = {'\r','b','A','1',' '})
		void test2(char type) {
			Item item = new Item();
			Exception e = Assertions.assertThrows(IllegalArgumentException.class, 
					() -> {item.setMealType(MealType.getMealType(type));});
			Assertions.assertEquals("Bad code value", e.getMessage());
		}
	}
	
	/**
	 * Test setCalories
	 */
	@DisplayName("Testing set calories")
	@Nested
	class testSetCalories{
		
		/**
		 * Invalid calories
		 * @param cal for calories
		 */
		@DisplayName("Invalid calories")
		@ParameterizedTest(name = "calories = {0}")
		@ValueSource(ints = {-100,10000000,-2048, 2049})
		void test(int cal) {
			Item item = new Item();
			Exception e = Assertions.assertThrows(IllegalArgumentException.class, 
					() -> {item.setCalories(cal);});
			Assertions.assertEquals("invalid calories", e.getMessage());
		}
		
		/**
		 * Valid calories
		 * @param cal for calories
		 */
		@DisplayName("Valid calories")
		@ParameterizedTest(name = "calories = {0}")
		@ValueSource(ints = {0, 100, 2048, 1000, 2000, 2047})
		void test1(int cal) {
			Item item = new Item();
			item.setCalories(cal);
			assertEquals(cal, item.getCalories());
		}
	}
	
	/**
	 * Test set fat
	 */
	@DisplayName("Testing set fat")
	@Nested
	class testSetFat{
		/**
		 * Testing Invalid fat
		 * @param fat gram of fat
		 */
		@DisplayName("Invalid fat")
		@ParameterizedTest(name = "fat = {0}")
		@ValueSource(doubles = {-1.0, 100000.5, 100000.01, 100000.001})
		void test(double fat) {
			Item item = new Item();
			Exception e = Assertions.assertThrows(IllegalArgumentException.class, 
					() -> {item.setFat(fat);});
			Assertions.assertEquals("invalid fat", e.getMessage());
		}
		
		/**
		 * Testing valid fat
		 * @param fat gram of fat
		 */
		@DisplayName("Valid fat")
		@ParameterizedTest(name = "fat = {0}")
		@ValueSource(doubles = {0.0, 1.0, 99999.9, 100000.0})
		void test1(double fat) {
			Item item = new Item();
			item.setFat(fat);
			assertEquals(fat, item.getFat(), 0.1);
		}
	}

	/**
	 * Hash code and Equals
	 *
	 */
	@DisplayName("Hash Code and Equals")
	@Nested
	class testHE{
		/**
		 * Testing Hashcode and equals
		 */
		@Test
		@DisplayName("Testing Hashcode and Equals")
		void test() {
			Item item1 = new Item("coke", MealType.Snack, 1000, 125.1);
			Item item2 = new Item("coke", MealType.Snack, 1000, 125.1);
			assertTrue(item1.equals(item2) && item2.equals(item1));
			assertTrue(item1.hashCode() == item2.hashCode());
			Item item3 = new Item();
			String s = " ";
			assertFalse(item1.equals(item3) && item3.equals(item1));
			assertFalse(item1.equals(item3) != item3.equals(item1));
			assertFalse(item1.equals(null));
			assertTrue(item1.equals(item1));
			assertFalse(item1.equals(s));
			
		}
	}
}
