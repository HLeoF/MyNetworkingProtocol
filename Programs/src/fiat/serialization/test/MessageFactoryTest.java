package fiat.serialization.test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import fiat.serialization.Add;
import fiat.serialization.Error;
import fiat.serialization.Get;
import fiat.serialization.Interval;
import fiat.serialization.Item;
import fiat.serialization.ItemList;
import fiat.serialization.MealType;
import fiat.serialization.Message;
import fiat.serialization.MessageFactory;
import fiat.serialization.MessageInput;
import fiat.serialization.MessageOutput;
import fiat.serialization.TokenizerException;

/**
 * Message Factory Testing
 * @author Maiqi Hou
 * @version 1.3
 * ADD Interval message tests
 *
 */
class MessageFactoryTest {
	private static final Charset UTF8 = StandardCharsets.UTF_8; //standard charsets UTF-8
	
	/**
	 *Test Decode
	 */
	@DisplayName("Test Decode")
	@Nested
	class testDecode{
		
		/**
		 * Check whether before REQUEST
		 */
		@DisplayName("Check whether before REQUEST")
		@Nested
		class BeforeRequest{
			
			/**
			 * Invalid Protocol name
			 * @param s for protocol name
			 */
			@DisplayName("Invalid Protocol name")
			@ParameterizedTest(name = "Protocol name = {0}")
			@ValueSource(strings = {"FT1.o ","FT1.1 ","AF1.0 ","!F0.0 "})
			void test(String s) {
				String temp = s + " 1000 GET \n";
			  assertThrows(TokenizerException.class, 
						 ()->{MessageFactory.decode(new MessageInput(
				new ByteArrayInputStream(temp.getBytes(UTF8))));});
			}
			
			/**
			 * Invalid Message Timestamp
			 * @param time message timestamp
			 */
			@DisplayName("Invalid timestamp")
			@ParameterizedTest(name = "TimeStamp = {0}")
			@ValueSource(strings = {"-10000000", "9223372036854775808"})
			void test1(String time) {
				String temp = "FT1.0 " + time + " GET \n";
				assertThrows(TokenizerException.class, 
						 ()->{MessageFactory.decode(new MessageInput(
				new ByteArrayInputStream(temp.getBytes(UTF8))));});
			}
			
			/**
			 * Valid Message Timestamp
			 * @param time message timestamp
			 * @throws TokenizerException  if validation failure 
			 * @throws IOException I/O problem
			 */
			@DisplayName("Valid timestamp")
			@ParameterizedTest(name = "TimeStamp = {0}")
			@ValueSource(strings = {"20221031130056", "9223372036854775807"})
			void test2(String time) throws TokenizerException, IOException {
				String temp = "FT1.0 " + time + " GET \n";
				Get get = (Get) MessageFactory.decode(new MessageInput(
						new ByteArrayInputStream(temp.getBytes(UTF8))));
				long t = Long.parseUnsignedLong(time);
				assertAll(()->assertTrue("Test timestamp", t == get.getTimestamp()),
						  ()->assertEquals("GET", get.getRequest()));
			}
			
