import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Used for sending request for creation of new account from client to server
 */
public class AccountCreateRequest extends Request {
	public final String email;
	public final char[] passwd;

	/**
	 * Constructor for AccountCreateRequest
	 * @param email
	 * @param passwd
	 * @param sessionID
	 */
	public AccountCreateRequest(String email, char[] passwd, long sessionID){
		super(RequestType.CreateAccount, sessionID);
		this.email = email;
		this.passwd = passwd;
	}

	/**
	 * Send object to ObjectOutput
	 * @param oo ObjectOutput object
	 * @throws IOException Network failure
	 */
	@Override
	public void send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);
		oo.writeUTF(email);
		oo.writeObject(passwd);
		oo.flush();
	}

	/**
	 * Method for getting AccountCreateRequest object from ObjectInput
	 * @param oi ObjectInput object
	 * @return AccountCreateRequest object
	 */
	public static AccountCreateRequest readArgs(ObjectInput oi){
		try {
			long sessionID = oi.readLong();
			String email = oi.readUTF();
			char[] passwd = (char[])oi.readObject();
			return new AccountCreateRequest(email, passwd, sessionID);
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}

	}
}
