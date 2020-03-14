import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class Account {
	public final int accountID;
	public final Map<CurrencyType, Long> Values;
	public Account(String email){
		this.accountID = email.hashCode();
		Values = new Hashtable<CurrencyType, Long>();
	}
	public Account(AccountInfoResponse air){
		accountID = air.accountID;
		this.Values = air.Values;
	}
}
