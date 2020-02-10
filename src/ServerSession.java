import java.net.Socket;

public class ServerSession extends Thread {
    Socket socket;
    public ServerSession(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        super.run();
    }

    @Override
    public synchronized void start() {
        super.start();
    }
}
