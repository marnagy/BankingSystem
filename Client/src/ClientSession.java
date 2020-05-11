import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Used as wrapper for socket on client-side
 */
public final class ClientSession {
	private static int defaultPort = 5000;
	private final Socket socket;
	final String host;
	final int port;
	Long sessionID = null;

	/**
	 * Constructor using default port
	 * @throws IOException Network failure
	 */
	public ClientSession() throws IOException {
		host = "localhost";
		this.port = defaultPort;
		socket = new Socket(host, port);
	}

	/**
	 * Constructor to use when specifying port number
	 * @param port Port number to connect to
	 * @throws IOException Network failure
	 */
	public ClientSession(int port) throws IOException {
		host = "localhost";
		this.port = port;
		socket = new Socket(host, port);
	}

	/**
	 * Used for getting output stream from socket
	 * @return Output stream from socket
	 * @throws IOException Network failure
	 */
	public OutputStream getOutputStream() throws IOException {
		return socket.getOutputStream();
	}

	/**
	 * Used for getting input stream from socket
	 * @return Input stream from socket
	 * @throws IOException Network failure
	 */
	public InputStream getInputStream() throws IOException {
		return socket.getInputStream();
	}

	/**
	 * Method for closing the socket
	 * @throws IOException Network failure
	 */
	public void close() throws IOException {
		socket.close();
	}

	/**
	 * Method for getting sessionID. Used in the beginning of connection
	 * @param oi Object Input to read from
	 * @return long object of sessionID
	 * @throws IOException Network failure
	 */
	public long getID(ObjectInput oi) throws IOException {
		sessionID = oi.readLong();
		return sessionID;
	}
}
