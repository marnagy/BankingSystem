import java.io.*;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

public class AccountHistoryHandler {
	public static Response Run(PrintWriter outPrinter, PrintWriter errPrinter,
	                           ObjectInput oi, ObjectOutput oo,
	                           Dictionary<Integer, Account> accounts, long sessionID) {
		try {
			PaymentHistoryRequest req = PaymentHistoryRequest.ReadArgs(oi);
			final List<Payment> history = new ArrayList<Payment>();
			File monthHistoryFile = new File( MasterServerSession.AccountsFolder.getAbsolutePath() + MasterServerSession.FileSystemSeparator
			+ req.accountID + MasterServerSession.FileSystemSeparator + req.monthYear.year + "_" + req.monthYear.month.getValue());
			if ( monthHistoryFile.exists() ) {
				String[] lineParts;
				String line;
				Payment temp;
				try(BufferedReader br = new BufferedReader( new FileReader(monthHistoryFile.getAbsolutePath()))){
					while( (line = br.readLine()) != null ) {
						lineParts = line.split(":");
						temp = Payment.FromFile(new File(MasterServerSession.PaymentsFolder.getAbsolutePath() +
								MasterServerSession.FileSystemSeparator + lineParts[0]));
						temp.category = PaymentCategory.valueOf(lineParts[1]);
						history.add(temp);
					}
				}
			}
			return new PaymentHistoryResponse(history, sessionID);
		}
		catch (Exception e){
			e.printStackTrace(errPrinter);
			errPrinter.flush();
			return new IllegalRequestResponse(sessionID);
		}
	}
}
