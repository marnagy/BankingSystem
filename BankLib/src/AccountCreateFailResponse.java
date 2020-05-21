import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Sends failed response from server to client.
 */
public class AccountCreateFailResponse extends Response {
	/**
	 * Error message
	 */
	public final String mssg;

	/**
	 * Stores message of failure
	 * @param errMssg Massage to store
	 * @param sessionID Long identifier of session
	 */
	public AccountCreateFailResponse(String errMssg, long sessionID){
		super(ResponseType.AccountCreateFailResponse, sessionID);
		this.mssg = errMssg;
	}

	/**
	 * Sends object to ObjectOutput
	 * @param oo ObjectOutput to send the object through
	 * @throws IOException Network failure
	 */
	@Override
	void send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);
		oo.writeUTF(this.mssg);

		oo.flush();
	}

	/**
	 * Loads object from ObjectInput
	 * @param oi ObjectInput object
	 * @return AccountCreateFailResponse object
	 * @throws IOException Network failure
	 */
	public static AccountCreateFailResponse readArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		String mssg = oi.readUTF();
		return new AccountCreateFailResponse(mssg, sessionID);
	}
}
