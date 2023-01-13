/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 7
* Class: CSI 4321
*
************************************************/
package fiat.app.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import fiat.serialization.Add;
import fiat.serialization.Error;
import fiat.serialization.Interval;
import fiat.serialization.Item;
import fiat.serialization.ItemList;
import fiat.serialization.MealType;
import fiat.serialization.Message;
import fiat.serialization.MessageFactory;
import fiat.serialization.MessageInput;
import fiat.serialization.MessageNIODeframer;
import fiat.serialization.MessageOutput;
import fiat.serialization.TokenizerException;
import foop.app.server.FOOPServer;


/**
 * Fiat Server with AIO
 * @author Maiqi Hou
 * @version 1.1
 */
public class ServerAIO {
	private static final int ONE         = 1;	  //constant value of 1
	private static final int ZERO        = 0;     //constant value of 0
	private static final int TWO         = 2;     //the number of parameters
	private static final int MAXBUFF     = 65510; //The maximum size of byte buffer
	private static final int OTHERCLOSED = -1;    //if read bytes is -1 in read handler
	private static final long UNITMILLI = 60000L; //Convert one minus to millisecond
	
	private static File infile;		  //declare file 
	private static FileWriter writer; //declare new file write to write item information into a file
	private static ItemList list  = new ItemList(ZERO, ZERO); //declare item list
	private static Logger logger  = Logger.getLogger("FiatAIOLog"); //declare logger
	//declare message buffer to collect message bytes
	private static ByteBuffer messageBuffer = ByteBuffer.allocate(MAXBUFF); 
	private static byte[] delimiter = "\r\n\0".getBytes(StandardCharsets.UTF_8);//declare message delimiter
	//declare message de-framer to collect message
	private static MessageNIODeframer deframer = new MessageNIODeframer(delimiter);
	
