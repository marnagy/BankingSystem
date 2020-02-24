import java.util.Map;

public class Account {

	public final int accountID;
	public final Map<CurrencyType, Long> Values;
	public Account(AccountInfoResponse air){
		accountID = air.accountID;
		this.Values = air.Values;
	}
}
