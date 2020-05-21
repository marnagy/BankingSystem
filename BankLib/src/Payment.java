import java.io.*;
import java.time.ZonedDateTime;

/**
 * Stores information about payment
 */
public class Payment {
	/**
	 * Sender's accountID
	 */
	public final int senderAccountID;
	/**
	 * Receiver's accountID
	 */
	public final int receiverAccountID;
	/**
	 * Amount of money to send with 2 decimal places
	 */
	public final long amount;
	/**
	 * Currency from the amount is being taken
	 */
	public final CurrencyType fromCurr;
	/**
	 * Final currency of the payment
	 */
	public final CurrencyType toCurr;
	/**
	 * ZonedDateTime of sending payment
	 */
	public final ZonedDateTime sendingDateTime;
	/**
	 * ZonedDateTime of receiving and processing payment
	 */
	public final ZonedDateTime receivedDateTime;
	/**
	 * Rate for conversion in case of different currencies.
	 * If not needed, it is set to null,
	 */
	public final Double convRate;
	/**
	 * Category of payment
	 */
	public PaymentCategory category;

	/**
	 * Used when loading payment from Payment request object
	 * @param pr Payment request
	 */
	public Payment(PaymentRequest pr) {
		this(pr.senderAccountID, pr.receiverAccountID, pr.amount, pr.fromCurr, pr.toCurr, null,
				pr.sendingDateTime, ZonedDateTime.now(), PaymentCategory.Other);
	}

	/**
	 * Loads payment from Payment request object with a conversion rate
	 * @param pr Payment request
	 * @param convRate Conversion rate in double
	 */
	public Payment(PaymentRequest pr, Double convRate){
		this(pr.senderAccountID, pr.receiverAccountID, pr.amount, pr.fromCurr, pr.toCurr, convRate,
				pr.sendingDateTime, ZonedDateTime.now(), PaymentCategory.Other);
	}

	/**
	 *
	 * @param senderID AccountID of sender
	 * @param receiverID AccountID of receiver
	 * @param amount Amount to send with 2 decimal places
	 * @param fromCurr From CurrencyType
	 * @param toCurr To CurrencyType
	 * @param rate Conversion rate
	 * @param sendingDateTime Datetime of sending
	 * @param receivedDateTime Datetime of receiving
	 * @param category PaymentCategory object
	 */
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

	/**
	 * Loads a payment object from ObjectInput
	 * @param oi ObjectInput object
	 * @return Loaded Payment
	 * @throws IOException Network failure
	 */
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

	/**
	 * Sends this object to ObjectOutput
	 * @param oo ObjectOutput object
	 * @throws IOException Network failure
	 */
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

	/**
	 * Loads a payment from file
	 * @param paymentFile Payment File
	 * @return Payment object
	 * @throws IOException Files failure
	 * @throws InvalidFormatException File name not satisfying format
	 */
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

	/**
	 * Saves payment to file
	 * @param paymentFile File where payment will be saved to
	 * @return File containing payment
	 * @throws IOException Files failure
	 */
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

	/**
	 * Loads ZonedDateTime from custom formatted String
	 * @param text String to be processed
	 * @return ZonedDateTime object
	 */
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

	/**
	 * Saves ZonedDateTime to custom formatted String
	 * @param datetime ZonedDateTime object
	 * @return Formatted String
	 */
	public static String stringify(ZonedDateTime datetime){
		return String.format("%02d%02d%04d-%02d%02d%02d", datetime.getDayOfMonth(), datetime.getMonthValue(),
				datetime.getYear(), datetime.getHour(), datetime.getMinute(), datetime.getSecond());
	}

	/**
	 * Changes category of payment.
	 * Creates new Payment object
	 * @param cat PaymentCategory object
	 * @return new Payment object with given category
	 */
	public Payment WithCategory(PaymentCategory cat){
		return new Payment(senderAccountID, receiverAccountID, amount, fromCurr, toCurr, convRate,
				sendingDateTime, receivedDateTime, cat);
	}

	/**
	 * Creating custom filename specific for the payment object
	 * @return Formatted String
	 */
	public String GetFileName(){
		return this.senderAccountID + "_" + this.receiverAccountID + "_"
				+ Payment.stringify(this.sendingDateTime) + "_" + Payment.stringify(this.receivedDateTime) + ".payment";
	}
}
