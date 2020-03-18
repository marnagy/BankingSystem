import java.io.*;
import java.util.Map;
import java.util.Set;

public class PaymentHandler {
	static Set<Integer> accountIDs;
	static long sessionID;
	public static void Run(PrintWriter outPrinter, PrintWriter errPrinter,
	                       ObjectInput oi, ObjectOutput oo, Map<Integer, Account> accounts,
	                       long sessionID) throws IOException {
		try {
			PaymentRequest pr = PaymentRequest.ReadArgs(oi);
			Payment payment = MakePayment(pr, accounts);
			if ( payment == null ){
				outPrinter.println("Check receiver's ID and amount.");
				outPrinter.flush();
			}
			else{
				outPrinter.println("Payment made.");
				outPrinter.println("Creating file for payment...");
				CreatePaymentFile(payment);
			}
		} catch (IOException e) {
			e.printStackTrace(errPrinter);
			new UnknownErrorResponse("Unknown I/O error", sessionID).Send(oo);
		} catch (ClassNotFoundException e) {
			e.printStackTrace(errPrinter);
		}
	}

	private static void CreatePaymentFile(Payment payment) throws IOException {
		File paymentFile = new File(MasterServerSession.PaymentsFolder.getAbsolutePath() + MasterServerSession.FileSystemSeparator
				+ payment.senderAccountID + "_" + payment.receiverAccountID + "_" + System.nanoTime());
		if (paymentFile.createNewFile()){

		}
	}

	private static Payment MakePayment(PaymentRequest pr, Map<Integer, Account> accounts) {
		int senderID = pr.senderAccountID;
		int receiverID = pr.receiverAccountID;

		// wrong IDs
		if ( accounts.get(senderID) == null || accounts.get(receiverID) == null){
			return null;
		}

		Account senderAccount = accounts.get(senderID);
		Account receiverAccount = accounts.get(receiverID);

		synchronized (senderAccount){
			synchronized (receiverAccount){
				if (senderAccount.Values.get(pr.curr) == null){
					senderAccount.Values.put(pr.curr, 0L);
				}
				Long senderAmount = senderAccount.Values.get(pr.curr);
				if (senderAmount <= pr.amount){
					return null;
				}

				if (receiverAccount.Values.get(pr.curr) == null){
					receiverAccount.Values.put(pr.curr, 0L);
				}
				Long receiverAmount = senderAccount.Values.get(pr.curr);

				// because wrapper types are immutable
				senderAccount.Values.put(pr.curr, senderAmount - pr.amount);
				receiverAccount.Values.put(pr.curr, receiverAmount + pr.amount);
			}
		}
		return new Payment(pr);
	}
}
