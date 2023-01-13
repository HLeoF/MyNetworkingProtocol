/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 0
* Class: CSI 4321
*
************************************************/
package fiat.serialization;

import java.text.DecimalFormat;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represents Item
 * @author Maiqi Hou
 * @version 1.20
 */
public class Item {
	private static final int    MAXUNSINT = 2048;          //calories, character count max value 
	private static final int    MINUNSINT = 0;             //calories, character count min value
	private static final int    ZERO = 0;                  //Constant integer value 0
	private static final double DZERO = 0.0;               //fat min value
	private static final double DMAX = 100000.0;           //fat max value
	private static final String PATTERN = "\\p{N}|\\p{L}";//regex for 0-9,A-Z,a-z, check first char
	private static final String dPATTERN = "#";            //regex for fat value keep one decimal
	private static final String UNICODE = "\\p{N}|\\p{L}|\\p{P}|\\p{S}"; //for check valid unicode categroy
	private static final String SP = " ";                  //String space
	 
	
	private String name;       //name of item
	private MealType mealType; //type of meal
	private int calories;      //number of calories in item
	private double fat;        //grams of fat in item
	
	/**
	 * Constructor item without values
	 */
	public Item() {}
	
	/**
	 * Constructs item with set values
	 * @param name name of item
	 * @param mealType type of meal
	 * @param calories number of calories in item
	 * @param fat grams of fat in item
	 */
	public Item(String name, MealType mealType,
			int calories, double fat) {
		setName(name);
		setMealType(mealType);
		setCalories(calories);
		setFat(fat);
	}
	
	
	/**
	 * String form of item
	 * @return 
	 *  string of the form
	 */
	@Override
	public String toString() {
		double d = Math.round(getFat());
		DecimalFormat f = new DecimalFormat(dPATTERN);
		return getName() + " with " 
				+ getCalories() + " calories and "
				+ f.format(d) +"g of fat eaten at "
				+ getMealType();
	
	}
	
	
	
	/**
	 * Sets Name
	 * @param name new name
	 * @return this object with new value
	 * @throws 
	 * IllegalArgumentException if validation fails
	 */
	public Item setName(String name) {
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
	 * Return name
	 * @return name
	 */
	public String getName() {
		return this.name;
	}
	
	
	
	/**
	 * Set meal type
	 * @param mealType new mealType
	 * @return 
	 * 	this object with new value
	 * @throws 
	 * IllegalArgumentException if validation fails
	 */
	public Item setMealType(MealType mealType) {
		if(mealType == null) {
			throw new IllegalArgumentException("MealType is null");
		}
		this.mealType = mealType;
		return this;
	}
	
	/**
	 * Returns calories
	 * @return
	 * 	Calories
	 */
	public MealType getMealType() {
		return this.mealType;
	}
	

	/**
	 * Set Calories
	 * @param calories new calories
	 * @return
	 *  this object with new value
	 * @throws 
	 * IllegalArgumentException if validation fails
	 */
	public Item setCalories(int calories) {
		//check calories valid 
		if(calories < ZERO || calories > MAXUNSINT) {
			throw new IllegalArgumentException("invalid calories");
		}
		this.calories = calories;
		return this;
	}
	
	/**
	 * Returns calories
	 * @return
	 *  calories
	 */
	public int getCalories() {
		return this.calories;
	}
	
	
	/**
	 * Set fat
	 * @param fat new fat grams
	 * @return
	 *  this object with new value
	 * @throws 
	 * IllegalArgumentException if validation fails
	 */
	public Item setFat(double fat){
		//check fat valid
		if(fat < DZERO ||fat > DMAX) {
			throw new IllegalArgumentException("invalid fat");
		}
		this.fat = fat;
		return this;
	}
	
	/**
	 * Get fat
	 * @return
	 *  grams of fat
	 */
	public double getFat() {
		return fat;
	}
	
	
	
	
    /**
     * Hash Code
     */
	@Override
	public int hashCode() {
		return Objects.hash(calories, fat, mealType, name);
	}

	
	/**
	 * Equals method
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Item other = (Item) obj;
		return calories == other.calories 
				&& Double.doubleToLongBits(fat) 
				== Double.doubleToLongBits(other.fat)
				&& mealType == other.mealType 
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
		//name is empty or name out of range 2048
		return (num > MINUNSINT && num <= MAXUNSINT);
	}
	
	/**
	 * Check character list is valid
	 * @param name  for name
	 * @return if character list is valid, return true
	 * 		   if character list is not valid, return false
	 */
	public boolean checkCharList(String name) {
		char[] ch = new char[name.length()]; //declare character list
		char c = name.charAt(0); //get character list first char

		//check first character must be letter or number 
		if(Pattern.matches(PATTERN, Character.toString(c)) == false) {
			return false;
		}
		
		//check characters are valid
		for(int i = 1; i < name.length(); i++) {
			String temp = Character.toString(name.charAt(i));
			if(Pattern.matches(UNICODE, temp) == false && !temp.equals(SP)) {
				return false;
			}
			ch[i] = name.charAt(i);	
		}
		return true;
	}
}
