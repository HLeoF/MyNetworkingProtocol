/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 4
* Class: CSI 4321
*
************************************************/
package foop.serialization.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
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

import fiat.serialization.MealType;
import foop.serialization.ACK;
import foop.serialization.Addition;
import foop.serialization.Error;
import foop.serialization.MessageFactory;
import foop.serialization.Register;

/**
 * Test Message Factory(Decode and encode)
 * @author Maiqi Hou
 * @version 1.0
 *
 */
class MessageFactoryTest {
	// Standard Charsets UTF 8 for message input, message out, decode, and encode
	private static final Charset UTF8  = StandardCharsets.UTF_8;
		
	/**
	 * Test Message Decode
	 */
	@DisplayName("Test Message decode")
	@Nested
	class DecodeTest{
		/**
		 * Test Packet is null
		 */
		@Test
		@DisplayName("Test packet is null")
		void t() {
			new MessageFactory();
			Exception e = assertThrows(NullPointerException.class, 
					()->{MessageFactory.decode(null);});
			assertEquals("message packet is null", e.getMessage());
		}
		
		/**
		 * Test Receive packet is to small
		 */
		@Test
		@DisplayName("Test packet is to large when decoding")
		void test1() {
			byte [] pkt = new byte[1];
			Exception e = assertThrows(IllegalArgumentException.class, 
					()->{MessageFactory.decode(pkt);});
			assertEquals("Packet Size small", e.getMessage());
		}
		
		/**
		 * Test packet version is invalid
		 * @param verison message packet version
		 */
		@DisplayName("Test packet version is invalid")
		@ParameterizedTest(name = "Version: {0}")
		@ValueSource(ints = {1, 2, 15, 8})
		void test2(int verison) {
			byte t = (byte) (verison << 4 | 3);
			byte[] arr = {t, (byte)255};
			Exception e = assertThrows(IllegalArgumentException.class, 
					()->{MessageFactory.decode(arr);});
			assertEquals("Invalid Foop version", e.getMessage());
		}
		
		/**
		 * Test packet code is invalid
		 * @param code message code
		 */
		@DisplayName("Test packet code is invalid")
		@ParameterizedTest(name = "Message code: {0}")
		@ValueSource(ints = {4, 5, 10, 15, 8})
		void test3(int code) {
			byte t = (byte) (3 << 4 | code);
			byte[] arr = {t, (byte)255};
			assertThrows(IllegalArgumentException.class, 
					()->{MessageFactory.decode(arr);});
		}
		
		/**
		 * Decode with ACK
		 */
		@DisplayName("Decode with ACK")
		@Nested
		class DecodeACK{
			
			/**
			 * Test ACK decode with valid Message ID
			 * @param id message ID
			 */
			@DisplayName("Test ACK decode with valid Message ID")
			@ParameterizedTest(name = "Message ID: {0}")
			@ValueSource(ints = {1, 2, 255, 10})
			void test1(int id) {
				byte t = (byte) (3 << 4 | 3);
				byte[] arr = {t, (byte)id};
				ACK ack = (ACK) MessageFactory.decode(arr);
				String s = "ACK: MsgID=" + id;
				String s1 = ack.toString();
				assertEquals(id, ack.getMsgID());
				assertEquals(s, s1);
			}
			
			/**
			 * Test ACK decode with packet is out of range
			 */
			@Test
			@DisplayName("Test ACK decode with packet is out of range")
			void test2() {
				byte t = (byte) (3 << 4 | 3);
				byte[] arr = {t, (byte)1, (byte)2};
				Exception e = assertThrows(IllegalArgumentException.class, 
						()->{MessageFactory.decode(arr);});
				assertEquals("ACK packet out of range", e.getMessage());
			}
			
		}
		
