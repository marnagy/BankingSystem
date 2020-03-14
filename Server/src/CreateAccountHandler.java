import java.io.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;

public class CreateAccountHandler {
	static AccountCreateRequest req;
	static Response resp;
	public static Response Run(final ObjectInput oi, final Set<Integer> accountIDs, long sessionID) {
		req = AccountCreateRequest.ReadArgs(oi);

		try {
			if (req != null) {
				AccountCreateRequest acr = req;
				//check if email is already registered
				if (!accountIDs.contains(acr.email.hashCode()) && CreateAccount(acr.email, acr.passwd, acr.currency)) {
					resp = new SuccessResponse(sessionID);
					int accountCreated = acr.email.hashCode();
					accountIDs.add(accountCreated);
				} else {
					resp = new EmailAlreadySignedUpResponse(sessionID);
				}
			} else {
				resp = new AccountCreateFailResponse("Invalid request format.", sessionID);
			}
			return resp;
		}
		catch (IOException | NullPointerException e){
			return new AccountCreateFailResponse("Account already exists.", sessionID);
		}
	}
	private static boolean CreateAccount(String email, char[] passwd, CurrencyType curr) throws IOException {
		File newAccountFolder = new File(MasterServerSession.AccountsFolder.getAbsolutePath() + MasterServerSession.FileSystemSeparator + email.hashCode());
		if ( newAccountFolder.mkdir() ) {
			return CreateAccountInfoFile( newAccountFolder, email, passwd, curr);
		}
		else{
			return false;
		}
	}
	private static boolean CreateAccountInfoFile(File accountFolderFile, String email, char[] passwd, CurrencyType curr) throws IOException {
		File infoFile = new File(accountFolderFile.getAbsolutePath() + MasterServerSession.FileSystemSeparator + ".info");
		File currenciesFile = new File(accountFolderFile.getAbsolutePath() + MasterServerSession.FileSystemSeparator + ".curr");
		if (infoFile.createNewFile() && currenciesFile.createNewFile()){
			try (BufferedWriter bwInfoFile = new BufferedWriter(new FileWriter(infoFile));
			     BufferedWriter bwCurrenciesFile = new BufferedWriter(new FileWriter(currenciesFile))){
				//hash of email will be accountID
				bwInfoFile.write(email + "\n");
				int salt = MasterServerSession.rand.nextInt();
				bwInfoFile.write(salt + "\n");
				int passwdHash = Arrays.hashCode(passwd);
				int checkHash = email.hashCode() + salt + passwdHash;
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
}
