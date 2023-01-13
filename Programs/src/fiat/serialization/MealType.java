/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 0
* Class: CSI 4321
*
************************************************/

package fiat.serialization;

/**
 * Meal type
 * @author Maiqi Hou
 * @version 1.0
 */
public enum MealType {
	/**
	 * Breakfast
	 */
	Breakfast('B'),
	/**
	 * Lunch
	 */
	Lunch('L'), 
	/**
	 * Dinner
	 */
	Dinner('D'),
	/**
	 * Snack
	 */
	Snack('S');
	
	private char code;

	
	/**
	 * Create the new meal type enum
	 * @param name meal type name
	 * @param code meal type code
	 */
	MealType(char code){
		this.code = code;
	}

	/**
	 * check the code is valid or not
	 * @param code meal type code
	 * @return if code is wrong return false
	 * 		   if code is right return true
	 */
	public static boolean checkCode(char code) {
		if(code != 'B' && code != 'L'
		   && code != 'D' && code != 'S') {
			return false;
		}
		return true;
	}
	
	/**
	 * Get code for meal type
	 * @return meal type code
	 */
	public char getMealTypeCode() {
		return code;
	}
	
	/**
	 * Get meal type for give code
	 * @param code code of meal type
	 * @return meal type corresponding to code
	 * @throws IllegalArgumentException -if bad code value 
	 */
	public static MealType getMealType(char code) {
		//check code valid or not
		if(checkCode(code) == false) {
			throw new IllegalArgumentException("Bad code value");
		}
		for(MealType mealType : values()) {
			if(mealType.getMealTypeCode() == code) {
				return mealType;
			}
		}
		return null;
	}
}