		/**
		 * Decode with Error Message
		 */
		@DisplayName("Decode with Error")
		@Nested
		class DecodeError{
			/**
			 * Test ACK decode with valid Message ID
			 * @param id ACK messaeg ID
			 * @throws IOException IO problem if write bytes have problem
			 */
			@DisplayName("Test Error decode with valid Message ID")
			@ParameterizedTest(name = "Message ID: {0}")
			@ValueSource(ints = {1, 2, 255, 10})
			void test(int id) throws IOException {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte t = (byte) (3 << 4 | 2);
				out.write(t);
				t = (byte)id;
				out.write(t);
				out.write("Error".getBytes(UTF8));
				byte [] arr = out.toByteArray();
				Error error = (Error) MessageFactory.decode(arr);
				assertEquals(id, error.getMsgID());
			}
					
			/**
			 * Test Error Decode with invalid Error Message
			 * @param msg Error Message
			 * @throws IOException IO problem if write bytes have problem 
			 */
			@DisplayName("Test ERROR Decode with invalid Error Message")
			@ParameterizedTest(name = "Error Message: {0}")
			@ValueSource(strings = { "\r0","W\nning", "No\r\npe", "Err\n"})
			void test1(String msg) throws IOException {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte t = (byte) (3 << 4 | 2);
				out.write(t);
				t = (byte) 255;
				out.write(t);
				out.write(msg.getBytes(UTF8));
				byte [] arr = out.toByteArray();
				Exception e = assertThrows(IllegalArgumentException.class, 
						()->{MessageFactory.decode(arr);});
				assertEquals("Foop Message is invalid", e.getMessage());
			}
			
			/**
			 * Test Error Decode with Valid Error Message
			 * @param msg error Message
			 * @throws IOException IO problem if write bytes have problem
			 */
			@DisplayName("Test ERROR Decode with valid Error Message")
			@ParameterizedTest(name = "Error Message: {0}")
			@ValueSource(strings = { "0!","CAUTION!!", "NOT FOUND"})
			void test2(String msg) throws IOException {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte t = (byte) (3 << 4 | 2);
				out.write(t);
				t = (byte) 255;
				out.write(t);
				out.write(msg.getBytes(UTF8));
				byte [] arr = out.toByteArray();
				Error error = (Error) MessageFactory.decode(arr);
				String s = "Error: MsgID=255"+" Message="+msg;
				String s1 = error.toString();
				assertEquals(s, s1);
			}
			
		}
		
		/**
		 * Decode with Register
		 */
		@DisplayName("Decode with Register")
		@Nested
		class decodeRegister{
			/**
			 * Test Register decode with valid message ID
			 * @param id message ID
			 * @throws IOException IO problem if write bytes have problem
			 */
			@DisplayName("Test register decode with valid Message ID")
			@ParameterizedTest(name = "Message ID = {0}")
			@ValueSource(ints = {10, 255, 1, 254})
			void test(int id) throws IOException {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte t = (byte) (3 << 4 | 0);
				out.write(t);
				t = (byte) id;
				out.write(t);
				byte[] a = {(byte)2,(byte)1, (byte)10, (byte)192};
				out.write(a);
				a = new byte[]{(byte)0, (byte)0};
				out.write(a);
				byte[] arr = out.toByteArray();
				Register register = (Register) MessageFactory.decode(arr);
				assertEquals(id, register.getMsgID());
			}
			
			
			/**
			 * Test Register decode with with valid port
			 * @param port Register port
			 * @throws IOException IO problem if write bytes have problems
			 */
			@DisplayName("Test Register decode with valid Port")
			@ParameterizedTest(name = "Register Port: {0}")
			@ValueSource(ints = {65535, 1000, 8080, 80, 5000})
			void test1(int port) throws IOException {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte t = (byte) (3 << 4 | 0);
				out.write(t);
				t = (byte) 100;
				out.write(t);
				byte[] a = {(byte)2,(byte)1, (byte)10, (byte)192};
				out.write(a);
				a = new byte[] {(byte) (byte) (port & 0x000000ff),
						(byte) (port >>> 8 & 0x00000ff)};
				out.write(a);
				byte[] arr = out.toByteArray();
				Register register = (Register) MessageFactory.decode(arr);
				assertEquals(port, register.getPort());
			}
			
