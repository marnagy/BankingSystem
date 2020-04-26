import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AccountHistoryHandler {
	public static Response run(PrintWriter errPrinter, File accountsFolder, File paymentFolder,
	                           ObjectInput oi, long sessionID) {
		try {
			PaymentHistoryRequest req = PaymentHistoryRequest.readArgs(oi);
			final List<Payment> history = new ArrayList<Payment>();
			File monthHistoryFile = Paths.get(accountsFolder.getAbsolutePath(),req.accountID + "",
					req.monthYear.getYear() + "_" + req.monthYear.getMonthValue()).toFile();
			if ( monthHistoryFile.exists() ) {
				String[] lineParts;
				String line;
				Payment temp;
				try(BufferedReader br = new BufferedReader( new FileReader(monthHistoryFile.getAbsolutePath()))){
					while( (line = br.readLine()) != null ) {
						lineParts = line.split(":");
						temp = Payment.fromFile(Paths.get(paymentFolder.getAbsolutePath(), lineParts[0]).toFile());
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
