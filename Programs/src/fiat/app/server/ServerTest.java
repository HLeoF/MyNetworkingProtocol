package fiat.app.server;

import static fiat.serialization.MessageFactory.decode;
import static fiat.serialization.MessageFactory.encode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import fiat.serialization.Add;
import fiat.serialization.Error;
import fiat.serialization.Get;
import fiat.serialization.Item;
import fiat.serialization.ItemList;
import fiat.serialization.MealType;
import fiat.serialization.Message;
import fiat.serialization.MessageInput;
import fiat.serialization.MessageOutput;
import fiat.serialization.TokenizerException;

@DisplayName("FIAT server tests")
@TestMethodOrder(OrderAnnotation.class)
public class ServerTest {
  private static final Charset CHARENC = StandardCharsets.UTF_8;
  private static Socket clntSock;
  private static MessageInput in;
  private static MessageOutput out;
  private static InetAddress serverAddr;
  private static int serverPort;

  @BeforeAll
  static void initialize() throws IOException {
    // Determine server identity and port
    serverPort = System.getProperty("serverport") == null ? 6000
        : Integer.parseInt(System.getProperty("serverport"));
    serverAddr = (InetAddress) InetAddress
        .getByName(System.getProperty("server") == null ? "localhost" : System.getProperty("server"));
  }
  
  @BeforeEach
  void setup() throws IOException {
    // Set up TCP socket
    clntSock = new Socket(serverAddr, serverPort);
    in = new MessageInput(clntSock.getInputStream());
    out = new MessageOutput(clntSock.getOutputStream());
  }

  class LearJet extends FilterInputStream {

    protected LearJet(InputStream in) {
      super(in);
    }

    @Override
    public boolean markSupported() {
      return false;
    }

    @Override
    public void mark(int readLimit) {
      // Shouldn't be called since mark is not supported
    }

    @Override
    public void reset() throws IOException {
      throw new IOException("mark not supported");
    }

    @Override
    public int available() {
      return 0;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      return in.read(b, off, 1);
    }

    @Override
    public int read(byte[] b) throws IOException {
      return read(b, 0, 1);
    }
  }

  @AfterEach
  void cleanup() {
    try {
      clntSock.close();
    } catch (IOException e) {
    }
  }

  private static long lastTS, lastModTS;

  @DisplayName("Empty GET")
  @Test
  @Order(1)
  void testInitialGet() throws IOException, TokenizerException {
    // Send Get
    encode(new Get(System.currentTimeMillis()), out);

    // Receive empty list
    ItemList l = (ItemList) decode(in);
    lastTS = l.getTimestamp();
    // Expect empty item list and 0 modification timestamp
    assertAll(() -> assertTrue(l.getItemList().isEmpty(), "Response empty"),
        () -> assertEquals(0, l.getModifiedTimestamp()));
  }

  @DisplayName("ADDs")
  @Test
  @Order(2)
  void testAddGet() throws IOException, TokenizerException {
    // Send ADD
    final var i = new Item("xyz", MealType.Dinner, 3, 5.6);
    Message msg = new Add(System.currentTimeMillis(), i);
    encode(msg, out);

    // Receive list
    final var l = (ItemList) decode(in);
    // Expected single item, updated timestamp and modified timestamp
    assertAll("First add", () -> assertListEquals(List.of(i), l.getItemList()),
        () -> assertTrue(l.getTimestamp() > lastTS),
        () -> assertTrue(l.getModifiedTimestamp() > lastTS));
    lastTS = l.getTimestamp();
    lastModTS = l.getModifiedTimestamp();
    
    // Send ADD
    final var i2 = new Item("abc", MealType.Snack, 45, 0.7);
    msg = new Add(System.currentTimeMillis(), i2);
    encode(msg, out);

    // Receive list
    final var l2 = (ItemList) decode(in);
    // Expect two items, updated timestamp and modified timestamp
    assertAll("Second add", () -> assertListEquals(List.of(i, i2), l2.getItemList()), 
        () -> assertTrue(l2.getTimestamp() > lastTS),
        () -> assertTrue(l2.getModifiedTimestamp() > lastModTS));
    lastTS = l2.getTimestamp();
    lastModTS = l2.getModifiedTimestamp();
    cleanup();
    System.out.println("Restart student server.  Hit return to continue");
    System.in.read();
  }
  
  @DisplayName("Persistence")
  @Test
  @Order(3)
  void testPersistence() throws IOException, TokenizerException {
    // Send Get
    encode(new Get(System.currentTimeMillis()), out);

    // Receive list
    ItemList l = (ItemList) decode(in);
    // Expect two items, updated timestamp but same modified timestamp
    assertAll(() -> assertEquals(2, l.getItemList().size()), () -> assertEquals(lastModTS, l.getModifiedTimestamp()),
        () -> assertTrue(l.getTimestamp() >= lastTS, "Checking timestamp"));
    lastTS = l.getTimestamp();
  }
  
  @DisplayName("Unexpected type")
  @Test
  @Order(4)
  void testUnexpectedType() throws IOException, TokenizerException {
    // Send Error
    encode(new Error(System.currentTimeMillis(), "x"), out);

    // Receive response
    final var e = (Error) decode(in);
    // Expect correct error message
    assertTrue(e.getMessage().contains("Unexpected message type"), "incorrect error message");
    lastTS = e.getTimestamp();
    
    // Send Get
    encode(new Get(System.currentTimeMillis()), out);

    // Receive list
    ItemList l = (ItemList) decode(in);
    lastTS = l.getTimestamp();
    // Expect LIST response
    assertAll(
        () -> assertTrue(l.getTimestamp() >= lastTS, "Checking timestamp"));
  }
  
  @DisplayName("Unexpected version")
  @Test
  @Order(5)
  void testUnexpectedVersion() throws IOException, TokenizerException {
    clntSock.close();
    clntSock = new Socket(serverAddr, serverPort);
    // Send Get
    clntSock.getOutputStream().write("FT1.01 5 GET \n".getBytes(CHARENC));
    
    // Receive response
    in = new MessageInput(clntSock.getInputStream());
    final var e = (Error) decode(in);
    // Expect correct error message
    System.out.println("Version Error: " + e.getMessage());
    assertTrue(e.getMessage().matches("Unexpected.*version.*"), "incorrect version");
    lastTS = e.getTimestamp();
    
    // Confirm closed
    assertEquals(-1, clntSock.getInputStream().read());
  }

  protected void assertListEquals(List<Item> expected, List<Item> actual) {
    assertEquals(expected.size(), actual.size(), "List sizes not equal");
    IntStream.range(0, expected.size()).forEach(i -> {
      Item e = expected.get(i);
      Item a = actual.get(i);
      assertAll(() -> assertEquals(e.getName(), a.getName()), () -> assertEquals(e.getMealType(), a.getMealType()),
          () -> assertEquals(e.getCalories(), a.getCalories()), () -> assertEquals(e.getFat(), a.getFat()));
    });
  }
}
