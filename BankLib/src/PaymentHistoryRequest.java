import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.YearMonth;

public class PaymentHistoryRequest extends Request {
	public final YearMonth monthYear;
	public final int accountID;

	/**
	 * Constructor used when loading from ObjectInput
	 * @param accountID accountID
	 * @param date YearMonth of the history
	 * @param sessionID Long identifier of session
	 */
	private PaymentHistoryRequest(int accountID, YearMonth date, long sessionID){
		super(RequestType.AccountHistory, sessionID);
		this.accountID = accountID;
		monthYear = date;
	}
	public PaymentHistoryRequest(YearMonth monthYear, Account account, long sessionID){
		this(account.accountID, monthYear, sessionID);
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

		oo.writeInt(accountID);
		oo.writeInt(monthYear.getYear());
		oo.writeInt(monthYear.getMonthValue());

		oo.flush();
	}

	/**
	 * Used to load this object from ObjectInput
	 * @param oi ObjectInput object
	 * @return Request object
	 * @throws IOException Network failure
	 */
	public static PaymentHistoryRequest readArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		int accountID = oi.readInt();
		int year = oi.readInt();
		int month = oi.readInt();

		return new PaymentHistoryRequest( accountID, YearMonth.of(year, month), sessionID);
	}
}
