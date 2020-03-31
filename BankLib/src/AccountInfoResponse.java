import java.io.*;
import java.net.FileNameMap;
import java.nio.file.FileSystems;
import java.time.ZonedDateTime;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.regex.Pattern;


public class AccountInfoResponse extends Response {

	public final Dictionary<CurrencyType, Long> Values = new Hashtable<CurrencyType, Long>();
	public final Dictionary<MonthYear, Payment[]> History = new Hashtable<MonthYear, Payment[]>();
	public final int accountID;

	private AccountInfoResponse(String email, long sessionID){
		super(ResponseType.AccountInfo, sessionID);
		this.accountID = email.hashCode();
	}
	private AccountInfoResponse(int accountID, long sessionID){
		super(ResponseType.AccountInfo, sessionID);
		this.accountID = accountID;
	}

	public AccountInfoResponse(String email, File accountDir, long sessionID){
		super(ResponseType.AccountInfo, sessionID);
		accountID = email.hashCode();
		File currFile = new File(accountDir.getAbsolutePath() + FileSystems.getDefault().getSeparator() + ".curr");
		try( BufferedReader br = new BufferedReader(new FileReader(currFile)) ) {
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
		ZonedDateTime dateTime = ZonedDateTime.now();
		FilenameFilter filter = (File file, String s) ->
				Pattern.matches( dateTime.getYear() + "_" + dateTime.getMonthValue(), s) ||
						Pattern.matches( dateTime.getYear() + "_" + dateTime.getMonthValue(), file.getName());
		// file for current month exists
		String[] temp = accountDir.list(filter);
		if ( temp.length == 1 ){
			try(BufferedReader br = new BufferedReader(new FileReader(accountDir.listFiles(filter)[0]))){
				String line;
				String[] lineParts;
				while ( (line = br.readLine()) != null ){
					lineParts = line.split(":");

				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);
		oo.writeInt(accountID);

		oo.writeInt(Values.size());
		Values.keys().asIterator().forEachRemaining((x) -> {
			try {
				oo.writeInt(x.ordinal());
				oo.writeLong(Values.get(x));
			} catch (IOException e) {
				throw new Error("ObjectOutput IOException");
			}
		});

		oo.writeInt(History.size());
		History.keys().asIterator().forEachRemaining((x) -> {
			try {
				oo.writeInt(x.month.getValue());
				oo.writeInt(x.year);
				Payment[] payments = History.get(x);
				oo.writeInt(payments.length);
				for (int i = 0; i < payments.length; i++) {
					payments[i].Send(oo);
				}
			} catch (IOException e) {
				throw new Error("ObjectOutput IOException");
			}
		});


		oo.flush();
	}
	public static AccountInfoResponse ReadArgs(ObjectInput oi) throws IOException, InvalidFormatException {
		//String email = oi.readUTF();
		long sessionID = oi.readLong();
		int accountID = oi.readInt();
		AccountInfoResponse air = new AccountInfoResponse(accountID, sessionID);
		int currenciesSize = oi.readInt();
		CurrencyType currType;
		long Value;
		for (int i = 0; i < currenciesSize; i++) {
			currType = CurrencyType.values()[oi.readInt()];
			Value = oi.readLong();
			air.Values.put(currType, Value);
		}
		int historySize = oi.readInt();
		for (int i = 0; i < currenciesSize; i++) {
			int month = oi.readInt();
			int year = oi.readInt();
			int size = oi.readInt();
			Payment[] hist = new Payment[size];
			for (int j = 0; j < hist.length; j++) {
				hist[j] = Payment.FromObjInput(oi);
			}
			air.History.put(new MonthYear(month, year), hist);
		}
		return air;
	}
	public static AccountInfoResponse Load(String email, File accountFile, File paymentsFile, long sessionID){
		AccountInfoResponse air = new AccountInfoResponse(email.hashCode(), sessionID);

	}
}
