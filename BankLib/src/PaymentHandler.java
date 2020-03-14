import java.io.*;
import java.util.regex.Pattern;

public class PaymentHandler {
	private static final Pattern amountPattern = Pattern.compile("(([1-9][0-9]*)|0)(\\.[0-9]{2})?");
	public static void Run(PrintWriter pw, BufferedReader br, ObjectInput oiSocket,
	                       ObjectOutput ooSocket, Account account, long sessionID) throws IOException {
		Request req;
		pw.println("Enter receiverID:");
		pw.flush();
		int receiverID = Integer.parseInt(br.readLine());
		String varSymbol = "";
		String specSymbol = "";
		pw.println("Enter receiverID:");
		pw.flush();
		String recvInfo = br.readLine();
		boolean isValid; // valid format
		boolean canSend; // sender has enough money in account
		long amount = 0;
		CurrencyType curr = CurrencyType.EUR;
		do{

			isValid = false;
			canSend = false;
			pw.println("Enter amount");
			pw.flush();
			String amountS = br.readLine();

			if (CheckAmountFormat(amountS)) {
				amount = Long.parseLong(br.readLine());
				isValid = true;
			}
			if (account.Values.get(curr) >= amount){
				canSend = true;
			}
		} while (!(isValid && canSend));
		// Continue Here

		req = new PaymentRequest(account.accountID, receiverID, amount,
				curr, sessionID);
		req.Send(ooSocket);
		ResponseType respType = ResponseType.values()[oiSocket.readInt()];
		switch (respType){
			case InvalidReceiverIDResponse:
				pw.println("Invalid receiver ID.");
				pw.flush();
				break;
			case Success:
				pw.println("Payment sent and processed.");
				pw.flush();
				break;
		}
	}

	private static boolean CheckAmountFormat(String amountS) {
		return amountPattern.matcher(amountS).matches();
	}
}
