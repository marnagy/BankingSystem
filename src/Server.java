import java.io.*;
import java.net.ServerSocket;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Server {
//    static final String FileSystemSeparator = FileSystems.getDefault().getSeparator();
//    static final String RootFolderName = "Server";
//    static final String AccountsFolderName = "Accounts";
//    static final String PaymentsFolderName = "Payments";
//    static final String ServerConfFileName = "server.conf";
//
//    static final PrintWriter errWriter = new PrintWriter(new OutputStreamWriter(System.err));
//    static final PrintWriter outWriter = new PrintWriter(new OutputStreamWriter(System.out));
//    static final Reader inReader = new BufferedReader(new InputStreamReader(System.in));
//
//    static final File RootFolder = Paths.get(RootFolderName).toFile();
//    static final File AccountsFolder = Paths.get(RootFolderName + FileSystemSeparator + AccountsFolderName).toFile();
//    static final File PaymentsFolder = Paths.get(RootFolderName + FileSystemSeparator + PaymentsFolderName).toFile();
//    static final File ConfFile = Paths.get(RootFolderName + FileSystemSeparator + ServerConfFileName).toFile();
//
//    static final Set<String> loggedUsers = new HashSet<String>();
//    static final Dictionary<Long, ServerSession> threads = new Hashtable<Long, ServerSession>();
//    static final Set<Long> threadIDs = new HashSet<Long>();
//    static final Set<Long> accountIDs = new HashSet<Long>();
//
//    static final Random rand = new Random(System.nanoTime());

    public static void main(String[] args) {
//        //Init folders for accounts and payments
//        try {
//            InitFolders();
//        } catch (InitException e) {
//            errWriter.println("Error while initialization of folders");
//            e.printStackTrace(errWriter);
//        }
//        catch (IOException e){
//            errWriter.println("IOException while creating config file");
//            e.printStackTrace(errWriter);
//        }
//
//        //start server
//        try {
//            ServerSocket ss = new ServerSocket(5000);
//            long l;
//            while (true){
//                ServerSession session = new ServerSession(ss.accept(), threadIDs, accountIDs);
//                do {
//                    l = rand.nextLong();
//                } while (threadIDs.contains(l));
//                session.setName(l + "");
//                threadIDs.add(l);
//                session.start();
//            }
//
//        } catch (IOException e) {
//            errWriter.println("Failed to initiate ServerSocket");
//            e.printStackTrace(errWriter);
        }
    }
//    private static boolean ContainsFileWithName(File[] fileNames, File fileToFind) {
//        boolean result = false;
//        for ( File file : fileNames) {
//            if (file.equals(fileToFind)) {
//                result = true;
//                break;
//            }
//        }
//        return result;
//    }
//    private static void InitFolders() throws InitException, IOException {
//        boolean rootRes = RootFolder.mkdir();
//        boolean accountsRes = AccountsFolder.mkdir();
//        boolean paymentsRes = PaymentsFolder.mkdir();
//        boolean confRes = ConfFile.createNewFile();
//
////        if (confRes){
////
////        }
//
//        if ( !(RootFolder.exists() && AccountsFolder.exists() && PaymentsFolder.exists() && ConfFile.exists())){
//            throw new InitException();
//        }
//    }
}
