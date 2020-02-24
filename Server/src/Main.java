import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
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

    static final Set<Long> loggedUsers = new HashSet<Long>();

    // insert account ID, get ServerSession or null if the user is logged in currently
    static final Dictionary<Long, ServerSession> threads = new Hashtable<Long, ServerSession>();

    // for unique ID for each thread/session
    // used for authentication
    static final Set<Long> threadIDs = new HashSet<Long>();

    // all valid account IDs, loaded from appropriate folder
    // new are added
    static final Set<Long> accountIDs = new HashSet<Long>();

    // random variable
    static final Random rand = new Random(System.nanoTime());

    // used for testing
    static final boolean IsTest = true;

    public static void main(String[] args) {

        if (IsTest && RootFolder.exists()){
            deleteDirectory(RootFolder);
        }

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
            //ServerSocket ss = SSLServerSocketFactory.getDefault().createServerSocket(5000);
            ServerSocket ss = new ServerSocket(5000);
            long l;
            while (true){
                outWriter.println("Waiting for connection");
                outWriter.flush();
                Socket s = ss.accept();
                outWriter.println("Connection accepted");
                outWriter.flush();
                ServerSession session = new ServerSession(s, loggedUsers, accountIDs, rand.nextLong());
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
    private static boolean deleteDirectory(File directoryToBeDeleted){
        if (!directoryToBeDeleted.exists()){
            return true;
        }
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) { // is directory
            for (File file : allContents) {
                deleteDirectory(file);
                }
        }
        return directoryToBeDeleted.delete();
    }
}
