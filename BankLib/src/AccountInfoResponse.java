import java.io.*;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Hashtable;
import java.util.Map;


/**
 * Used to send informations about account on log in from server to client
 */
public class AccountInfoResponse extends Response {

	public final Map<CurrencyType, Long> Values = new Hashtable<CurrencyType, Long>();
	public final ZonedDateTime created;
	public final int accountID;

	/**
	 * Constructor used when loading from ObjectInput
	 * @param accountID accountID
	 * @param created ZonedDateTime object of creation of the account
	 * @param sessionID Long identifier of session
	 */
	private AccountInfoResponse(int accountID, ZonedDateTime created, long sessionID){
		super(ResponseType.AccountInfo, sessionID);
		this.accountID = accountID;
		this.created = created;
	}

	/**
	 * Constructor for loading from account folder
	 * @param accountDir account folder
	 * @param sessionID Long identifier of session
	 * @throws IOException Network failure
	 */
	public AccountInfoResponse(File accountDir, long sessionID) throws IOException {
		super(ResponseType.AccountInfo, sessionID);
		accountID = Integer.parseInt(accountDir.getName());
		File currFile = Paths.get(accountDir.getAbsolutePath(), ".curr").toFile();
		File infoFile = Paths.get(accountDir.getAbsolutePath(), ".info").toFile();
		try( BufferedReader brCurr = new BufferedReader(new FileReader(currFile));
		     BufferedReader brInfo = new BufferedReader(new FileReader(infoFile))) {
			String line;
			String[] lineParts;
			long l;
			CurrencyType currType;
			// can line be "" ?
			while ( (line = brCurr.readLine()) != null ){
				lineParts = line.split(":");
				currType = CurrencyType.valueOf(lineParts[0]);
				l = Long.parseLong(lineParts[1]);
				Values.put(currType, l);
			}
			for (int i = 0; i < 3; i++) {
				line = brInfo.readLine();
			}
			created = Payment.destringify(brInfo.readLine());
		}
	}

	/**
	 * Method to send object to ObjectOutput
	 * @param oo ObjectOutput to send the object through
	 * @throws IOException Network failure
	 */
	@Override
	void send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);
		oo.writeInt(accountID);
		oo.writeObject(this.created);

		oo.writeInt(Values.size());
		Values.keySet().iterator().forEachRemaining((x) -> {
			try {
				oo.writeInt(x.ordinal());
				oo.writeLong(Values.get(x));
			} catch (IOException e) {
				throw new Error("ObjectOutput IOException");
			}
		});
		oo.flush();
	}

	/**
	 * Load object from ObjectInput
	 * @param oi ObjectInput object
	 * @return AccountInfoResponse object
	 * @throws IOException Network failure
	 * @throws ClassNotFoundException Class loading failure
	 */
	public static AccountInfoResponse readArgs(ObjectInput oi) throws IOException, ClassNotFoundException {
		//String email = oi.readUTF();
		long sessionID = oi.readLong();
		int accountID = oi.readInt();
		ZonedDateTime created = (ZonedDateTime) oi.readObject();
		AccountInfoResponse air = new AccountInfoResponse(accountID, created, sessionID);
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
