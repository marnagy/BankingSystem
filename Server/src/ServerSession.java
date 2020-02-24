import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public class ServerSession extends Thread {
	Socket socket;
	public final long sessionID;

	// null if not logged in, otherwise accountID (hash of email)
	Integer userID = null;

	// on account number get active thread or null
	Map<Long, ServerSession> threadMap;
	Set<Long> accountIDs;
	Set<Long> loggedUsers;

	PrintWriter outPrinter;
	PrintWriter errPrinter;

	ObjectInput oi;
	ObjectOutput oo;

	public ServerSession(Socket socket, Set<Long> loggedUsers, Set<Long> accountIDs, long sessionID) throws IOException {
		this.socket = socket;
		this.accountIDs = accountIDs;
		this.sessionID = sessionID;
		this.loggedUsers = loggedUsers;
	}

	@Override
	public void run() {
		Long accountCreated;
		boolean loggedIn;
		while (true){
			accountCreated = null;
			loggedIn = false;
			try{
                oi = new ObjectInputStream(socket.getInputStream());
                oo = new ObjectOutputStream(socket.getOutputStream());
				System.out.println("Thread running");
				RequestType reqType = RequestType.values()[oi.readInt()];
				Request req;
				Response resp = null;
				switch (reqType){
					case CreateAccount:
						// read args
						req = AccountCreateRequest.ReadArgs(oi);

						if (req != null){
							AccountCreateRequest acr = (AccountCreateRequest)req;
							//check if email is already registered
							if (!accountIDs.contains(acr.email) && CreateAccount(acr.email, acr.passwd, acr.currency) ) {
								resp = new SuccessResponse();
								accountCreated = (long) acr.email.hashCode();
								accountIDs.add(accountCreated);
							}
							else{
								resp = new EmailAlreadySignedUpResponse();
							}
						}
						else{
							resp = new ArgumentMissingResponse();
						}
						break;
					case Login:
						// read args
						req = LoginRequest.ReadArgs(oi);
						if (req != null) {
							LoginRequest LoginReq = (LoginRequest) req;
							if (AccountCheck(LoginReq)){
								// accountDir has to exist (AccountCheck checks it)
								resp = new AccountInfoResponse(LoginReq.email, new File(Main.AccountsFolder.getCanonicalPath() +
										Main.FileSystemSeparator + LoginReq.email.hashCode()));
								loggedUsers.add((long) LoginReq.email.hashCode());
								this.userID = LoginReq.email.hashCode();
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
					accountCreated = null;
				}
			} catch (IOException e){

			}
		}

	}

	private boolean AccountCheck(LoginRequest loginReq) {
		File accountDir = new File(Main.AccountsFolder.getAbsolutePath() + Main.FileSystemSeparator + loginReq.email.hashCode());
		File infoFile = new File(accountDir.getAbsolutePath() + Main.FileSystemSeparator + ".info");
		try (BufferedReader br = new BufferedReader(new FileReader(infoFile))){
			String email = br.readLine();
			// for safety, check if email is same
			if (!email.equals(loginReq.email)){
				return false;
			}
			int salt = Integer.parseInt(br.readLine());
			int checkHashReq = email.hashCode() + salt + loginReq.passwd.hashCode();
			int checkHashSaved = Integer.parseInt(br.readLine());
			return checkHashReq == checkHashSaved;
		} catch (IOException e) {
			return false;
		}
	}

	private void deleteUserIDFromSet() {

	}

	@Override
	public synchronized void start() {
		this.run();
	}
	/**
	 * @param email
	 * @param passwd
	 * @return
	 */
	private boolean CreateAccount(String email, char[] passwd, CurrencyType curr) throws IOException {
		File newAccountFolder = new File(Main.AccountsFolder.getAbsolutePath() + Main.FileSystemSeparator + email.hashCode());
		if ( newAccountFolder.mkdir() ) {
			return CreateAccountInfoFile( newAccountFolder, email, passwd, curr);
		}
		else{
			return false;
		}
	}
	private boolean CreateAccountInfoFile(File accountFolderFile, String email, char[] passwd, CurrencyType curr) throws IOException {
		File infoFile = new File(accountFolderFile.getAbsolutePath() + Main.FileSystemSeparator + ".info");
		File currenciesFile = new File(accountFolderFile.getAbsolutePath() + Main.FileSystemSeparator + ".curr");
		if (infoFile.createNewFile() && currenciesFile.createNewFile()){
			try (BufferedWriter bwInfoFile = new BufferedWriter(new FileWriter(infoFile));
					BufferedWriter bwCurrenciesFile = new BufferedWriter(new FileWriter(currenciesFile))){
				//hash of email will be accountID
				bwInfoFile.write(email + "\n");
				int salt = Main.rand.nextInt();
				bwInfoFile.write(salt + "\n");
				int checkHash = email.hashCode() + salt + passwd.hashCode();
				bwInfoFile.write(checkHash + "\n");
				LocalDateTime creationDateTime = LocalDateTime.now();
				bwInfoFile.write(creationDateTime.getYear() + "_" + creationDateTime.getMonthValue() + "_" +
						creationDateTime.getDayOfMonth() + "-" +
						creationDateTime.getHour() + ":" + creationDateTime.getMinute() + "\n");
				bwInfoFile.close();

				//currencies File
				bwCurrenciesFile.write(curr.name() + ":" + 0L);
				bwCurrenciesFile.close();
				return true;
			}

		}
		else{
			return false;
		}
	}
	public void setPrinters(PrintWriter out, PrintWriter err){
		this.outPrinter = out;
		this.errPrinter = err;
	}
}
