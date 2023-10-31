package foop.app.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Scanner;

import foop.serialization.Message;
import foop.serialization.MessageFactory;

/**
 * Multicast FOOP Client
 * @author Maiqi Hou
 * @version 1.2 fix the receive error message problem
 */
public class MulticastClient {
	
	private static final int ONE = 1;            //The constant value of 1 and the second parameter
	private static final int ZERO = 0;			 //The constant value of 0 and the first parameter
	private static final int NUMPARA = 2;        //The number of parameter
	private static final int MAXSIZEBUFF = 65510;//The max size of packet buffer
	private static boolean end = false;			 //Determine whether end FOOP Client Server
	private static boolean termin = false;
	/**
	 * Main start the FOOP client 
	 * @param args multicast address and port
	 * @throws IOException If IO problem when connect server or join multicast group
	 */
	public static void main(String[] args) throws IOException{
		try {
			//check whether parameter invalid
			if(args.length != NUMPARA)
				throw new IllegalArgumentException("Parameter");
			
			InetSocketAddress address = new InetSocketAddress(args[ZERO], ZERO);
			//check whether address is a multicast address
			if(!address.getAddress().isMulticastAddress())
				throw new IllegalArgumentException("invalid Multicast Address");
			
			int port = Integer.parseUnsignedInt(args[ONE]);
			
			//create the multicast socket
			MulticastSocket socket = new MulticastSocket(port);
			socket.joinGroup(address, null);//join to the multicast group
			
			MainCommunication(socket, address);//enter main communication
			
		} catch (IllegalArgumentException e) {
			//if invalid multicast address
			if(e.getMessage().contains("invalid Multicast Address")) {
				System.err.println("Unable to communicate: <Invalid Multicast Address>");
			}else {
				System.err.println("Unable to communicate: <Parameter(s): <Multicast Address> [Port]>");
			}
		}catch (UnknownHostException e) {
			System.err.println("Unable to communication: <Unkonw Host/IP address>");
		}
	}
	
	/**
	 * Monitoring user whether enter quit for quit the client
	 * @param socket socket connect the client and server
	 * @param address the client's address
	 */
	public static void monitorQUIT(MulticastSocket socket, InetSocketAddress address) {
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
			if(!termin) {
				try {
					socket.leaveGroup(address, null);
				} catch (IOException e) {
					//if IO problem between client and server
					System.err.println("Unable to communicate: <Client leave Group has problem>");
				}
				socket.close();
			}
		});
		t.start(); //quit monitor thread start
	}

	/**
	 * Main communication part to receive the message from the Server
	 * @param socket multicast socket
	 * @param address multicast address
	 * @throws IOException if IO problem during client communicate with the Server
	 */
	public static void MainCommunication(MulticastSocket socket, InetSocketAddress address) 
			throws IOException {
		System.out.println("......Welcome The FOOP Client System......");
		System.out.println(".........Starting Receive Message.........");
		System.out.println("Type 'quit' for quit FOOPClient\n");
		byte[] buffer = new byte[MAXSIZEBUFF];
		DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
		monitorQUIT(socket, address);
		end = false;
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
					termin = true;
					break;
				}
				//if decode message has some problems
			} catch (IllegalArgumentException e) {
				if(end == false) {
					//print the error message
					termin = true; 
					System.err.println("Unable to parse message: <" + e.getMessage()+ ">");
					break;
				}
			}
		}
		//if receive other type of message termination
		if(termin == true) {
			System.out.println("Due to FOOPClient receive unexpected message"
		+"or unable to parse message PLEASE ENTER QUIT");
			socket.leaveGroup(address, null);
			socket.close();
		}
	}
}
