import java.io.File;
import java.util.*;

public class Account {
	public final int accountID;
	public final Dictionary<CurrencyType, Long> Values;
	public final Dictionary<MonthYear, Payment> payments = new Hashtable<MonthYear, Payment>();
	public Account(String email){
		this.accountID = email.hashCode();
		Values = new Hashtable<CurrencyType, Long>();
		for ( CurrencyType curr: CurrencyType.values() ) {
			Values.put(curr, 3000L);
		}
	}
	private Account(AccountInfoResponse air){
		accountID = air.accountID;
		this.Values = air.Values;
	}
	private Account(int accountID){
		this.accountID = accountID;
		Values = new Hashtable<CurrencyType, Long>();
	}
	public static Account FromDictionary(File accountFile, File paymentsDict){
		Account account = new Account(Integer.parseInt(accountFile.getName()));
//		File[] files = accountFile.listFiles();
//		for (File file: files ) {
//			String[] nameParts;
//			// check if info file or curr (current balance) file
//			if ( !( file.getName().endsWith(".info") || file.getName().endsWith(".curr") )){
//				nameParts = file.getName().split("_");
//				int year = Integer.parseInt(nameParts[0]);
//				int month = Integer.parseInt(nameParts[1]);
//			}
//		}
		return account;
	}
	public static Account fromAccountInfoResponse(AccountInfoResponse air){
		return new Account(air);
	}
}
