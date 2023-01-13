/************************************************
*
* Author: Maiqi Hou
* Assignment: Program 6
* Class: CSI 4321
*
************************************************/
package foop.app.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import fiat.serialization.Add;
import fiat.serialization.Item;
import foop.serialization.ACK;
import foop.serialization.Addition;
import foop.serialization.Error;
import foop.serialization.Message;
import foop.serialization.MessageFactory;
import foop.serialization.Register;

/**
 * UDP FOOP Server
 * @author Maiqi Hou
 * @version 1.2
 * add the addition message handler 
 * add some handle problem parsing message and problem communication
 * fix the Incorrect Register Port packet sent problem
 */
public class FOOPServer {
	private static int port = 0;	     //declare the initial port
	private static byte[] buffer;	     //declare the initial receive and send buffer
	private static DatagramPacket packet;//declare the datagram packet
	private static DatagramSocket socket;//declare the datagram socket
	private static final int ONE = 1;    //message code 1 for addition 
	private static final int TWO = 2;    //message code 2 for error 
	private static final int THREE = 3;  //message code 3 for ACK
	private static final int MINMSGID = 0;  //declare the Minimum Message ID
	private static final int MAXMSGID = 255;//declare the Maximum Message ID 
	private static final int MAXBUFFERSIZE = 65510;//the buffer maximum length
	private static List<Map<Inet4Address, Integer>> list; //the list store the register address and port
	private static Logger logger = Logger.getLogger("FOOPlog");//declare the initial logger
	
	
	/**
	 * Start the Foop Server 
	 * @param p the port of Fiat server
	 */
	public static void startFOOPServer(int p) {
		try {
			port = p;
			socket = new DatagramSocket(port); 
			list = new ArrayList<>();//register clients list is array list
			//initial logger 
			FileHandler file = new FileHandler("foop.log",true);
			//making logging information does not show on the console
			logger.setUseParentHandlers(false);
			logger.setLevel(Level.ALL);
			//log file formatter as simple formatter
			file.setFormatter(new SimpleFormatter());
			logger.addHandler(file);
			logger.info("FOOP Server Starting.....");
			
			receiveRegister();
		} catch (Exception e) {
			logger.warning("Unable to communicate: <IO problem>");
		}
	}
	
	/**
	 * Handle the receive register message from client
	 * @throws IOException IO problem when receiving message 
	 */
	public static void receiveRegister() throws IOException {
		Thread thread = new Thread(()->{
			while(true) {
				try {
					buffer = new byte[MAXBUFFERSIZE];
					packet = new DatagramPacket(buffer,buffer.length);
					socket.receive(packet);//receive the message form the clients
				} catch (IOException e) {
					logger.warning("Unable to communicate: <IO problem>");
				}
				handleRegisterInfo(packet, buffer);
			}
		});
		thread.start();
	}
	
	/**
	 * Handle the addition item and send back to All register clients
	 * @param add for addition item
	 */
	public static void handleAddition(Add add) {
		Random random = new Random();
		int id = random.nextInt(MAXMSGID);//generate random MSG ID
		Item item = add.getItem();//get the addition item
		Addition addition = new Addition(id, item.getName(), item.getMealType(), item.getCalories());
		//Send addition message to all registered clients
		for(Map<Inet4Address, Integer> map : list) {
			for(Entry<Inet4Address, Integer> entry : map.entrySet()) {
				byte[] b = new byte[MAXBUFFERSIZE];
				b = MessageFactory.encode(addition);
				DatagramPacket pkt = new DatagramPacket(b, b.length, 
						entry.getKey(), entry.getValue());
				pkt.setData(b);
				try {
					socket.send(pkt);
					logger.info("An item has been added and told to all registered clients.");
				} catch (IOException e) {
					logger.warning("Unable to communicate: <IO problem>");
				}
			}
		}
		
	}
	
