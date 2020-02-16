import java.io.IOException;
import java.net.Socket;

public class ClientSession {
    Socket socket;
    public ClientSession() throws IOException {
        socket = new Socket("localhost", 5000);
    }
}
