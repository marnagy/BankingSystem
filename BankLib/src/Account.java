import java.io.File;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.*;

public class Account {
	public final int accountID;
	public final ZonedDateTime created;
	public final Dictionary<CurrencyType, Long> Values;
	public final Dictionary<YearMonth, Payment[]> History = new Hashtable<YearMonth, Payment[]>();
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
	private Account(int accountID, ZonedDateTime created){
		this.accountID = accountID;
		Values = new Hashtable<CurrencyType, Long>();
		this.created = created;
	}
	public static Account fromAccountInfoResponse(AccountInfoResponse air){
		return new Account(air);
	}
}
