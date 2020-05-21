import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Sends login request
 */
public class LoginRequest extends Request {
	/**
	 * Email to be checked
	 */
	public final String email;
	/**
	 * Password corresponding to the email
	 */
	public final char[] passwd;

	/**
	 * Creates LoginRequest object
	 * @param email Identifing email
	 * @param passwd Password to the email
	 * @param sessionID Long identifier of session
	 */
	public LoginRequest(String email, char[] passwd, long sessionID){
		super(RequestType.Login, sessionID);
		this.email = email;
		this.passwd = passwd;
	}

	/**
	 * Sends this object to ObjectOutput
	 * @param oo ObjectOutput object
	 * @throws IOException Network failure
	 */
	@Override
	public void send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(sessionID);

		oo.writeUTF(email);
		oo.writeObject(passwd);
		oo.flush();
	}

	/**
	 * Loads this object from ObjectInput
	 * @param oi ObjectInput object
	 * @return Request object
	 */
	public static Request readArgs(ObjectInput oi){
		try {
			long sessionID = oi.readLong();
			String email = oi.readUTF();
			char[] passwd = (char[])oi.readObject();
			return new LoginRequest(email, passwd, sessionID);
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}

	}
}
