package fiat.serialization;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


/**
 * Factory to create deserialized and serialized item
 * @author Maiqi Hou
 * @version 1.2 
 * update the encode 
 * update decode method
 * Update check whether exits ELON at end of Message
 * Update Interval method in the decode method
 */
public class MessageFactory {
	
	// Standard Charsets UTF 8 for message input, message out, decode, and encode
	private static final Charset UTF8  = StandardCharsets.UTF_8; 
	private static final String  ELON  = "\n"; //string type elon character
	private static final int     SIX   = 6;    //constant six for mark the protocol type
	private static final int     ZERO  = 0;    //constant value of 0 mark read position and initial value
	private static final int     ONE   = 1;    //constant value of 1 mark read position
	private static final int     EOS  = -1;	   //integer indicate the end of stream
	private static final int     INTELON = 10; //integer value of elon character
	
	/**
	 * log position 
	 */
	public static int pos = 0;

	/**
	 * Constructs message using deserialization 
	 * @param in Message input
	 * @return
	 * According message request type, return each type
	 * @throws TokenizerException If validation failure
	 * @throws NullPointerException If in is null 
	 */
	public static Message decode(MessageInput in) 
			throws TokenizerException{
		//check the in whether null
		Objects.requireNonNull(in,"in is null");
		try {
			checkHeader(in);//check FT1.0 whether valid
			long timestamp = getTS(in); // get time stamp
			String request = checkRequest(in); //get request
			
			//If the request equal to ERROR
			if("ERROR".equals(request)) {
				//initial Error request
				Error error = new Error(timestamp, "ERROR");
				msgError(in, error);//get the error message
				return error;
			}
			
			//If the request equal to ADD
			if("ADD".equals(request)) {
				//initial Add request
				Add add = new Add(timestamp, new Item());
				msgAdd(in, timestamp, add);//get the adding item
				checkEndwithELON(in);
				return add;
			}
			
			//If the request LIST
			if("LIST".equals(request)) {
				long modifiTime = getTS(in);//get the modified time stamp
				int listC = ItemFactory.getcount(in);//get the item list county
				//initial itemlist request
				ItemList list = new ItemList(timestamp, modifiTime);
				itemL(in, listC, list);//get item in list
				checkEndwithELON(in);
				return list;
			}
			
			//If the request Interval
			if("INTERVAL".equals(request)) {
				int time = interVal(in);
				checkEndwithELON(in);
				return new Interval(timestamp, time);
			}
			
			checkEndwithELON(in);
			return new Get(timestamp);//return GET
		} catch (IOException e) {
			throw new TokenizerException(pos, e.getMessage());
		}
	}
	

	/**
	 * Serializes item
	 * @param msg message to serialize
	 * @param out output sink target for serialization
	 * @throws IOException I/O problem
	 * @throws NullPointerException if msg or out is null
	 */
	public static void encode(Message msg, MessageOutput out) throws IOException {
		//check msg whether null
		Objects.requireNonNull(msg,"messgae is null");//check message is null
		Objects.requireNonNull(out,"out is null");//check output is null
		
		String estring = "FT1.0 ";
		out.write(estring.getBytes(UTF8)); //write the protocol type
		estring = msg.getTimestamp()+ " "; 
		out.write(estring.getBytes(UTF8)); //write time stamp
		estring = msg.getRequest();
		out.write(estring.getBytes(UTF8)); //write request + ELON
		estring = msg.getRequestInfo() + ELON;
		out.write(estring.getBytes(UTF8)); //write request information + ELON
		
		out.flush();
		//out.close();
	}

	
	
	
	/**
	 * Check FT1.0 whether valid
	 * @param in Message input
	 * @throws TokenizerException If validation failure
	 * @throws IOException I/O problem
	 */
	public static void checkHeader(MessageInput in) 
			throws TokenizerException, IOException{
		//declare the char array get the protocol type
		char[] h = typeAndMsg(in, SIX);
		String t = new String(h);
		String e = "FT1.0 ";
		//check protocol type valid
		if(!e.equals(t)) {
			throw new TokenizerException(pos, 
					"Wrong protocol version " + t);
		}
		pos += SIX;
	}
	
