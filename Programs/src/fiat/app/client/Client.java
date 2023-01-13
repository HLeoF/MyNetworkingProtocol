/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 2
* Class: CSI 4321
*
************************************************/
package fiat.app.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Scanner;

import fiat.serialization.Add;
import fiat.serialization.Error;
import fiat.serialization.Get;
import fiat.serialization.Interval;
import fiat.serialization.Item;
import fiat.serialization.ItemList;
import fiat.serialization.MealType;
import fiat.serialization.Message;
import fiat.serialization.MessageFactory;
import fiat.serialization.MessageInput;
import fiat.serialization.MessageOutput;
import fiat.serialization.TokenizerException;

/**
 * TCP Fiat client 
 * @author Maiqi Hou
 * @version 1.2
 * update Event follows instruction
 * Fix GET meal type problem in ADD operation
 */
public class Client {
	private static final int FOUR   = 4;    //enter ADD command lien information
	private static final int MINPAR = 2;    //the parameter number
	private static final int MAXPAR = 3;    //limit of parameter number
	private static final int TIMEOUT= 30000;//socket connect time out
	private static final int MININT = 0;    //minimum interval time
	private static final int MAXINT = 2048; //maximum interval time
	private static Scanner scan = new Scanner(System.in); //read command line from system
	private static String line  = ""; //get the command line
	
	//declare a command string array for store command 
	private static String[] cod = {"Request (ADD|GET|INTERVAL): ","Name: ", 
							"Meal type (B, L, D, S): ", "Calories: ",
							"Fat: ", "Continue (y/n): ", "Time:"};
	//declare a error message string array for show error message
	private static String[] err = {"Unknown request", 
			"name cannot be null, empty, too large, or contain illegal characters",
			"illegal code", "calories has illegal format or is null",
			"fat has illegal format or is null", "Time has illegal format or in null"};
	
	
	/**
	 * Check Message Request
	 * @param req Message request
	 * @return if message request valid return true
	 *         if message request invalid return false
	 */
	public static boolean checkRequest(String req) {
		return ("ADD".equals(req) || "GET".equals(req) 
				|| "ERROR".equals(req) || "LIST".equals(req)
				||"INTERVAL".equals(req));
	}
	
	/**
	 * operate Add message command lines
	 * @param item Item
	 */
	public static void commandwithADD(Item item) {
		for(int i = 1; i <= FOUR; i++) {
			while(true) {
				System.out.print(cod[i]);
				line = scan.nextLine();
				try {
					switch (i) {
					case 1: //get item name
						item.setName(line);
						break;
					case 2: //get the meal type
						if(line.length() != 1) {//check meal type whether valid
							throw new Exception();
						}
						MealType mealType = MealType.getMealType(line.charAt(0));
						item.setMealType(mealType);
						break;
					case 3: //get the calories
						int cal = Integer.parseUnsignedInt(line);
						item.setCalories(cal);
						break;
					case 4: //get the fat
						//check double format valid
						if(!line.contains(".")) {
							throw new Exception();
						}
						double fat = Double.parseDouble(line);
						item.setFat(fat);
						break;
					}
					break;
				} catch (Exception e) {
					//if set item has some problem 
					//show error message
					System.err.println(err[i]);
				}
			}
		}
	}
	
	/**
	 * command with Interval 
	 * @return the interval time
	 */
	public static int commandwithINTERVAL() {
		while(true) {
			System.out.print(cod[6]);
			line = scan.nextLine();
			
			try {
				int time = Integer.parseUnsignedInt(line);
				if(time < MININT || time > MAXINT) {
					throw new Exception();
				}
				return time;
			} catch (Exception e) {
				//if set item has some problem 
				//show error message
				System.err.println(err[5]);
			}
		}
	}
	
