import java.io.File;
import java.io.IOException;
import java.io.ObjectOutput;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.regex.Pattern;

public class Payment {
	public final int senderAccountID, receiverAccountID;
	public final long amount;
	public final CurrencyType fromCurr, toCurr;
	public final ZonedDateTime sendingDateTime, receivedDateTime;
	public PaymentCategory category;
	public Payment(PaymentRequest pr) {
		this(pr.senderAccountID, pr.receiverAccountID, pr.amount, pr.fromCurr, pr.toCurr,
				pr.sendingDateTime, ZonedDateTime.now(), PaymentCategory.Other);
	}
	private Payment(int senderID, int receiverID, long amount, CurrencyType fromCurr, CurrencyType toCurr,
	                ZonedDateTime sendingDateTime, ZonedDateTime receivedDateTime, PaymentCategory category){
		this.receivedDateTime = receivedDateTime;

		this.senderAccountID = senderID;
		this.receiverAccountID = receiverID;
		this.amount = amount;
		this.fromCurr = fromCurr;
		this.toCurr = toCurr;
		this.sendingDateTime = sendingDateTime;
		this.category = category;
	}
	public void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(senderAccountID);
		oo.writeInt(receiverAccountID);
		oo.writeLong(amount);
		oo.writeInt(fromCurr.ordinal());
		oo.writeInt(toCurr.ordinal());
		oo.writeInt(sendingDateTime.getNano());
		oo.writeInt(receivedDateTime.getNano());
		oo.writeInt(category.ordinal());
		oo.flush();
	}
	public static Payment FromFile(File paymentFile) throws IOException {
		String[] nameParts = paymentFile.getName().split("_");
		if (nameParts.length == 4){
			int senderAccountID = Integer.parseInt(nameParts[0]);
			int receiverAccountID = Integer.parseInt(nameParts[1]);
			//ZonedDateTime sendingDateTime = ZonedDateTime.
			return null;
		}
		else{
			throw new IOException("Illegal name of file: " + paymentFile.getAbsolutePath());
		}
	}
	private static ZonedDateTime Destringify(String text){
		ZonedDateTime datetime = ZonedDateTime.now();
		String[] textPart = text.split("-");
		long date = Long.parseLong(textPart[0]);
		int year = (int)date % 10_000;
		date = date / 10_000;
		int month = (int)date % 100;
		date = date / 100;
		int day = (int)date % 100;
		int time = Integer.parseInt(textPart[1]);
		int hour = time/100;
		int minutes = time % 100;
		return datetime.withHour(hour).withMinute(minutes).
				withYear(year).withMonth(month).withDayOfMonth(day);
	}
}
