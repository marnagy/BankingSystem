import java.util.*;

public class Account {
	public final int accountID;
	public final Map<CurrencyType, Long> Values;
	private List<Payment> payments = new ArrayList<Payment>();
	public Account(String email){
		this.accountID = email.hashCode();
		Values = new Hashtable<CurrencyType, Long>();
	}
	public Account(AccountInfoResponse air){
		accountID = air.accountID;
		this.Values = air.Values;
	}
	public synchronized Payment[] GetPaymentsAndInitNextMonth(){
		Payment[] res;
		synchronized (this){
			res = new Payment[payments.size()];
			for (int i = 0; i < payments.size(); i++){
				res[i] = payments.get(i);
			}
			payments = new ArrayList<Payment>();
			return res;
		}
	}
}
