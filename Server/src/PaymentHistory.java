import java.io.*;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.List;

public class PaymentHistory {
	public static Payment[] FromFile(File historyFile, File paymentDir) throws IOException, InvalidFormatException {
		Payment[] res;
		List<Payment> list = new ArrayList<Payment>();
		Payment temp;
		String[] lineParts;
		try(BufferedReader br = new BufferedReader(new FileReader(historyFile))){
			lineParts = br.readLine().split(":");
			temp = Payment.fromFile( new File(paymentDir.getAbsolutePath() + MasterServerSession.FileSystemSeparator
			+ lineParts[0]));
			temp.category = PaymentCategory.valueOf(lineParts[1]);
			list.add(temp);
		}
		res = new Payment[list.size()];
		list.toArray(res);
		return res;
	}
}
