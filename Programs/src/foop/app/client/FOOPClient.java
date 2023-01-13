/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 5
* Class: CSI 4321
*
************************************************/
package foop.app.client;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import foop.serialization.Message;
import foop.serialization.MessageFactory;
import foop.serialization.Register;
import foop.utility.AddressUtility;

/**
 * UDP Foop Client 
 * @author Maiqi Hou
 * @version 1.2
 * Fix the receive "Unable to parse message" message 
 * cannot continues receive addition message problems
 */
public class FOOPClient {
	private static final int NUMPARA     = 2;    //The number of parameter can accepted
	private static final int MAXMSGID    = 255;  //The MAX Message ID 
	private static final int MAXSIZEBUFF = 65510;//The max size of packet buffer 
	private static final long WAITTIME   = 3000L;//The time wait for receive correct ACK
	private static final long DELTIME    = 0L;   //delayed 0 second to start the timer
	private static int id = 0;					 //The message ID
	private static int count = 0;				 //The number of waiting correct ACK
	private static boolean end = false;			 //Determine whether receive a correct ACK
	/**
	 * Maximum timer starting
	 */
	protected static final int TWO = 2;      
	
	/**
	 * Start up with generate a register information and perpare send to server
	 * @param socket connect client and server
	 * @return
	 * bytes of register information
	 * @throws SocketException if problems create a socket 
	 */
	public static byte[] registerInfo(DatagramSocket socket) throws SocketException {
		Random random = new Random();
		id = random.nextInt(MAXMSGID);//generate random MSG ID
		Inet4Address address = (Inet4Address) AddressUtility.getAddress(); //get the client local address
		int port = socket.getLocalPort(); //get the client local port
		Register register = new Register(id,address, port); //declare a register information
		return MessageFactory.encode(register); // encoding get the bytes of register information
	}
		
	/**
	 * Start up stage for receive ACK with correct MSG ID and handle other message
	 * @param buff the buffer receive from the UDP server
	 * @param timer Receiving ACK timer
	 * @param socket connect client and server
	 */
	public static void startPrint(byte[] buff, Timer timer, DatagramSocket socket) {
		try {
			Message message = MessageFactory.decode(buff); //decode the receive message
			String temp = message.toString();// to string the decode message
			
			if(temp.contains("ACK: ")) { //if receive message is ACK
				if( id == message.getMsgID()) { // defined whether has same message ID 
					end = true; //start up loop
					timer.cancel(); //timer cancel
					//print received correct ACK
					System.out.println("Received correct ACK...........YEE!");
					MainCommunication(socket);//move to main communication stage
				}else {
					//receive ACK without correct ACK
					System.err.println("Unexpected MSG ID"); 
					end = true;
				}
				
				//if receive message Addition or Error, print them to console
			}else if(temp.contains("Addition: ") || (temp.contains("Error: "))) {
				System.out.println(temp);
				end = true;
			}else {
				//if ceive message is not ACK, Addition, or Error, print error message
				System.err.println("Unexpected Message Type");
				end = true;
			}
		} catch (IllegalArgumentException e) {
			//if decode message has problem, print the error message
			System.err.println("Unable to parse message: <" + e.getMessage()+ ">");
		}
	}
	
