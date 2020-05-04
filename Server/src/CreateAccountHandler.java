import java.io.*;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

public class CreateAccountHandler {
	static AccountCreateRequest req;
	static Response resp;

	/**
	 * Method for handling creation of account including folders and files creation
	 * @param oi Object Input object
	 * @param accounts Map form accountID to Account object
	 * @param accountsFolder Folder containing accounts
	 * @param rand Random object
	 * @param sessionID Long identifier of session
	 * @return Response object. Can return SuccessResponse, EmailAlreadySignedUpResponse, AccountCreateFailResponse
	 */
	public static Response run(final ObjectInput oi, final Map<Integer, Account> accounts,
	                           File accountsFolder, Random rand, long sessionID) {
		req = AccountCreateRequest.readArgs(oi);

		try {
			if (req != null) {
				AccountCreateRequest acr = req;
				Account account;
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

	/**
	 * Method to create Account object as well as files necessary
	 * @param email Email linked to the account
	 * @param passwd Password specific to this account
	 * @param accountsFolder Folder of all accounts
	 * @param rand Random object
	 * @return Account object if success, otherwise null
	 * @throws IOException Files creation error
	 */
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

	/**
	 * Method handling creation of .info file for the account
	 * @param accountFolderFile File object for folder that contains current account to create
	 * @param account Account object that is being created
	 * @param email Email for the account
	 * @param passwd Password to the account
	 * @param rand Random object
	 * @return Returns success
	 * @throws IOException File creation trouble
	 */
	private static boolean createAccountInfoFile(File accountFolderFile, Account account, String email, char[] passwd, Random rand) throws IOException {
		File infoFile = Paths.get(accountFolderFile.getAbsolutePath(),".info").toFile();
		File currenciesFile = Paths.get(accountFolderFile.getAbsolutePath(),".curr").toFile();
		if (infoFile.createNewFile() && currenciesFile.createNewFile()){
			try (BufferedWriter bwInfoFile = new BufferedWriter(new FileWriter(infoFile));
			     BufferedWriter bwCurrenciesFile = new BufferedWriter(new FileWriter(currenciesFile))){
				//hash of email will be accountID
				bwInfoFile.write(email + "\n");

				// random salt for case of security breach
				int salt = rand.nextInt();
				bwInfoFile.write(salt + "\n");

				int passwdHash = Arrays.hashCode(passwd);

				// THIS HASH IS CHECK WHEN USERS TRY TO LOG IN
				int checkHash = email.hashCode() + salt + passwdHash;
				bwInfoFile.write(checkHash + "\n");
				// save creation datetime to file
				bwInfoFile.write(Payment.stringify(ZonedDateTime.now()) + "\n");

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
