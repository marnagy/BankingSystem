import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Set;

public class ServerSession extends Thread {
    Socket socket;
    long sessionID;
    Set<ServerSession> sessions;
    Set<Long> threadIDs;
    long timer;
    ObjectInput oi;

    public ServerSession(Socket socket, Set<Long> threadIDs, Set<Long> accountIDs) throws IOException {
        this.socket = socket;
        oi = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        //super.run();
        try{

        }
        catch (Exception e){

        }
    }

    private void deleteUserIDFromSet() {

    }

    @Override
    public synchronized void start() {
        this.run();
    }
    public void setID(long id){
        this.sessionID = id;
    }
}
