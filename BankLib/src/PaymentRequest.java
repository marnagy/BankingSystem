import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class PaymentRequest extends Request {
	int senderAccountID, receiverAccountID;
	long amount;
	CurrencyType curr;
	//
	public PaymentRequest(int senderAccountID, int receiverAccountID, long amount, CurrencyType curr){
		super(RequestType.Payment);
		this.senderAccountID = senderAccountID;
		this.receiverAccountID = receiverAccountID;
		this.amount = amount;
		this.curr = curr;
		// ADD sendingDate
	}
	@Override
	public void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		// ADD sending

		oo.flush();
	}

	public static Request ReadArgs(ObjectInput oi){
		// ADD reading
		//To-Do: add reading
		return null;
	}
}
