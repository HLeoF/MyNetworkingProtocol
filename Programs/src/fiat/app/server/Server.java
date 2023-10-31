package fiat.app.server;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
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
import fiat.serialization.MessageOutput;
import fiat.serialization.TokenizerException;
import foop.app.server.FOOPServer;


/**
 * TCP Fiat Server 
 * @author Maiqi Hou
 * @version 2.1
 * fix some problems for send error message
 * Use Executor to instead of While loop (Thread pool)
 * Fix GET the modified should be 0 problem
 * Fix the arguments invalid show on the console problem
 * Add the FOOP Server
 * 
 */
public class Server {
	private static final int MAXCOMMTIME = 15000; //set Maximum time server connected time
	private static final int ZERO = 0;	          //pool number cannot be 0
	private static final int THREE = 3;			  //parameters number limitation
	private static Logger logger = Logger.getLogger("log");//declare a logger

	/**
	 * Main TCP Sever part
	 * @param args parameters for port, item file name, and Pool number
	 * @throws SecurityException security issue
	 * @throws IOException I/O problem
	 */
	public static void main(String[] args) throws SecurityException, IOException {	
		/*
		 * Logger initial
		 */
		FileHandler file = new FileHandler("fiat.log",true);
		logger.setUseParentHandlers(false);//logging does not show on the console
		logger.setLevel(Level.ALL); //set level of logging
		file.setFormatter(new SimpleFormatter()); //set format of logging
		logger.addHandler(file); //set file handler
		
		try {
			int port = Integer.parseUnsignedInt(args[0]); //server port
			int poolSize = Integer.parseUnsignedInt(args[2]); //thread pool size
			
			if(args.length != THREE) {
				throw new ArrayIndexOutOfBoundsException();
			}
			
			//If the pool size is equal to 0, server cannot be start
			if(poolSize == ZERO) {
				throw new NumberFormatException();
			}
			
			ItemList list = new ItemList(ZERO, ZERO); //declare the item memory
			File infile = new File(args[1]); //get the file path
			readItem(infile, list, logger, args[1]);//process the items in file
			//open the writer to record new item into file
			FileWriter writer = new FileWriter(infile, true);
			
			
			//Create a server socket with specific port,
			ServerSocket Ssocket = new ServerSocket(port);
		
			//Set ReuseAddress able to start server
			Ssocket.setReuseAddress(true);
			//declare the thread pool
			Executor executor = Executors.newFixedThreadPool(poolSize);
			FOOPServer.startFOOPServer(port); //start the FOOPServer
			//Thread pool
			while(true) {
				Socket sock = Ssocket.accept(); 
				sock.setReuseAddress(true);
				try {
					sock.setSoTimeout(MAXCOMMTIME);//Server set Time out
					executor.execute(new operator(sock, logger, 
							list, writer, infile));
				} catch (IOException e) {
					logger.warning("Sever has IO problem, Closed");
					Ssocket.close();
					FOOPServer.closeSerevr(); // close the FOOPServer
					break;
				}
			}
			
		} catch (NumberFormatException e) {
			//log waring if port or pool size is invalid
			logger.warning("port or pool size is invalid");
			
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.warning("Parameter(s): <Port> <Item file name> <Pool number>");
			
		} catch (BindException e) {
			logger.warning("Address already in use: bind");
		}
	}
	
	/**
	 * Read item from item file
	 * @param infile item file
	 * @param list list memory
	 * @param logger logger
	 * @param file file path
	 * @throws FileNotFoundException if file not found exception
	 */
	public static void readItem(File infile, ItemList list, Logger logger, String file) 
			throws FileNotFoundException {
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
				//if file corrupted
				logger.warning("item file corrupted");
				list = new ItemList(ZERO, ZERO);//declare new item list
				scan.close(); 
				if(infile.delete());//initialize new file 
				infile = new File(file);
			}
			
		}
	}
}


