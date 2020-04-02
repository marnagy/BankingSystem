import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintWriter;
import java.util.Dictionary;

public class DelayedPaymentThread extends Thread {
	private PaymentRequest req;
	public final long sessionID;
	final Dictionary<Integer, Account> accounts;
	final PrintWriter outPrinter;
	final PrintWriter errWriter;
	public DelayedPaymentThread(PaymentRequest paymentRequest, PrintWriter outPrinter, PrintWriter errWriter,
	                            Dictionary<Integer, Account> accounts, long sessionID){
		this.sessionID = sessionID;
		this.req = paymentRequest;
		this.outPrinter = outPrinter;
		this.errWriter = errWriter;
		this.accounts = accounts;
	}
	public boolean isValid(){
		return accounts.get(req.senderAccountID) != null &&
				accounts.get(req.receiverAccountID) != null &&
				req.amount > 0;
	}
	@Override
	public void run(){
		try {
			Thread.sleep(((long)req.hoursDelay * 60 + (long)req.minutesDelay)*60 * 1000, 0);
			PaymentHandler.Run(outPrinter, errWriter, req, accounts, sessionID);
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}
}
