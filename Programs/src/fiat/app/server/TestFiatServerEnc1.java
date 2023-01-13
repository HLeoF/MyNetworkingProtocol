package fiat.app.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Run basic testing of fiat server
 */
public class TestFiatServerEnc1 {

	/**
	 * Character encoding
	 */
	private static final Charset ENC = StandardCharsets.UTF_8;
	/**
	 * Intercharacter delay for sending
	 */
	private static final int SLOWDELAYMS = 10;
	/**
	 * Socket for communication with server
	 */
	protected static Socket clientSocket;
	protected static BufferedReader r;
	/**
	 * Identity of server
	 */
	protected static String server;
	/**
	 * Port of server
	 */
	protected static int port;
	/*
	 * Delay - True if delay between bytes; false if all at once
	 */
	protected static boolean delay = false;

	/**
	 * Method to run before use of client socket
	 * 
	 * @throws Exception if IO, etc. problem
	 */
	protected static void before() throws Exception {
		clientSocket = new Socket(server, port);
		r = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), ENC));
	}

	/**
	 * Method to run after use of client socket
	 */
	protected static void after() {
		try {
			clientSocket.close();
		} catch (IOException e) {
			// Ignore because there's nothing we can/would do
		}
	}

	/**
	 * Print header for test
	 * 
	 * @param testName name of test
	 */
	protected static synchronized void printTest(String testName) {
		System.err.println("***************************");
		System.err.println(testName);
		System.err.println("***************************");
	}

	/**
	 * Send message specified by string with delay over given socket
	 * 
	 * @param msg message to send
	 * 
	 * @throws Exception if problem occurs
	 */
	protected static synchronized void sendSlowly(String msg) throws Exception {
		for (byte b : msg.getBytes(ENC)) {
			clientSocket.getOutputStream().write(b);
			TimeUnit.MILLISECONDS.sleep(SLOWDELAYMS);
		}
    System.out.println("=" + r.readLine());
	}

	/**
	 * Send message (entirely) specified by string over given socket
	 * 
	 * @param msg message to send
	 * 
	 * @throws Exception if problem occurs
	 */
	protected static synchronized void send(String msg) throws Exception {
	  if (delay) {
	    for (byte b : msg.getBytes(ENC)) {
	      clientSocket.getOutputStream().write(b);
	      Thread.sleep(5);
	    }
	  } else {
	    clientSocket.getOutputStream().write(msg.getBytes(ENC));
	  }
    System.out.println("=" + r.readLine());
	}

	/**
	 * @param msg
	 */
	protected static synchronized void printExpected(String msg) {
		System.err.println(">" + msg);
	}

	protected static void parameters(String[] args) {
		// Test and assign server parameters
		if (args.length < 2 || args.length > 3) {
			throw new IllegalArgumentException("Parameter(s): <server> <port> [-d]");
		}
		server = args[0];
		port = Integer.parseInt(args[1]);
		if (args.length == 3 && "-d".equals(args[2])) {
		  delay = true;
		}
	}
	
	public static void main(String[] args) throws Exception {
		parameters(args);
		
		/* 
		 * EMPTY GET
		 */
    printTest("Empty GET");

    before();
    sendSlowly("FT1.0 5 GET \n");
    printExpected("FT1.0 %d LIST 0 0 ".formatted(System.currentTimeMillis()));

    /*
     * ADDs
     */
    printTest("ADDs");
    
    send("FT1.0 5 ADD 3 xyzD3 4.5 \n");
    printExpected("FT1.0 %d LIST #1            1 3 xyzD3 4.5 - #1 <= TS".formatted(System.currentTimeMillis()));

    send("FT1.0 5 ADD 3 1 ⸠D40 0.5 \n");
    printExpected("FT1.0 %d LIST #2            2 3 xyzD3 4.5 3 1 ⸠D40 0.5 - #2 <= TS".formatted(System.currentTimeMillis()));
    after();
    System.out.println("Restart student server.  Hit return to continue");
    System.in.read();
    
    /*
     * Persistence
     */
    printTest("Persistence");

    before();
    send("FT1.0 5 GET \n");
    printExpected("FT1.0 %d LIST #3            2 3 xyzD3 4.5 3 1 ⸠D40 0.5 - #3 == #2".formatted(System.currentTimeMillis()));
    
    send("FT1.0 5 ADD 1 8S0 4.8 \n");
    printExpected("FT1.0 %d LIST #4            2 3 xyzD3 4.5 3 1 ⸠D40 0.5 1 8S0 4.8 - #4 <= TS".formatted(System.currentTimeMillis()));
    after();
    
    /*
     * Unexpected Message Type
     */
    printTest("Unexpected message type");

    before();
    send("FT1.0 5 ERROR 1 x\n");
    printExpected("FT1.0 5 ERROR XX Unexpected message type");
    
    send("FT1.0 5 GET \n");
    printExpected("FT1.0 %d LIST #5            2 3 xyzD3 4.5 3 1 ⸠D40 0.5 1 8S0 4.8 - #5 == #4".formatted(System.currentTimeMillis()));
    after();
    
    /*
     * Unexpected version
     */
    printTest("Unexpected message type");

    before();
    send("FT1.01 5 GET \n");
    printExpected("FT1.0 %d ERROR XX Unexpected version".formatted(System.currentTimeMillis()));
    after();
	}
}
