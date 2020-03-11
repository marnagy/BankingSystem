import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.ZonedDateTime;
import java.util.Set;

public class PaymentRequest extends Request {
	public final int senderAccountID, receiverAccountID;
	public final long amount;
	public final CurrencyType curr;
	public final ZonedDateTime sendingDateTime;

	// used by client
	public PaymentRequest(int senderAccountID, int receiverAccountID, long amount, CurrencyType curr,
	                      long sessionID){
		super(RequestType.Payment, sessionID);
		if (amount <= 0){
			throw new IllegalArgumentException("Minimum amount of money to send: 0.01");
		}
		this.senderAccountID = senderAccountID;
		this.receiverAccountID = receiverAccountID;

		this.amount = amount;
		this.curr = curr;
		this.sendingDateTime = ZonedDateTime.now();
	}
	// used when reading request on server
	private PaymentRequest(int senderAccountID, int receiverAccountID, long amount,
	                       CurrencyType curr, ZonedDateTime dateTime, long sessionID){
		super(RequestType.Payment, sessionID);
		if (amount <= 0){
			throw new IllegalArgumentException("Minimum amount of money to send: 0.01");
		}
		this.senderAccountID = senderAccountID;
		this.receiverAccountID = receiverAccountID;

		this.amount = amount;
		this.curr = curr;
		this.sendingDateTime = dateTime;
		// ADD sendingDate
	}
	@Override
	public void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);

		oo.writeInt(senderAccountID);
		oo.writeInt(receiverAccountID);
		oo.writeLong(amount);
		oo.writeInt(curr.ordinal());
		oo.writeObject(sendingDateTime);

		oo.flush();
	}

	public static PaymentRequest ReadArgs(ObjectInput oi) throws IOException, ClassNotFoundException {
		// ADD reading
		long sessionID = oi.readLong();
		int senderAccountID = oi.readInt();
		int receiverAccountID = oi.readInt();
		// amount already check in client, no need to test here
		long amount = oi.readLong();
		CurrencyType curr = CurrencyType.values()[ oi.readInt() ];
		ZonedDateTime dateTime = (ZonedDateTime) oi.readObject();
		return new PaymentRequest(senderAccountID, receiverAccountID, amount, curr, sessionID);
	}
}