			/**
			 * Test Register decode with valid IP address
			 * @param a1 IPv4 address first byte
			 * @param a2 IPv4 address second byte
			 * @param a3 IPv4 address third byte
			 * @param a4 IPv4 address fourth byte
			 * @throws IOException IO problem if write bytes have problem
			 */
			@DisplayName("Test Register decode with valid IP address")
			@ParameterizedTest(name = "Register IP address: {0}.{1}.{2}.{3}")
			@MethodSource("IPtest1")
			void test2(int a1, int a2, int a3, int a4) throws IOException {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte t = (byte) (3 << 4 | 0);
				out.write(t);
				t = (byte) 100;
				out.write(t);
				byte[] a = {(byte)a4,(byte)a3, (byte)a2, (byte)a1};
				byte[] aa = {(byte)a1,(byte)a2, (byte)a3, (byte)a4};
				out.write(a);
				Inet4Address address = (Inet4Address) Inet4Address.getByAddress(aa);
				a = new byte[]{(byte)0, (byte)0};
				out.write(a);
				byte[] arr = out.toByteArray();
				Register register = (Register) MessageFactory.decode(arr);
				assertEquals(address, register.getAddress());
			}
			/**
			 * Static stream for Register IP address valid value
			 * @return
			 * IP addresses stream as parameters
			 */
			static Stream<Arguments>IPtest1(){
				return Stream.of(
						Arguments.of(10,0,0,1),
						Arguments.of(240,0,0,0),
						Arguments.of(192,16,2,3)
				);
			}
			
			/**
			 * Test Register decode with invalid IP address
			 * @param a1 IPv4 address first byte
			 * @param a2 IPv4 address second byte
			 * @param a3 IPv4 address third byte
			 * @param a4 IPv4 address fourth byte
			 * @throws IOException IO problem if write bytes have problem
			 */
			@DisplayName("Test Register decode with invalid IP address")
			@ParameterizedTest(name = "Register IP address: {0}.{1}.{2}.{3}")
			@MethodSource("IPtest2")
			void test3(int a1, int a2, int a3, int a4) throws IOException {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte t = (byte) (3 << 4 | 0);
				out.write(t);
				t = (byte) 100;
				out.write(t);
				byte[] a = {(byte)a4,(byte)a3, (byte)a2, (byte)a1};
				out.write(a);
				a = new byte[]{(byte)0, (byte)0};
				out.write(a);
				byte[] arr = out.toByteArray();
				assertThrows(IllegalArgumentException.class, 
						()->{MessageFactory.decode(arr);});
			}
			/**
			 * Static stream for Register IP address valid value
			 * @return
			 * IP addresses stream as parameters
			 */
			static Stream<Arguments>IPtest2(){
				return Stream.of(
						Arguments.of(224,0,0,1),
						Arguments.of(239,0,0,0),
						Arguments.of(239,16,2,3)
				);
			}
			
			
			/**
			 * Test register decode with incomplete packet
			 * @throws IOException IO problem if write bytes have problem
			 */
			@Test
			@DisplayName("Test Register decode with incomplete packet")
			void test4() throws IOException {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte t = (byte) (3 << 4 | 0);
				out.write(t);
				t = (byte) 100;
				out.write(t);
				byte[] a = {(byte)1,(byte)2};
				out.write(a);
				a = new byte[]{(byte)0, (byte)0};
				out.write(a);
				byte[] arr = out.toByteArray();
				Exception e = assertThrows(IllegalArgumentException.class, 
						()->{MessageFactory.decode(arr);});
				assertEquals("Register packet is small", e.getMessage());
			}

