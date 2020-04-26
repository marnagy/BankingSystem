import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TO-DO:
// Change naming of subdirectories
// Add checking for session ID
// Continue with payments

public class MasterServerSession {
	private static int defaultPort = 5000;
	private static MasterServerSession instance = null;
	private String emailAddr;
	private char[] emailPasswd;

	final int port;
	final String FileSystemSeparator = FileSystems.getDefault().getSeparator();
	final String rootFolderName = "ServerFiles";
	final String accountsFolderName = "Accounts";
	final String paymentsFolderName = "Payments";
	final String serverConfFileName = "server.conf";

	final PrintWriter errWriter = new PrintWriter(new OutputStreamWriter(System.err));
	final PrintWriter outWriter = new PrintWriter(new OutputStreamWriter(System.out));

	final File rootFolder = Paths.get(rootFolderName).toFile();
	final File accountsFolder = Paths.get(rootFolderName, accountsFolderName).toFile();
	final File paymentsFolder = Paths.get(rootFolderName, paymentsFolderName).toFile();
	final File confFile = Paths.get(rootFolderName, serverConfFileName).toFile();
	final Set<Integer> loggedUsers = Collections.synchronizedSet(new HashSet<Integer>());

	// insert account ID, get ServerSession or null if the user is logged in currently
	final Dictionary<Integer, ServerSession> threads = new Hashtable<Integer, ServerSession>();

	// for unique ID for each thread/session
	// used for authentication
	final Set<Long> threadIDs = Collections.synchronizedSet(new HashSet<Long>());

	// all valid account IDs, loaded from appropriate folder
	// new are added
	final Dictionary<Integer, Account> accounts = new Hashtable<Integer, Account>();

	// random variable
	final Random rand = new Random(System.nanoTime());

	private boolean IsTest = true;

	public static MasterServerSession getDefault(){
		if (instance == null){
			var master = MasterServerSession.getDefault(defaultPort);
			instance = master;

			return master;
		}
		else{
			return instance;
		}

	}
	public static MasterServerSession getDefault(int port){
		if (instance == null){
			var master = new MasterServerSession(port);
			instance = master;
			return master;
		}
		else{
			return instance;
		}
	}

	private MasterServerSession(int port){
		this.port = port;
	}
	public void run(String arg, char[] passwd) {
		emailAddr = arg;
		emailPasswd = passwd;

		if (IsTest && rootFolder.exists()){
			deleteDirectory(rootFolder);
			outWriter.println("Root folder reset.");
			outWriter.flush();
		}

		//Init folders for accounts and payments
		try {
			initFolders();
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
		File[] accountsDirs = accountsFolder.listFiles();
		for (int i = 0; i < accountsDirs.length; i++){
			loadAccountFromDir(accountsDirs[i], accounts);
		}
		outWriter.println("Accounts loaded");
		outWriter.flush();

		//start server
		ServerSocket ss;
		outWriter.println("Starting server socket");
		outWriter.flush();

		try {
			ss = new ServerSocket(port);
		} catch (IOException e) {
			errWriter.println("IOException while starting ServerSocket");
			errWriter.flush();
			e.printStackTrace(errWriter);
			return;
		}
		ExecutorService pool = Executors.newFixedThreadPool(Thread.activeCount());

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
				ServerSession session = new ServerSession(s, loggedUsers, accounts, threads, accountsFolder,
						paymentsFolder, emailAddr, emailPasswd, sessionID, rand, outWriter, errWriter, threadIDs);
				session.setName(sessionID + "");
				threadIDs.add(sessionID);
				pool.execute(session);
			}
			catch (IOException e) {
				errWriter.println("IOException while running ServerSocket");
				e.printStackTrace(errWriter);
			}
		}
	}
	private void loadAccountFromDir(File accountsDir, Dictionary<Integer,Account> accounts) {
		try{
			Account account = Account.fromDir(accountsDir);
			accounts.put(account.accountID, account);
		}
		catch (IOException e) {
			throw new Error("Invalid Account state for " + accountsDir.getName() + ".");
		}
	}
	private void initFolders() throws InitException, IOException {
		if(!rootFolder.mkdir()){
			return;
		}
		boolean accountsRes = accountsFolder.mkdir();
		boolean paymentsRes = paymentsFolder.mkdir();
		boolean confRes = confFile.createNewFile();

		if ( !(rootFolder.exists() && accountsFolder.exists() && paymentsFolder.exists() && confFile.exists())){
			throw new InitException();
		}
	}
	private boolean deleteDirectory(File directoryToBeDeleted){
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
