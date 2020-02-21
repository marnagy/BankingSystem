import java.io.*;
import java.net.Socket;
import java.util.Currency;
import java.util.Map;
import java.util.Set;

public class ServerSession extends Thread {
	Socket socket;
	long sessionID;
	// null if not logged in, otherwise accountID (hash of email)
	Integer userID = null;
	// on account number get active thread or null
	Map<Long, ServerSession> threadMap;
	Set<Long> accountIDs;
	ObjectInput oi;
	ObjectOutput oo;

	public ServerSession(Socket socket, Set<Long> loggedUsers, Set<Long> accountIDs) throws IOException {
		this.socket = socket;
		this.accountIDs = accountIDs;

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
								resp = new AccountInfoResponse();
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
	public void setID(long id){
		this.sessionID = id;
	}

	/**
	 * @param email
	 * @param passwd
	 * @return
	 */
	private boolean CreateAccount(String email, char[] passwd, CurrencyType curr) throws IOException {
		File newAccountFolder = new File(Main.AccountsFolder.getAbsolutePath() + Main.FileSystemSeparator + email.hashCode());
		if ( newAccountFolder.mkdir() ) {
			File infoFile = new File(newAccountFolder.getAbsolutePath() + Main.FileSystemSeparator + ".info");
			return CreateAccountInfoFile(infoFile, email, passwd, curr);
		}
		else{
			return false;
		}
	}
	private boolean CreateAccountInfoFile(File infoFile, String email, char[] passwd, CurrencyType curr) throws IOException {
		if (infoFile.createNewFile()){
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(infoFile))){
				//hash of email will be accountID
				bw.write(email + "\n");
				int salt = Main.rand.nextInt();
				bw.write(salt + "\n");
				int checkHash = email.hashCode() + salt + passwd.hashCode();
				bw.write(checkHash + "\n");
				bw.write(curr.toString() + "\n");
				bw.flush();
				return true;
			}

		}
		else{
			return false;
		}
	}
}
