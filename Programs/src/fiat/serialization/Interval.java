
package fiat.serialization;

import java.util.Objects;

/**
 * Represents a Interval
 * @author Maiqi Hou
 * @version 1.0
 * Add new Message method(Interval)
 */
public class Interval extends Message {
	
	private static final int    MAXUNSINT = 2048;  //Maximum value of intervalTime
	private static final int    MINUNSINT = 0;     //Minimum value of intervalTime
	private static final String        SP = " ";   //string type space
	
	/**
	 * declare a message request INTERVAL
	 */
	public final String request = "INTERVAL";
	
	/**
	 * Interval Time
	 */
	public int intervalTime;
	
	
	/**
	 * * Constructs Interval using set values
	 * @param messageTimestamp  message time stamp
	 * @param intervalTime  minutes into the past for interval
	 * @throws IllegalArgumentException if validation fails
	 */
	public Interval(long messageTimestamp, int intervalTime) {
		super.setTimestamp(messageTimestamp);
		setIntervalTime(intervalTime);
		
	}
	
	/**
	 * Return request
	 * @return request type
	 */
	@Override
	public String getRequest() {
		return request;
	}
	
	
	/**
	 * get the request information
	 * @return the message request information
	 */
	@Override
	public String getRequestInfo() {
		return SP + getIntervalTime() + SP;
	}
	
	/**
	 * Set the interval
	 * @param intervalTime interval time
	 * @return this object with new value
	 * @throws
	 * IllegalArgumentException if validation fails
	 */
	public Interval setIntervalTime(int intervalTime) {
		if(intervalTime < MINUNSINT || intervalTime > MAXUNSINT) {
			throw new IllegalArgumentException("invalid intervalTime");
		}
		this.intervalTime = intervalTime;
		return this;
	}
	
	/**
	 * Return interval 
	 * @return the interval
	 */
	public int getIntervalTime() {
		return this.intervalTime;
	}
	
	/**
	 * Return string of the form
	 */
	@Override
	public String toString() {
		return "INTERVAL (TS="+
				getTimestamp() + ") time="+
				getIntervalTime();
	}

	/**
	 * HashCode
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(intervalTime);
		return result;
	}

	/**
	 * Equals methods
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass()) return false;
		Interval other = (Interval) obj;
		return intervalTime == other.intervalTime;
	}
}
