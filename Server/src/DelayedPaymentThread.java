import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Runnable object for delaying payment
 */
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

	/**
	 * Runnable object used when payment is delayed
	 * @param paymentRequest Payment request used
	 * @param outPrinter Writer for OUT
	 * @param errWriter Writer for ERR
	 * @param accounts Map from accountIDs to Account objects
	 * @param accountsFolder Folder containing all accounts
	 * @param paymentsFolder Folder containing all payments
	 * @param emailAddr Email address to send confirmation email from
	 * @param emailPasswd Password to the email
	 * @param sessionID Long identifier of session
	 */
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

	/**
	 * Check of the IDs are valid
	 * @return Success
	 */
	public boolean isValid(){
		return accounts.get(req.senderAccountID) != null &&
				accounts.get(req.receiverAccountID) != null &&
				req.amount > 0;
	}
	@Override
	/**
	 * Method to start payment action.
	 */
	public void run(){
		try {
			PaymentHandler.run(outPrinter, errWriter, accountsFolder, paymentsFolder, req, accounts,
					emailAddr, emailPasswd, sessionID);
		} catch (IOException e) {
			e.printStackTrace(errWriter);
		}
	}
}
