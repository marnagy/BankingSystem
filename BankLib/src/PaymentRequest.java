import java.io.IOException;
import java.io.ObjectInput;

public class PaymentRequest extends Request {
	int senderAccountID, receiverAccountID;
	long amount;
	CurrencyType curr;
	//
	public PaymentRequest(int senderAccountID, int receiverAccountID, long amount, CurrencyType curr){
		this.senderAccountID = senderAccountID;
		this.receiverAccountID = receiverAccountID;
		this.amount = amount;
		this.curr = curr;
		// ADD sendingDate
	}
	@Override
	public void Send(ObjectOutput oo) throws IOException {
		// ADD sending

		oo.flush();
	}

	public static Request ReadArgs(ObjectInput oi){
		// ADD reading
	}
}