			/**
			 * Invalid Request Type
			 * @param type request type
			 */
			@DisplayName("invalid Request Type")
			@ParameterizedTest(name = "Request Type: {0}")
			@ValueSource(strings = {"BAD", "OK", "UPDATE","FIX"})
			void test3(String type) {
				String temp = "FT1.0 1 " + type + " \n";
				assertThrows(TokenizerException.class, 
						 ()->{MessageFactory.decode(new MessageInput(
				new ByteArrayInputStream(temp.getBytes(UTF8))));});
			}
		}
		
		
		/** 
		 *Subrequest Decode
		 */
		@DisplayName("SubRequest Decode")
		@Nested
		class subrequest {
			
			
			/**
			 * Get Decode test
			 */
			@DisplayName("GET Decode")
			@Nested
			class GETDecode{
				/**
				 * Valid timestamp
				 * @param time tiemstamp
				 * @throws TokenizerException if validation
				 * @throws IOException if I/O problem
				 */
				@DisplayName("Valid timestamp")
				@ParameterizedTest(name = "TimeStamp = {0}")
				@ValueSource(strings = {"20221031130056", "9223372036854775807", "1000000", "22321"})
				void test(String time) throws TokenizerException, IOException {
					String temp = "FT1.0 " + time + " GET \n";
					Get get = (Get) MessageFactory.decode(new MessageInput(
							new ByteArrayInputStream(temp.getBytes(UTF8))));
					long t = Long.parseUnsignedLong(time);
					assertAll(()->assertTrue("Test timestamp", t == get.getTimestamp()),
							  ()->assertEquals("GET", get.getRequest()));
					MessageOutput output = new MessageOutput(new ByteArrayOutputStream());
					MessageFactory.encode(get, output);
					byte[] a = temp.getBytes(UTF8);
					byte[] b = output.toByteArray();
					assertArrayEquals(a, b);
					
				}
				
				
				/**
				 * Invalid TimeStamp
				 * @param time TimeStamp
				 */
				@DisplayName("Invalid TimeStamp")
				@ParameterizedTest(name = "TimeStamp = {0}")
				@ValueSource(strings = {"9223372036854775808", "-1", "-20221031130056"})
				void test1(String time) {
					String temp = "FT1.0 " + time + " GET \n";
					assertThrows(TokenizerException.class, 
							 ()->{MessageFactory.decode(new MessageInput(
					new ByteArrayInputStream(temp.getBytes(UTF8))));});
				}
			}
			
			
			
			/**
			 * Error Decode test
			 *
			 */
			@DisplayName("ERROR Decode")
			@Nested
			class ERRORDecode{
				
				/**
				 * Test Message count is valid
				 * @param s message character count
				 */
				@DisplayName("Test Message count is valid")
				@ParameterizedTest(name = "Count = {0}")
				@ValueSource(strings = {"A", "\r", "!", "~", "\n","5!", "29@","790!"})
				void test(String s) {
					String temp = "FT1.0 1 ERROR " + s + " Bad\n";
					assertThrows(TokenizerException.class, 
							 ()->{MessageFactory.decode(new MessageInput(
					new ByteArrayInputStream(temp.getBytes(UTF8))));});
				}
				
				
				/**
				 * Test Message first char is invalid
				 * @param s first character of message
				 */
				@DisplayName("Test first char of char list invalid")
				@ParameterizedTest(name = "first char = {0}")
				@ValueSource(strings = {"!", "@", "*", " ", "\r"})
				void test1(String s) {
					String temp = "FT1.0 1 ERROR 3 "+ s +"ad\n";
					assertThrows(TokenizerException.class, 
							 ()->{MessageFactory.decode(new MessageInput(
					new ByteArrayInputStream(temp.getBytes(UTF8))));});
				}
				
				/**
				 * Test Message character list invalid
				 * @param s message 
				 */
				@DisplayName("Test Message character list inavlid")
				@ParameterizedTest(name = "message = {0}")
				@ValueSource(strings = {"B\rd","B\nd","Bn\r","Bn\n"})
				void test2(String s) {
					String temp = "FT1.0 1 ERROR 3 " + s + "\n";
					assertThrows(TokenizerException.class, 
							 ()->{MessageFactory.decode(new MessageInput(
					new ByteArrayInputStream(temp.getBytes(UTF8))));});
				}
				
