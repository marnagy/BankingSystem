import java.io.*;
import java.nio.file.Paths;
import java.time.ZonedDateTime;

/**
 * Handler of changinf category of payment in history of an account
 */
public class PaymentCategoryChangeHandler {
	/**
	 * Method to handle category change for a payment in history
	 * @param userID ID of account
	 * @param pcChReq PaymentCategoryChangeRequest object
	 * @param accountsFolder Folder containing all accounts
	 * @param sessionID Long identifier
	 * @return Response
	 */
	public static Response Run(Integer userID, PaymentCategoryChangeRequest pcChReq, File accountsFolder,
	                           long sessionID) {
		ZonedDateTime dt = null;
		String paymentFileName;
		if (userID == pcChReq.toChange.senderAccountID){
			dt = pcChReq.toChange.sendingDateTime;
		}
		else if (userID == pcChReq.toChange.receiverAccountID){
			dt = pcChReq.toChange.receivedDateTime;
		}
		try{
			String name = dt.getYear() + "_" + dt.getMonthValue();
			File monthHistory = Paths.get(accountsFolder.getAbsolutePath(),
					userID + "", name).toFile();
			paymentFileName = pcChReq.toChange.GetFileName();
			StringBuilder sb = new StringBuilder();
			String line;
			String[] lineParts;
			boolean changed = false;
			try (BufferedReader br = new BufferedReader(new FileReader(monthHistory))){
				while ( (line = br.readLine()) != null ){
					lineParts = line.split(":");
					if (paymentFileName.equals(lineParts[0]) && !changed){
						sb.append(lineParts[0] + ":" + pcChReq.newCategory);
						changed = true;
					}
					else{
						sb.append(line);
					}

					sb.append("\n");
				}
			}
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(monthHistory))){
				bw.write(sb.toString());
			}
			return new SuccessResponse(sessionID);
		}
		catch (NullPointerException | IOException e){
			return new UnknownErrorResponse("AccountID does not match with payment.", sessionID);
		}
	}
}
