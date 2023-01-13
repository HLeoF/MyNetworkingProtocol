/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 4
* Class: CSI 4321
*
************************************************/
package foop.serialization.test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import foop.serialization.Register;

/**
 * Test register method
 * @author Maiqi Hou
 * @version 1.0
 *
 */
class RegisterTest {
	
	/**
	 * Register Constructor Testing
	 */
	@DisplayName("Register Constructor Testing")
	@Nested
	class constructorTest{
		
		/**
		 * Test Constructor with IP address is null
		 */
		@Test
		@DisplayName("Test constructor with null address")
		void test() {
			Exception e = assertThrows(IllegalArgumentException.class, 
						()->{new Register(0, null, 0);});
			assertEquals("IP address is null", e.getMessage());
		}
		

		
		/**
		 * Test constructor with multicast IP address
		 * @param ip register address
		 * @throws UnknownHostException if hsot unknown
		 */
		@DisplayName("Test consturctor with multicast IP address")
		@ParameterizedTest(name = "IP address = {0}")
		@ValueSource(strings = {"224.0.0.0", "230.1.2.3", "239.255.255.255"})
		void test2(String ip) throws UnknownHostException {
			Exception e = assertThrows(IllegalArgumentException.class, 
					()->{new Register(0, (Inet4Address) Inet4Address.getByName(ip), 0);});
			assertEquals("IP address is Multicast", e.getMessage());
		}
		
		/**
		 * Test constructor with invalid IP port
		 * @param port IP port
		 */
		@DisplayName("Test consturctor with invalid IP port")
		@ParameterizedTest(name = "IP port = {0}")
		@ValueSource(ints = {-1, 65536, -65535})
		void test3(int port) {
			Exception e = assertThrows(IllegalArgumentException.class, 
					()->{new Register(0, (Inet4Address) Inet4Address.getByName("191.1.2.3"), port);});
			assertEquals("IP port is out of range", e.getMessage());
		}
		
		/**
		 * Test Constructor with valid IP address
		 * @param ip IP address
		 * @throws UnknownHostException if host unknown
		 */
		@DisplayName("Test consturctor with valid IP address")
		@ParameterizedTest(name = "IP addr: {0}")
		@ValueSource(strings = {"1.0.0.0", "126.10.1.2","192.168.121.1"})
		void test4(String ip) throws UnknownHostException {
			Register register = new Register(0,(Inet4Address) Inet4Address.getByName(ip),0);
			InetSocketAddress scok = register.getSocketAddress();
			String s = "Register: MsgID=" + 0 + " Address=" + ip + " Port="+0;
			String s1 = register.toString();
			assertEquals(s, s1);
			assertEquals((Inet4Address) Inet4Address.getByName(ip), scok.getAddress());
		}
	
	}
	
	/**
	 * Test Register method hash code and equals
	 * @throws UnknownHostException if host is unknown
	 */
	@Test
	@DisplayName("Test Register hash code and equals method")
	void test() throws UnknownHostException {
		Register r1 = new Register(0, 
				(Inet4Address) Inet4Address.getByName("1.0.0.0"), 65535);
		Register r2 = new Register(0, 
				(Inet4Address) Inet4Address.getByName("1.0.0.0"), 65535);
		Register r3 = new Register(1, 
				(Inet4Address) Inet4Address.getByName("192.168.1.1"), 0);
		assertTrue(r1.equals(r2) && r2.equals(r1));
		assertTrue(r1.hashCode() == r2.hashCode());
		assertFalse(r1.equals(r3) && r3.equals(r1));
		assertFalse(r1.equals(r3) != r3.equals(r1));
		assertFalse(r1.equals(null));
		assertTrue(r1.equals(r1));
		assertFalse(r1.equals(" "));
	}
	

}
