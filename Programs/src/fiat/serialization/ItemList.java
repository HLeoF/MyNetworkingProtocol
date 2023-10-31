package fiat.serialization;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a List of items and provides serialization/deserialization
 * @author Maiqi Hou
 * @version 1.1
 * update getRequest method
 * add getReuqestinfo method(), which get all information after the request
 * fix some small problem, such super setTimestampe
 * update string request to final string request
 */
public class ItemList extends Message{


	private static final long ZERO    = 0;              //time stamp min value
	private static final long MAXTIME = Long.MAX_VALUE; //time stamp max value
	private static final String SP    = " ";            //String type space
	// Standard Charsets UTF 8 for message input, message output, decode, encode
	private static final Charset UTF8 = StandardCharsets.UTF_8; 
	
	
	/**
	 * modified time stamp
	 */
	public long modifiedtimestamp;
	/**
	 * Item list
	 */
	public List<Item> list = new ArrayList<Item>();
	/**
	 * declare a message request LIST
	 */
	public final String request = "LIST";
	
	
	
	/**
	 * Constructs ItemList using set values
	 * @param messageTimestamp message time stamp
	 * @param modifiedTimestamp time stamp of last modification
	 * @throws
	 * IllegalArgumentException if validation fails
	 */
	public ItemList(long messageTimestamp, long modifiedTimestamp) {
		super.setTimestamp(messageTimestamp);
		setModifiedTimestamp(modifiedTimestamp);
	}
	
	
	
	/**
	 * override Message request
	 */
	@Override
	public String getRequest() {
		return request;
	}
	
	/**
	 * get request information
	 * @return message request information 
	 */
	@Override
	public String getRequestInfo() {
		int lc = getItemList().size(); //get item list count
		String s = SP + getModifiedTimestamp() + SP + lc
		          + SP;
		
		//encode item in the item list
		try {
			for(int i = 0; i < lc; i++) {
				//declare a item message output stream
				MessageOutput o = new MessageOutput(new ByteArrayOutputStream());
				//encode items information
				ItemFactory.encode(getItemList().get(i), o); //encode item list
				//string item
				String t = new String(o.toByteArray(),UTF8);//get item encode inform
				s += t;
				o.close();//close item message out stream
			}
		} catch (Exception e) {}
		
		return s;
	}
	
	
	/**
	 * Returns string of the form
	 */
	@Override
	public String toString() {
		int length = this.list.size();//get list length
		String[] temp = new String[length];//declare string array to store item
		//store item to string array
		for(int i = 0; i < length; i++) {
			String t = this.list.get(i).toString();
			temp[i] = t;
		}
		
		return "LIST (TS=" +getTimestamp()
				   + ") last mod="+getModifiedTimestamp()
				   +", list={"+String.join(",", temp)+"}";
	}
	
	/**
	 * Returns modified time stamp
	 * @return
	 * modified time stamp
	 */
	public long getModifiedTimestamp() {
		return this.modifiedtimestamp;
	}
	
	
	/**
	 * Set modified time stamp
	 * @param modifiedTimestamp modification time stamp
	 * @return
	 * this object with new value
	 */
	public ItemList setModifiedTimestamp(long modifiedTimestamp) {
		
		//check modified time stamp whether valid
		if(modifiedTimestamp < ZERO || modifiedTimestamp > MAXTIME) {
			throw new IllegalArgumentException("ModifiedTiemStamp is invalid");
		}
		this.modifiedtimestamp = modifiedTimestamp;
		return this;
	}
	
	/**
	 * Returns list of items
	 * @return
	 * list of items
	 */
	public List<Item> getItemList(){
		return this.list;
	}
	
	
	/**
	 * Adds item
	 * @param item  new item to add
	 * @return
	 * this object with new value
	 * @throws
	 * IllegalArgumentException if null item
	 */
	public ItemList addItem(Item item) {
		//check item whether null
		if(item == null) {
			throw new IllegalArgumentException("item is null");
		}
		this.list.add(item);
		return this;
	}



	/**
	 * hashCode
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(list, modifiedtimestamp);
		return result;
	}


	/**
	 * equals method
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass()) return false;
		ItemList other = (ItemList) obj;
		return Objects.equals(list, other.list) 
				&& modifiedtimestamp == other.modifiedtimestamp;
	}



	
	
	
	
}
