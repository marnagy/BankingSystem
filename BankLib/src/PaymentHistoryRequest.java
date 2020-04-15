import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.LocalDate;
import java.time.YearMonth;

public class PaymentHistoryRequest extends Request {
	public final YearMonth monthYear;
	public final int accountID;
	private PaymentHistoryRequest(int accountID, YearMonth date, long sessionID){
		super(RequestType.AccountHistory, sessionID);
		this.accountID = accountID;
		monthYear = date;
	}
	public PaymentHistoryRequest(YearMonth monthYear, Account account, long sessionID){
		this(account.accountID, monthYear, sessionID);
	}
	private PaymentHistoryRequest(YearMonth monthYear, int accountID, long sessionID){
		this(accountID, monthYear, sessionID);
	}

	@Override
	public void send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);

		oo.writeInt(accountID);
		oo.writeInt(monthYear.getYear());
		oo.writeInt(monthYear.getMonthValue());

		oo.flush();
	}
	public static PaymentHistoryRequest readArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		int accountID = oi.readInt();
		int year = oi.readInt();
		int month = oi.readInt();

		//return new PaymentHistoryRequest(new MonthYear(month, year), accountID, sessionID);
		return new PaymentHistoryRequest( YearMonth.of(year, month), accountID, sessionID);
	}
}
