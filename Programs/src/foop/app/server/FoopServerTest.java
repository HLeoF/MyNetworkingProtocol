package foop.app.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Run basic testing of server
 */
public class FoopServerTest {

	/**
	 * Character encoding
	 */
  private static final Charset ENC = StandardCharsets.UTF_8;

	/**
	 * Maximum datagram size
	 */
	private static final int MAXDG = 65507;

	/**
	 * Socket for communication with fiat server
	 */
	protected static Socket fiatSocket;
	/**
	 * Socket for communication with foop server
	 */
	protected static DatagramSocket foopSocket;
	/**
	 * Identity of server
	 */
	protected static String server;
	/**
	 * Port of fiat/foop server
	 */
	protected static int port;

	/**
	 * Setup Fiat and Foop sockets
	 * 
	 * @throws UnknownHostException if specified host unknown
	 * @throws IOException if I/O problem
	 */
	private static void setup() throws UnknownHostException, IOException {
		fiatSocket = new Socket(server, port);
		foopSocket = new DatagramSocket();
		foopSocket.connect(new InetSocketAddress(server, port));
	}

	private static void teardown() throws IOException {
		fiatSocket.close();
		foopSocket.close();
	}

	/**
	 * Print header for test
	 * 
	 * @param testName name of test
	 */
	protected static synchronized void printTest(String testName) {
		System.out.println("***************************");
		System.out.println(testName);
		System.out.println("***************************");
	}

	/**
	 * Send message (entirely) specified by string over given socket
	 * 
	 * @param clientSocket socket to send
	 * @param msg          message to send
	 * 
	 * @throws Exception if problem occurs
	 */
	protected static synchronized void send(Socket clientSocket, String msg) throws Exception {
		clientSocket.getOutputStream().write(msg.getBytes(ENC));
	}

	/**
	 * Swap endian of byte array
	 * 
	 * @param bytes bytes to swap
	 * @return swapped byte array
	 */
	private static byte[] swapEndian(final byte[] bytes) {
		byte[] swpBytes = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			swpBytes[bytes.length - i - 1] = bytes[i];
		}

