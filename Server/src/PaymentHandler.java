import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.YearMonth;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaymentHandler {
	static Set<Integer> accountIDs;
	static long sessionID;
	private static ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(Thread.activeCount());

	static Pattern exchangePattern = Pattern.compile("[1-9]*[0-9](\\.[0-9]+)");
	public static Response Run(PrintWriter outPrinter, PrintWriter errPrinter, File accountsFolder, File paymentsFolder,
	                           PaymentRequest pr, Map<Integer, Account> accounts,
	                           String emailAddr, char[] emailPasswd, long sessionID) {
		Payment payment;
		try {
			if (pr.hoursDelay > 0 || pr.minutesDelay > 0){
				DelayedPaymentThread thread = new DelayedPaymentThread(pr, outPrinter, errPrinter, accounts,
						accountsFolder, paymentsFolder, emailAddr, emailPasswd, sessionID);
				if (thread.isValid()){
					pool.schedule(thread,(long)pr.hoursDelay * 60 + (long)pr.minutesDelay, TimeUnit.MINUTES);
					return new SuccessResponse(sessionID);
				}
				else{
					return new IllegalRequestResponse(sessionID);
				}
			}
			else{
				payment = run(outPrinter, errPrinter,accountsFolder, paymentsFolder,
						 pr, accounts, emailAddr, emailPasswd,sessionID);
				return new SuccessPaymentResponse( payment, sessionID);
			}

		} catch (IOException e) {
			e.printStackTrace(errPrinter);
			return new UnknownErrorResponse("I/O error: " + e.getMessage(), sessionID);
		} catch (NullPointerException e) {
			e.printStackTrace(errPrinter);
			return new UnknownErrorResponse("Server error: " + e.getMessage(), sessionID);
		}
	}
	public static Payment run(PrintWriter outPrinter, PrintWriter errPrinter, File accountsFolder, File paymentsFolder,
	                          PaymentRequest req, Map<Integer, Account> accounts,
	                          String emailAddr, char[] emailPasswd, long sessionID) throws IOException {
		File paymentFile;
		Payment payment = makePayment(req, accountsFolder, accounts, errPrinter);
		if ( payment == null ){
			// TO-DO
			outPrinter.println("Check receiver's ID and amount.");
			outPrinter.flush();
			throw new IOException("wrong ID or amount");
		}
		else{
			outPrinter.println("Payment made.");
			outPrinter.println("Creating file for payment...");
			paymentFile = createPaymentFile(payment, paymentsFolder, errPrinter);
			savePaymentToAccounts(payment, paymentFile.getName(), accountsFolder.getAbsolutePath());
			sendEmail(payment, accountsFolder, emailAddr, emailPasswd);
		}
		return payment;
	}

	private static void sendEmail(Payment payment, File accountsFolder, String emailAddr, char[] myPasswd) {
		Properties prop = new Properties();
		prop.put("mail.smtp.auth", true);
		prop.put("mail.smtp.starttls.enable", "true");
		prop.put("mail.smtp.host", "smtp.gmail.com");
		prop.put("mail.smtp.port", "587");

		String recipientAddr = getRecipientAddr(payment, accountsFolder);

		Session session = Session.getDefaultInstance(prop, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(emailAddr, myPasswd.toString());
			}
		});

		Message msg = new MimeMessage(session);
		try {
			msg.setFrom(new InternetAddress(emailAddr));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientAddr));
			msg.setSubject("Payment received");
			msg.setText( createEmailText(payment) );
			Transport.send(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String createEmailText(Payment payment) {
		return "To your account was sent " + String.format("%.2f",payment.amount / 100D) + " " + payment.fromCurr + " converted to " +
				payment.toCurr + ".\n\nYour BankingApp";
	}

	private static String getRecipientAddr(Payment payment, File accountsFolder) {
		try(BufferedReader br = new BufferedReader(new FileReader(Paths.get(accountsFolder.getAbsolutePath(),
				payment.receiverAccountID + "", ".info").toFile()))){
			return br.readLine();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static synchronized void savePaymentToAccounts(Payment payment, String paymentFileName, String accountsFolderPath) throws IOException {
		int senderAccountID = payment.senderAccountID;
		int receiverAccountID = payment.receiverAccountID;
		YearMonth yearMonth = YearMonth.now();
		int year = yearMonth.getYear();
		int month = yearMonth.getMonthValue();

		File currMonthPaymentsSenderFile = Paths.get(accountsFolderPath,senderAccountID + "", (year + "_" + month)).toFile();

		currMonthPaymentsSenderFile.createNewFile(); // if not created, create new empty file
		try (FileWriter fw = new FileWriter(currMonthPaymentsSenderFile.getAbsolutePath(), true)){
			fw.write( paymentFileName + ":" + PaymentCategory.Other + "\n");
		}
		if (senderAccountID != receiverAccountID) {
			File currMonthPaymentsReceiverFile = Paths.get(accountsFolderPath,receiverAccountID + "", (year + "_" + month)).toFile();
			currMonthPaymentsReceiverFile.createNewFile(); // if not created, create new empty file
			try (FileWriter fw = new FileWriter(currMonthPaymentsReceiverFile.getAbsolutePath(), true)) {
				fw.write(paymentFileName + ":" + PaymentCategory.Other + "\n");
			}
		}
	}

	private static synchronized File createPaymentFile(Payment payment, File paymentsFolder, PrintWriter errPrinter) throws IOException {
		File paymentFile = Paths.get(paymentsFolder.getAbsolutePath(), payment.GetFileName()).toFile();
		return payment.toFile(paymentFile);
	}

	private static synchronized Payment makePayment(PaymentRequest pr, File accountsFolder, Map<Integer, Account> accounts, PrintWriter errPrinter) throws IOException {
		int senderID = pr.senderAccountID;
		int receiverID = pr.receiverAccountID;
		Double rate = 1D;

		// wrong IDs
		if ( accounts.get(senderID) == null || accounts.get(receiverID) == null){
			return null;
		}

		Account senderAccount = accounts.get(senderID);
		Account receiverAccount = accounts.get(receiverID);

		synchronized (senderAccount){
			synchronized (receiverAccount){
				Long senderAmount = senderAccount.getBalance(pr.fromCurr);
				if (senderAmount <= pr.amount){
					return null;
				}
				Long receiverAmount = senderAccount.getBalance(pr.toCurr);
				long amount = pr.amount;
				if (pr.fromCurr != pr.toCurr){
					rate = convert(pr.fromCurr, pr.toCurr);
				}
				// because wrapper types are immutable
				senderAccount.trySubtract(pr.fromCurr, pr.amount);
				receiverAccount.tryAdd(pr.toCurr, (long)Math.floor(rate * amount));
				modifyValueFiles(senderAccount, accountsFolder);
				modifyValueFiles(receiverAccount, accountsFolder);
			}
		}
		if (rate == null){
			return new Payment(pr);
		}
		else{
			return new Payment(pr, rate);
		}
	}

	private static Double convert(CurrencyType from, CurrencyType to) throws IOException {
		String apiKey = "3fa4ba21d24d7294d6a9";
		try {

			String addr = "https://free.currconv.com/api/v7/convert?q=" + from + "_" + to + "&compact=ultra&apiKey=" + apiKey;
			URL url = new URL(addr);
			StringBuffer sb = new StringBuffer();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader( url.openStream() )) ){
				reader.lines().forEach(x -> sb.append(x + "\n"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			Matcher matcher = exchangePattern.matcher(sb.toString());
			if (matcher.find()){
				String matched = matcher.group();
				return Double.valueOf(matched);
			}
			else{
				throw new IOException("API malfunction.");
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return -1D;
	}

	private static synchronized void modifyValueFiles(Account account, File accountsFolder) throws IOException {
		try( var bw = new BufferedWriter( new FileWriter( Paths.get(accountsFolder.getAbsolutePath(),
				account.accountID + "", ".curr").toFile()))){
			for ( CurrencyType curr: CurrencyType.values()) {
				bw.write( curr.name() + ":" + account.getBalance(curr) + "\n");
			}
		}

	}
}
