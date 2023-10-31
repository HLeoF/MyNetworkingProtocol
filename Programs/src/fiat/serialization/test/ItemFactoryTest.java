package fiat.serialization.test;


import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import fiat.serialization.Item;
import fiat.serialization.ItemFactory;
import fiat.serialization.MealType;
import fiat.serialization.MessageInput;
import fiat.serialization.MessageOutput;
import fiat.serialization.TokenizerException;

/**
 * Item factory test
 * @author Maiqi Hou
 * @version 1.4
 * Adding test case for IO problem and premature problem
 */
class ItemFactoryTest {
	private static final Charset UTF8 = StandardCharsets.UTF_8; //Standard charsets UTF-8
	/**
	 * Testing decoed method
	 *
	 */
	@DisplayName("Testing decode method")
	@Nested
	class DecodeTest{
		/**
		 * Testing messageinput is null
		 */
		@Test
		@DisplayName("Message in is null")
		void test() {
			MessageInput in = null;
			Exception e = Assertions.assertThrows(NullPointerException.class, 
					()->{ItemFactory.decode(in);});
			assertEquals("in is null", e.getMessage());
		}
		
		/**
		 * Testing invalid encode 
		 *
		 */
		@DisplayName("Invalid decode")
		@Nested
		class Invaliddecode{
			
			/**
			 * Test character count is invalid
			 * @param s character count
			 */
			@DisplayName("Test Character count is invalid")
			@ParameterizedTest(name = "Count = {0}")
			@ValueSource(strings = {"A", "\r", "!", "~", "\n","5!", "29@","790!"})
			void test(String s) {
				String temp = s + " FriesB512 5.6 ";
				new ItemFactory();
				assertThrows(TokenizerException.class, 
				 ()->{ItemFactory.decode(new MessageInput(
					new ByteArrayInputStream(temp.getBytes(UTF8))));});
			}
			
			/**
			 * Test first char of char list invalid 
			 * @param s first character of character list
			 */
			@DisplayName("Test first char of char list invalid")
			@ParameterizedTest(name = "name = {0}")
			@ValueSource(strings = {"\rOk", "\nYT", "~H", " S", "\rAA"})
			void test1(String s) {
				String temp = "5 " + s + "B512 5.6 ";
				assertThrows(TokenizerException.class, 
						 ()->{ItemFactory.decode(new MessageInput(
							new ByteArrayInputStream(temp.getBytes(UTF8))));});
			}
			
			/**
			 * Test character list invalid
			 * @param s character list
			 */
			@DisplayName("Test character list invalid")
			@ParameterizedTest(name = "name = {0}")
			@ValueSource(strings = {"F\ries", "Fr\nes","Fri\ns", "Frie\n"})
			void test2(String s) {
				InputStream in = new ByteArrayInputStream("ss".getBytes(UTF8)); 
				MessageInput input = new MessageInput(in);
				assertThrows(TokenizerException.class, 
						 ()->{ItemFactory.decodeString(input);});
			}
			
			/**
			 * Test mealtype invalid
			 * @param c meal type
			 */
			@DisplayName("Test mealtype invalid")
			@ParameterizedTest(name = "Mealtype = {0}")
			@ValueSource(chars = {'Y','Z','!','@','\r'})
			void test3(char c) {
				String temp = "5 Fries"+c+"512 5.6 ";
				assertThrows(TokenizerException.class, 
						 ()->{ItemFactory.decode(new MessageInput(
							new ByteArrayInputStream(temp.getBytes(UTF8))));});
			}
			
			/**
			 * Testing calories invalid
			 * @param s calories of item
			 */
			@DisplayName("Testing calories invalid")
			@ParameterizedTest(name = "Calories = {0}")
			@ValueSource(strings = {"2049","-111","21000","-102","-1","78999"})
			void test4(String s) {
				String temp = "5 FriesB"+s+" 5.6 ";
				assertThrows(TokenizerException.class, 
						 ()->{ItemFactory.decode(new MessageInput(
							new ByteArrayInputStream(temp.getBytes(UTF8))));});
			}
			
