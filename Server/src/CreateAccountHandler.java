import java.io.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class CreateAccountHandler {
	static AccountCreateRequest req;
	static Response resp;
	public static Response Run(final ObjectInput oi, final Map<Integer, Account> accounts, long sessionID) {
		req = AccountCreateRequest.ReadArgs(oi);

		try {
			if (req != null) {
				AccountCreateRequest acr = req;
				//check if email is already registered
				if (!(accounts.get(acr.email.hashCode()) != null) && CreateAccount(acr.email, acr.passwd)) {
					resp = new SuccessResponse(sessionID);
					int accountCreated = acr.email.hashCode();
					accounts.put(accountCreated, new Account(acr.email) );
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
	private static boolean CreateAccount(String email, char[] passwd) throws IOException {
		File newAccountFolder = new File(MasterServerSession.AccountsFolder.getAbsolutePath() + MasterServerSession.FileSystemSeparator + email.hashCode());
		if ( newAccountFolder.mkdir() ) {
			return CreateAccountInfoFile( newAccountFolder, email, passwd);
		}
		else{
			return false;
		}
	}
	private static boolean CreateAccountInfoFile(File accountFolderFile, String email, char[] passwd) throws IOException {
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
				//bwInfoFile.close();

				//currencies File
				CurrencyType[] currs = CurrencyType.values();
				for ( CurrencyType curr: currs ) {
					bwCurrenciesFile.write(curr.name() + ":" + 3000L + "\n");
				}
				bwCurrenciesFile.close();
				return true;
			}

		}
		else{
			return false;
		}
	}
}
