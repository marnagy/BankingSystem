import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.ZonedDateTime;

/**
 * Sends request for payment
 */
public class PaymentRequest extends Request {
	/**
	 * Sender's accountID
	 */
	public final int senderAccountID;
	/**
	 * Receiver's accountID
	 */
	public final int receiverAccountID;
	/**
	 * Amount of hours to delay the payment
	 */
	public final int hoursDelay;
	/**
	 * Amount of minutes to delay the payment
	 */
	public final int minutesDelay;
	/**
	 * Amount of money to send with 2 decimal places
	 */
	public final long amount;
	/**
	 * Containing 2 Strings: Variable and Specific symbols for the payment
	 */
	public final String[] symbols;
	/**
	 * Information about the payment
	 */
	public final String information;
	/**
	 * Currency from the amount is being taken
	 */
	public final CurrencyType fromCurr;
	/**
	 * Final currency of the payment
	 */
	public final CurrencyType toCurr;
	/**
	 * ZonedDateTime of receiving and processing payment
	 */
	public final ZonedDateTime sendingDateTime;

	/**
	 * Creates PaymentRequest object
	 * @param senderAccountID AccountID of sender
	 * @param receiverAccountID AccountID of receiver
	 * @param amount Amount to send with 2 decimal places
	 * @param hoursDelay Delay for hours
	 * @param minutesDelay Delay for minutes
	 * @param fromCurr From CurrencyType
	 * @param toCurr To CurrencyType
	 * @param symbols Variable and specific symbols of the payment
	 *                Both are of type String
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
	 * Private load without adding new information
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
	 * Sends this object to ObjectOutput
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
	 * Loads this object from ObjectInput
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
