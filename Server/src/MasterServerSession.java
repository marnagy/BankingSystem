import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.*;

// TO-DO:
// Change naming of subdirectories
// Add checking for session ID
// Continue with payments

public class MasterServerSession {
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

	static final Set<Integer> loggedUsers = Collections.synchronizedSet(new HashSet<Integer>());

	// insert account ID, get ServerSession or null if the user is logged in currently
	static final Dictionary<Integer, ServerSession> threads = new Hashtable<Integer, ServerSession>();

	// for unique ID for each thread/session
	// used for authentication
	static final Set<Long> threadIDs = Collections.synchronizedSet(new HashSet<Long>());

	// all valid account IDs, loaded from appropriate folder
	// new are added
	//static final Set<Integer> accountIDs = Collections.synchronizedSet(new HashSet<Integer>());
	static final Dictionary<Integer, Account> accounts = new Hashtable<Integer, Account>();

	// random variable
	static final Random rand = new Random(System.nanoTime());

	// used for testing
	static final boolean IsTest = false;
	public static void Run(){
		if (IsTest && RootFolder.exists()){
			deleteDirectory(RootFolder);
			outWriter.println("Root folder reset.");
			outWriter.flush();
		}


		//Init folders for accounts and payments
		try {
			InitFolders();
			outWriter.println("Folders initiated.");
			outWriter.flush();
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

		//load accounts from dirictories
		File[] accountsDirs = AccountsFolder.listFiles();
		for (int i = 0; i < accountsDirs.length; i++){
			LoadAccountFromDir(accountsDirs[i], accounts);
		}
		outWriter.println("Accounts loaded");
		outWriter.flush();

		//start server
		ServerSocket ss;
		outWriter.println("Starting server socket");
		outWriter.flush();

		try {
			//ServerSocket ss = SSLServerSocketFactory.getDefault().createServerSocket(5000);
			ss = new ServerSocket(5000);
		} catch (IOException e) {
			errWriter.println("IOException while starting ServerSocket");
			errWriter.flush();
			e.printStackTrace(errWriter);
			return;
		}
		long sessionID;
		while (true){
			try {
				outWriter.println("Waiting for connection");
				outWriter.flush();
				Socket s = ss.accept();
				outWriter.println("Connection accepted");
				outWriter.flush();

				do {
					sessionID = rand.nextLong();
				} while (threadIDs.contains(sessionID));
				ServerSession session = new ServerSession(s, loggedUsers, accounts, threads,
						sessionID, outWriter, errWriter, threadIDs);
				session.setName(sessionID + "");
				threadIDs.add(sessionID);
				session.start();
			}
			catch (IOException e) {
				errWriter.println("IOException while running ServerSocket");
				e.printStackTrace(errWriter);
			}
		}
	}

	private static void LoadAccountFromDir(File accountsDir, Dictionary<Integer,Account> accounts) {
		int accountID = Integer.parseInt(accountsDir.getName());
		File infoFile = new File(accountsDir.getAbsolutePath() + FileSystemSeparator
		+ ".info");
		File currFile = new File(accountsDir.getAbsolutePath() + FileSystemSeparator
				+ ".curr");
		try {
			final Map<CurrencyType, Long> Values = new Hashtable<CurrencyType, Long>();
			String email;
			try (BufferedReader br = new BufferedReader(new FileReader(infoFile))) {
				email = br.readLine();
			}
			Account account = new Account(email);
			try (BufferedReader br = new BufferedReader(new FileReader(currFile))) {
				String line;
				String[] lineParts;
				while ( (line = br.readLine()) != null ) {
					lineParts = line.split(":");
					account.Values.put( CurrencyType.valueOf(lineParts[0]) , Long.parseLong(lineParts[1]));
				}
			}
			accounts.put(email.hashCode(), account);
		}
		catch (IOException e) {
			throw new Error("Invalid Account " + accountID + " state");
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
		if(!RootFolder.mkdir()){
			return;
		}
		boolean accountsRes = AccountsFolder.mkdir();
		boolean paymentsRes = PaymentsFolder.mkdir();
		boolean confRes = ConfFile.createNewFile();

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
