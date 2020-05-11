import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Hashtable;
import java.util.Map;

/**
 * Used for storing information about bank account after log in.
 * Stores accountID, datetime of creation/registration, current balance and history of payments.
 */
public class Account {
	/**
	 * Account ID of the account
	 */
	public final int accountID;
	/**
	 * ZonedDateTime of creation of the account
	 */
	public final ZonedDateTime created;
	/**
	 * Current balance of different currencies
	 */
	private final Map<CurrencyType, Long> Values;
	/**
	 * Loaded history of payments
	 */
	private final Map<YearMonth, Payment[]> History = new Hashtable<YearMonth, Payment[]>();

	/**
	 * Constructor used on server when creating new account
	 * @param email Email adress of the account
	 */
	public Account(String email){
		this.accountID = email.hashCode();
		Values = new Hashtable<CurrencyType, Long>();
		for ( CurrencyType curr: CurrencyType.values() ) {
			Values.put(curr, 3000L);
		}
		// current Date
		this.created = ZonedDateTime.now();
	}

	/**
	 * Constructor used when loading from AccountInfoResponse
	 * @param air AccountInfoResponse object
	 */
	private Account(AccountInfoResponse air){
		accountID = air.accountID;
		this.Values = air.Values;
		this.created = air.created;
	}

	/**
	 * Wrapper method for adding money to current balance
	 * @param currencyType CurrencyType for which you want to change value
	 * @param amount Amount to be added
	 * @return Success value
	 */
	public boolean tryAdd(CurrencyType currencyType, long amount){
		long currVal = Values.get(currencyType);
		// overflow
		if (amount + currVal < 0){
			return false;
		}
		else{
			Values.put(currencyType, currVal + amount);
			return true;
		}
	}
	/**
	 * Wrapper method for subtracting money from current balance
	 * @param currencyType CurrencyType for which you want to change value
	 * @param amount Amount to be subtracted
	 * @return Success value
	 */
	public boolean trySubtract(CurrencyType currencyType, long amount){
		long currVal = Values.get(currencyType);
		if (amount > currVal){
			return false;
		}
		else{
			Values.put(currencyType, currVal - amount);
			return true;
		}
	}

	/**
	 * Wrapper method for getting current balance for CurrencyType
	 * @param curr CurrencyType object
	 * @return Balance in long with 2 decimal places
	 */
	public long getBalance(CurrencyType curr){
		return Values.get(curr);
	}

	/**
	 * Method used to save payment history to the Account object
	 * @param yearMonth YearMonth corresponding to the histrory
	 * @param payments Array of payments made in corresponding YearMonth
	 */
	public void updatePaymentHistory(YearMonth yearMonth, Payment[] payments){
		History.put(yearMonth, payments);
	}

	/**
	 * Method for loading Account object on successful log in
	 * @param air AccountInfoResponse
	 * @return Loaded Account object
	 */
	public static Account fromAccountInfoResponse(AccountInfoResponse air){
		return new Account(air);
	}

	/**
	 * Static method used for loading Account object from folder.
	 * Used by server.
	 * @param accountDir File object containing account's information
	 * @return Loaded Account object
	 * @throws IOException Files failure
	 */
	public static Account fromDir(File accountDir) throws IOException {
		File infoFile = Paths.get(accountDir.getAbsolutePath(), ".info").toFile();
		File currFile = Paths.get(accountDir.getAbsolutePath(), ".curr").toFile();
		String email;
		try (BufferedReader br = new BufferedReader(new FileReader(infoFile))) {
			email = br.readLine();
		}
		Account account = new Account(email);
		try (BufferedReader br = new BufferedReader(new FileReader(currFile))) {
			String line;
			String[] lineParts;
			while ((line = br.readLine()) != null) {
				lineParts = line.split(":");
				account.Values.put(CurrencyType.valueOf(lineParts[0]), Long.parseLong(lineParts[1]));
			}
		}
		return account;
	}
}