		return swpBytes;
	}

	/**
	 * Encode a register message for given socket
	 * 
	 * @param s socket on which register message will be sent
	 * @param start first few bytes of register message
	 * 
	 * @return bytes of register message
	 * @throws IOException if I/O problem
	 */
	private static byte[] encodeRegister(DatagramSocket s, byte[] start) throws IOException {
		// Create byte sink and write start bytes
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		bout.write(start);
		
		// Write properly encoded register address and port
		Inet4Address clntAddr = (Inet4Address) fiatSocket.getLocalAddress();
		int clntPort = s.getLocalPort();
		bout.write(swapEndian(clntAddr.getAddress()));
		byte[] portBuf = new byte[] { (byte) ((clntPort >> 8) & 0xFF), (byte) (clntPort & 0xFF) };
		bout.write(swapEndian(portBuf));

		return bout.toByteArray();
	}

	/**
	 * Convert byte array to hex string
	 * From https://www.baeldung.com/java-byte-arrays-hex-strings
	 * 
	 * @param bytes array of bytes to convert
	 * @return hex string
	 */
	public static String bytesToHexString(byte[] bytes) {
		BigInteger bigInteger = new BigInteger(1, bytes);
		return String.format("%0" + (bytes.length << 1) + "x", bigInteger);
	}

	/**
	 * Send given bytes on specified socket
	 * 
	 * @param socket socket on which to send bytes
	 * @param bytes bytes to send
	 * @throws IOException if I/O problem
	 */
	private static void FoopSnd(DatagramSocket socket, byte[] bytes) throws IOException {
		socket.send(new DatagramPacket(bytes, bytes.length));
	}

	private static void FoopRcv(DatagramSocket socket, String rcvTest) throws IOException {
		socket.setSoTimeout(2000);
		DatagramPacket rcvPkt = new DatagramPacket(new byte[MAXDG], MAXDG);
		try {
			socket.receive(rcvPkt);
		} catch (SocketTimeoutException e) {
			System.err.println("Fail: Timeout on receive");
			return;
		}
		byte[] rcvd = Arrays.copyOf(rcvPkt.getData(), rcvPkt.getLength());
		String hx = bytesToHexString(rcvd).toUpperCase();
		if (hx.matches(rcvTest)) {
			System.out.println("Pass: %s (%s)".formatted(hx, new String(rcvd, ENC)));
		} else {
			System.err.println("Fail: received " + hx + "; expected " + rcvTest);
		}
	}

	/**
	 * Send Fiat message on specified socket
	 * 
	 * @param socket socket on which to send message
	 * @param s string of message to send
	 * 
	 * @throws IOException if I/O problem
	 */
	private static void FiatSnd(Socket socket, String s) throws IOException {
		socket.getOutputStream().write(s.getBytes(ENC));
	}

	/**
	 * Test basic registration and add
	 * 
	 * @throws Exception if problem occurs
	 */
	protected static void TestBasic() throws Exception {
		printTest("Test Basic");

		FoopSnd(foopSocket, encodeRegister(foopSocket, new byte[] { 0x30, 0x2D }));
		FoopRcv(foopSocket, "332D");
		FiatSnd(fiatSocket, "FT1.0 5 ADD 3 1 2D40 0.5 \n");
		FoopRcv(foopSocket, "31..0331203244002800");
	}

	/**
	 * Test multiple Foop clients
	 * 
	 * @throws Exception if problem occurs
	 */
	protected static void TestMultiple() throws Exception {
		printTest("Test Multiple");

		// Make second foop client
		DatagramSocket FoopSocket2 = new DatagramSocket();
		FoopSocket2.connect(new InetSocketAddress(server, port));
		// Register second foop client
		FoopSnd(FoopSocket2, encodeRegister(FoopSocket2, new byte[] { 0x30, 0x2E }));
		FoopRcv(FoopSocket2, "332E");
		// Send FL addition and verify BOTH foop client receive
		FiatSnd(fiatSocket, "FT1.0 7 ADD 2 xyB4 1.7 \n");
		FoopRcv(foopSocket, "31..02787942000400");
		FoopRcv(FoopSocket2, "31..02787942000400");

		// Register third Foop client
		DatagramSocket FoopSocket3 = new DatagramSocket();
		FoopSocket3.connect(new InetSocketAddress(server, port));
		FoopSnd(FoopSocket3, encodeRegister(FoopSocket3, new byte[] { 0x30, 0x3E }));
		FoopRcv(FoopSocket3, "333E");

		// Send new item
		FiatSnd(fiatSocket, "FT1.0 7 ADD 2 xyB4 1.7 \n");
		// Expect
		// Receive on first Foop client
		FoopRcv(foopSocket, "31..02787942000400");

    // Message on second Foop client
    FoopRcv(FoopSocket2, "31..02787942000400");

    // Message on third Foop client
		FoopRcv(FoopSocket3, "31..02787942000400");

		FoopSocket2.close();
		FoopSocket3.close();
	}

	/**
	 * Test truncated registration
	 * 
	 * @throws Exception if problem occurs
	 */
	protected static void TestTruncatedRegistration() throws Exception {
		printTest("Test Truncated Registration");

		// Truncated register of new Foop client
		DatagramSocket FoopSocket2 = new DatagramSocket();
		FoopSocket2.connect(new InetSocketAddress(server, port));

		byte[] badRegister = Arrays.copyOf(encodeRegister(FoopSocket2, new byte[] { 0x30, 0x2E }), 6);
		FoopSocket2.send(new DatagramPacket(badRegister, badRegister.length));
		// Expect error message
		System.out.println("If pass, verify Unable to parse");
		FoopRcv(FoopSocket2, "3200.*");

		// Send new item
		FiatSnd(fiatSocket, "FT1.0 7 ADD 2 xyB4 1.7 \n");
		FoopRcv(foopSocket, "31..02787942000400");
		// No message on second Foop client
		try {
			FoopSocket2.setSoTimeout(1000);
			FoopSocket2.receive(new DatagramPacket(new byte[MAXDG], MAXDG));
			System.err.println("Fail: Unregistered client receives addition");
		} catch (SocketTimeoutException e) {
			System.out.println("Pass: Unregistered client receives nothing");
		}
	}

	 /**
   * Test bad port registration
   * 
   * @throws Exception if problem occurs
   */
  protected static void TestBadPortRegistration() throws Exception {
    printTest("Test Bad Port Registration");

    // Bad port register of new Foop client
    DatagramSocket FoopSocket2 = new DatagramSocket();
    FoopSocket2.connect(new InetSocketAddress(server, port));

    byte[] badRegister = encodeRegister(FoopSocket2, new byte[] { 0x30, 0x2E });
    badRegister[6]++;
    FoopSocket2.send(new DatagramPacket(badRegister, badRegister.length));
    // Expect error message
    System.out.println("If pass, verify Incorrect port");
    FoopRcv(FoopSocket2, "322E.*");
  }
  
 /**
  * Test send unexpected message type to server
  * 
  * @throws Exception if problem occurs
  */
 protected static void TestUnexpectedMessageType() throws Exception {
   printTest("Test Unexpected Message Type");

   // Truncated register of new Foop client
   DatagramSocket FoopSocket2 = new DatagramSocket();
   FoopSocket2.connect(new InetSocketAddress(server, port));

   FoopSocket2.send(new DatagramPacket(new byte[] {0x33, 0x4A}, 2));
   // Expect error message
   System.out.println("If pass, verify Unexpected message type");
   FoopRcv(FoopSocket2, "324A.*");
 }
 
	/**
	 * Test command line arguments
	 * 
	 * @param args array of command line arguments
	 */
	protected static void parameters(String[] args) {
		// Test and assign server parameters
		if (args.length != 2) {
			throw new IllegalArgumentException("Parameter(s): <server> <port>");
		}
		server = args[0];
		port = Integer.parseInt(args[1]);
	}

	public static void main(String[] args) throws UnknownHostException, IOException {
		// Test arguments
		parameters(args);

		// Set up sockets for test
		setup();

		// Run tests
		try {
			TestBasic();
			TestMultiple();
			TestTruncatedRegistration();
			TestBadPortRegistration();
			TestUnexpectedMessageType();
		} catch (Exception e) {
			System.err.println("Abandoning test");
		}

		// Teardown sockets for test
		teardown();
	}
}
