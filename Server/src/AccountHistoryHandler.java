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
			+ req.accountID + MasterServerSession.FileSystemSeparator + req.monthYear.year + "_" + req.monthYear.month);
			if ( monthHistoryFile.exists() ) {
				String[] lineParts;
				try(BufferedReader br = new BufferedReader( new FileReader(monthHistoryFile.getAbsolutePath()))){
					lineParts = br.readLine().split(":");
					history.add( Payment.FromFile( new File(MasterServerSession.PaymentsFolder.getAbsolutePath() +
							MasterServerSession.FileSystemSeparator + lineParts[0]) ) );
				}
			}
		}
		catch (IOException e){
			return new IllegalRequestResponse(sessionID);
		}
	}
}
