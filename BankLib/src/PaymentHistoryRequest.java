import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.LocalDate;

public class PaymentHistoryRequest extends Request {
	public final MonthYear monthYear;
	public final int accountID;
	private PaymentHistoryRequest(int accountID, MonthYear date, long sessionID){
		super(RequestType.AccountHistory, sessionID);
		this.accountID = accountID;
		monthYear = date;
	}
	public PaymentHistoryRequest(MonthYear monthYear, Account account, long sessionID){
		this(account.accountID, monthYear, sessionID);
	}
	private PaymentHistoryRequest(MonthYear monthYear, int accountID, long sessionID){
		this(accountID, monthYear, sessionID);
	}

	@Override
	public void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);

		oo.writeInt(accountID);
		oo.writeInt(monthYear.year);
		oo.writeInt(monthYear.month.getValue());

		oo.flush();
	}
	public static PaymentHistoryRequest ReadArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		int accountID = oi.readInt();
		int year = oi.readInt();
		int month = oi.readInt();
		try {
			return new PaymentHistoryRequest(new MonthYear(month, year), accountID, sessionID);
		}
		catch (InvalidFormatException e){
			return null;
		}
	}
}
