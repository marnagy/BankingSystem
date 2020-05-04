import java.io.*;
import java.time.ZonedDateTime;

public class Payment {
	public final int senderAccountID, receiverAccountID;
	public final long amount;
	public final CurrencyType fromCurr, toCurr;
	public final ZonedDateTime sendingDateTime, receivedDateTime;
	public final Double convRate;
	public PaymentCategory category;

	public Payment(PaymentRequest pr) {
		this(pr.senderAccountID, pr.receiverAccountID, pr.amount, pr.fromCurr, pr.toCurr, null,
				pr.sendingDateTime, ZonedDateTime.now(), PaymentCategory.Other);
	}
	public Payment(PaymentRequest pr, Double convRate){
		this(pr.senderAccountID, pr.receiverAccountID, pr.amount, pr.fromCurr, pr.toCurr, convRate,
				pr.sendingDateTime, ZonedDateTime.now(), PaymentCategory.Other);
	}
	private Payment(int senderID, int receiverID, long amount, CurrencyType fromCurr, CurrencyType toCurr, Double rate,
	                ZonedDateTime sendingDateTime, ZonedDateTime receivedDateTime, PaymentCategory category){
		this.receivedDateTime = receivedDateTime;
		this.senderAccountID = senderID;
		this.receiverAccountID = receiverID;
		this.amount = amount;
		this.fromCurr = fromCurr;
		this.toCurr = toCurr;
		this.sendingDateTime = sendingDateTime;
		this.category = category;
		convRate = rate;
	}

	public static Payment FromObjInput(ObjectInput oi) throws IOException {
		int senderAccountID = oi.readInt();
		int receiverAccountID = oi.readInt();
		long amount = oi.readLong();
		CurrencyType fromCurr =  CurrencyType.values()[oi.readInt()];
		CurrencyType toCurr = CurrencyType.values()[oi.readInt()];
		Double convRate = oi.readDouble();
		ZonedDateTime sendingDateTime = destringify(oi.readUTF());
		ZonedDateTime receivedDateTime = destringify(oi.readUTF());
		PaymentCategory category = PaymentCategory.values()[oi.readInt()];
		return new Payment(senderAccountID, receiverAccountID, amount, fromCurr, toCurr, convRate,
				sendingDateTime, receivedDateTime, category);
	}

	public void send(ObjectOutput oo) throws IOException {
		oo.writeInt(senderAccountID);
		oo.writeInt(receiverAccountID);
		oo.writeLong(amount);
		oo.writeInt(fromCurr.ordinal());
		oo.writeInt(toCurr.ordinal());
		oo.writeDouble(convRate);
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
			Double convRate;
			long amount;
			try(BufferedReader br = new BufferedReader(new FileReader(paymentFile))){
				amount = Long.parseLong(br.readLine());
				convRate = Double.parseDouble(br.readLine().split(":")[1]);
				fromCurr = CurrencyType.valueOf(br.readLine().split(":")[1]);
				toCurr = CurrencyType.valueOf(br.readLine().split(":")[1]);
			}
			return new Payment(senderAccountID, receiverAccountID, amount, fromCurr, toCurr, convRate,
					sendingDateTime, receivedDateTime, PaymentCategory.Other);
		}
		else{
			throw new IOException("Illegal name of file: " + paymentFile.getAbsolutePath());
		}
	}
	public File toFile(File paymentFile) throws IOException {
		if (paymentFile.createNewFile()) {
			try (PrintWriter pw = new PrintWriter(paymentFile)) {
				pw.println(this.amount);
				pw.println("rate:" + this.convRate);
				pw.println("from:" + this.fromCurr.name());
				pw.println("to:" + this.toCurr.name());
				// each account can set different category in their history
			}
			return paymentFile;
		}
		else{
			throw new IOException("Failed to create payment file.");
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
		return new Payment(senderAccountID, receiverAccountID, amount, fromCurr, toCurr, convRate,
				sendingDateTime, receivedDateTime, cat);
	}
	public String GetFileName(){
		return this.senderAccountID + "_" + this.receiverAccountID + "_"
				+ Payment.stringify(this.sendingDateTime) + "_" + Payment.stringify(this.receivedDateTime) + ".payment";
	}
}