			/**
			 * Test register decode with packet is out of range
			 * @throws IOException IO problem if write bytes have problem
			 */
			@Test
			@DisplayName("Test Register decode with packet is out of range")
			void test5() throws IOException {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte t = (byte) (3 << 4 | 0);
				out.write(t);
				t = (byte) 100;
				out.write(t);
				byte[] a = {(byte)1,(byte)2, (byte)10, (byte)192};
				out.write(a);
				a = new byte[]{(byte)0, (byte)0, (byte)0};
				out.write(a);
				byte[] arr = out.toByteArray();
				Exception e = assertThrows(IllegalArgumentException.class, 
						()->{MessageFactory.decode(arr);});
				assertEquals("Register packet out of range", e.getMessage());
			}
		}
		
		/**
		 * Decode with Addition
		 */
		@DisplayName("Decode with Addition")
		@Nested
		class AdditionDecode{
			/**
			 * Test Addition decode with valid Message ID
			 * @param id Message ID for Addition 
			 * @throws IOException IO problem if write bytes have problem
			 */
			@DisplayName("Test Addition decode with valid Message ID")
			@ParameterizedTest(name = "Message ID = {0}")
			@ValueSource(ints = {10, 255, 1, 254})
			void test(int id) throws IOException {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte t = (byte) (3 << 4 | 1);
				out.write(t);
				byte a[] = {(byte)id, (byte)5};
				out.write(a);
				out.write("Error".getBytes(UTF8));
				out.write((byte)'B');
				out.write((byte)0);
				a = new byte[] {(byte) (10 & 0x000000ff), 
						(byte) (10 >>> 8 & 0x00000ff)};
				out.write(a);

				byte[] arr = out.toByteArray();
				Addition addition = (Addition) MessageFactory.decode(arr);
				String s = "Addition: MsgID="+id+" Name=Error Calories=10 Meal=Breakfast";
				String s1 = addition.toString();
				assertEquals(s, s1);
			}
			
			/**
			 * Test Addition decode with invalid name
			 * @param name  addition item name
			 * @throws IOException IO problem if write bytes have problem
			 */
			@DisplayName("Test Addition decode with invalid Name")
			@ParameterizedTest(name = "Addition item name = {0}")
			@ValueSource(strings = {"B\nd","Bn\n"})
			void test1(String name) throws IOException {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte t = (byte) (3 << 4 | 1);
				out.write(t);
				byte a[] = {(byte)60, (byte)3};
				out.write(a);
				out.write(name.getBytes(UTF8));
				out.write((byte)'B');
				out.write((byte)0);
				a = new byte[] {(byte) (10 >>> 8 & 0x00000ff), 
						(byte) (10 & 0x000000ff)};
				out.write(a);
				byte[] arr = out.toByteArray();
				Exception e = assertThrows(IllegalArgumentException.class, 
						()->{MessageFactory.decode(arr);});
				assertEquals("name is invalid", e.getMessage());
			}
			
			/**
			 * Test Addition decode with valid Name
			 * @param name Addition item name
			 * @throws IOException IO problem if write bytes have problem
			 */
			@DisplayName("Test Addition decoede with valid Name")
			@ParameterizedTest(name = "Addition item name = {0}")
			@ValueSource(strings = {"1kk","Ahh", "abc","!bd"," ar"})
			void test2(String name) throws IOException {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte t = (byte) (3 << 4 | 1);
				out.write(t);
				byte a[] = {(byte)60, (byte)3};
				out.write(a);
				out.write(name.getBytes(UTF8));
				out.write((byte)'B');
				out.write((byte)0);
				a = new byte[] {(byte) (10 & 0x000000ff),
						(byte) (10 >>> 8 & 0x00000ff)};
				out.write(a);
				byte[] arr = out.toByteArray();
				Addition addition = (Addition) MessageFactory.decode(arr);
				String s = "Addition: MsgID=60 Name="+name+" Calories=10 Meal=Breakfast";
				String s1 = addition.toString();
				assertEquals(s, s1);
			}
			
