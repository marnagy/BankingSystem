import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.ZonedDateTime;
import java.util.Set;

public class PaymentRequest extends Request {
	public final int senderAccountID, receiverAccountID;
	public final int hoursDelay, minutesDelay;
	public final long amount;
	public final String[] symbols;
	public final String information;
	public final CurrencyType fromCurr, toCurr;
	public final ZonedDateTime sendingDateTime;

	// used by client
	public PaymentRequest(int senderAccountID, int receiverAccountID, long amount, int hoursDelay, int minutesDelay,
	                      CurrencyType fromCurr, CurrencyType toCurr,
	                      String[] symbols, String information,
	                      long sessionID){
		super(RequestType.Payment, sessionID);
		if (amount <= 0){
			throw new IllegalArgumentException("Minimum amount of money to send: 0.01");
		}
		this.senderAccountID = senderAccountID;
		this.receiverAccountID = receiverAccountID;
		this.hoursDelay = hoursDelay;
		this.minutesDelay = minutesDelay;
		this.amount = amount;
		this.fromCurr = fromCurr;
		this.toCurr = toCurr;
		this.symbols = symbols;
		this.information = information;
		this.sendingDateTime = ZonedDateTime.now();
	}
	// used when reading request on server
	private PaymentRequest(int senderAccountID, int receiverAccountID, long amount, int hoursDelay, int minutesDelay,
	                       CurrencyType fromCurr, CurrencyType toCurr, ZonedDateTime dateTime,
	                       String[] symbols, String information, long sessionID){
		super(RequestType.Payment, sessionID);
		if (amount <= 0){
			throw new IllegalArgumentException("Minimum amount of money to send: 0.01");
		}
		this.senderAccountID = senderAccountID;
		this.receiverAccountID = receiverAccountID;
		this.hoursDelay = hoursDelay;
		this.minutesDelay = minutesDelay;
		this.amount = amount;
		this.fromCurr = fromCurr;
		this.toCurr = toCurr;
		this.symbols = symbols;
		this.information = information;
		this.sendingDateTime = dateTime;
	}
	@Override
	public void send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);
		// numbers
		oo.writeInt(senderAccountID);
		oo.writeInt(receiverAccountID);
		oo.writeLong(amount);
		oo.writeInt(hoursDelay);
		oo.writeInt(minutesDelay);
		// enum
		oo.writeInt(fromCurr.ordinal());
		oo.writeInt(toCurr.ordinal());
		// ZonedDateTime
		oo.writeObject(sendingDateTime);
		// String
		oo.writeUTF(symbols[0]);
		oo.writeUTF(symbols[1]);
		oo.writeUTF(information);

		oo.flush();
	}

	public static PaymentRequest readArgs(ObjectInput oi) throws IOException, ClassNotFoundException {
		// ADD reading
		long sessionID = oi.readLong();
		int senderAccountID = oi.readInt();
		int receiverAccountID = oi.readInt();
		// amount already check in client, no need to test here
		long amount = oi.readLong();
		int hoursDelay = oi.readInt();
		int minutesDelay = oi.readInt();
		CurrencyType fromCurr = CurrencyType.values()[ oi.readInt() ];
		CurrencyType toCurr = CurrencyType.values()[ oi.readInt() ];
		ZonedDateTime dateTime = (ZonedDateTime) oi.readObject();
		String variableSymbol = oi.readUTF();
		String specificSymbol = oi.readUTF();
		String[] symbols = {variableSymbol, specificSymbol};
		String information = oi.readUTF();
		return new PaymentRequest(senderAccountID, receiverAccountID, amount, hoursDelay, minutesDelay,
				fromCurr, toCurr, dateTime, symbols, information, sessionID);
	}
}
