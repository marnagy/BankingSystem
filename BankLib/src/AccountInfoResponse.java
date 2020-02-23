import java.io.*;
import java.nio.file.FileSystems;
import java.util.List;

public class AccountInfoResponse extends Response {

	public final List<CurrencyType> currencies;
	public final List<Long> currenciesValues;

	public AccountInfoResponse(String email, File accountDir){
		super(ResponseType.AccountInfo);
		File currFile = new File(accountDir.getAbsolutePath() + FileSystems.getDefault().getSeparator() + ".curr");
		currencies = null;
		currenciesValues = null;
		try(BufferedReader br = new BufferedReader(new FileReader(currFile))) {
			String line;
			String[] lineParts;
			// can line be "" ?
			while ( (line = br.readLine()) != null ){
				lineParts = line.split(":");
				currencies.add(CurrencyType.valueOf(lineParts[0]));
				currenciesValues.add(Long.parseLong(lineParts[1]));
			}
			if (currencies.size() != currenciesValues.size()){
				throw new IOException();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		int size = currencies.size();
		oo.writeInt(size);
		for (int i = 0; i < size; i++) {
			oo.writeInt(currencies.get(i).ordinal());
			oo.writeLong(currenciesValues.get(i));
		}

		oo.flush();
	}
	public static Response ReadArgs(ObjectInput oi) {
		// CONTINUE HERE
		return new ArgumentMissingResponse();
	}
}
