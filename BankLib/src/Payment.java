import javax.naming.InvalidNameException;
import java.io.*;
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

	public static Payment FromObjInput(ObjectInput oi) throws IOException {
		int senderAccountID = oi.readInt();
		int receiverAccountID = oi.readInt();
		long amount = oi.readLong();
		CurrencyType fromCurr =  CurrencyType.values()[oi.readInt()];
		CurrencyType toCurr = CurrencyType.values()[oi.readInt()];
		ZonedDateTime sendingDateTime = destringify(oi.readUTF());
		ZonedDateTime receivedDateTime = destringify(oi.readUTF());
		PaymentCategory category = PaymentCategory.values()[oi.readInt()];
		return new Payment(senderAccountID, receiverAccountID, amount, fromCurr, toCurr,
				sendingDateTime, receivedDateTime, category);
	}

	public void send(ObjectOutput oo) throws IOException {
		oo.writeInt(senderAccountID);
		oo.writeInt(receiverAccountID);
		oo.writeLong(amount);
		oo.writeInt(fromCurr.ordinal());
		oo.writeInt(toCurr.ordinal());
		oo.writeUTF(stringify(sendingDateTime));
		oo.writeUTF(stringify(receivedDateTime));
		oo.writeInt(category.ordinal());
		oo.flush();
	}
	public static Payment fromFile(File paymentFile) throws IOException, InvalidFormatException {
		if (!paymentFile.getName().endsWith(".payment")){
			throw new InvalidFormatException("Payment file doesn't end with '.payment'");
		}
		String[] nameParts = paymentFile.getName().split("\\.")[0].split("_");
		if (nameParts.length == 4){
			int senderAccountID = Integer.parseInt(nameParts[0]);
			int receiverAccountID = Integer.parseInt(nameParts[1]);
			ZonedDateTime sendingDateTime = destringify(nameParts[2]);
			ZonedDateTime receivedDateTime = destringify(nameParts[3]);
			CurrencyType fromCurr;
			CurrencyType toCurr;
			long amount;
			try(BufferedReader br = new BufferedReader(new FileReader(paymentFile))){
				amount = Long.parseLong(br.readLine());
				fromCurr = CurrencyType.valueOf(br.readLine().split(":")[1]);
				toCurr = CurrencyType.valueOf(br.readLine().split(":")[1]);
			}
			return new Payment(senderAccountID, receiverAccountID, amount, fromCurr, toCurr,
					sendingDateTime, receivedDateTime, PaymentCategory.Other);
		}
		else{
			throw new IOException("Illegal name of file: " + paymentFile.getAbsolutePath());
		}
	}
	public static ZonedDateTime destringify(String text){
		ZonedDateTime datetime = ZonedDateTime.now();
		String[] textPart = text.split("-");
		long date = Long.parseLong(textPart[0]);
		int year = (int)date % 10_000;
		date = date / 10_000;
		int month = (int)date % 100;
		date = date / 100;
		int day = (int)date % 100;
		int time = Integer.parseInt(textPart[1]);
		int seconds = time % 100;
		time = time / 100;
		int hour = time / 100;
		int minutes = time % 100;
		return datetime.withHour(hour).withMinute(minutes).withSecond(seconds).
				withYear(year).withMonth(month).withDayOfMonth(day);
	}

	public static String stringify(ZonedDateTime datetime){
		return String.format("%02d%02d%04d-%02d%02d%02d", datetime.getDayOfMonth(), datetime.getMonthValue(),
				datetime.getYear(), datetime.getHour(), datetime.getMinute(), datetime.getSecond());
	}

	public Payment WithCategory(PaymentCategory cat){
		return new Payment(senderAccountID, receiverAccountID, amount, fromCurr, toCurr,
				sendingDateTime, receivedDateTime, cat);
	}
}
