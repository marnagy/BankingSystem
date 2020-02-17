import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientSession {
    Socket socket;
    final String host;
    final int port;
    public ClientSession() throws IOException {
        host = "localhost";
        port = 5000;
    }

    public void connect() throws IOException {
        socket = SSLSocketFactory.getDefault().createSocket(host, port);
    }

    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }
}
