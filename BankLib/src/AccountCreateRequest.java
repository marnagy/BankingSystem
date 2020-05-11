import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Used for sending request for creation of new account from client to server
 */
public class AccountCreateRequest extends Request {
	/**
	 * Email to register
	 */
	public final String email;
	/**
	 * Password to be used with given email
	 */
	public final char[] passwd;

	/**
	 * Constructor for AccountCreateRequest
	 * @param email Email to register
	 * @param passwd Password to go along with the email
	 * @param sessionID Long identifier of session
	 */
	public AccountCreateRequest(String email, char[] passwd, long sessionID){
		super(RequestType.CreateAccount, sessionID);
		if (email == null || passwd == null){
			throw new Error("Invalid argument: NULL");
		}
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
	 * In case of failure, returns null
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
