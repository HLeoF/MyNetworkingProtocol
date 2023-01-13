/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 4
* Class: CSI 4321
*
************************************************/
package foop.serialization;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Pattern;

import fiat.serialization.MealType;

/**
 * Represents item addition
 * @author Maiqi Hou
 * @version 1.1
 * Update encodeMsg, end of packet should add two empty byte
 *
 */
public class Addition extends Message{
	// Standard Charsets UTF 8 for message input, message out, decode, and encode
	private static final Charset UTF8   = StandardCharsets.UTF_8;
	private static final int MINUNSINT  = 0;	 //minimum unsigned integer value
	private static final int MAXUNSINT  = 2048;  //maximum unsigned integer value
	private static final String UNICODE = "\\p{N}|\\p{L}|\\p{P}|\\p{S}"; //for check valid unicode category
	private static final String SP = " ";        //String space
	private static final int MAXSTRING  = 255;   //Max length of name
	
	
	private String name;			//addition item name
	private MealType mealType;		//addition item meal type
	private int calories;			//addition item calories
	
	/**
	 * Constructs from given values
	 * @param msgID message ID
	 * @param name name of item
	 * @param mealType type of meal
	 * @param Calories number of calories in item
	 * @throws IllegalArgumentException if validation failure
	 */
	public Addition(int msgID, String name, MealType mealType, int Calories) {
		super.setMsgID(msgID);
		setName(name);
		setMealType(mealType);
		setCalories(Calories);
	}
	
	/**
	 * Get the name
	 * @return
	 * addition item name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Set Name
	 * @param name new name
	 * @return
	 * name with new value
	 * @throws IllegalArgumentException if name is invalid
	 */
	public Addition setName(String name) {
		//check name is null or not
		if(checkNull(name) == true) {
			throw new IllegalArgumentException("name is null");
		}
		//check name length and name character
		if(CheckUnINT(name) == false || checkCharList(name) == false){
			throw new IllegalArgumentException("name is invalid");
		}
		this.name = name;
		return this;
	}
	
	
	/**
	 * Get the meal type
	 * @return
	 * addition item meal type
	 */
	public MealType getMealType() {
		return this.mealType;
	}
	
	/**
	 * Sets meal type
	 * @param mealType new meal type
	 * @return
	 * meal type with new value
	 * @throws IllegalArgumentException if meal type is null, meal type is invalid
	 */
	public Addition setMealType(MealType mealType) {
		//check meal type whether is null
		if(mealType == null) {
			throw new IllegalArgumentException("MealType is null");
		}
		this.mealType = mealType;
		return this;
	}
	
	/**
	 * Get the calories
	 * @return
	 * addition item calories
	 */
	public int getCalories() {
		return this.calories;
	}
	
	/**
	 * Sets Calories
	 * @param calories new addition item calories
	 * @return
	 * addition item calories
	 * @throws IllegalArgumentException if calories out of range
	 */
	public Addition setCalories(int calories) {
		//check calories valid 
		if(calories < MINUNSINT || calories > MAXUNSINT) {
			throw new IllegalArgumentException("invalid calories");
		}
		this.calories = calories;
		return this;
	}
	
	/**
	 * Encode Message for Addition Type
	 * @throws IOException IO problem if write bytes have problem
	 */
	@Override
	public byte[] encodeMsg() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int verison = 3; //the protocol version is 3
		int code = 1;    //get the Addition code is 1
		//combine version and code into one byte
		byte t = (byte) (verison << 4 | code);
		out.write(t);
		t = (byte) getMsgID();//get the message ID byte
		out.write(t);
		t = (byte) (getName().length()); //get the name length byte
		out.write(t);
		out.write(getName().getBytes(UTF8));//the name bytes
		char type = getMealType().getMealTypeCode();
		out.write((char)type);//get the meal type byte
		out.write((byte)MINUNSINT);//get the empty byte(0)
		byte[] cal = convertIntto16bit(getCalories());//get 16 bit calories
		out.write(cal);
		byte[] arr = out.toByteArray();//combine all bytes into a byte array
		return arr;
	}
	
	/**
	 * Convert Integer value to little endian 16bit 
	 * @param val register port
	 * @return
	 * little endian 16 bit endian
	 */
	public static byte[] convertIntto16bit(int val) {
		return new byte[] {(byte) (val & 0x000000ff), 
				(byte) (val >>> 8 & 0x00000ff)};
	}
	
	/**
	 * Return String of the addition information
	 */
	@Override
	public String toString() {
		return "Addition: MsgID="+getMsgID()
				+" Name=" + getName()
				+" Calories=" + getCalories()
				+" Meal=" + getMealType();
	}
	
	
	/**
	 * Addition hash code method
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(calories, mealType, name);
		return result;
	}
	
	/**
	 * Addition equals method
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass()) return false;
		Addition other = (Addition) obj;
		return calories == other.calories && mealType == other.mealType 
				&& Objects.equals(name, other.name);
	}
	
	
	/**
	 * Check string is null or not
	 * @param name  for name 
	 * @return if name is not null, return true;
	 * 		   if name is null, return false;
	 */
	public static boolean checkNull(String name) {
		return (name == null);
	}
	
	/**
	 * Check character count valid
	 * @param name  for name
	 * @return if character count is valid, return true
	 * 		   if character count is invalid, return false
	 */
	public boolean CheckUnINT(String name) {
		int num = name.length(); //get name length
		//name is empty or name out of range 255
		return (num > MINUNSINT && num <= MAXSTRING);
	}
	
	/**
	 * Check character list is valid
	 * @param name  for name
	 * @return if character list is valid, return true
	 * 		   if character list is not valid, return false
	 */
	public boolean checkCharList(String name) {
		//check characters are valid
		for(int i = 0; i < name.length(); i++) {
			String temp = Character.toString(name.charAt(i));
			if(Pattern.matches(UNICODE, temp) == false && !temp.equals(SP)) {
				return false;
			}
		}
		return true;
	}
}