			/**
			 * Test Addition decode with invalid meal type
			 * @param ch addition item meal type
			 * @throws IOException IO problem if write bytes have problem
			 */
			@DisplayName("Test Addition decode with invalid Meal type")
			@ParameterizedTest(name = "Addition item meal type: {0}")
			@ValueSource(chars = {'Q','A','C','P'})
			void test3(char ch) throws IOException {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte t = (byte) (3 << 4 | 1);
				out.write(t);
				byte a[] = {(byte)60, (byte)3};
				out.write(a);
				out.write("Okk".getBytes(UTF8));
				out.write((byte)ch);
				out.write((byte)0);
				a = new byte[] {(byte) (10 & 0x000000ff),
						(byte) (10 >>> 8 & 0x00000ff)};
				out.write(a);
				byte[] arr = out.toByteArray();
				Exception e = assertThrows(IllegalArgumentException.class, 
						()->{MessageFactory.decode(arr);});
				assertEquals("Bad code value", e.getMessage());
			}
			
			/**
			 * Test Addition decode with valid meal type
			 * @param ch addition item meal type
			 * @throws IOException IO problem if write bytes have problem
			 */
			@DisplayName("Test Addition decode with valid meal type")
			@ParameterizedTest(name = "Addition item meal type: {0}")
			@ValueSource(chars = {'B','L','D','S'})
			void test4(char ch) throws IOException {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte t = (byte) (3 << 4 | 1);
				out.write(t);
				byte a[] = {(byte)60, (byte)3};
				out.write(a);
				out.write("Okk".getBytes(UTF8));
				out.write((byte)ch);
				out.write((byte)0);
				a = new byte[] {(byte) (10 & 0x000000ff),
						(byte) (10 >>> 8 & 0x00000ff)};
				out.write(a);
				byte[] arr = out.toByteArray();
				Addition addition = (Addition) MessageFactory.decode(arr);
				String s = "Addition: MsgID=60 Name=Okk Calories=10 Meal=" 
				+ MealType.getMealType(ch);
				String s1 = addition.toString();
				assertEquals(s, s1);
			}
			
			/**
			 * Test Addition decode with invalid item calories
			 * @param cal addition item calories
			 * @throws IOException IO problem if write bytes have problem
			 */
			@DisplayName("Test Addition decode with invalid item calories")
			@ParameterizedTest(name = "Addition item calories: {0}")
			@ValueSource(ints = {2049, -1, 5000})
			void test5(int cal) throws IOException {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte t = (byte) (3 << 4 | 1);
				out.write(t);
				byte a[] = {(byte)60, (byte)3};
				out.write(a);
				out.write("Okk".getBytes(UTF8));
				out.write((byte)'B');
				out.write((byte)0);
				a = new byte[] {(byte) (cal & 0x000000ff),
						(byte) (cal >>> 8 & 0x00000ff)};
				out.write(a);
				byte[] arr = out.toByteArray();
				Exception e = assertThrows(IllegalArgumentException.class, 
						()->{MessageFactory.decode(arr);});
				assertEquals("invalid calories", e.getMessage());
			}
			
			/**
			 * Test Addition decode with valid item calories
			 * @param cal Addition item calories
			 * @throws IOException IO problem if write bytes have problem
			 */
			@DisplayName("Test Addition decode with valid item calories")
			@ParameterizedTest(name = "Addition item calories: {0}")
			@ValueSource(ints = {0, 100, 1, 2048})
			void test6(int cal) throws IOException {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte t = (byte) (3 << 4 | 1);
				out.write(t);
				byte a[] = {(byte)60, (byte)3};
				out.write(a);
				out.write("Okk".getBytes(UTF8));
				out.write((byte)'B');
				out.write((byte)0);
				a = new byte[] {(byte) (cal & 0x000000ff),
						(byte) (cal >>> 8 & 0x00000ff)};
				out.write(a);
				byte[] arr = out.toByteArray();
				Addition addition = (Addition) MessageFactory.decode(arr);
				String s = "Addition: MsgID=60 Name=Okk Calories="+cal+" Meal=Breakfast";
				String s1 = addition.toString();
				assertEquals(s, s1);
			}
			
