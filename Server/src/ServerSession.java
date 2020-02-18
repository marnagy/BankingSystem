import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Set;

public class ServerSession extends Thread {
    Socket socket;
    long sessionID;
    // on account number get active thread or null
    Map<Long, ServerSession> threadMap;
    Set<Long> accountIDs;
    long timer;
    ObjectInput oi;
    ObjectOutput oo;

    public ServerSession(Socket socket, Set<Long> loggedUsers, Set<Long> accountIDs) throws IOException {
        this.socket = socket;
        this.accountIDs = accountIDs;

    }

    @Override
    public void run() {
        while (true){
            try{
//                oi = new ObjectInputStream(socket.getInputStream());
//                oo = new ObjectOutputStream(socket.getOutputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("Thread running");
                RequestType reqType = RequestType.valueOf(br.readLine());
                switch (reqType){
                    case CreateAccount:
                        String email = br.readLine();
                        String passwd = br.readLine();
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

    /**
     * @param email
     * @param passwd
     * @return
     */
//    private boolean CreateAccount(String email, String passwd){
//
//    }
}
