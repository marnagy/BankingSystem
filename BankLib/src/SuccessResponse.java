import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class SuccessResponse extends Response {
	/**
	 * Constructor of SuccessResponse object
	 * @param sessionID Long identifier of session
	 */
	public SuccessResponse(long  sessionID) {
		super(ResponseType.Success, sessionID);
	}

	/**
	 * Method used for sending this object using ObjectOutput
	 * @param oo ObjectOutput to send the object through
	 * @throws IOException Network failure
	 */
	@Override
	void send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);
		oo.flush();
	}

	/**
	 * Used for loading this object from ObjectInput
	 * @param oi ObjectInput object
	 * @return SuccessResponse object
	 * @throws IOException Network failure
	 */
	public static SuccessResponse readArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		return new SuccessResponse(sessionID);
	}
}