	/**
	 * Handle register information from  clients
	 * @param packet received and send datagram packet
	 * @param buffer received and send buffer
	 */
	public static void handleRegisterInfo(DatagramPacket packet, byte[] buffer) {
	
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			out.write(packet.getData(), packet.getOffset(), packet.getLength());
			buffer = out.toByteArray();
			Message message = MessageFactory.decode(buffer);
			String temp = message.toString();
			
			//if message is not register message
			if(!temp.contains("Register: ")) {
				if(temp.contains("ACK: " ) || temp.contains("Addition: ")
						|| temp.contains("Error: ")) {
					int p = message.getMsgID();
					receiveOtherMessage(temp, p, packet);//send error message to clients
				}
				
				
			}else {
				//if message is received register message
				Register register = (Register) message;
				temp = register.toString();
				logger.info(temp);
				
				//Check register message whether has a correct port
				if(register.getPort() != packet.getPort()) {
					buffer = new byte[MAXBUFFERSIZE];
					//if register message with incorrect port, send error message
					Error error = new Error(register.getMsgID(), "Incorrect port: <" + register.getPort()+">");
					buffer = MessageFactory.encode(error);
					
					DatagramPacket packet1 = new DatagramPacket(buffer, buffer.length, 
							register.getAddress(), packet.getPort());
					packet1.setData(buffer);
					socket.send(packet1);
					
					//log warning message
					logger.warning("Client MSG ID: "+ register.getMsgID()
					+" IP: " + register.getAddress() + " port: " 
					+ register.getPort() + " Incorrect port");
				}
				
				//Check client register information whether already registered.
				if(alreadyRegister(register)) {
	
					//if the client register information has not registered
					buffer = new byte[MAXBUFFERSIZE];
					ACK ack  = new ACK(register.getMsgID());
					buffer = MessageFactory.encode(ack);
					//add client address and port to the registered list
					Map<Inet4Address, Integer> elements = new HashMap<>();
					elements.put(register.getAddress(), register.getPort());
					list.add(elements);
					
					//send the message back the client
					DatagramPacket sendpacket = new DatagramPacket(buffer, buffer.length, 
							register.getAddress(), packet.getPort());
					sendpacket.setData(buffer);
					socket.send(sendpacket);
					logger.info("Message has sent to client");
					
					//log the information
					logger.info("Client MSG ID: "+ register.getMsgID()
							+" IP: " + register.getAddress() + " port: " 
							+ register.getPort() + " Successfully Registered");
				} else {
					//if the client register information has already registered
					buffer = new byte[MAXBUFFERSIZE];
					Error error = new Error(register.getMsgID(), "Already registered");
					buffer = MessageFactory.encode(error);
					
					//log warning message
					logger.warning("Client MSG ID: "+ register.getMsgID()
							+" IP: " + register.getAddress() + " port: " 
							+ register.getPort() + " Successfully Registered");	
					//send the message back the client
					
					DatagramPacket packet2 = new DatagramPacket(buffer, buffer.length, 
							register.getAddress(), packet.getPort());
					packet2.setData(buffer);
					socket.send(packet2);
					logger.info("Message has sent to client");
				}
			}
		} catch (IOException e) {
			logger.warning("Unable to communicate: <IO problem>");
			
		} catch (IllegalArgumentException e) {
			try {
				Error error = new Error(MINMSGID, " ");
				String eMsg = "";
				buffer = new byte[MAXBUFFERSIZE];
				//if Received unknown CODE, send the error message to Clients
				if(e.getMessage().contains("Invalid Foop code:")) {
					String [] s = e.getMessage().split(" ");
					eMsg = "Unknow code: <" + s[THREE] +">";
					error.setMessage(eMsg);
					
					//log the warning to the log file
					logger.warning("The FOOPServer recevied Unknown code: " 
					+ s[THREE]);
					
				}else {
					//if Has problem parsing message, send the error message to client 
					eMsg = "Unable to parse message: <"+ e.getMessage()+">";
					error.setMessage(eMsg);
					//log the warning to the log file
					logger.warning("The FOOPSerevr unable to parse received message: " 
					+ e.getMessage());
				}
				buffer = MessageFactory.encode(error);
				packet.setData(buffer);
				socket.send(packet);
			} catch (IOException e1) {
				logger.warning("Unable to communicate: <IO problem>");
			}
			
		}
	}
	
	/**
	 * check whether client address and port registered.
	 * @param register received register information from the client
	 * @return if not registered return true, if already registered return false
	 */
	public static boolean alreadyRegister(Register register) {
		Inet4Address address = register.getAddress();
		int port = register.getPort();
		//check register list
		for(Map<Inet4Address, Integer> map: list) {
			if(map.containsKey(address)) {
				if(map.containsValue(port)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static void sendPacket() {
		
	}
	/**
	 * Handle receive any other FOOP message
	 * @param temp received message
	 * @param id the message code
	 * @param packet the datagram packet want to send
	 * @throws IOException if IO problem when receiving or sending message
	 */
	public static void receiveOtherMessage(String temp, int id, DatagramPacket packet) 
			throws IOException{
		try {
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		int code = ONE;
		if(temp.contains("ACK: ")) 
			code = THREE;
		if(temp.contains("Error: "))
			code = TWO;
		
		//send error message to client
		buffer = new byte[MAXBUFFERSIZE];
		String eMsg = "Unexpected message type: <" + code +">";
		Error error = new Error(id, eMsg);
		buffer = MessageFactory.encode(error);
		packet.setData(buffer);
		socket.send(packet);
		
		logger.warning("Received unexpected message type: " + code);
	}
	
	/**
	 * Close the FOOP Server
	 */
	public static void closeSerevr() {
		socket.close();
	}
}
