import java.io.*;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;

public class AccountInfoResponse extends Response {

	public final Map<CurrencyType, Long> Values = new HashMap<CurrencyType, Long>();

	//public final String email;
	public final int accountID;

//	public AccountInfoResponse(String email){
//		super(ResponseType.AccountInfo);
//		//this.email = email;
//		accountID = email.hashCode();
//	}
	private AccountInfoResponse(long sessionID){
		super(ResponseType.AccountInfo, sessionID);
		//this.email = email;
		accountID = 0;
	}

	public AccountInfoResponse(String email, File accountDir, long sessionID){
		super(ResponseType.AccountInfo, sessionID);
		//this.email = email;
		accountID = email.hashCode();
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
				Values.put(currType, l);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);
		//oo.writeUTF(email);
		oo.writeInt(accountID);
		int size = Values.size();
		CurrencyType[] currs = new CurrencyType[size];
		Values.keySet().toArray(currs);
		oo.writeInt(size);
		for ( CurrencyType curr: currs) {
			oo.writeInt(curr.ordinal());
			oo.writeLong(Values.get(curr));
		}

		oo.flush();
	}
	public static AccountInfoResponse ReadArgs(ObjectInput oi) throws IOException {
		//String email = oi.readUTF();
		long sessionID = oi.readLong();
		AccountInfoResponse air = new AccountInfoResponse(sessionID);
		int currenciesSize = oi.readInt();
		CurrencyType currType;
		long Value;
		for (int i = 0; i < currenciesSize; i++) {
			currType = CurrencyType.values()[oi.readInt()];
			Value = oi.readLong();
			air.Values.put(currType, Value);
		}
		return air;
	}
}
