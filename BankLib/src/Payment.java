import java.time.ZonedDateTime;

public class Payment {
	public final int senderAccountID, receiverAccountID;
	public final long amount;
	public final CurrencyType curr;
	public final ZonedDateTime sendingDateTime, receivedDateTime;
	public Payment(PaymentRequest pr) {
		receivedDateTime = ZonedDateTime.now();

		senderAccountID = pr.senderAccountID;
		receiverAccountID = pr.receiverAccountID;
		amount = pr.amount;
		curr = pr.curr;
		sendingDateTime = pr.sendingDateTime;
	}
}
