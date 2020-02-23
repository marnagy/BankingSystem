import java.io.*;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountInfoResponse extends Response {

	// change to one hashMap?
//	public final List<CurrencyType> currencies;
//	public final List<Long> currenciesValues;

	// CONTINUE HERE
	public final Map<CurrencyType, Long> Values;

	public final String email;

	public AccountInfoResponse(String email){
		super(ResponseType.AccountInfo);
		this.email = email;
//		currencies = new ArrayList<CurrencyType>();
//		currenciesValues = new ArrayList<Long>();
		Values = new HashMap<CurrencyType, Long>();
	}

	public AccountInfoResponse(String email, File accountDir){
		super(ResponseType.AccountInfo);
		this.email = email;
//		currencies = new ArrayList<CurrencyType>();
//		currenciesValues = new ArrayList<Long>();
		Values = new HashMap<CurrencyType, Long>();
		File currFile = new File(accountDir.getAbsolutePath() + FileSystems.getDefault().getSeparator() + ".curr");
		try(BufferedReader br = new BufferedReader(new FileReader(currFile))) {
			String line;
			String[] lineParts;
			long l;
			CurrencyType currType;
			// can line be "" ?
			while ( (line = br.readLine()) != null ){
				lineParts = line.split(":");
				currType = CurrencyType.valueOf(lineParts[0]);
				l = Long.parseLong(lineParts[1]);
//				currencies.add(currType);
//				currenciesValues.add(l);
				Values.put(currType, l);
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
		oo.writeUTF(email);
		int size = currencies.size();
		oo.writeInt(size);
		for (int i = 0; i < size; i++) {
			oo.writeInt(currencies.get(i).ordinal());
			oo.writeLong(currenciesValues.get(i));
		}

		oo.flush();
	}
	public static Response ReadArgs(ObjectInput oi) {
		try {
			String email = oi.readUTF();
			AccountInfoResponse air = new AccountInfoResponse(email);
			int currenciesSize = oi.readInt();
			CurrencyType currType;
			long Value;
			for (int i = 0; i < currenciesSize; i++) {
				currType = CurrencyType.values()[oi.readInt()];
				Value = oi.readLong();
				air.currencies.add(currType);
				air.currenciesValues.add(Value);
			}
			return air;
		} catch (IOException e) {
			return new IllegalResponse();
		}
	}
}
