import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class PaymentHandler {
	static Set<Integer> accountIDs;
	static long sessionID;
	public static Response Run(PrintWriter outPrinter, PrintWriter errPrinter,
	                                        ObjectInput oi, ObjectOutput oo, Map<Integer, Account> accounts,
	                                        long sessionID) throws IOException {
		String paymentFilePath = null; // just init
		File paymentFile = null;
		try {
			PaymentRequest pr = PaymentRequest.ReadArgs(oi);
			Payment payment = MakePayment(pr, accounts, errPrinter);
			if ( payment == null ){
				// TO-DO
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

	private static synchronized void SavePaymentToAccounts(Payment payment, String paymentFileName, String accountsFolderPath) throws IOException {
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
			fw.write( paymentFileName + ":" + "Unknown" + "\n");
		}
		currMonthPaymentsReceiverFile.createNewFile(); // if not created, create new empty file
		try (FileWriter fw = new FileWriter(currMonthPaymentsReceiverFile.getAbsolutePath(), true)){
			fw.write( paymentFileName + ":" + "Unknown" + "\n");
		}
	}

	private static synchronized File CreatePaymentFile(Payment payment, PrintWriter errPrinter) throws IOException {
//		String sendingDate = DateTimeToString(payment.sendingDateTime);
//		String receivedDate = DateTimeToString(payment.receivedDateTime);

		String paymentFileName = MasterServerSession.PaymentsFolder.getAbsolutePath() + MasterServerSession.FileSystemSeparator
				+ payment.senderAccountID + "_" + payment.receiverAccountID + "_"
				+ payment.sendingDateTime.getNano() + "_" + payment.receivedDateTime.getNano() + ".payment";
		File paymentFile = new File(paymentFileName);
		if (paymentFile.createNewFile()){
			try(PrintWriter pw = new PrintWriter(paymentFile)){
				pw.println(payment.amount);
				pw.println("from:" + payment.fromCurr.name());
				pw.println("to:" + payment.toCurr.name());
			}
		}
		return paymentFile;
	}

	private static String DateTimeToString(ZonedDateTime dateTime) {
		StringBuilder sb = new StringBuilder();

		sb.append(dateTime.getYear());
		sb.append('_');
		sb.append(dateTime.getMonthValue());
		sb.append('_');
		sb.append(dateTime.getDayOfMonth());
		sb.append('-');
		sb.append(dateTime.getHour());
		sb.append(':');
		sb.append(dateTime.getMinute());
		sb.append('_');
		sb.append(dateTime.getSecond());

		return sb.toString();
	}

	private static synchronized Payment MakePayment(PaymentRequest pr, Map<Integer, Account> accounts, PrintWriter errPrinter) throws IOException {
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
				Long senderAmount = senderAccount.Values.get(pr.fromCurr);
				if (senderAmount <= pr.amount){
					return null;
				}
				Long receiverAmount = senderAccount.Values.get(pr.toCurr);
				long amount = pr.amount;
				if (pr.fromCurr != pr.toCurr){
					amount = Convert(amount, pr.fromCurr, pr.toCurr);
				}
				// because wrapper types are immutable
				senderAccount.Values.put(pr.fromCurr, senderAmount - pr.amount);
				receiverAccount.Values.put(pr.toCurr, receiverAmount + amount);
				ModifyValueFiles(senderAccount,errPrinter);
				ModifyValueFiles(receiverAccount, errPrinter);
			}
		}
		return new Payment(pr);
	}

	private static long Convert(long amount, CurrencyType from, CurrencyType to){
		try {
			String addr = "https://api.exchangeratesapi.io/latest?symbols=" + from.name() + "," + "to";
			URL url = new URL(addr);
			StringBuffer sb = new StringBuffer();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader( url.openStream() )) ){
				reader.lines().forEach(x -> sb.append(x + "\n"));
				System.out.println(sb);
				return -1;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	private static synchronized void ModifyValueFiles(Account Account, PrintWriter errPrinter) throws IOException {
		Map<CurrencyType, Long> Values = Account.Values;
		try( var bw = new BufferedWriter( new FileWriter(MasterServerSession.AccountsFolder.getAbsolutePath()
		+ MasterServerSession.FileSystemSeparator + Account.accountID + MasterServerSession.FileSystemSeparator
		+ ".curr"))){
			Set<CurrencyType> keySet = Values.keySet();
			for ( CurrencyType curr: keySet) {
				bw.write( curr.name() + ":" + Values.get(curr) + "\n");
			}
		}

	}
}
