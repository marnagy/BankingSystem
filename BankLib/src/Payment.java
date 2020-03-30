import java.io.File;
import java.io.IOException;
import java.io.ObjectOutput;
import java.time.ZonedDateTime;
import java.util.regex.Pattern;

public class Payment {
	public final int senderAccountID, receiverAccountID;
	public final long amount;
	public final CurrencyType fromCurr, toCurr;
	public final ZonedDateTime sendingDateTime, receivedDateTime;
	public PaymentCategory category;
	public Payment(PaymentRequest pr) {
		receivedDateTime = ZonedDateTime.now();

		senderAccountID = pr.senderAccountID;
		receiverAccountID = pr.receiverAccountID;
		amount = pr.amount;
		fromCurr = pr.fromCurr;
		toCurr = pr.toCurr;
		sendingDateTime = pr.sendingDateTime;
		category = PaymentCategory.Other;
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
	}
	public static Payment FromFile(File paymentFile){
		String[] nameParts = paymentFile.getName().split("_");
	}
}
