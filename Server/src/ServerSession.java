import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class ServerSession extends Thread {
	final Socket socket;
	public final long sessionID;

	// null if not logged in, otherwise accountID (hash of email)
	Integer userID = null;

	// on account number get active thread or null
	Map<Long, ServerSession> threadMap;
	final Set<Integer> accountIDs;
	final Set<Integer> loggedUsers;

	PrintWriter outPrinter;
	PrintWriter errPrinter;

	ObjectInput oi;
	ObjectOutput oo;

	public ServerSession(Socket socket, Set<Integer> loggedUsers, Set<Integer> accountIDs, long sessionID) throws IOException {
		this.socket = socket;
		this.accountIDs = accountIDs;
		this.sessionID = sessionID;
		this.loggedUsers = loggedUsers;
	}

	@Override
	public void run() {
		Integer accountCreated;
		boolean loggedIn;
		try{
			SetInputOutput(socket);
			SendSessionID(socket);
			System.out.println("Thread running");
			loggedIn = false;
			while (true){
				accountCreated = null;

				RequestType reqType = RequestType.values()[oi.readInt()];
				Request req;
				Response resp = null;
				switch (reqType){
					case CreateAccount:
						// read args
						resp = CreateAccountHandler.Run(oi, accountIDs);
						break;
					case Login:
						// read args
						req = LoginRequest.ReadArgs(oi);
						if (req != null) {
							LoginRequest LoginReq = (LoginRequest) req;
							if (AccountCheck(LoginReq)){
								// accountDir has to exist (AccountCheck checks it)
								resp = new AccountInfoResponse(LoginReq.email, new File(MasterServerSession.AccountsFolder.getCanonicalPath() +
										MasterServerSession.FileSystemSeparator + LoginReq.email.hashCode()));
								loggedUsers.add(LoginReq.email.hashCode());
								this.userID = LoginReq.email.hashCode();
								loggedIn = true;
							}
							else{
								resp = new IncorrectLoginResponse();
							}
						}
						else{
							resp = new ArgumentMissingResponse();
						}
						break;
					case Payment:
						break;
					default:
						resp = new IllegalRequestResponse();
				}
				resp.Send(oo);
				resp = null;
				if (accountCreated != null){
					outPrinter.println("Thread " + this.getName() + " has created account number " + accountCreated + ".");
					outPrinter.flush();
					accountCreated = null;
				}
				if (loggedIn){
					break;
				}
			}
		} catch (IOException e){
			errPrinter.println("IOException occurred");
			e.printStackTrace(errPrinter);
			errPrinter.flush();
		}

	}

	private void SetInputOutput(Socket s) throws IOException {
		oi = new ObjectInputStream(socket.getInputStream());
		oo = new ObjectOutputStream(socket.getOutputStream());
	}
	private void SendSessionID(Socket socket) throws IOException {
		long l = Long.parseLong(this.getName());
		oo.writeLong(l);
		oo.flush();
	}

	private boolean AccountCheck(LoginRequest loginReq) {
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

	@Override
	public synchronized void start() {
		super.start();
	}


	public void setPrinters(PrintWriter out, PrintWriter err){
		this.outPrinter = out;
		this.errPrinter = err;
	}
}
