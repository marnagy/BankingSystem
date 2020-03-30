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
			File historyFile = new File( MasterServerSession.AccountsFolder.getAbsolutePath() + MasterServerSession.FileSystemSeparator
			+ req.accountID + MasterServerSession.FileSystemSeparator + req.monthYear.year + "_" + req.monthYear.month);
			if ( historyFile.exists() ) {
				try(BufferedReader br = new BufferedReader( new FileReader(historyFile.getAbsolutePath()))){

				}
			}
		}
		catch (IOException e){
			return new IllegalRequestResponse(sessionID);
		}
	}
}