/**
 * Thread for communication with each client
 *
 */
class operator extends Thread{
	private static final long UNITMILLI = 60000L; //convert minus to millis
	private Logger logger;     //log server issue
	private Socket clntSock;   //client socket
	private ItemList list;     //item list
	private FileWriter fWriter;//File writer 
	private File file;         //item file
	/**
	 * Communication operation default constructor
	 * @param clntSock client socket
	 * @param logger log server issues
	 * @param list item list
	 * @param fWriter file write
	 * @param file item file
	 */
	public operator(Socket clntSock, Logger logger, 
			ItemList list, FileWriter fWriter, File file) {
		
		this.clntSock = clntSock;
		this.logger = logger;
		this.list = list;
		this.fWriter = fWriter;
		this.file = file;
	}
	
	/**
	 * Run method
	 */
	@Override
	public void run() {
		
		handle(this.clntSock, this.logger, this.list, 
				this.fWriter, this.file);
	}
	
	/**
	 * main part to deal with message received from client and send to client 
	 * @param clntSock client socket
	 * @param logger log server issues
	 * @param list item list
	 * @param fw filer writer
	 * @param file item file
	 */
	public static void handle(Socket clntSock, Logger logger, 
			ItemList list, FileWriter fw, File file) {
		//log info for new connection
		logger.info("New Connection "+ clntSock.getRemoteSocketAddress()+"-"+clntSock.getPort()
		+" with thread id "+currentThread().getId());
		
		try {
			while(true) {
				InputStream in = clntSock.getInputStream();
				OutputStream out = clntSock.getOutputStream();
		
				//Received Message from Client
				Message message = new Message() {};
				Instant instant = Instant.now();
				Long timestamp = instant.toEpochMilli();
				list.setTimestamp(timestamp);
				try {
					
					message = MessageFactory.decode(new MessageInput(in));
					logger.info("Received message from" + clntSock.getRemoteSocketAddress() 
				  				+ "-" + clntSock.getPort() + " " +message.toString());
					
				} catch (TokenizerException e) {
					
					//Network I/O blocks problem
					if("Read timed out".equals(e.getMessage())){
						//send error message to the client
						MessageFactory.encode(new Error(timestamp, "BAD IO"), 
								new MessageOutput(out));
						//log error message to fiat.log
						logger.warning("Bad IO problem with" + 
								clntSock.getRemoteSocketAddress() + "-" 
								+clntSock.getPort() + " with thread id "
								+Thread.currentThread().getId());
					}
							
					if(e.getMessage().contains("premature")) {
						logger.info("Close connection "+
								clntSock.getRemoteSocketAddress() + "-"
								+clntSock.getPort() + " with thread id "
								+Thread.currentThread().getId());
					}
					//Received the unexpected version
					if(e.getMessage().contains("Wrong protocol")) {
						String[] split = e.getMessage().split(" ");//get the version
						//send error message to the client
						MessageFactory.encode(new Error(timestamp, 
								"Unexpected version: <" 
						+ split[split.length-1]+">"), new MessageOutput(out));
						
						//log error message to fiat.log
						logger.warning("Unexpected Version Received" + 
								clntSock.getRemoteSocketAddress() + "-" 
								+clntSock.getPort() + " with thread id "
								+Thread.currentThread().getId());
					}
					
					//Received unknown request 
					if(e.getMessage().contains("Wrong req")) {
						//get the request 
						String[] split = e.getMessage().split(" ");
						//send message to client 
						MessageFactory.encode(new Error(timestamp, 
								"UnKnown operation: <" 
						+ split[split.length-1]+">"), new MessageOutput(out));
						//log error message to fiat.log
						logger.warning("UnKnown operation:" + 
								clntSock.getRemoteSocketAddress() + "-" 
								+clntSock.getPort() + " with thread id "
								+Thread.currentThread().getId());
					}
					
					//Problem parsing message, such as short packet
					if(e.getMessage().contains("as error is")) {
						//send message to client
						MessageFactory.encode(new Error(timestamp, 
								"Unable to parse message <" 
						+ e.getMessage()+">"), new MessageOutput(out));
						//log error message to fiat.log
						logger.warning("Unable to parse message" + 
								clntSock.getRemoteSocketAddress() + "-" 
								+clntSock.getPort() + " with thread id "
								+Thread.currentThread().getId());
					}
					//Problem with Message not end with ELON
					if(e.getMessage().contains("End not with ELON")) {
						//send message to client
						MessageFactory.encode(new Error(timestamp, 
								"Message <" 
						+ e.getMessage()+">"), new MessageOutput(out));
						//log error message to fiat.log
						logger.warning("End not with ELON" + 
								clntSock.getRemoteSocketAddress() + "-" 
								+clntSock.getPort() + " with thread id "
								+Thread.currentThread().getId());
					}
					
					clntSock.shutdownOutput(); 
					//wait 0.5 second for sending
					clntSock.wait(500);
					//close connection the client with server
					clntSock.close();
					
				}
				/*
				 * Handle Add request message 
				 */
				if("ADD".equals(message.getRequest())) {
					Add add = (Add) message;
					instant = Instant.now();
					long modified = instant.toEpochMilli();
					list.setTimestamp(add.getTimestamp());
					list.setModifiedTimestamp(modified);
					list.addItem(add.getItem()); 
					
					writeItem(fw, add, modified, logger); //record new item to item file
				
					logger.info("Received message from" + clntSock.getRemoteSocketAddress() 
			       	+ "-" + clntSock.getPort() +" "+ add.toString());
					MessageFactory.encode(list, new MessageOutput(out));//send message to client
					logger.info("Send message from" + clntSock.getRemoteSocketAddress() 
			       	+ "-" + clntSock.getPort() +" "+ list.toString() );
					
					
					/*
					 * handle Get request message
					 */
				}else if ("GET".equals(message.getRequest())) {
					//if item list size equal to zero
					if(list.getItemList().size() == 0) {
						list.setModifiedTimestamp(0L);//modified time stamp is 0
					}
					logger.info("Received message from" + clntSock.getRemoteSocketAddress() 
			       	+ "-" + clntSock.getPort() +" "+ message.toString());
					MessageFactory.encode(list, new MessageOutput(out));//send message to client
					logger.info("Send message from" + clntSock.getRemoteSocketAddress() 
			       	+ "-" + clntSock.getPort() +" "+ list.toString() );
					
					
					
					/*
					 * handle Interval request message
					 */
				}else if("INTERVAL".equals(message.getRequest())) {
					logger.info("Received message from" + clntSock.getRemoteSocketAddress() 
			       	+ "-" + clntSock.getPort() +" "+ message.toString());
					
					ItemList list2 = new ItemList(timestamp, 0);
					Interval inter = (Interval)message;
					
					handleInterval(inter.getIntervalTime(), list2, file);//deal with items within interval time
			
					MessageFactory.encode(list2, new MessageOutput(out)); //send item list to 
					logger.info("Send message from" + clntSock.getRemoteSocketAddress() 
			       	+ "-" + clntSock.getPort() +" "+ list2.toString() );
					
				}else {
					//send message if client send unexpected message type
					logger.warning("Received message from" + clntSock.getRemoteSocketAddress() 
			       	+ "-" + clntSock.getPort() +" Unexpected message type");
					MessageFactory.encode(new Error(instant.toEpochMilli(),  
							"Unexpected message type: <" + message.getRequest() + ">"), 
							new MessageOutput(out));
					logger.warning("Send message from" + clntSock.getRemoteSocketAddress() 
			       	+ "-" + clntSock.getPort() +" "+ list.toString() );
				} 
			}
		} catch (Exception e) {}
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
		FOOPServer.handleAddition(add);
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
	
}