				/**
				 * Test message valid 
				 * @param c character count
				 * @param m message
				 * @throws TokenizerException if validation
				 * @throws IOException if I/O problem
				 */
				@DisplayName("Test message valid")
				@ParameterizedTest(name = "time: {0}, msg#: {1}, msg: {2}")
				@MethodSource("ed")
				void test3(String c, String m) throws 
				TokenizerException, IOException {
					String temp = "FT1.0 1 ERROR " + c + " " + m + "\n";
					String temp1 = "ERROR";
					Error e = (Error) MessageFactory.decode(new MessageInput(
							new ByteArrayInputStream(temp.getBytes(UTF8))));
					assertAll(()-> assertEquals(m, e.getMessage()),
							  ()->assertEquals(temp1,e.getRequest()));
					MessageOutput output = new MessageOutput(new ByteArrayOutputStream());
					MessageFactory.encode(e, output);
					byte[] a = temp.getBytes(UTF8);
					byte[] b = output.toByteArray();
					assertArrayEquals(a, b);
				}
				/**
				 * Static stream for Error decode with valid value
				 * @return
				 * a stream with parameters
				 */
				static Stream<Arguments>ed(){
					return Stream.of(
							Arguments.of("3","Bad"),
							Arguments.of("5","Oops~"),
							Arguments.of("4","Ohh!"),
							Arguments.of("5","Ahh!~")
					);
				}
				
			}
			
			
			/**
			 * Add Encode Test
			 *
			 */
			@DisplayName("Add Encode Test")
			@Nested
			class AddEncode{
				
				/**
				 * Test item name invalid
				 * @param s item name
				 */
				@DisplayName("Test item name invalid")
				@ParameterizedTest(name = "item name = {0}")
				@ValueSource(strings = {"A coke", "3 \rok", "3 1\rN", "4 !\rjs"})
				void test(String s) {
					String temp = "FT1.0 1 ADD "+ s +"S101 10.8 \n";
					assertThrows(TokenizerException.class, 
							 ()->{MessageFactory.decode(new MessageInput(
					new ByteArrayInputStream(temp.getBytes(UTF8))));});
				}
				
				/**
				 * Test item meal type invalid 
				 * @param s  item meal type
				 */
				@DisplayName("Test item meal type invalid")
				@ParameterizedTest(name = "item meal type = {0}")
				@ValueSource(chars = {'Y','Z','!','@','\r'})
				void test1(char s) {
					String temp = "FT1.0 1 ADD 3 AHH"+s+"100 1.2 \n";
					assertThrows(TokenizerException.class, 
							 ()->{MessageFactory.decode(new MessageInput(
					new ByteArrayInputStream(temp.getBytes(UTF8))));});
				}
				
				/**
				 * Test item calories invalid
				 * @param s item calories
				 */
				@DisplayName("Test item calories invalid")
				@ParameterizedTest(name = "item calories = {0}")
				@ValueSource(strings = {"2049","1A1","100!","-102","-1Js","-100"})
				void test2(String s) {
					String temp = "FT1.0 1 ADD 3 AHHB"+s+" 1.2 \n";
					assertThrows(TokenizerException.class, 
							 ()->{MessageFactory.decode(new MessageInput(
					new ByteArrayInputStream(temp.getBytes(UTF8))));});
				}
				
				/**
				 * Test item fat invalid
				 * @param s item fat
				 */
				@DisplayName("Test item fat invalid")
				@ParameterizedTest(name = "item fat = {0}")
				@ValueSource(strings = {"100000.1","-1000.0","-1A.0","23!.0"})
				void test3(String s) {
					String temp = "FT1.0 1 ADD 3 AHHB100 "+s+" \n";
					assertThrows(TokenizerException.class, 
							 ()->{MessageFactory.decode(new MessageInput(
					new ByteArrayInputStream(temp.getBytes(UTF8))));});
				}
				
				/**
				 * Test Add decode 1
				 * @throws TokenizerException if validation
				 * @throws IOException if I/O problem
				 */
				@Test
				@DisplayName("Test Add decode 1")
				void test4() 
						throws TokenizerException, IOException {
					String s = "FT1.0 1 ADD 1 AB100 1.0 \n";
					Add add = (Add) MessageFactory.decode(new MessageInput(
							new ByteArrayInputStream(s.getBytes(UTF8))));
					MessageOutput output = new MessageOutput(new ByteArrayOutputStream());
					MessageFactory.encode(add, output);
					byte[] a = s.getBytes(UTF8);
					byte[] b = output.toByteArray();
					assertArrayEquals(a, b);
				}
				
