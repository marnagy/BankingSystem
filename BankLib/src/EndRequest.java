import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Sends request for logging out and ending connection
 */
public class EndRequest extends Request {
	/**
	 * Creates an EndRequest object
	 * @param sessionID Long identifier of session
	 */
	public EndRequest(long sessionID){
		super(RequestType.End, sessionID);
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
	 * @return EndRequest object
	 * @throws IOException Network failure
	 */
	public static EndRequest readArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		return new EndRequest(sessionID);
	}
}
