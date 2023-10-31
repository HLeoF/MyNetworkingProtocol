package fiat.serialization.test;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import fiat.serialization.MealType;

/**
 * Meal Type Test
 * @author Maiqi Hou
 * @version 1.0
 *
 */
class MealTypeTest {
	
	/**
	 * Nest test for getMealType
	 *
	 */
	@DisplayName("Testing getMealType")
	@Nested
	class getMealTypeTest{
		
		/**
		 * Testing valid meal code  B
		 */
		@Test
		@DisplayName("valid code B")
		void test() {
			String s = MealType.getMealType('B').toString();
			assertEquals("Breakfast",s);
		}
		
		/**
		 * Testing valid meal code  L
		 */
		@Test
		@DisplayName("valid code L")
		void test1() {
			String s = MealType.getMealType('L').toString();
			assertEquals("Lunch",s);
		}
		
		/**
		 * Testing valid meal code  D
		 */
		@Test
		@DisplayName("valid code D")
		void test2() {
			String s = MealType.getMealType('D').toString();
			assertEquals("Dinner",s);
		}
		
		/**
		 * Testing valid meal code  S
		 */
		@Test
		@DisplayName("valid code S")
		void test3() {
			String s = MealType.getMealType('S').toString();
			assertEquals("Snack",s);
		}
		
		/**
		 * Testing invalid meal code 
		 * @param code  invalid meal code
		 */
		@DisplayName("invalid code")
		@ParameterizedTest(name = "code = {0}")
		@ValueSource(chars = {'$', 'a', 'X', '\r', '!', '0', '9'})
		void test4(char code) {
			Exception e = Assertions.assertThrows(
					IllegalArgumentException.class, ()->{MealType.getMealType(code);});
			Assertions.assertEquals("Bad code value", e.getMessage());
		}
		
	}

}