			/**
			 * Test Addition decode with samll packet
			 */
			@DisplayName("Test Addition decode with samll packet")
			@Nested
			class testPacket{
				/**
				 * Test Addition decode with small packet 1
				 * @throws IOException IO problem if write bytes have problem
				 */
				@Test
				@DisplayName("The small packet 1")
				void test7() throws IOException {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					byte t = (byte) (3 << 4 | 1);
					out.write(t);
					byte a[] = {(byte)60, (byte)3};
					out.write(a);
					byte[] arr = out.toByteArray();
					Exception e = assertThrows(IllegalArgumentException.class, 
							()->{MessageFactory.decode(arr);});
					assertEquals("Addition Packet is small", e.getMessage());
				}
				
				/**
				 * Test Addition decode with small packet 2
				 * @throws IOException IO problem if write bytes have problem
				 */
				@Test
				@DisplayName("The small packet 2")
				void test8() throws IOException {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					byte t = (byte) (3 << 4 | 1);
					out.write(t);
					byte a[] = {(byte)60, (byte)3};
					out.write(a);
					out.write("Ok".getBytes(UTF8));
					out.write((byte)'B');
					out.write((byte)0);
					a = new byte[] {(byte) (10 >>> 8 & 0x00000ff), 
							(byte) (10 & 0x000000ff)};
					out.write(a);
					byte[] arr = out.toByteArray();
					Exception e = assertThrows(IllegalArgumentException.class, 
							()->{MessageFactory.decode(arr);});
					assertEquals("Addition Packet is small", e.getMessage());
				}
				
				/**
				 * Test Addition decode with small packet 3
				 * @throws IOException IO problem if write bytes have problem
				 */
				@Test
				@DisplayName("The small packet 3")
				void test9() throws IOException {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					byte t = (byte) (3 << 4 | 1);
					out.write(t);
					byte a[] = {(byte)60, (byte)3};
					out.write(a);
					out.write("Okk".getBytes(UTF8));
					out.write((byte)'B');
					out.write((byte)0);
					byte[] arr = out.toByteArray();
					Exception e = assertThrows(IllegalArgumentException.class, 
							()->{MessageFactory.decode(arr);});
					assertEquals("Addition Packet is small", e.getMessage());
				}
				
				/**
				 * Test Addition decode with small packet 4
				 * @throws IOException IO problem if write bytes have problem
				 */
				@Test
				@DisplayName("The small packet 4")
				void test10() throws IOException {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					byte t = (byte) (3 << 4 | 1);
					out.write(t);
					byte a[] = {(byte)60};
					out.write(a);
					byte[] arr = out.toByteArray();
					Exception e = assertThrows(IllegalArgumentException.class, 
							()->{MessageFactory.decode(arr);});
					assertEquals("Addition Packet is small", e.getMessage());
				}
			}
			/**
			 * Test the Addition decode with out of range
			 * @throws IOException IO problem is write bytes have issues
			 */
			@Test
			@DisplayName("Test the Addition decode with out of range")
			void test() throws IOException {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte t = (byte) (3 << 4 | 1);
				out.write(t);
				byte a[] = {(byte)60, (byte)3};
				out.write(a);
				out.write("Okk".getBytes(UTF8));
				out.write((byte)'B');
				out.write((byte)0);
				a = new byte[] {(byte) (10 >>> 8 & 0x00000ff), 
						(byte) (10 & 0x000000ff)};
				out.write(a);
				out.write((byte)0);
				out.write((byte)0);
				out.write("Okk".getBytes(UTF8));
				byte[] arr = out.toByteArray();
				Exception e = assertThrows(IllegalArgumentException.class, 
						()->{MessageFactory.decode(arr);});
				assertEquals("Addition Packet out of range", e.getMessage());
			}
			
