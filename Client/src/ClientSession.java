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
        //socket = SSLSocketFactory.getDefault().createSocket("localhost", 5000);
        socket = new Socket(host, port);
    }

    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }
}
