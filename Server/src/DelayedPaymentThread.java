import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class DelayedPaymentThread implements Runnable {
	private PaymentRequest req;
	public final long sessionID;
	final Map<Integer, Account> accounts;
	final PrintWriter outPrinter;
	final PrintWriter errWriter;

	final File accountsFolder;
	final File paymentsFolder;

	final String emailAddr;
	final char[] emailPasswd;
	public DelayedPaymentThread(PaymentRequest paymentRequest, PrintWriter outPrinter, PrintWriter errWriter,
	                            Map<Integer, Account> accounts, File accountsFolder, File paymentsFolder,
	                            String emailAddr, char[] emailPasswd, long sessionID){
		this.sessionID = sessionID;
		this.req = paymentRequest;
		this.outPrinter = outPrinter;
		this.errWriter = errWriter;
		this.accounts = accounts;

		this.accountsFolder = accountsFolder;
		this.paymentsFolder = paymentsFolder;

		this.emailAddr = emailAddr;
		this.emailPasswd = emailPasswd;
	}
	public boolean isValid(){
		return accounts.get(req.senderAccountID) != null &&
				accounts.get(req.receiverAccountID) != null &&
				req.amount > 0;
	}
	@Override
	public void run(){
		try {
			PaymentHandler.run(outPrinter, errWriter, accountsFolder, paymentsFolder, req, accounts,
					emailAddr, emailPasswd, sessionID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
