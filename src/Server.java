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
    static File RootFolder = Paths.get("Server").toFile();
    static File AccountsFolder = Paths.get("Server" + FileSystemSeparator + "Accounts").toFile();
    static File PaymentsFolder = Paths.get("Server" + FileSystemSeparator + "Payments").toFile();

    public static void main(String[] args) {
        //Init folders for accounts and payments
        InitFolders();
        File rootFile = Paths.get("").toFile();
        File[] RootFolderFiles = rootFile.listFiles();
        File ServerFolder = Paths.get("Server").toFile();
        if (!ServerFolder.mkdir()){
            outWriter.println("Created Server folder");
        }

        //start server
        try {
            ServerSocket ss = new ServerSocket(5000);

        } catch (IOException e) {
            errWriter.println("Failed to initiate ServerSocket");
            e.printStackTrace(errWriter);
        }
    }
    private static boolean ContainsFileWithName(File[] fileNames, File fileToFind) {
        boolean result = false;
        for ( File file : fileNames) {
            if (file.equals(fileToFind)){
                result = true;
                break;
            }
        }
        return result;
    }
    private static void InitFolders(){
        File rootFile = Paths.get("").toFile();
        File[] RootFolderFiles = rootFile.listFiles();
        File ServerFolder = Paths.get("Server").toFile();
        if (!ServerFolder.mkdir()){
            outWriter.println("Created Server folder");
        }
        File AccountsFolder = Paths.get("Server" + FileSystemSeparator + "Accounts").toFile();
        File PaymentsFolder = Paths.get("Server" + FileSystemSeparator + "Payments").toFile();
    }
}
