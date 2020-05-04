import java.io.*;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class ServerSession extends Thread {
	final Socket socket;
	public final long sessionID;

	// null if not logged in, otherwise accountID (hash of email)
	Integer userID = null;

	// on account number get active thread or null
	Map<Integer, ServerSession> threadMap;
	final Map<Integer, Account> accounts;
	final Set<Integer> loggedUsers;
	final Set<Long> threadIDs;

	final PrintWriter outPrinter;
	final PrintWriter errPrinter;

	final Random rand;
	final File accountsFolder;
	final File paymentsFolder;

	final String emailAddr;
	final char[] emailPasswd;

	ObjectInput oi;
	ObjectOutput oo;

	/**
	 *
	 * @param socket Socket object that
	 * @param loggedUsers Set of accountIDs for logged in users
	 * @param accounts Map from accountID to Account
	 * @param accountToThread Map from accountID to Account
	 * @param accountsFolder Folder where accounts are stored
	 * @param paymentsFolder Folder where payments are stored
	 * @param emailAddr Your gmail address
	 * @param emailPasswd Password to your gmail address
	 * @param sessionID Session identifier
	 * @param rand Random object
	 * @param outWriter Writer object for OUT
	 * @param errWriter Writer object for ERR
	 * @param threadIDs Set of running sessions.
	 */
	public ServerSession(Socket socket, Set<Integer> loggedUsers, Map<Integer, Account> accounts,
	                     Map<Integer, ServerSession> accountToThread, File accountsFolder, File paymentsFolder,
	                     String emailAddr, char[] emailPasswd, long sessionID, Random rand,
	                     PrintWriter outWriter, PrintWriter errWriter, Set<Long> threadIDs) {
		this.socket = socket;
		this.accounts = accounts;
		this.sessionID = sessionID;
		this.loggedUsers = loggedUsers;
		this.outPrinter = outWriter;
		this.errPrinter = errWriter;
		this.threadMap = accountToThread;
		this.threadIDs = threadIDs;
		this.accountsFolder = accountsFolder;
		this.paymentsFolder = paymentsFolder;
		this.emailAddr = emailAddr;
		this.emailPasswd = emailPasswd;
		this.rand = rand;
	}

	@Override
	/**
	 * Method to call to start session
	 */
	public void run() {
		boolean loggedIn = false, endSession = false, logout = false;
		try{
			setInputOutput(socket);
			sendSessionID(socket);
			outPrinter.println("Thread " + sessionID + " running");
			outPrinter.flush();
			while (true){

				RequestType reqType = RequestType.values()[oi.readInt()];
				//long retSessionID = oi.readLong();
				// check if sessionIDs match
				Request req;
				Response resp = null;
				if (!(NeedLogin(reqType) && !loggedIn)) {
					switch (reqType) {
						case CreateAccount:
							// read args
							synchronized (accounts) {
								resp = CreateAccountHandler.run(oi, accounts, accountsFolder, rand, sessionID);
							}
							break;
						case Login:
							// read args
							req = LoginRequest.readArgs(oi);
							if (req != null) {
								LoginRequest LoginReq = (LoginRequest) req;
								if (accountCheck(LoginReq)) {
									// accountDir has to exist (AccountCheck checks it)
									resp = new AccountInfoResponse(Paths.get(accountsFolder.getAbsolutePath(),
											LoginReq.email.hashCode() + "").toFile(), sessionID);
									loggedUsers.add(LoginReq.email.hashCode());
									this.userID = LoginReq.email.hashCode();
									loggedIn = true;
								} else {
									resp = new IncorrectLoginResponse(sessionID);
								}
							} else {
								resp = new ArgumentMissingResponse(sessionID);
							}
							break;
						case Payment:
							PaymentRequest pr = PaymentRequest.readArgs(oi);
							if (!loggedIn) {
								resp = new UnknownErrorResponse("You are not logged in.", sessionID);
							}
							resp = PaymentHandler.Run(outPrinter, errPrinter, accountsFolder, paymentsFolder, pr,
									accounts, emailAddr, emailPasswd, sessionID);
							break;
						case PaymentCategoryChange:
							PaymentCategoryChangeRequest PCChReq = PaymentCategoryChangeRequest.ReadArgs(oi);
							if (!loggedIn) {
								resp = new UnknownErrorResponse("You are not logged in.", sessionID);
							}
							resp = PaymentCategoryChangeHandler.Run(userID, PCChReq, accountsFolder, sessionID);
							break;
						case AccountHistory:
							if (!loggedIn) {
								resp = new UnknownErrorResponse("You are not logged in.", sessionID);
							}
							resp = AccountHistoryHandler.run(errPrinter, accountsFolder, paymentsFolder,
									oi, sessionID);
							break;
						case End:
							EndRequest eReq = EndRequest.readArgs(oi);
							if (userID == null || !loggedIn) {
								resp = new IllegalRequestResponse(sessionID);
							} else {
								resp = new SuccessResponse(sessionID);
								logout = true;
								endSession = true;
							}
							break;
						case Logout:
							LogOutRequest LOReq = LogOutRequest.readArgs(oi);
							if (!loggedIn) {
								resp = new IllegalRequestResponse(sessionID);
								break;
							} else {
								resp = new SuccessResponse(sessionID);
								logout = true;
							}
							break;
						default:
							resp = new IllegalRequestResponse(sessionID);
					}
				}
				else{
					resp = new UnknownErrorResponse("You are not logged in.", sessionID);
				}

				resp.send(oo);
				resp = null;
				if (logout){
					logout = false;
					userID = null;
					loggedIn = false;
				}
				if (endSession){
					socket.close();
					break;
				}
			}
		} catch (IOException | ClassNotFoundException e){
			synchronized (errPrinter) {
				errPrinter.println("IOException occurred (mostly client suddenly disconnected)");
				e.printStackTrace(errPrinter);
				errPrinter.flush();
			}
			try {
				if (!socket.isClosed()){
					socket.close();
				}
			} catch (IOException ex) {
				synchronized (errPrinter) {
					errPrinter.println("IOException occurred during ");
					ex.printStackTrace(errPrinter);
					errPrinter.flush();
				}
			}
		}
		synchronized (threadIDs){
			threadIDs.remove(sessionID);
		}
		outPrinter.println("Thread " + sessionID + " ended.");
		outPrinter.flush();
	}

	/**
	 * Method for recognizing whether request requires to be logged in
	 * @param reqType Request type from request
	 * @return Boolean whether login is needed
	 */
	private boolean NeedLogin(RequestType reqType) {
		return reqType == RequestType.Logout ||
				reqType == RequestType.Payment ||
				reqType == RequestType.AccountHistory ||
				reqType == RequestType.PaymentCategoryChange;
	}

	/**
	 * Set Input&Ouput object stream for session
	 * @param s Socket object
	 * @throws IOException Network trouble
	 */
	private void setInputOutput(Socket s) throws IOException {
		oo = new ObjectOutputStream(socket.getOutputStream());
		oi = new ObjectInputStream(socket.getInputStream());
	}

	/**
	 * For sending sessionID on the start of the session
	 * @param socket Socket object
	 * @throws IOException Network trouble
	 */
	private void sendSessionID(Socket socket) throws IOException {
		long l = Long.parseLong(this.getName());
		oo.writeLong(l);
		oo.flush();
	}

	/**
	 * Login check
	 * @param loginReq Login request
	 * @return Returns whether login is correct
	 */
	private boolean accountCheck(LoginRequest loginReq) {
		File accountDir = Paths.get(accountsFolder.getAbsolutePath(), loginReq.email.hashCode() + "").toFile();
		File infoFile = Paths.get(accountDir.getAbsolutePath(), ".info").toFile();
		try (BufferedReader br = new BufferedReader(new FileReader(infoFile))){
			String email = br.readLine();
			// for safety, check if email is same
			if (!email.equals(loginReq.email)){
				return false;
			}
			int salt = Integer.parseInt(br.readLine());
			int emailHash = loginReq.email.hashCode();
			int passwdHash = Arrays.hashCode(loginReq.passwd);
			int checkHashReq = emailHash + salt + passwdHash;
			int checkHashSaved = Integer.parseInt(br.readLine());
			return checkHashReq == checkHashSaved;
		} catch (IOException e) {
			return false;
		}
	}
}
