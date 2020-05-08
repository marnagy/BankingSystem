import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Used when client tries to log in and sent information doesn't match with those in database
 */
public class IncorrectLoginResponse extends Response {
	/**
	 * Constructor of IncorrectLoginResponse object
	 * @param sessionID Long identifier of session
	 */
	public IncorrectLoginResponse(long sessionID){
		super(ResponseType.IncorrectLoginError, sessionID);
	}

	/**
	 * Method used for sending this object using ObjectOutput
	 * @param oo ObjectOutput to send the object through
	 * @throws IOException Network failure
	 */
	@Override
	public void send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);
		oo.flush();
	}

	/**
	 * Used for loading this object from ObjectInput
	 * @param oi ObjectInput object
	 * @return IncorrectLoginResponse object
	 * @throws IOException Network failure
	 */
	public static IncorrectLoginResponse readArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		return new IncorrectLoginResponse(sessionID);
	}
}
