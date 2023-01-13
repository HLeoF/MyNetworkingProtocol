/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 0
* Class: CSI 4321
*
************************************************/
package fiat.serialization.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import fiat.serialization.TokenizerException;

/**
 * Test TokenizerException methods 
 * @author Maiqi Hou
 * @version 1.1
 * Update two tests for test offset value
 */
class TokenizerExceptionTest {
	
	/**
	 * Testing toknizer exception with the throwable cause 1
	 */
	@Test()
	@DisplayName("Test TokenizerException with Cause")
	void test() {
		byte type = (byte) 'B';
		byte temp = (byte) 'A';
		TokenizerException e = null;
		if(type != temp) {
			e = new TokenizerException(0, "Meal type is wrong", e);
		}
		Assertions.assertEquals(0, e.getOffset());
		Assertions.assertEquals("Meal type is wrong", e.getMessage());
		Assertions.assertEquals(null, e.getCause());
	}
	
	/**
	 * Testing toknizer exception with throwable cause 2
	 */
	@Test()
	@DisplayName("Test TokenizerException with Cause 2")
	void test1() {
		TokenizerException e = null;
		byte [] a = "A FriesB512 5.6  ".getBytes(StandardCharsets.UTF_8);
		
		if(a[0] < 20 || a[0] > 39) {
			e = new TokenizerException(0, "Meal type is wrong", e);
		}
	
		Assertions.assertEquals(0, e.getOffset());
		Assertions.assertEquals("Meal type is wrong", e.getMessage());
		Assertions.assertEquals(null, e.getCause());
	}

	/**
	 * Testing tokenizer exception without throwable cause 1
	 */
	@Test()
	@DisplayName("Test Tokenizer Exception without cause")
	void test2() {
		byte type = (byte) 'L';
		byte temp = (byte) 'A';
		TokenizerException e = null;
		if(type != temp) {
			e = new TokenizerException(0, "Meal type is wrong");
		}
		Assertions.assertEquals(0, e.getOffset());
		Assertions.assertEquals("Meal type is wrong", e.getMessage());
	}
	
	/**
	 * Testing tokenizer exception without throwable cause 2
	 */
	@Test()
	@DisplayName("Test TokenizerException without Cause 2")
	void test3() {
		TokenizerException e = null;
		byte [] a = "A FriesB512 5.6  ".getBytes(StandardCharsets.UTF_8);
		
		if(a[0] < 20 || a[0] > 39) {
			e = new TokenizerException(0, "Meal type is wrong");
		}
	
		Assertions.assertEquals(0, e.getOffset());
		Assertions.assertEquals("Meal type is wrong", e.getMessage());
	}
	
	/**
	 * Check offset value Tokenizer Exception with cause
	 */
	@Test()
	@DisplayName("Check offset value Tokenizer Exception with cause")
	void test4() {
		
		Exception e = Assertions.assertThrows(
		 IllegalArgumentException.class, ()->{new TokenizerException(-1, "check",new Exception());});
		assertEquals("Offset Invalid", e.getMessage());
	}

	/**
	 * Check offset value Tokenizer Exception without cause
	 */
	@Test()
	@DisplayName("Check offset value Tokenizer Exception without cause")
	void test5() {
		Exception e = Assertions.assertThrows(
		 IllegalArgumentException.class, ()->{new TokenizerException(-2147483648, "check");});
		assertEquals("Offset Invalid", e.getMessage());
	}
	
}