				/**
				 * Test Add decode 2
				 * @throws TokenizerException if validation
				 * @throws IOException if I/O problem
				 */
				@Test
				@DisplayName("Test Add decode 1")
				void test5() 
						throws TokenizerException, IOException {
					String s = "FT1.0 1 ADD 4 cokeS1000 9.0 \n";
					Add add = (Add) MessageFactory.decode(new MessageInput(
							new ByteArrayInputStream(s.getBytes(UTF8))));
					MessageOutput output = new MessageOutput(new ByteArrayOutputStream());
					MessageFactory.encode(add, output);
					byte[] a = s.getBytes(UTF8);
					byte[] b = output.toByteArray();
					assertArrayEquals(a, b);
				}
				
			}
			
			
			/**
			 * ItemList Decode test
			 *
			 */
			@DisplayName("ItemList Decode test")
			@Nested
			class ItemListTest{
				
				/**
				 * Test modified tiemstamp invalid
				 * @param s modified timestamp
				 */
				@DisplayName("Test modified timestamp invalid")
				@ParameterizedTest(name = "modified time = {0}")
				@ValueSource(strings = {"9223372036854775808", "-20221031130056","-1"})
				void test(String s) {
					String temp = "FT1.0 1 LIST " +s+ " 0 \n";
					assertThrows(TokenizerException.class, 
							 ()->{MessageFactory.decode(new MessageInput(
					new ByteArrayInputStream(temp.getBytes(UTF8))));});}
			
				/**
				 * Test invalid list count
				 * @param s list count
				 */
				@DisplayName("Test invalid list count")
				@ParameterizedTest(name = "count = {0}")
				@ValueSource(strings =  {"0","-1", "2049"})
				void test1(String s) {
					String temp = "FT1.0 1 LIST 1 "+s+" 4 cokeS100 1.2 \n";
					assertThrows(TokenizerException.class, 
							 ()->{MessageFactory.decode(new MessageInput(
					new ByteArrayInputStream(temp.getBytes(UTF8))));});}
				
				/**
				 * Test invalid ItemList Decode 
				 * @throws TokenizerException if validation failure
				 */
				@Test
				@DisplayName("Test invalid ItemList Decode")
				void test2() throws TokenizerException {
					String temp = "FT1.0 1 LIST 1 2 "
			                +"4 cokeQ100 1.2 "
						    +"2 okD1 3.4 \n";
					assertThrows(TokenizerException.class, 
							 ()->{MessageFactory.decode(new MessageInput(
					new ByteArrayInputStream(temp.getBytes(UTF8))));});}
				
				/**
				 * Test valid ItemList decode
				 * @throws TokenizerException if validation
				 * @throws IOException if I/O problem
				 */
				@Test
				@DisplayName("Test valid ItemList decode")
				void test3() throws TokenizerException, IOException {
					String temp = "FT1.0 1 LIST 1 2 "
				                +"4 cokeS100 1.2 "
							    +"2 okD1 3.4 \n";
					ItemList list = (ItemList) MessageFactory.decode(new MessageInput(
							new ByteArrayInputStream(temp.getBytes(UTF8))));
					Item item = new Item("coke", MealType.Snack, 100, 1.2);
					Item item2 = new Item ("ok", MealType.Dinner, 1, 3.4);
					assertEquals(item, list.getItemList().get(0));
					assertEquals(item2, list.getItemList().get(1));
					MessageOutput output = new MessageOutput(new ByteArrayOutputStream());
					MessageFactory.encode(list, output);
					byte[] a = temp.getBytes(UTF8);
					byte[] b = output.toByteArray();
					assertArrayEquals(a, b);
				}
			}
			
			/**
			 * Interval Decode Test
			 */
			@DisplayName("Interval Decode Test")
			@Nested
			class IntervalTest{
				
				/**
				 * Test time stamp invalid 
				 * @param s time stamp value
				 */
				@DisplayName("Test timestamp invalid")
				@ParameterizedTest(name = "TimeStamp = {0}")
				@ValueSource(strings = {"-20221031130056","-1"})
				void test(String s) {
					String temp = "FT1.0 " + s + " INTERVAL 1 \n";
					assertThrows(TokenizerException.class, 
							 ()->{MessageFactory.decode(new MessageInput(
					new ByteArrayInputStream(temp.getBytes(UTF8))));});}
				