			/**
			 * Test the error byte between meal type and calories
			 * @throws IOException IO problem if write bytes have problem
			 */
			@Test
			@DisplayName("Test the error byte between meal type and calories")
			void test1() throws IOException {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte t = (byte) (3 << 4 | 1);
				out.write(t);
				byte a[] = {(byte)60, (byte)3};
				out.write(a);
				out.write("Okk".getBytes(UTF8));
				out.write((byte)'B');
				out.write((byte)8);
				a = new byte[] {(byte) (10 >>> 8 & 0x00000ff), 
						(byte) (10 & 0x000000ff)};
				out.write(a);
				byte[] arr = out.toByteArray();
				Exception e = assertThrows(IllegalArgumentException.class, 
						()->{MessageFactory.decode(arr);});
				assertEquals("Error byte between meal type and calories", e.getMessage());
			}
		}
	}
	
	
	/**
	 * Test Message encode
	 */
	@DisplayName("Test Message encode")
	@Nested
	class EncodeTest{
		
		/**
		 * Test Message is null
		 */
		@Test
		@DisplayName("Test Message is null")
		void test() {
			Exception e = assertThrows(NullPointerException.class,
					()->{MessageFactory.encode(null);});
			assertEquals("message is null", e.getMessage());
		}
		
		/**
		 * Test ACK Message is valid
		 */
		@Test
		@DisplayName("Test valid ACK encode")
		void test1() {
			byte vc  = (byte) (3 << 4 | 3);
			byte [] arr = {vc, (byte)255};
			ACK ack = new ACK(255);
			byte[] b = MessageFactory.encode(ack);
			ACK ack2 = (ACK) MessageFactory.decode(b);
			String s1 = ack.toString();
			String s2 = ack2.toString();
			assertArrayEquals(arr, b);
			assertEquals(s1, s2);
		}
		
		/**
		 * Test Error Message is valid
		 * @throws IOException IO problem if write bytes have problem
		 */
		@Test
		@DisplayName("Test valid Error encode")
		void test2() throws IOException {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte vc  = (byte) (3 << 4 | 2);
			out.write(vc);
			out.write((byte)0);
			out.write("ERROR!".getBytes(UTF8));
			byte[] arr = out.toByteArray();
			Error error = new Error(0, "ERROR!");
			byte[] b  = MessageFactory.encode(error);
			Error error2 = (Error) MessageFactory.decode(b);
			String s1 = error.toString();
			String s2 = error2.toString();
			assertArrayEquals(arr, b);
			assertEquals(s1, s2);
		}
		
		/**
		 * Test Valid Register encode
		 * @throws UnknownHostException if host is unknown
		 */
		@Test
		@DisplayName("Test Valid Register encode")
		void test3() throws UnknownHostException {
			byte vc  = (byte) (3 << 4 | 0);
			byte[] arr = {vc, (byte)255, (byte)2, (byte)1, 
					(byte)168, (byte)198, (byte)150, (byte)0};
			Inet4Address address = (Inet4Address) Inet4Address.getByName("198.168.1.2");
			Register register = new Register(255, address, 150);
			byte [] b = MessageFactory.encode(register);
			Register register2 = (Register) MessageFactory.decode(b);
			String s1 = register.toString();
			String s2 = register2.toString();
			assertArrayEquals(arr, b);
			assertEquals(s1, s2);
		}
		
		/**
		 * Test Valid Addition encode
		 */
		@Test
		@DisplayName("Test Valid Addition encode")
		void test4() {
			byte vc = (byte)(3<<4 | 1);
			byte[] arr = {vc, (byte)255, (byte)1, (byte)'A', 
					(byte)'B', (byte)0, (byte)150, (byte)0};
			Addition addition = new Addition(255, "A", MealType.Breakfast, 150);
			byte[] b = MessageFactory.encode(addition);
			Addition addition2 = (Addition) MessageFactory.decode(b);
			String s1 = addition.toString();
			String s2 = addition2.toString();
			assertArrayEquals(arr, b);
			assertEquals(s1, s2);
		}
	}
}
