import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Map;
import java.util.Set;

public class ServerSession extends Thread {
	final Socket socket;
	public final long sessionID;

	// null if not logged in, otherwise accountID (hash of email)
	Integer userID = null;

	// on account number get active thread or null
	Dictionary<Integer, ServerSession> threadMap;
	final Dictionary<Integer, Account> accounts;
	final Set<Integer> loggedUsers;
	final Set<Long> threadIDs;

	final PrintWriter outPrinter;
	final PrintWriter errPrinter;

	ObjectInput oi;
	ObjectOutput oo;

	public ServerSession(Socket socket, Set<Integer> loggedUsers, Dictionary<Integer, Account> accounts,
	                     Dictionary<Integer, ServerSession> accountToThread, long sessionID,
	                     PrintWriter outWriter, PrintWriter errWriter, Set<Long> threadIDs) throws IOException {
		this.socket = socket;
		this.accounts = accounts;
		this.sessionID = sessionID;
		this.loggedUsers = loggedUsers;
		this.outPrinter = outWriter;
		this.errPrinter = errWriter;
		this.threadMap = accountToThread;
		this.threadIDs = threadIDs;
	}

	@Override
	public void run() {
		Integer accountCreated;
		boolean loggedIn = false, endSession = false, logout = false;
		try{
			setInputOutput(socket);
			sendSessionID(socket);
			outPrinter.println("Thread " + sessionID + " running");
			outPrinter.flush();
			//loggedIn = false;
			while (true){
				accountCreated = null;

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
								resp = CreateAccountHandler.run(oi, accounts, sessionID);
							}
							break;
						case Login:
							// read args
							req = LoginRequest.readArgs(oi);
							if (req != null) {
								LoginRequest LoginReq = (LoginRequest) req;
								if (accountCheck(LoginReq)) {
									// accountDir has to exist (AccountCheck checks it)
									resp = new AccountInfoResponse(new File(MasterServerSession.AccountsFolder.getCanonicalPath() +
											MasterServerSession.FileSystemSeparator + LoginReq.email.hashCode()), sessionID);
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
							resp = PaymentHandler.Run(outPrinter, errPrinter, pr, oo, accounts, sessionID);
							break;
						case PaymentCategoryChange:
							PaymentCategoryChangeRequest PCChReq = PaymentCategoryChangeRequest.ReadArgs(oi);
							if (!loggedIn) {
								resp = new UnknownErrorResponse("You are not logged in.", sessionID);
							}
							resp = PaymentCategoryChangeHandler.Run(userID, PCChReq, MasterServerSession.PaymentsFolder, oo,
									sessionID);
							break;
						case AccountHistory:
							if (!loggedIn) {
								resp = new UnknownErrorResponse("You are not logged in.", sessionID);
							}
							resp = AccountHistoryHandler.run(outPrinter, errPrinter, oi, oo, accounts, sessionID);
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
				if (accountCreated != null){
					outPrinter.println("Thread " + this.getName() + " has created account number " + accountCreated + ".");
					outPrinter.flush();
					accountCreated = null;
				}
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

	private boolean NeedLogin(RequestType reqType) {
		return reqType == RequestType.Logout ||
				reqType == RequestType.Payment ||
				reqType == RequestType.AccountHistory ||
				reqType == RequestType.PaymentCategoryChange;
	}

	private void setInputOutput(Socket s) throws IOException {
		oo = new ObjectOutputStream(socket.getOutputStream());
		oi = new ObjectInputStream(socket.getInputStream());
	}
	private void sendSessionID(Socket socket) throws IOException {
		long l = Long.parseLong(this.getName());
		oo.writeLong(l);
		oo.flush();
	}

	private boolean accountCheck(LoginRequest loginReq) {
		File accountDir = new File(MasterServerSession.AccountsFolder.getAbsolutePath() + MasterServerSession.FileSystemSeparator + loginReq.email.hashCode());
		File infoFile = new File(accountDir.getAbsolutePath() + MasterServerSession.FileSystemSeparator + ".info");
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