	/**
	 * Main TCP client operation
	 * @param out socket output stream
	 * @param in socket input stream
	 * @throws IOException I/O Problem
	 * @throws TokenizerException if validation failure
	 */
	public static void communicate(OutputStream out, InputStream in) 
			throws IOException, TokenizerException {
		MessageInput input = new MessageInput(in);
		MessageOutput output = new MessageOutput(out);
		System.out.println("------Welcome Fiat--------");
		
		
		while(true) {
			//get the Request part
			while(true) {
				System.out.print(cod[0]);
				line = scan.nextLine();
				//check request whether valid
				if(checkRequest(line))
					break;
				System.err.println(err[0]);
			}
			
			//get the time stamp
			Instant instant = Instant.now();
			long time = instant.toEpochMilli();

			switch (line) {
				case "ADD"://if request is ADD
					Item item = new Item();
					//processing enter item information from command line
					commandwithADD(item); 
					//send Add Message to server
					MessageFactory.encode(new Add(time, item), output);		
					break;
				case "GET"://if request is GET
					//Send Get Message to server
					MessageFactory.encode(new Get(time), output);
					break;
				case "ERROR":// if request is ERROR
					MessageFactory.encode(new Error(time, "Error"), output);
					break;
				case "LIST": // if request is LIST
					MessageFactory.encode(new ItemList(time, time), output);
					break;
				case "INTERVAL":
					int interVal = commandwithINTERVAL();//processing enter interval time
					//send INTERVAL Message to server
					MessageFactory.encode(new Interval(time, interVal), output);
					break;
			}
			
			try {
				//Receive Message from server
				Message message = MessageFactory.decode(input);
				
				//if received request is not LIST
				if(!"LIST".equals(message.getRequest())) {
					//then check whether received request is ERROR
					if("ERROR".equals(message.getRequest())) {
						//Print Error message, which received from Server
						String warining = "Error: <" + message.toString() + ">";
						System.err.println(warining);
					}else {
						//if received message is not ItemLits or Error reuqest
						//Print Error message, which Unexpected message
						String warining = "Unexpected message: <"
								+ message.toString()+">";
						System.err.println(warining);
					}
					
				}else {
					//print the received message from the Server
					System.out.println(message.toString());
				}
					
			} catch (Exception e) { //Received message with invalid message filed.
				String invalid = "Invalid message: <"+e.getMessage()+">";
				System.err.println(invalid);
				break;
			}
			
			//check whether user want to continue.
			while(true) {
				System.out.print(cod[5]);
				line = scan.nextLine();
				if("y".equals(line) || "n".equals(line))
					break;
			}
			
			//if user does not continue to enter, finished communication
			if("n".equals(line))
				break;
		}
	}
	
	
	/**
	 * Main connection part 
	 * @param args for server and port number
	 * @throws IOException I/O problem
	 * @throws TokenizerException if validation failure
	 */
	public static void main(String[] args) 
			throws IOException, TokenizerException{
		try {
			
			//if invalid parameter numbers
			if (args.length < MINPAR || (args.length >= MAXPAR)) 
				 throw new IllegalArgumentException();
				
			String server = args[0]; //get the remote host
			int servePort = Integer.parseUnsignedInt(args[1]);//get serve Port
			Socket socket = new Socket(server, servePort);//create new socket
			socket.setSoTimeout(TIMEOUT); //set socket time out
			System.out.println("Connected to the Fiat Server");
			
			InputStream in = socket.getInputStream(); //declare input to reading message form serve
			OutputStream out = socket.getOutputStream();//declare output to sending message to serve
			//into main communicate part to handle console information
			communicate(out, in); 
			
			//check socket whether connected and whether close
			if(socket.isConnected() && !socket.isClosed()) {
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();//then close the socket
			}
			
			System.out.println("Disconnted with Fiat Server, Bye!");
			
		}catch (UnknownHostException e) {//print error message when Unknown remote IP address
			System.err.println("Unable to communicate: <Unkown Host/IP address>");
		} catch (NumberFormatException e) {//print error message when Port is invalid number
			System.err.println("Unable to communicate: <Invalid Port>");
		} catch (ConnectException e) {//print error message when Client cannot connected with Serve
			System.err.println("Unable to communicate: <Connection refused>");
		} catch (SocketException e) {//print error message communicate with Serve timeout, I/O problem
			System.err.println("Unable to communicate: <Broken Pipe>");
		} catch (IllegalArgumentException e) { //print error message, if parameter number incorrect.
			System.err.println("Unable to communicate: <Parameter(s): <Server> [<Port>]>");
		}
		
	}
	
}
