import java.io.*;
import java.net.Socket;
import java.util.Set;

public class ServerSession extends Thread {
    Socket socket;
    long sessionID;
    Set<ServerSession> sessions;
    Set<Long> accountIDs;
    long timer;
    ObjectInput oi;
    ObjectOutput oo;

    public ServerSession(Socket socket, Set<Long> threadIDs, Set<Long> accountIDs) throws IOException {
        this.socket = socket;
        this.accountIDs = accountIDs;
        oi = new ObjectInputStream(socket.getInputStream());
        oo = new ObjectOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        while (true){
            try{
                RequestType reqType = RequestType.valueOf(oi.readUTF());
                switch (reqType){
                    case CreateAccount:
                        String email = oi.readUTF();
                        String passwd = oi.readUTF();
                        //check if email is already registered
                        if (!accountIDs.contains(email)){
                            // CONTINUE HERE
                        }
                        break;
                    default:
                        throw new UnknownTypeException("Unknown type received " + reqType);
                }
            } catch (IOException e){

            } catch (UnknownTypeException e) {
                e.printStackTrace();
            }
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
    private boolean CreateAccount(String email, String passwd){

    }
}
