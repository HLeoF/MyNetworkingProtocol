/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 0
* Class: CSI 4321
*
************************************************/
package fiat.serialization;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Objects;


/**
 * Factory to create de-serialized and serialized items
 * @author Maiqi Hou
 * @version 1.6
 * Update the encode (include premature issue, I/O problem, EoS problem)
 * Update the decode 
 */
public class ItemFactory {
	private static final Charset UTF8 = StandardCharsets.UTF_8; //charsets UTF-8
	private static final String  dPATTERN = "#0.0";	//regex for fat keep one decimal
	private static final int 	 SP   = 32;	 //integer value of char space
	private static final int     ONE  = 1;   //constant integer value 1 mark error position
	private static final int     ZERO = 0;   //constant integer value 0 initial value 
	private static final int     EOS  = -1;	 //integer indicate the end of stream	
	private static final int     MAXINT = 2048;
	
	/**
	 * error Position 
	 */
	public static int pos = 0;
	
	/**
	 * Constructs item using deserialization
	 * @param in deserialization input source
	 * @return new (deserializaed) item
	 * @throws TokenizerException if in is null
	 */
	public static Item decode(MessageInput in) throws TokenizerException {
		Objects.requireNonNull(in, "in is null"); //check MessageInput is not null
		Item item = new Item();
		try {
			int charC = getcount(in); //get character count
			name(in, item, charC); //get item name
			mealtype(in, item); //get meal type;
			calories(in, item); //get item calories
			fat(in, item);      //get item fat	
			return item;
		}catch (IOException e) {
			throw new TokenizerException(pos, "as error IO problem");
		}
		
	}
	
	
	/**
	 * Serializes item
	 * @param item item to serialize
	 * @param out output sink target for serialization
	 * @throws IOException if I/O problem
	 * 		   NullPointerException if out is null
	 */
	public static void encode(Item item, MessageOutput out) throws IOException {
		
		Objects.requireNonNull(item,"item is null");// check item is null
		Objects.requireNonNull(out,"out is null");//check out is null
		
		String estring = null; //declare a string for store serialization info
		
		DecimalFormat f = new DecimalFormat(dPATTERN); //keep one decimal place
		int count = item.getName().length(); //get character count
		
		//serializes item
		estring = Integer.toUnsignedString(count) + " ";//encode character count
		out.write(estring.getBytes(UTF8));
		estring = item.getName();//encode name
		out.write(estring.getBytes(UTF8));
		estring = "" + item.getMealType().getMealTypeCode();//encode meal type
		out.write(estring.getBytes(UTF8)); 
		estring = Integer.toUnsignedString(item.getCalories())+" ";//encode calories
		out.write(estring.getBytes(UTF8));
		estring = f.format(item.getFat()) + " ";
		out.write(estring.getBytes(UTF8));

		out.flush();
		//out.close();
		//serialization Done.......
	}
	
	
	/**
	 * Decode String
	 * @param in Message input
	 * @return a string
	 * @throws IOException I/O problem
	 * @throws TokenizerException if validation fails
	 */
	public static String decodeString (MessageInput in) 
			throws IOException, TokenizerException {
		StringBuilder builder = new StringBuilder();
		int ch;
	
		while((ch = in.read()) != SP){
			if(ch == EOS) { //whether exist premature EOS
				throw new TokenizerException(pos,"premature");
			}
			builder.append((char)ch); //get int value
		}
		String temp =builder.toString();
		builder.delete(ZERO, temp.length());//clean string bulider
		pos += temp.length();
		return temp;
	}
	
	
	/**
	 * Decode for get Count
	 * @param in Message Input
	 * @return
	 * numebr of character count
	 * @throws IOException I/O problem
	 * @throws TokenizerException if validation fails
	 */
	public static int getcount(MessageInput in) 
			throws IOException, TokenizerException {
		String s = decodeString(in);//get string
		int charC = 0;
		try {
			charC = Integer.parseUnsignedInt(s);
			if(charC > MAXINT) {
				throw new Exception();
			}
		} catch (Exception e) {
			throw new TokenizerException(pos, 
					pos+" as error is "+s);
		}
		pos = s.length() + ONE;//get position
		return charC;
	}
	
	/**
	 * Decode for item name
	 * @param in Message Input
	 * @param item food item
	 * @param charC character count
	 * @throws IOException if I/O problem
	 * @throws TokenizerException if validation fails
	 */
	public static void name(MessageInput in, Item item, int charC) 
			throws IOException, TokenizerException {
		String name = "";
		try {
			//declare the char array get the item name
			char[] a = new char[charC];
			int off = 0;
			while(off < charC) {
				int chara = in.read(a, off, charC - off);
				//if read the name has  premature EOS
				if(chara == EOS) {
					throw new TokenizerException(pos,"premature");
				}
				off += chara;
			}
			name = String.valueOf(a);
			item.setName(name);//item set name
			pos += charC + ONE;//move forward position
		} catch (Exception e) {
			throw new TokenizerException(pos, 
					pos+" as error is "+name);
		}
	}
	
	/**
	 * Decode for item mealtype
	 * @param in Message Input
	 * @param item food item
	 * @throws IOException I/O problem
	 * @throws TokenizerException if validation fails
	 */
	public static void mealtype(MessageInput in, Item item) 
			throws IOException, TokenizerException {
		int charC = in.read();//get Meal type
		//check meal type 
		try {
			char type = (char) charC;
			MealType.getMealType(type);//check whether type valid
			item.setMealType(MealType.getMealType(type)); //item set meal type
;		} catch (IllegalArgumentException e) {
			throw new TokenizerException(pos, pos 
					+ " as error is " +(char)charC);
		}
		pos++; //move forward
	}
	
	/**
	 * Decode for item calories
	 * @param in Message Input
	 * @param item food item
	 * @throws TokenizerException If validation fails
	 * @throws IOException I/O problem
	 */
	public static void calories(MessageInput in, Item item) 
			throws TokenizerException, IOException {
		String s = decodeString(in);
		
		try {
			int calories = Integer.parseUnsignedInt(s);//get the calories
			item.setCalories(calories);
		} catch (IllegalArgumentException e) {
			throw new TokenizerException(pos-s.length(), 
					pos-s.length()+" as error is "+s);
		}
		pos++; //position move forward
	}
	
	/**
	 * Decode for item fat
	 * @param in MessageInput
	 * @param item food item
	 * @throws TokenizerException If validation fails
	 * @throws IOException I/O problem
	 */
	public static void fat(MessageInput in, Item item) 
			throws TokenizerException, IOException {
		String s = decodeString(in);
		double f = 0.0;
		//check temp string  whether a valid double string
		if(!s.contains(".")) {
			throw new TokenizerException(pos, pos+" as error is "+s);
		}
		try {
			//get the fat
			f = Double.parseDouble(s);
			item.setFat(f);
		} catch (Exception e) {
			throw new TokenizerException(pos, pos+" as error is "+s);
		}
		pos++;
	}
	
}
