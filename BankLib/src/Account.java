import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.*;

public class Account {
	public final int accountID;
	public final ZonedDateTime created;
	private final Map<CurrencyType, Long> Values;
	private final Map<YearMonth, Payment[]> History = new Hashtable<YearMonth, Payment[]>();
	public Account(String email){
		this.accountID = email.hashCode();
		Values = new Hashtable<CurrencyType, Long>();
		for ( CurrencyType curr: CurrencyType.values() ) {
			Values.put(curr, 3000L);
		}
		// current Date
		this.created = ZonedDateTime.now();
	}
	private Account(AccountInfoResponse air){
		accountID = air.accountID;
		this.Values = air.Values;
		this.created = air.created;
	}
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
	public long getBalance(CurrencyType curr){
		return Values.get(curr);
	}
	public List<Payment> getPaymentHistory(YearMonth yearMonth){
		return Collections.unmodifiableList( Arrays.asList(History.get(yearMonth)));
	}
	public void updatePaymentHistory(YearMonth yearMonth, Payment[] payments){
		History.put(yearMonth, payments);
	}

	public static Account fromAccountInfoResponse(AccountInfoResponse air){
		return new Account(air);
	}
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
