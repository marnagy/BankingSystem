import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Date;
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
	ObjectInput oi;
	ObjectOutput oo;

	public ServerSession(Socket socket, Set<Long> loggedUsers, Set<Long> accountIDs, long sessionID) throws IOException {
		this.socket = socket;
		this.accountIDs = accountIDs;
		this.sessionID = sessionID;
	}

	@Override
	public void run() {
		boolean accountCreated;
		boolean loggedIn;
		while (true){
			accountCreated = false;
			loggedIn = false;
			try{
                oi = new ObjectInputStream(socket.getInputStream());
                oo = new ObjectOutputStream(socket.getOutputStream());
				//BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				System.out.println("Thread running");
				RequestType reqType = RequestType.values()[oi.readInt()];
				Request req;
				Response resp;
				switch (reqType){
					case CreateAccount:
						// read args
						req = AccountCreateRequest.ReadArgs(oi);
						AccountCreateRequest acr = null;
						if (req != null){
							acr = (AccountCreateRequest)req;
							//check if email is already registered
							if (!accountIDs.contains(acr.email) && CreateAccount(acr.email, acr.passwd, acr.currency) ) {
								resp = new SuccessResponse();
							}
							else{
								resp = new EmailAlreadySignedUpResponse();
							}
						}
						else{
							resp = new ArgumentMissingResponse();
						}
						resp.Send(oo);
						accountCreated = true;
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
							}
							else{
								resp = new IncorrectLoginResponse();
							}
						}
						else{
							resp = new ArgumentMissingResponse();
						}
						resp.Send(oo);
						break;
					case Payment:
						break;
					default:
						throw new UnknownTypeException("Unknown type received " + reqType);
				}
				resp = null;
				if (accountCreated || loggedIn){
					break;
				}
			} catch (IOException e){

			} catch (UnknownTypeException e) {
				e.printStackTrace();
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
				bwInfoFile.write(curr.toString() + "\n");
				LocalDateTime creationDateTime = LocalDateTime.now();
				bwInfoFile.write(creationDateTime.getYear() + "_" + creationDateTime.getMonthValue() + "_" +
						creationDateTime.getDayOfMonth() + "-" +
						creationDateTime.getHour() + ":" + creationDateTime.getMinute() + "\n");
				bwInfoFile.close();

				//currencies File
				bwCurrenciesFile.write(curr.name() + ":" + 0);
				bwCurrenciesFile.close();
				return true;
			}

		}
		else{
			return false;
		}
	}
}
