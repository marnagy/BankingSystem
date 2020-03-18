import java.io.*;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;

public class PaymentHandler {
	static Set<Integer> accountIDs;
	static long sessionID;
	public static synchronized Response Run(PrintWriter outPrinter, PrintWriter errPrinter,
	                                        ObjectInput oi, ObjectOutput oo, Map<Integer, Account> accounts,
	                                        long sessionID) throws IOException {
		String paymentFilePath = null; // just init
		File paymentFile = null;
		try {
			PaymentRequest pr = PaymentRequest.ReadArgs(oi);
			Payment payment = MakePayment(pr, accounts, errPrinter);
			if ( payment == null ){
				outPrinter.println("Check receiver's ID and amount.");
				outPrinter.flush();
			}
			else{
				outPrinter.println("Payment made.");
				outPrinter.println("Creating file for payment...");
				paymentFile = CreatePaymentFile(payment, errPrinter);
				SavePaymentToAccounts(payment, paymentFile.getAbsolutePath(), MasterServerSession.AccountsFolder.getAbsolutePath());
			}
		} catch (IOException e) {
			e.printStackTrace(errPrinter);
			return new UnknownErrorResponse("I/O error", sessionID);
		} catch (ClassNotFoundException e) {
			e.printStackTrace(errPrinter);
			return new UnknownErrorResponse("Server error", sessionID);
		}
		return new SuccessPaymentResponse( paymentFile.getName(), sessionID);
	}

	private static void SavePaymentToAccounts(Payment payment, String paymentFilePath, String accountsFolderPath) throws IOException {
		int senderAccountID = payment.senderAccountID;
		int receiverAccountID = payment.receiverAccountID;
		ZonedDateTime dateTime = ZonedDateTime.now();
		int year = dateTime.getYear();
		int month = dateTime.getMonthValue();

		File currMonthPaymentsSenderFile = new File(accountsFolderPath + MasterServerSession.FileSystemSeparator
				+ senderAccountID + MasterServerSession.FileSystemSeparator + year + "_" + month);
		File currMonthPaymentsReceiverFile = new File(accountsFolderPath + MasterServerSession.FileSystemSeparator
				+ receiverAccountID + MasterServerSession.FileSystemSeparator + year + "_" + month);
		currMonthPaymentsSenderFile.createNewFile(); // if not created, create new empty file
		try (FileWriter fw = new FileWriter(currMonthPaymentsSenderFile.getAbsolutePath(), true)){
			fw.write( paymentFilePath + ":" + "Unknown" + "\n");
		}
		currMonthPaymentsReceiverFile.createNewFile(); // if not created, create new empty file
		try (FileWriter fw = new FileWriter(currMonthPaymentsReceiverFile.getAbsolutePath(), true)){
			fw.write( paymentFilePath + ":" + "Unknown" + "\n");
		}
	}

	private static File CreatePaymentFile(Payment payment, PrintWriter errPrinter) throws IOException {
		String paymentFileName = MasterServerSession.PaymentsFolder.getAbsolutePath() + MasterServerSession.FileSystemSeparator
				+ payment.senderAccountID + "_" + payment.receiverAccountID + "_"
				+ payment.sendingDateTime + "_" + payment.receivedDateTime + ".payment";
		File paymentFile = new File(paymentFileName);
		String paymentFilePath = paymentFile.getAbsolutePath();
		if (paymentFile.createNewFile()){
			try(PrintWriter pw = new PrintWriter(paymentFile)){
				pw.println(payment.amount);
				pw.println(payment.curr.name());
			}
		}
		return paymentFile;
	}

	private static Payment MakePayment(PaymentRequest pr, Map<Integer, Account> accounts, PrintWriter errPrinter) throws IOException {
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
				ModifyValueFiles(senderAccount,errPrinter);
				ModifyValueFiles(receiverAccount, errPrinter);
			}
		}
		return new Payment(pr);
	}

	private static synchronized void ModifyValueFiles(Account Account, PrintWriter errPrinter) throws IOException {
		Map<CurrencyType, Long> Values = Account.Values;
		try( var bw = new BufferedWriter( new FileWriter(MasterServerSession.AccountsFolder.getAbsolutePath()
		+ MasterServerSession.FileSystemSeparator + Account.accountID + MasterServerSession.FileSystemSeparator
		+ ".curr"))){
			Set<CurrencyType> keySet = Values.keySet();
			for ( CurrencyType curr: keySet) {
				bw.write( curr.name() + ":" + Values.get(curr));
			}
		}

	}
}