	/**
	 * Start up beginning stage, send the register message 
	 * and set timer receive correct ACK
	 * @param socket socket connect the client and server
	 * @param port the server port
	 * @param address the server address
	 * @throws SocketException if problems create a socket 
	 */
	public static void startUP(DatagramSocket socket, int port, Inet4Address address) 
			throws SocketException{
		byte [] data = registerInfo(socket);
		
		Timer timer = new Timer(); //declare timer
		//declare the timer task
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				try {
					
					end = false;
					//if timer run two times, client unable to register
					if(count == TWO) {
						throw new IllegalArgumentException();
					}
					
					DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
					socket.send(packet);
					
					//handle messages and receive ACK during 3 seconds
					while(!end) {
						byte[] buffer = new byte[MAXSIZEBUFF];
						packet = new DatagramPacket(buffer,buffer.length);
						socket.receive(packet); //receive message from the Server
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						out.write(packet.getData(), packet.getOffset(), packet.getLength());
						buffer = out.toByteArray();
						//processing message in the startPrint() funcation
						startPrint(buffer, timer, socket);
					}
					count++;//timer running counter
					
				} catch (IOException e) {//if have some IO problem during communicating
					timer.cancel();
					System.err.println("Unable to communicate: <IO problem>");
					socket.close();
					
				} catch (IllegalArgumentException e) {
					timer.cancel();
					System.err.println("Unable to register");
					socket.close();
				}
			}
		};
		timer.schedule(task,DELTIME,WAITTIME); // timer start
	}
	
	/**
	 * Monitoring user whether enter quit for quit the client
	 * @param socket socket connect the client and server
	 */
	public static void monitorQUIT(DatagramSocket socket) {
		Scanner scan = new Scanner(System.in); // receive system input
		Thread t = new Thread(()->{
			boolean quit = false;
			while(!quit) {
				//if user enter 'quit', the client will terminated
				if("quit".equals(scan.nextLine())) { 
					System.out.println("\n.........Exit the FOOPClient Bye..........");
					end = true;
					quit = true;//break while loop
				}
			}
			scan.close(); //close scanner 
			socket.close(); //close socket
		});
		t.start(); //quit monitor thread start
	}
	
	
	/**
	 * Main Communication after the start up
	 * @param socket socket connect the client and server
	 */
	public static void MainCommunication(DatagramSocket socket) {
		System.out.println(".........Starting Receive Message.........");
		System.out.println("Type 'quit' for quit FOOPClient\n");
		byte[] buffer = new byte[MAXSIZEBUFF];
		DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
		end = false;
		monitorQUIT(socket); //start thread to monitoring user whether enter quit
		
		while(end == false) {
			try {
				//receive message from the server
				socket.receive(packet);
			} catch (IOException e) {
				if(end == false) {
					//if IO problem between client and server
					System.err.println("Unable to communicate: <IO problem>");
				}
				break;
			}
			//analysis message type(Addition, ERROR, other message type)
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			out.write(packet.getData(), packet.getOffset(), packet.getLength());
			buffer = out.toByteArray();
			try {
				Message message = MessageFactory.decode(buffer);
				String temp = message.toString();
				//if the message type is Addition or Error, print the message 
				if(temp.contains("Addition: ") || (temp.contains("Error: "))) {
					System.out.println(temp);
				}else {
					//if the message type is other, print the error message
					System.err.println("Unexpected Message Type");
				}
				//if decode message has some problems
			} catch (IllegalArgumentException e) {
				if(end == false) {
					//print the error message
					System.err.println("Unable to parse message: <" + e.getMessage()+ ">");
				}
			}
		}
		socket.close();//close socket between client and server
	}
	
	/**
	 * Prepare connecting with the client and the Server
	 * @param args parameter for server address and server port
	 */
	public static void main(String[] args){
		try {
			if(args.length != NUMPARA)
				throw new IllegalArgumentException();
			Inet4Address address = (Inet4Address) Inet4Address.getByName(args[0]); //get the server address
			int port = Integer.parseUnsignedInt(args[1]); //get the server port
			DatagramSocket socket = new DatagramSocket();
			startUP(socket, port, address);	
		
			//if the host address unknown print the error message
		} catch (UnknownHostException e) {
			System.err.println("Unable to communicate: <Unkown Host/IP address>");
			//print error message, if parameter number incorrect.
		} catch (IllegalArgumentException e) { 
			System.err.println("Unable to communicate: <Parameter(s): <Server> [<Port>]>");
		} catch (SocketException e) {
			System.err.println("Unable to communicate: <Broken Pipe>");
		}
			
	}
}