	/**
	 * Set the AIO Server and prepare accept client to connect
	 * @param args port and file name
	 * @throws IOException if any IO problem
	 */
	public static void main(String[] args) throws IOException {
		FileHandler file = new FileHandler("fiat.log", true); //declare a file to logging message
		logger.setUseParentHandlers(false); //set logging cannot show on the console
		logger.setLevel(Level.ALL); //set level of logger
		file.setFormatter(new SimpleFormatter()); //set the format of logger
		logger.addHandler(file); //set file logger handler

		
		try {
			int port = Integer.parseUnsignedInt(args[ZERO]);
			if(args.length != TWO)
				throw new ArrayIndexOutOfBoundsException();
			
			
			infile = new File(args[ONE]); //declare and create the file to log item have been add
			readItem(infile, list, args[ONE]);//process the items in file
			writer = new FileWriter(infile,true); 
			FOOPServer.startFOOPServer(port);//open the FOOP Server
			logger.info("FOOP Sever started");
			
			 try{
				 AsynchronousServerSocketChannel listenChannel = AsynchronousServerSocketChannel.open();
				 //Bind the port from the parameters
		         listenChannel.bind(new InetSocketAddress(port));
		         
		         // Create the clients accept handler
		         listenChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
	                @Override
	                public void completed(AsynchronousSocketChannel clntChan, Void attachment) {
	                	loggingInfo(clntChan, "The Server accept a client: ", logger);
	                    listenChannel.accept(null, this);
	                    handleAccept(clntChan);
	                }

	                @Override
	                public void failed(Throwable e, Void attachment) {
	                	logger.warning("Server Cloose Failed: \n" + e);
	                }
		                
		          });
		         
	            // It will block until the current thread is die.
	            Thread.currentThread().join();
	        } catch (InterruptedException e) {
	        	logger.severe("Fiat Server Interruted: \n" + e);
	        }
			 
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.warning("Parameter(s): <Port> <Item file name>");
		}catch (NumberFormatException e) {
			logger.warning("Port is invalid");
		}catch (FileNotFoundException e) {
			logger.warning("File cannot found");
		}
	}

	
	/**
	 * once new client accepted, call this function and prepare to read message 
	 * @param clchnel new client asynchrounous socket Channel
	 */
	public static void handleAccept(AsynchronousSocketChannel clchnel) {
		initRead(clchnel, ByteBuffer.allocateDirect(MAXBUFF));
	}
	
	/**
	 * Step into the read process
	 * @param clchnel a channel receive message send from a client
	 * @param buffer receive message byte buffer
	 */
	public static void initRead(AsynchronousSocketChannel clchnel, ByteBuffer buffer) {
		
		clchnel.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
			
            public void completed(Integer bytesRead, ByteBuffer buffer) {
                    readHandler(clchnel, buffer, bytesRead);
            }

            public void failed(Throwable ex, ByteBuffer v) {
            	 failhander(clchnel, "issue for read", ex);
            }
        });
	}

	/**
	 * Step into the write process
	 * @param clchnel the channel send message to a client
	 * @param buffer send message byte buffer
	 */
    public static void initWrite(AsynchronousSocketChannel clchnel, ByteBuffer buffer) {
    	
    	clchnel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
    		
            public void completed(Integer bytesWritten, ByteBuffer buffer) {
            	writeHandler(clchnel, buffer);
            }

            public void failed(Throwable ex, ByteBuffer buffer) {
                failhander(clchnel, "issue for write", ex);
            }
        });
    }

    
    /**
     * Handler for read message bytes from the client sent
     * @param clchnel new client asynchronous socket channel
     * @param buffer the byte buffer read the message bytes
     * @param bytesRead number of  bytes have been read
     */
    public static void readHandler(AsynchronousSocketChannel clchnel, ByteBuffer buffer, int bytesRead) {
    	//check other end whether closes
        if (bytesRead == OTHERCLOSED) {
            try {
            	clchnel.close();
            	loggingInfo(clchnel, "A Client close: ", logger);
            } catch (IOException e) {
            	 failhander(clchnel, "issue for close other end", e);
            }
            
        } else if (bytesRead > ZERO) {
        	//collect the message bytes from the byte buffer
        	buffer.flip(); //paper to collection the bytes
        	//get the message bytes from the buffer
            byte [] b = new byte[buffer.remaining()];
            buffer.get(b);
            //check the bytes array whether exist delimiter
            byte[] m= deframer.nextMsg(b);
            
            //if message buffer remain 0 size
        	if(messageBuffer.remaining()<=ZERO) {
        		//we need re-allocate the capacity of message buffer
        		messageBuffer = ByteBuffer.allocate(MAXBUFF);
        	}
        	
            if(m != null) {
            	//if the byte array does exist delimiter
            	buffer.clear();
            	//put the last segment message into messageBuffer
            	messageBuffer.put(b);
            	//prepare deal with message and send to client
            	MessageHandler(messageBuffer, clchnel);
            }else {
            	//if the byte array does not exist delimiter 
            	messageBuffer.put(b);
            	buffer.clear();
            	//Continue to receive message byte, 
            	//and store the message bytes segment into message buffer
            	initRead(clchnel, buffer);
            }
            
        }
    }
    
    /**
     * Message handler will process the sending message byte from a client.
     * It will handler these message byte and will prepare to send message packet to a client
     * @param msgbuffer buffer contains the message bytes send by client
     * @param clchnel client who send the message
     */
    public static void MessageHandler(ByteBuffer msgbuffer, AsynchronousSocketChannel clchnel) {
    	msgbuffer.flip(); //prepare obtain message buffer bytes
    	byte[] arr = new byte[msgbuffer.remaining()];
    	msgbuffer.get(arr);
    	msgbuffer.flip();//prepare to get the bytes
    	
    	try {
    		MessageInput in = new MessageInput(new ByteArrayInputStream(arr));
        	MessageOutput out = new MessageOutput(new ByteArrayOutputStream());
        	Instant instant = Instant.now(); // get the current time
    		Long timestamp = instant.toEpochMilli();//get the time stamp
    		list.setTimestamp(timestamp);//item list set time stamp
        	
        	Message message = new Message() {};//declare message type
        	
    		try {
    			message = MessageFactory.decode(in); //decode message send from a Client
    			//if message type is GET 
    			if("GET".equals(message.getRequest())) {
    				//if list is empty
            		if(list.getItemList().size() == ZERO) {
            			//set the modified time stamp of list is zero
            			list.setModifiedTimestamp(0L);
            		}
            		loggingInfo(clchnel,"The Server receive GET message from Client: ", logger);
            		MessageFactory.encode(list, out);//send get message to a client
            		
            		//if message type is ADD
            	}else if ("ADD".equals(message.getRequest())){
            		Add add = (Add) message;
    				instant = Instant.now();
    				long modified = instant.toEpochMilli();//get the modified time stamp
    				list.setTimestamp(add.getTimestamp());
    				list.setModifiedTimestamp(modified);
    				list.addItem(add.getItem()); //list add new item information
    				loggingInfo(clchnel, "The Server receive Add message from Client: ", logger);
    				writeItem(writer, add, modified, logger);
    				MessageFactory.encode(list, out);//send message to client
    				
            		//if message type is INTERVAL
            	}else if ("INTERVAL".equals(message.getRequest())) {
            		ItemList list2 = new ItemList(timestamp, ZERO);
            		Interval inval = (Interval) message;
            		loggingInfo(clchnel, "The Server receive Interval message form Client: ", logger);
            		handleInterval(inval.getIntervalTime(), list2, infile);//process list according to interval time
            		MessageFactory.encode(list2, out);
            		
            	}else {
            		MessageFactory.encode(new Error(instant.toEpochMilli(),  
    				  "Unexpected message type: <" + message.getRequest() + ">"), out);
            	}
        		loggingInfo(clchnel, "The Server send a message to Client: ", logger);
    			
    		} catch (TokenizerException e) {
    			//Network I/O blocks problem
    			if("Read timed out".equals(e.getMessage())){
    				//send error message to the client
    				loggingWarn(clchnel, "Bad IO from the Client: ", logger);
    				MessageFactory.encode(new Error(timestamp, "BAD IO"), out);
    			}
    			
    			if(e.getMessage().contains("premature")) {
    				MessageFactory.encode(new Error(timestamp, "Premature Problem"), out);
    				loggingWarn(clchnel, "Premature Problem from the Client: ", logger);
				}
    			
    			//Received the unexpected version
				if(e.getMessage().contains("Wrong protocol")) {
					
					String[] split = e.getMessage().split(" ");//get the version
					//send error message to the client
					MessageFactory.encode(new Error(timestamp, 
							"Unexpected version: <" 
					+ split[split.length-1]+">"), out);
					loggingWarn(clchnel, "Unexpected version send from the Client: ", logger);
				}
				
				//Received unknown request 
				if(e.getMessage().contains("Wrong req")) {
					//get the request 
					String[] split = e.getMessage().split(" ");
					//send message to client 
					MessageFactory.encode(new Error(timestamp, 
							"UnKnown operation: <" 
					+ split[split.length-1]+">"), out);
					loggingWarn(clchnel, "Unknown operation send from the Client: ", logger);
				}
				
				//Problem parsing message, such as short packet
				if(e.getMessage().contains("as error is")) {
					//send message to client
					MessageFactory.encode(new Error(timestamp, 
							"Unable to parse message <" 
					+ e.getMessage()+">"), out);
					loggingWarn(clchnel, "Message unable to parse from the Client: ", logger);
				}
				
				//Problem with Message not end with ELON
				if(e.getMessage().contains("End not with ELON")) {
					//send message to client
					MessageFactory.encode(new Error(timestamp, 
							"Message <" 
					+ e.getMessage()+">"), out);
					loggingWarn(clchnel, "Messsage not with ELON from the Client: ", logger);
				}
    		}
        	
    		arr = out.toByteArray(); //get the message bytes
    		msgbuffer.clear(); //message buffer to clear and ready for putting new message bytes
    		msgbuffer = ByteBuffer.allocate(MAXBUFF);
    		msgbuffer.put(arr);
    		msgbuffer.flip();//prepare to write
    		
		} catch (Exception e) {}
    	
    	initWrite(clchnel, msgbuffer);//send message to client
	}
    
    /**
     * handler write bytes and send message to a client
     * @param clchnel channel send message to a client
     * @param buffer message buffer
     */
    public static void writeHandler(AsynchronousSocketChannel clchnel, ByteBuffer buffer) {
        if (buffer.hasRemaining()) { 
            initWrite(clchnel, buffer);
        } else {
        	buffer.clear();//clear write buffer
        	messageBuffer.clear();//clear message buffer
            initRead(clchnel, buffer); //ready to receive new message from client
        }
    }
    

	/**
	 * Read item from item file
	 * @param infile item file
	 * @param list list memory
	 * @param file file path
	 * @throws FileNotFoundException if file not found
	 */
	public static void readItem(File infile, ItemList list, String file) 
			throws FileNotFoundException  {
		Instant instant = Instant.now();
		list.setTimestamp(instant.toEpochMilli());
		if(infile.exists()) { //check file whether exists
			Scanner scan = new Scanner(infile);
			try {
				//read detail for item
				while(scan.hasNext()) {
					Item item = new Item();
					list.setModifiedTimestamp(scan.nextLong());
					item.setName(scan.next());
					item.setMealType(MealType.getMealType(scan.next().charAt(0)));
					item.setCalories(scan.nextInt());
					item.setFat(scan.nextDouble());
					list.addItem(item);
				}
				scan.close();
			} catch (Exception e) {
				list = new ItemList(ZERO, ZERO);//declare new item list
				scan.close(); 
				if(infile.delete());//initialize new file 
				infile = new File(file);
			}
			
		}
	}
    
    
	
    /**
	 * Client send a item to client, client can write the item
	 * into specific file 
	 * @param fw File write
	 * @param add Add message type
	 * @param modified modified time stamp
	 * @param logger log Server issues
	 */
	public static void writeItem(FileWriter fw, 
			Add add, long modified, Logger logger){
		FOOPServer.handleAddition(add); //Foop Server handle addition information
		try {
			//format the item
			String obje = modified + " "+ add.getItem().getName() + " " +
	    			 add.getItem().getMealType() + " " + add.getItem().getCalories()
	    			 + " " + add.getItem().getFat() + "\n";
		    fw.append(obje);//write to file
		    fw.flush();
		} catch (IOException e) {
			logger.warning("write item into specific has issues");
		}
		
	    
	}
 
	
	/**
	 * Handle Interval message
	 * @param time interval time stamp
	 * @param tempList declare a item list
	 * @param file items file
	 * @throws FileNotFoundException if file not exists
	 */
	public static void handleInterval(long time, ItemList tempList, File file) 
					throws FileNotFoundException {
		long minStamp = tempList.getTimestamp() - time*UNITMILLI; //get low bound of time stamp
		Scanner scan = new Scanner(file);//read data
	
		while(scan.hasNext()) {
			long stamp = scan.nextLong();
			Item item = new Item();
			item.setName(scan.next());
			item.setMealType(MealType.getMealType(scan.next().charAt(0)));
			item.setCalories(scan.nextInt());
			item.setFat(scan.nextDouble());
			if(stamp >= minStamp) {//if time stamp greater than low bound
				tempList.addItem(item); //add item to list
				tempList.setModifiedTimestamp(stamp);
			}else{
				tempList.setModifiedTimestamp(0L);
			}
		}
		scan.close();//close file scanner
	}
    
	/**
	 * handler a series fail events
	 * @param clchnel channel get message from clients
	 * @param msg error message 
	 * @param e exception information
	 */
    public static void failhander(AsynchronousSocketChannel clchnel, String msg, Throwable e) {
    	logger.severe(msg + "\n" + e); 
        try {
        	clchnel.close();
        } catch (IOException e1) {
        	logger.warning("Close Failed: \n" + e);
        }
    }
    
    /**
     * logging info level handler 
     * @param clchnel channel get message from clients
     * @param msg information message for processing
     * @param logger Server logger
     */
    public static void loggingInfo(AsynchronousSocketChannel clchnel, 
    		String msg, Logger logger) {
		try {
			String temp = msg + clchnel.getRemoteAddress().toString();
			logger.info(temp);
		} catch (IOException e) {}
    }
    
    /**
     * logging info level warning
     * @param clchnel channel get message from client
     * @param msg warning message for processing 
     * @param logger Server logger
     */
    public static void loggingWarn(AsynchronousSocketChannel clchnel, String msg, Logger logger) {
    	try {
			String temp = msg + clchnel.getRemoteAddress().toString();
			logger.warning(temp);
		} catch (IOException e) {}
    }
}
