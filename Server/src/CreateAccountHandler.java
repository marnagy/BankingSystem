import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

public class CreateAccountHandler {
	static AccountCreateRequest req;
	static Response resp;
	public static Response run(final ObjectInput oi, final Dictionary<Integer, Account> accounts,
	                           File accountsFolder, Random rand, long sessionID) {
		req = AccountCreateRequest.readArgs(oi);

		try {
			if (req != null) {
				AccountCreateRequest acr = req;
				Account account = null;
				//check if email is already registered
				if (accounts.get(acr.email.hashCode()) == null &&
						(account = createAccount(acr.email, acr.passwd, accountsFolder, rand)) != null) {
					int accountCreated = acr.email.hashCode();
					assert accountCreated == account.accountID;
					accounts.put(accountCreated, account );
					resp = new SuccessResponse(sessionID);
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
	private static Account createAccount(String email, char[] passwd, File accountsFolder, Random rand) throws IOException {
		File newAccountFolder = Paths.get(accountsFolder.getAbsolutePath(), email.hashCode() + "").toFile();
		Account account = new Account(email);
		if ( newAccountFolder.mkdir() ) {
			if ( createAccountInfoFile( newAccountFolder, account, email, passwd, rand) ){
				return account;
			}
			else {
				return null;
			}
		}
		else{
			return null;
		}
	}
	private static boolean createAccountInfoFile(File accountFolderFile, Account account, String email, char[] passwd, Random rand) throws IOException {
		File infoFile = Paths.get(accountFolderFile.getAbsolutePath(),".info").toFile();
		File currenciesFile = Paths.get(accountFolderFile.getAbsolutePath(),".curr").toFile();
		if (infoFile.createNewFile() && currenciesFile.createNewFile()){
			try (BufferedWriter bwInfoFile = new BufferedWriter(new FileWriter(infoFile));
			     BufferedWriter bwCurrenciesFile = new BufferedWriter(new FileWriter(currenciesFile))){
				//hash of email will be accountID
					bwInfoFile.write(email + "\n");
				int salt = rand.nextInt();
					bwInfoFile.write(salt + "\n");
				int passwdHash = Arrays.hashCode(passwd);
				int checkHash = email.hashCode() + salt + passwdHash;
					bwInfoFile.write(checkHash + "\n");
				ZonedDateTime creationDateTime = ZonedDateTime.now();
					bwInfoFile.write(Payment.stringify(creationDateTime) + "\n");
				//bwInfoFile.close();

				//currencies File
				CurrencyType[] currs = CurrencyType.values();
				for ( CurrencyType curr: currs ) {
					bwCurrenciesFile.write(curr.name() + ":" + account.getBalance(curr) + "\n");
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