				/**
				 * Test time stamp valid
				 * @param s time stamp value
				 * @throws TokenizerException if validation failure
				 */
				@DisplayName("Test timestamp valid")
				@ParameterizedTest(name = "Timestamp = {0}")
				@ValueSource(strings = {"1", "2048888", "9223372036854775807"})
				void test1(String s) throws TokenizerException {
					String temp = "FT1.0 " + s + " INTERVAL 1 \n";
					String s1 = "INTERVAL (TS="+s+") time=1";
					Interval interval = (Interval) MessageFactory.decode(new MessageInput(
							new ByteArrayInputStream(temp.getBytes(UTF8))));
					String s2 = interval.toString();
					assertEquals(s1, s2);
					
				}
				
				/**
				 * Test interval time invalid
				 * @param s interval time
				 */
				@DisplayName("Test interval time invalid")
				@ParameterizedTest(name = "interval time = {0}")
				@ValueSource(strings = {"2049","-1"})
				void test2(String s) {
					String temp = "FT1.0 1 INTERVAL "+s+" \n";
					assertThrows(TokenizerException.class, 
							 ()->{MessageFactory.decode(new MessageInput(
					new ByteArrayInputStream(temp.getBytes(UTF8))));});}
				
				
				/**
				 * Test interval time valid
				 * @param s interval time
				 * @throws TokenizerException if validation failure
				 */
				@DisplayName("Test interval time valid")
				@ParameterizedTest(name = "interval time = {0}")
				@ValueSource(strings = {"1", "2048"})
				void test3(String s) throws TokenizerException {
					String temp = "FT1.0 1 INTERVAL " +s+" \n";
					String s1 = "INTERVAL (TS=1) time="+s;
					Interval interval = (Interval) MessageFactory.decode(new MessageInput(
							new ByteArrayInputStream(temp.getBytes(UTF8))));
					String s2 = interval.toString();
					assertEquals(s1, s2);
				
				}
				
				/**
				 * Check decode not end with ELON
				 */
				@Test
				@DisplayName("Check decode not end with ELON")
				void test4() {
					new MessageFactory();
					Exception e = assertThrows(TokenizerException.class, 
							 ()->{MessageFactory.decode(new MessageInput(
					new ByteArrayInputStream("FT1.0 1 INTERVAL 2 ".getBytes(UTF8))));});
					assertEquals("End not with ELON", e.getMessage());
				}
			}
			
			
			
			/**
			 * Test Message decode with premature problem
			 * @param temp protocol type or message
			 */
			@DisplayName("Test Message decode with premature problem")
			@ParameterizedTest(name = "Message = {0}")
			@ValueSource(strings = {"FT1.", "FT1.0 2 Er", "FT1.0 2 ERROR 3 AB", 
					"FT1.0 2 ADD 2", "FT1.0 2 INTERVAL "})
			void test(String temp) {
				assertThrows(TokenizerException.class, 
						 ()->{MessageFactory.decode(new MessageInput(
							new ByteArrayInputStream(temp.getBytes(UTF8))));});
			}
		
			
			
			/**
			 * Test Message Decode IO problem
			 * @throws TokenizerException if validation failure
			 * @throws IOException IO problem
			 */
			@Test
			@DisplayName("Test Decode with IO problem")
			void test1() throws TokenizerException, IOException {
				MessageInput input = new MessageInput(
						new ByteArrayInputStream("FT1.0 1 GET \n".getBytes(UTF8)));
				MessageFactory.decode(input);
				input.close();
				assertThrows(TokenizerException.class, 
						 ()->{MessageFactory.decode(input);});
			}
			
