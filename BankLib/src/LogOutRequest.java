import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Sends a logout request
 */
public class LogOutRequest extends Request {
	/**
	 * Creates LogOutRequest object
	 * @param sessionID Long identifier of session
	 */
	public LogOutRequest(long sessionID){
		super(RequestType.Logout, sessionID);
	}

	/**
	 * Sends this object to ObjectOutput
	 * @param oo ObjectOutput object
	 * @throws IOException Network failure
	 */
	@Override
	public void send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);

		oo.flush();
	}

	/**
	 * Loads this object from ObjectInput
	 * @param oi ObjectInput object
	 * @return Request object
	 * @throws IOException Network failure
	 */
	public static LogOutRequest readArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		return new LogOutRequest(sessionID);
	}
}