			/**
			 * Testing fat invalid
			 * @param s gram of fat
			 */
			@DisplayName("Testing fat invalid")
			@ParameterizedTest(name = "Fat = {0}")
			@ValueSource(strings = {"100000.1","-1000.0","-1A.0","23!.0,","5","1"})
			void test5(String s) {
				String temp = "5 FriesB512 "+s+" ";
				assertThrows(TokenizerException.class, 
						 ()->{ItemFactory.decode(new MessageInput(
							new ByteArrayInputStream(temp.getBytes(UTF8))));});
			}
			
			/**
			 * Test decode premature problem
			 * @param temp - item
			 */
			@DisplayName("Test decode premature problem")
			@ParameterizedTest(name = "item = {0}")
			@ValueSource(strings = {"5 Fries", "4 Ok!", "1 AB", "2 OKB1 1."})
			void test6(String temp) {
				assertThrows(TokenizerException.class, 
						 ()->{ItemFactory.decode(new MessageInput(
							new ByteArrayInputStream(temp.getBytes(UTF8))));});
			}
			
			
			
			/**
			 * Test decode IO problem
			 * @throws TokenizerException if validation failure
			 * @throws IOException - I/O problem
			 */
			@Test
			@DisplayName("Test decode IO problem")
			void test7() throws TokenizerException, IOException {
				byte[] b = {52, 32, 112, -55, -91, 117, 109, 66, 51, 32, 52, 46, 53, 32};
				MessageInput input = new MessageInput(new ByteArrayInputStream(b));
				ItemFactory.decode(input);
				input.close();
				assertThrows(TokenizerException.class, 
						 ()->{ItemFactory.decode(input);});
			}
		}
		
		/**
		 * Testing valid decode
		 */
		@DisplayName("Valid decode")
		@Nested
		class Validdecode{
			/**
			 * Valid Encode test 1
			 * @throws IOException if I/O problem
			 * @throws TokenizerException if validation failure
			 */
			@Test
			@DisplayName("valid decode test 1")
			void test() throws IOException, TokenizerException {
				Item i = ItemFactory.decode(new MessageInput(
				new ByteArrayInputStream("5 FriesB512 5.6 ".getBytes(UTF8))));
				assertAll(()->assertEquals("Fries", i.getName()),
				        () -> assertEquals(MealType.Breakfast, i.getMealType()),
				        () -> assertEquals(512, i.getCalories()),
				        () -> assertEquals(5.6, i.getFat(), 0.0001));
			}
			
			/**
			 * Valid Encode test2
			 * @throws IOException if I/O problem
			 * @throws TokenizerException if validation failure
			 */
			@Test
			@DisplayName("valid decode test 2")
			void test1() throws IOException, TokenizerException {
				Item i = ItemFactory.decode(new MessageInput(
				 new ByteArrayInputStream("10 ABCcd~qui!L2048 100000.0 ".getBytes(UTF8))));
				assertAll(()->assertEquals("ABCcd~qui!", i.getName()),
				        () -> assertEquals(MealType.Lunch, i.getMealType()),
				        () -> assertEquals(2048, i.getCalories()),
				        () -> assertEquals(100000.0, i.getFat(), 0.1));
			}
			
			/**
			 * valid Encode test 3
			 * @throws IOException if I/O problem
			 * @throws TokenizerException if validation failure
			 */
			@Test
			@DisplayName("valid decode test 3")
			void test2() throws IOException, TokenizerException{
				Item i = ItemFactory.decode(new MessageInput(
				 new ByteArrayInputStream("5 SteakD1999 1989.89 ".getBytes(UTF8))));
				assertAll(()->assertEquals("Steak", i.getName()),
				        () -> assertEquals(MealType.Dinner, i.getMealType()),
				        () -> assertEquals(1999, i.getCalories()),
				        () -> assertEquals(1989.9, i.getFat(), 0.1));
			}
			
			/**
			 * valid Encode test 4
			 * @throws IOException if I/O problem
			 * @throws TokenizerException if validation failure
			 */
			@Test
			@DisplayName("valid decode test 4")
			void test3() throws IOException, TokenizerException {
				Item i = ItemFactory.decode(new MessageInput(
				 new ByteArrayInputStream("4 CokeS2000 200.42 ".getBytes(UTF8))));
				assertAll(()->assertEquals("Coke", i.getName()),
				        () -> assertEquals(MealType.Snack, i.getMealType()),
				        () -> assertEquals(2000, i.getCalories()),
				        () -> assertEquals(200.4, i.getFat(), 0.1));
			}
			