			/**
			 * Check decode not end with ELON
			 */
			@Test
			@DisplayName("Check decode not end with ELON")
			void test2() {
				new MessageFactory();
				Exception e = assertThrows(TokenizerException.class, 
						 ()->{MessageFactory.decode(new MessageInput(
				new ByteArrayInputStream("FT1.0 1 GET ".getBytes(UTF8))));});
				assertEquals("End not with ELON", e.getMessage());
			}
		}
	}
	
	
	
	
	
	/**
	 * Test Message Factory Encode
	 *
	 */
	@DisplayName("Test Message Factory Encode")
	@Nested
	class TestEncode{
		
		/**
		 * Encode when message is null
		 */
		@Test
		@DisplayName("Encode when message is null")
		void test() {
			Message message = null;
			MessageOutput out = new MessageOutput(new ByteArrayOutputStream());
			Exception e = assertThrows(
					NullPointerException.class, ()->{MessageFactory.encode(message, out);;});
			assertEquals("messgae is null", e.getMessage());
		}
		
		/**
		 * Encode when out is null
		 */
		@Test
		@DisplayName("Encode when out is null")
		void test1() {
			Message message = new Get(1L);
			MessageOutput out = null;
			Exception e = assertThrows(
					NullPointerException.class, ()->{MessageFactory.encode(message, out);;});
			assertEquals("out is null", e.getMessage());
		}
		
		
		/**
		 * Valid Encode with GET
		 * @throws IOException if I/O problem
		 */
		@Test
		@DisplayName("Valid Encode with GET")
		void test2() throws IOException {
			MessageOutput o = new MessageOutput(new ByteArrayOutputStream());
			MessageFactory.encode(new Get(404L), o);
			String e = "FT1.0 404 GET \n";
			assertEquals(e, new String(o.toByteArray(),UTF8));
		}
		
		/**
		 * Valid Encode with ERROR
		 * @throws IOException if I/O problem
		 */
		@Test
		@DisplayName("Valid Encode with ERROR")
		void test3() throws IOException {
			MessageOutput o = new MessageOutput(new ByteArrayOutputStream());
			MessageFactory.encode(new Error(500L, "Bad"), o);
			String e = "FT1.0 500 ERROR 3 Bad\n";
			assertEquals(e, new String(o.toByteArray(),UTF8));
		}
		
		/**
		 * Valid Encode with ADD
		 * @throws IOException if I/O problem
		 */
		@Test
		@DisplayName("Valid Encode with ADD")
		void test4() throws IOException {
			Item item = new Item("coke", MealType.Snack,2048,8.99);
			MessageOutput o = new MessageOutput(new ByteArrayOutputStream());
			MessageFactory.encode(new Add(1L, item), o);
			String e = "FT1.0 1 ADD 4 cokeS2048 9.0 \n";
			assertEquals(e, new String(o.toByteArray(),UTF8));
		}
		
		/**
		 * Valid Encode with LIST
		 * @throws IOException if I/O problem
		 */
		@Test
		@DisplayName("Valid Encode with LIST")
		void test5() throws IOException {
			Item i1 = new Item("coke", MealType.Snack,2048,8.99);
			Item i2 = new Item("Steak", MealType.Dinner, 1286, 1756.42);
			MessageOutput o = new MessageOutput(new ByteArrayOutputStream());
			ItemList list = new ItemList(0L, 100L);
			list.addItem(i1);
			list.addItem(i2);
			MessageFactory.encode(list, o);
			String e = "FT1.0 0 LIST 100 2 "
					 + "4 cokeS2048 9.0 5 SteakD1286 1756.4 \n";
			assertEquals(e, new String(o.toByteArray(),UTF8));
		}
		
		
		/**
		 * Valid Encode with INTERVAL
		 * @throws IOException if I/O Problem
		 */
		@Test
		@DisplayName("Valid Encode with INTERVAL")
		void test6() throws IOException {
			MessageOutput o = new MessageOutput(new ByteArrayOutputStream());
			MessageFactory.encode(new Interval(1, 1), o);
			String e = "FT1.0 1 INTERVAL 1 \n";
			assertEquals(e, new String(o.toByteArray(),UTF8));
		}
	}

}
