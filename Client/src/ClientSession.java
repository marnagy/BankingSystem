import java.io.*;
import java.net.Socket;

public final class ClientSession {
	private final Socket socket;
	final String host;
	final int port;
	Long sessionID = null;
	public ClientSession() throws IOException {
		host = "localhost";
		port = 5000;
		socket = new Socket(host, port);
	}

	public OutputStream getOutputStream() throws IOException {
		return socket.getOutputStream();
	}
	public InputStream getInputStream() throws IOException {
		return socket.getInputStream();
	}
	public void close() throws IOException {
		socket.close();
	}

	public Object getID(ObjectInput oi) throws IOException {
		sessionID = oi.readLong();
		return sessionID;
	}
}
