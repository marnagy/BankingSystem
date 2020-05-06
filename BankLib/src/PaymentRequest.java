import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.ZonedDateTime;

public class PaymentRequest extends Request {
	public final int senderAccountID, receiverAccountID;
	public final int hoursDelay, minutesDelay;
	public final long amount;
	public final String[] symbols;
	public final String information;
	public final CurrencyType fromCurr, toCurr;
	public final ZonedDateTime sendingDateTime;

	/**
	 * Constructor used when creating PaymentRequest object
	 * @param senderAccountID AccountID of sender
	 * @param receiverAccountID AccountID of receiver
	 * @param amount Amount to send with 2 decimal places
	 * @param hoursDelay Delay for hours
	 * @param minutesDelay Delay for minutes
	 * @param fromCurr From CurrencyType
	 * @param toCurr To CurrencyType
	 * @param symbols Variable and specific symbols
	 * @param information Information for receiver
	 * @param sessionID Long identifier of session
	 */
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

	/**
	 * Constructor used when loading from ObjectInput
	 * @param senderAccountID AccountID of sender
	 * @param receiverAccountID AccountID of receiver
	 * @param amount Amount to send with 2 decimal places
	 * @param hoursDelay Delay for hours
	 * @param minutesDelay Delay for minutes
	 * @param fromCurr From CurrencyType
	 * @param toCurr To CurrencyType
	 * @param dateTime ZonedDateTime of creation
	 * @param symbols Variable and specific symbols
	 * @param information Information for receiver
	 * @param sessionID Long identifier of session
	 */
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

	/**
	 * Used to send this object to ObjectOutput
	 * @param oo ObjectOutput object
	 * @throws IOException Network failure
	 */
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

	/**
	 * Used to load this object from ObjectInput
	 * @param oi ObjectInput object
	 * @return Request object
	 * @throws IOException Network failure
	 * @throws ClassNotFoundException LoadingClass failure
	 */
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
