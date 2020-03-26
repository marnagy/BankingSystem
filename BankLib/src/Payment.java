import java.time.ZonedDateTime;

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
}
