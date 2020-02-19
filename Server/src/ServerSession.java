import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Set;

public class ServerSession extends Thread {
	Socket socket;
	long sessionID;
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
							if (!accountIDs.contains(acr.email) && CreateAccount(acr.email, acr.passwd) ) {
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
	private boolean CreateAccount(String email, char[] passwd) throws IOException {
		File newAccountFolder = new File(Main.AccountsFolder.getAbsolutePath() + Main.FileSystemSeparator + email);
		if ( newAccountFolder.mkdir() ) {
			File infoFile = new File(newAccountFolder.getAbsolutePath() + Main.FileSystemSeparator + ".info");
			return CreateAccountInfoFile(infoFile, email, passwd);
		}
		else{
			return false;
		}
	}
	private boolean CreateAccountInfoFile(File infoFile, String email, char[] passwd) throws IOException {
		if (infoFile.createNewFile()){
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(infoFile))){
				//hash of email will be accountID
				bw.write(email.hashCode() + "\n");
				int salt = Main.rand.nextInt();
				bw.write(salt + "\n");
				int checkHash = email.hashCode() + salt + passwd.hashCode();
				bw.write(checkHash + "\n");
				// CONTINUE HERE
				return true;
			}

		}
		else{
			return false;
		}
	}
}
