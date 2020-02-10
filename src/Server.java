import java.io.*;
import java.net.ServerSocket;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server {
    static final String FileSystemSeparator = FileSystems.getDefault().getSeparator();

    static final PrintWriter errWriter = new PrintWriter(new OutputStreamWriter(System.err));
    static final PrintWriter outWriter = new PrintWriter(new OutputStreamWriter(System.out));
    static final File RootFolder = Paths.get("Server").toFile();
    static final File AccountsFolder = Paths.get("Server" + FileSystemSeparator + "Accounts").toFile();
    static final File PaymentsFolder = Paths.get("Server" + FileSystemSeparator + "Payments").toFile();
    static final File ConfFile = Paths.get("Server" + FileSystemSeparator + "server.conf").toFile();

    public static void main(String[] args) {
        //Init folders for accounts and payments
        try {
            InitFolders();
        } catch (InitException e) {
            errWriter.println("Error while initialization of folders");
            e.printStackTrace(errWriter);
        }
        catch (IOException e){
            errWriter.println("IOException while creating config file");
            e.printStackTrace(errWriter);
        }

        //start server
//        try {
//            ServerSocket ss = new ServerSocket(5000);
//
//        } catch (IOException e) {
//            errWriter.println("Failed to initiate ServerSocket");
//            e.printStackTrace(errWriter);
//        }
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