	/**
	 * decode protocol type and error message
	 * @param in message input 
	 * @param count string length
	 * @return char array for type or error message
	 * @throws IOException I/O problem
	 * @throws TokenizerException if validation failure
	 */
	public static char[] typeAndMsg(MessageInput in, int count) 
			throws IOException, TokenizerException {
		char[] h = new char[count];
		int off = 0;
		while(off < count) {
			//read the protocol type 
			int chara = in.read(h, off, count-off);
			if(chara == EOS) {//check protocol type has premature EOS
				throw new TokenizerException(pos,"premature");
			}
			off += chara;
		}
		return h;
	}
	
	
	/**
	 * Get the message time stamp
	 * @param in inputStream reader
	 * @return time stamp
	 * @throws TokenizerException If validation failure
	 * @throws IOException I/O problem
	 */
	public static Long getTS(MessageInput in) 
			throws TokenizerException, IOException {
		String s = "";
		try {
			//get the time stamp from message input
			ItemFactory.pos = 0;//get position
			s = ItemFactory.decodeString(in);
			long n = 0L;
			//change time stamp from string type to long type
			n = Long.parseUnsignedLong(s);
			pos += ItemFactory.pos + ONE; //position move forward
			//check time stamp whether valid
			if(n < ZERO || n > Long.MAX_VALUE) {
				throw new TokenizerException(pos, 
						pos+" as error is "+n);
			}
			return n;
		} catch (Exception e) {
			throw new TokenizerException(pos, 
					pos+" as error is "+s);
		}
		
	}
	

	/**
	 * Check Message Request 
	 * @param in InputStreamReader
	 * @return message request
	 * @throws IOException I/O problem
	 * @throws TokenizerException If validation failure
	 */
	public static String checkRequest(MessageInput in) throws 
	IOException, TokenizerException {
		String req = "";
		try {
			//get the message request
			ItemFactory.pos = 0;
			req = ItemFactory.decodeString(in);
			//check request validation
			if(!"ADD".equals(req) 
					&& !"LIST".equals(req)
					&& !"GET".equals(req) 
					&& !"ERROR".equals(req)
					&& !"INTERVAL".equals(req)) {
				throw new TokenizerException(pos, "Wrong req " + req);
			}
			pos += ItemFactory.pos; //position move forward
			return req;
		} catch (TokenizerException e) {
			throw new TokenizerException(pos, 
					"Wrong req " + req);
		}
	}
	
	/**
	 * get ERROR message
	 * @param in message input
	 * @param error Error request of message
	 * @throws TokenizerException if validation failure
	 */
	public static void msgError(MessageInput in, Error error) 
			throws TokenizerException {
		String msg = "";
		try {
			//get the error message character count
			int count = ItemFactory.getcount(in);
			//position move forward
			pos += ItemFactory.pos;
			
			//get the error message
			char[] a = typeAndMsg(in, count);
			msg = String.valueOf(a);
			
			//determine whether message missing read or read more
			//compare with character count
			if(msg.contains("\n") || 
					((char) in.read() != '\n')) {
				throw new TokenizerException(pos, 
						pos+" as error is "+msg);
			}
			error.setMessage(msg);
		} catch (Exception e) {
			throw new TokenizerException(pos, 
					pos+" as error is "+msg);
		}
	}

	/**
	 * decode message of Add request
	 * @param in message input
	 * @param timestamp add request time stamp
	 * @param add  Add request of message
	 * @throws TokenizerException if validation failure
	 */
	public static void msgAdd(MessageInput in, long timestamp, Add add)
	throws TokenizerException {
		try {
			//get item decode
			Item item = ItemFactory.decode(in);
			add.setTimestamp(timestamp);//get the time stamp
			add.setItem(item); // get the item
		} catch (Exception e) {
			throw new TokenizerException(pos, 
				 pos+"+"+e.getMessage());
		}
	}
	
	/**
	 * decode message of item list
	 * @param in message input
	 * @param c list count
	 * @param list item list 
	 * @throws TokenizerException if validation failure
	 */
	public static void itemL (MessageInput in, int c, ItemList list) 
			throws TokenizerException {
		try {
			//for loop for decode item list
			for(int i = 0 ; i < c; i++) {
				Item item = ItemFactory.decode(in);
				list.addItem(item);
				pos += ItemFactory.pos;//position move forward
			}
		} catch (Exception e) {
			throw new TokenizerException(pos, 
					 pos+"+"+e.getMessage());
		}
		
	}
	
	
	/**
	 * decode message of interval time
	 * @param in message input
	 * @return 
	 * 	interval time
	 * @throws TokenizerException if validation failure
	 */
	public static int interVal(MessageInput in) 
			throws TokenizerException {
		try {
			int time = ItemFactory.getcount(in);
			pos += ItemFactory.pos;
			return time;
		} catch (Exception e) {
			throw new TokenizerException(pos, 
					pos+" as error is" + e.getMessage());
		}
	}
	
	/**
	 * Check the Message end with ELON
	 * @param in Message input
	 * @throws TokenizerException if validation failure
	 * @throws IOException I/O problem
	 */
	public static void checkEndwithELON(MessageInput in) 
			throws TokenizerException, IOException {
		if(in.read() != INTELON) {
			throw new TokenizerException(pos, 
					"End not with ELON");
		}
	}
	
}