			/**
			 * valid Encode test 5
			 * @throws IOException if I/O problem
			 * @throws TokenizerException if validation failure
			 */
			@Test
			@DisplayName("valid decode test 5")
			void test4() throws IOException, TokenizerException {
				Item i = ItemFactory.decode(new MessageInput(
						new ByteArrayInputStream("2 okL1928 99999.99 ".getBytes(UTF8))));
				assertAll(()->assertEquals("ok", i.getName()),
				        () -> assertEquals(MealType.Lunch, i.getMealType()),
				        () -> assertEquals(1928, i.getCalories()),
				        () -> assertEquals(100000.0, i.getFat(), 0.1));
			}
			
		}
		
	}
	
	
	
	
	
	/**
	 * Testing encode method
	 */
	@DisplayName("Testing encode method")
	@Nested
	class EncodeTest{
		
		/**
		 * encode when out is null
		 */
		@Test
		@DisplayName("encode when out is null")
		void test() {
			Item item = new Item("Fries",MealType.Snack,512,5.6);
			Exception e = Assertions.assertThrows(
				NullPointerException.class, ()->{ItemFactory.encode(item, null);});
			Assertions.assertEquals("out is null", e.getMessage());}
		
		/**
		 * encode when item is null
		 */
		@Test
		@DisplayName("encode when item is null")
		void test1() {
			Item item = null;
			MessageOutput out = new MessageOutput(new ByteArrayOutputStream());
			Exception e = Assertions.assertThrows(
				NullPointerException.class, ()->{ItemFactory.encode(item, out);});
			Assertions.assertEquals("item is null", e.getMessage());
		}
		
		/**
		 * Valid Encode
		 */
		@DisplayName("valid encode")
		@Nested
		class validEncode{
			/**
			 * Valid encode test 1 
			 * @throws IOException if I/O problem
			 */
			@Test
			@DisplayName("valid encode test1")
			void test() throws IOException {
				Item item = new Item("coke", MealType.Snack,2048,5.64);
				MessageOutput out = new MessageOutput(new ByteArrayOutputStream());
				ItemFactory.encode(item, out);
				String s = "4 cokeS2048 5.6 ";
				assertEquals(s, new String(out.toByteArray(),UTF8));
			}
			
			
			/**
			 * Valid encode test 2
			 * @throws IOException if I/O problem
			 */
			@Test
			@DisplayName("valid encode test2")
			void test1() throws IOException {
				Item item = new Item("Bread", MealType.Breakfast,199,5.67);
				MessageOutput out = new MessageOutput(new ByteArrayOutputStream());
				ItemFactory.encode(item, out);
				String s = "5 BreadB199 5.7 ";
				assertEquals(s, new String(out.toByteArray(),UTF8));
			}
			
			
			/**
			 * valid encode test3
			 * @throws IOException if I/O problem
			 */
			@Test
			@DisplayName("valid encode test3")
			void test2() throws IOException {
				Item item = new Item("pizza", MealType.Lunch,1008,2800.19);
				MessageOutput out = new MessageOutput(new ByteArrayOutputStream());
				ItemFactory.encode(item, out);
				String s = "5 pizzaL1008 2800.2 ";
				assertEquals(s, new String(out.toByteArray(),UTF8));
			}
			
			/**
			 * valid encode test4
			 * @throws IOException if I/O problem
			 */
			@Test
			@DisplayName("valid encode test4")
			void test3() throws IOException {
				Item item = new Item("Steak", MealType.Dinner, 1286, 1756.42);
				MessageOutput out = new MessageOutput(new ByteArrayOutputStream());
				ItemFactory.encode(item, out);
				String s = "5 SteakD1286 1756.4 ";
				assertEquals(s, new String(out.toByteArray(),UTF8));
			}
			
			/**
			 * valid encode test4
			 * @throws IOException if I/O problem
			 */
			@Test
			@DisplayName("valid encode test5")
			void test4() throws IOException {
				Item item = new Item("Steak", MealType.Dinner, 1286, 0.5);
				MessageOutput out = new MessageOutput(new ByteArrayOutputStream());
				ItemFactory.encode(item, out);
				String s = "5 SteakD1286 0.5 ";
				assertEquals(s, new String(out.toByteArray(),UTF8));
			}
		}
	}

}
