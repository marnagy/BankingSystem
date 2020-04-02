import com.sun.mail.smtp.SMTPTransport;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class PaymentHandler {
	static Set<Integer> accountIDs;
	static long sessionID;
	public static Response Run(PrintWriter outPrinter, PrintWriter errPrinter,
	                                        ObjectInput oi, ObjectOutput oo, Dictionary<Integer, Account> accounts,
	                                        long sessionID) throws IOException {
		File paymentFile = null;
		Payment payment = null;
		try {
			PaymentRequest pr = PaymentRequest.ReadArgs(oi);
			if (pr.hoursDelay > 0 || pr.minutesDelay > 0){
				DelayedPaymentThread thread = new DelayedPaymentThread(pr,
						outPrinter, errPrinter, accounts, sessionID);
				if (thread.isValid()){
					thread.start();
					return new SuccessResponse(sessionID);
				}
				else{
					return new IllegalRequestResponse(sessionID);
				}
			}
			else{
				payment = Run(outPrinter, errPrinter, pr, accounts, sessionID);
				return new SuccessPaymentResponse( payment, sessionID);
			}

		} catch (IOException e) {
			e.printStackTrace(errPrinter);
			return new UnknownErrorResponse("I/O error: " + e.getMessage(), sessionID);
		} catch (ClassNotFoundException | NullPointerException e) {
			e.printStackTrace(errPrinter);
			return new UnknownErrorResponse("Server error: " + e.getMessage(), sessionID);
		}
	}
	public static Payment Run(PrintWriter outPrinter, PrintWriter errPrinter,
	                  PaymentRequest req, Dictionary<Integer, Account> accounts,
	                  long sessionID) throws IOException {
		File paymentFile;
		Payment payment = MakePayment(req, accounts, errPrinter);
		if ( payment == null ){
			// TO-DO
			outPrinter.println("Check receiver's ID and amount.");
			outPrinter.flush();
			throw new IOException("wrong ID or amount");
		}
		else{
			outPrinter.println("Payment made.");
			outPrinter.println("Creating file for payment...");
			paymentFile = CreatePaymentFile(payment, errPrinter);
			SavePaymentToAccounts(payment, paymentFile.getName(), MasterServerSession.AccountsFolder.getAbsolutePath());
			SendEmail(payment);
		}
		return payment;
	}

	private static void SendEmail(Payment payment) {
		Properties prop = new Properties();
		prop.put("mail.smtp.auth", true);
		prop.put("mail.smtp.starttls.enable", "true");
		prop.put("mail.smtp.host", "smtp.gmail.com");
		prop.put("mail.smtp.port", "587");

		String myAccount = MasterServerSession.emailAddr;
		String myPasswd = new String(MasterServerSession.emailPasswd);
		String recipientAddr = GetRecipientAddr(payment);

		Session session = Session.getDefaultInstance(prop, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(myAccount, myPasswd);
			}
		});

		Message msg = new MimeMessage(session);
		try {
			msg.setFrom(new InternetAddress(myAccount));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientAddr));
			msg.setSubject("Payment received");
			msg.setText(CreateEmailText(payment));
			Transport.send(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String CreateEmailText(Payment payment) {
		return "To your account was sent " + String.format("%.2f",payment.amount / 100D) + " " + payment.fromCurr + " converted to " +
				payment.toCurr + ".\n\nYour BankingApp";
	}

	private static String GetRecipientAddr(Payment payment) {
		try(BufferedReader br = new BufferedReader(new FileReader(MasterServerSession.AccountsFolder.getAbsolutePath() +
				MasterServerSession.FileSystemSeparator + payment.receiverAccountID + MasterServerSession.FileSystemSeparator +
				".info"))){
			return br.readLine();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
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
			fw.write( paymentFileName + ":" + PaymentCategory.Other + "\n");
		}
		currMonthPaymentsReceiverFile.createNewFile(); // if not created, create new empty file
		try (FileWriter fw = new FileWriter(currMonthPaymentsReceiverFile.getAbsolutePath(), true)){
			fw.write( paymentFileName + ":" + PaymentCategory.Other + "\n");
		}
	}

	private static synchronized File CreatePaymentFile(Payment payment, PrintWriter errPrinter) throws IOException {
		String paymentFileName = MasterServerSession.PaymentsFolder.getAbsolutePath() + MasterServerSession.FileSystemSeparator
				+ payment.senderAccountID + "_" + payment.receiverAccountID + "_"
				+ Payment.Stringify(payment.sendingDateTime) + "_" + Payment.Stringify(payment.receivedDateTime) + ".payment";
		File paymentFile = new File(paymentFileName);
		if (paymentFile.createNewFile()){
			try(PrintWriter pw = new PrintWriter(paymentFile)){
				pw.println(payment.amount);
				pw.println("from:" + payment.fromCurr.name());
				pw.println("to:" + payment.toCurr.name());
				// each account can set different category in their history
				//pw.println("category:" + payment.category.name());
			}
		}
		return paymentFile;
	}

	private static synchronized Payment MakePayment(PaymentRequest pr, Dictionary<Integer, Account> accounts, PrintWriter errPrinter) throws IOException {
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
		Dictionary<CurrencyType, Long> Values = Account.Values;
		try( var bw = new BufferedWriter( new FileWriter(MasterServerSession.AccountsFolder.getAbsolutePath()
		+ MasterServerSession.FileSystemSeparator + Account.accountID + MasterServerSession.FileSystemSeparator
		+ ".curr"))){
			Set<CurrencyType> keySet = Collections.synchronizedSet(new HashSet(Collections.list(Values.keys())) );
			for ( CurrencyType curr: keySet) {
				bw.write( curr.name() + ":" + Values.get(curr) + "\n");
			}
		}

	}
}
