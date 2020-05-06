import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class EndRequest extends Request {
	/**
	 * Constructor for EndRequest object
	 * @param sessionID Lond identifier of session
	 */
	public EndRequest(long sessionID){
		super(RequestType.End, sessionID);
	}

	/**
	 * Used to send this object to ObjectOutput
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
	 * Used to load this object from ObjectInput
	 * @param oi ObjectInput object
	 * @return EndRequest object
	 * @throws IOException Network failure
	 */
	public static EndRequest readArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		return new EndRequest(sessionID);
	}
}
