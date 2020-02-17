import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    static final String FileSystemSeparator = FileSystems.getDefault().getSeparator();
    static final String RootFolderName = "ServerFiles";
    static final String AccountsFolderName = "Accounts";
    static final String PaymentsFolderName = "Payments";
    static final String ServerConfFileName = "server.conf";

    static final PrintWriter errWriter = new PrintWriter(new OutputStreamWriter(System.err));
    static final PrintWriter outWriter = new PrintWriter(new OutputStreamWriter(System.out));
    static final Reader inReader = new BufferedReader(new InputStreamReader(System.in));

    static final File RootFolder = Paths.get(RootFolderName).toFile();
    static final File AccountsFolder = Paths.get(RootFolderName + FileSystemSeparator + AccountsFolderName).toFile();
    static final File PaymentsFolder = Paths.get(RootFolderName + FileSystemSeparator + PaymentsFolderName).toFile();
    static final File ConfFile = Paths.get(RootFolderName + FileSystemSeparator + ServerConfFileName).toFile();

    static final Set<String> loggedUsers = new HashSet<String>();

    // insert account ID, get ServerSession or null if the user is logged in currently
    static final Dictionary<Long, ServerSession> threads = new Hashtable<Long, ServerSession>();

    // what is this set for ???
    // for keeping track of number of threads currently running?
    // wouldn't one number be enough?
    static final Set<Long> threadIDs = new HashSet<Long>();

    // all valid account IDs, loaded from appropriate folder
    // new are added
    static final Set<Long> accountIDs = new HashSet<Long>();

    // random variable
    static final Random rand = new Random(System.nanoTime());

    public static void main(String[] args) {
        //Init folders for accounts and payments
        try {
            InitFolders();
        } catch (InitException e) {
            errWriter.println("Error while initialization of folders");
            e.printStackTrace(errWriter);
            return;
        }
        catch (IOException e){
            errWriter.println("IOException while creating config file");
            e.printStackTrace(errWriter);
            return;
        }

        //start server
        try {
            ServerSocket ss = SSLServerSocketFactory.getDefault().createServerSocket(5000);
            long l;
            while (true){
                ServerSession session = new ServerSession(ss.accept(), threadIDs, accountIDs);
                do {
                    l = rand.nextLong();
                } while (threadIDs.contains(l));
                session.setName(l + "");
                threadIDs.add(l);
                session.start();
            }

        } catch (IOException e) {
            errWriter.println("IOException while starting or running ServerSocket");
            e.printStackTrace(errWriter);
        }
    }
    private static boolean ContainsFileWithName(File[] fileNames, File fileToFind) {
        boolean result = false;
        for ( File file : fileNames) {
            if (file.equals(fileToFind)) {
                result = true;
                break;
            }
        }
        return result;
    }
    private static void InitFolders() throws InitException, IOException {
        boolean rootRes = RootFolder.mkdir();
        boolean accountsRes = AccountsFolder.mkdir();
        boolean paymentsRes = PaymentsFolder.mkdir();
        boolean confRes = ConfFile.createNewFile();

//        if (confRes){
//
//        }

        if ( !(RootFolder.exists() && AccountsFolder.exists() && PaymentsFolder.exists() && ConfFile.exists())){
            throw new InitException();
        }
    }
}
