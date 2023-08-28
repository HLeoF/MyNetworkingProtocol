import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TCP Echo Server using Asynchronous I/O
 * 
 * The main() creates a TCP server socket channel, sets up the socket including
 * binding and setting the accept completion handler, and non-busily waits
 * (forever).
 * 
 * @author Michael Donahoo
 * @version 0.2
 */
public class BasicTCPEchoAIO {

    /**
     * Buffer size (bytes)
     */
    private static final int BUFSIZE = 256;
    /**
     * Global logger
     */
    private static final Logger logger = Logger.getLogger("Basic");

    public static void main(String[] args) throws IOException {
        if (args.length != 1) { // Test for correct # of args
            throw new IllegalArgumentException("Parameter(s): <Port>");
        }
		AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(), 10);
        try (AsynchronousServerSocketChannel listenChannel = AsynchronousServerSocketChannel.open(channelGroup)) {
            // Bind local port
            listenChannel.bind(new InetSocketAddress(Integer.parseInt(args[0])));

            // Create accept handler
            listenChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

                @Override
                public void completed(AsynchronousSocketChannel clntChan, Void attachment) {
                    listenChannel.accept(null, this);
                    handleAccept(clntChan);
                }

                @Override
                public void failed(Throwable e, Void attachment) {
                    logger.log(Level.WARNING, "Close Failed", e);
                }
            });
            // Block until current thread dies
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Server Interrupted", e);
        }
    }

    /**
     * Called after each accept completion
     * 
     * @param clntChan channel of new client
     */
    public static void handleAccept(AsynchronousSocketChannel clntChan) {
        // After accepting, the echo server reads from the client
        initRead(clntChan, ByteBuffer.allocateDirect(BUFSIZE));
    }

    /**
     * Called after each read completion
     * 
     * @param clntChan channel of new client
     * @param buf      byte buffer used in {@link Readable}
     * @param bytesRead number of bytes read
     */
    public static void handleRead(AsynchronousSocketChannel clntChan, ByteBuffer buf, int bytesRead) {
        if (bytesRead == -1) { // Did the other end close?
            try {
                clntChan.close();
            } catch (IOException e) {
                die(clntChan, "Unable to close", e);
            }
        } else if (bytesRead > 0) {
            // After reading, the echo server echos all bytes
            buf.flip(); // prepare to write
            initWrite(clntChan, buf);
        }
        logger.info(() -> "Handled read of " + bytesRead + " bytes"); 
    }

    /**
     * Called after each write
     * 
     * @param clntChan channel of new client
     * @param buf      byte buffer used in write
     */
    public static void handleWrite(AsynchronousSocketChannel clntChan, ByteBuffer buf) {
        if (buf.hasRemaining()) { // More to write
            initWrite(clntChan, buf);
        } else { // Back to reading
            // After writing all bytes, the server again reads
            buf.clear();
            initRead(clntChan, buf);
        }
    }

    public static void initRead(AsynchronousSocketChannel clntChan, ByteBuffer buf) {
        clntChan.read(buf, buf, new CompletionHandler<Integer, ByteBuffer>() {
            public void completed(Integer bytesRead, ByteBuffer buf) {
                    handleRead(clntChan, buf, bytesRead);
            }

            public void failed(Throwable ex, ByteBuffer v) {
                die(clntChan, "Read failed", ex);
            }
        });
    }

    public static void initWrite(AsynchronousSocketChannel clntChan, ByteBuffer buf) {
        clntChan.write(buf, buf, new CompletionHandler<Integer, ByteBuffer>() {
            public void completed(Integer bytesWritten, ByteBuffer buf) {
                handleWrite(clntChan, buf);
            }

            public void failed(Throwable ex, ByteBuffer buf) {
                die(clntChan, "Write failed", ex);
            }
        });
    }

    public static void die(AsynchronousSocketChannel clntChan, String msg, Throwable ex) {
        logger.log(Level.SEVERE, msg, ex);
        try {
            clntChan.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Close Failed", e);
        }
    }
}
