import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Sends history of payment for a month
 */
public class UnknownErrorResponse extends Response {
	/**
	 * Error message
	 */
	public final String msg;

	/**
	 * Creates an UnknownErrorResponse object
	 * @param msg Error message
	 * @param sessionID Long identifier of session
	 */
	public UnknownErrorResponse(String msg, long sessionID){
		super(ResponseType.UnknownErrorResponse, sessionID);
		this.msg = msg;
	}

	/**
	 * Sends this object using ObjectOutput
	 * @param oo ObjectOutput to send the object through
	 * @throws IOException Network failure
	 */
	@Override
	void send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);
		oo.writeUTF(this.msg);

		oo.flush();
	}

	/**
	 * Loads this object from ObjectInput
	 * @param oi ObjectInput object
	 * @return UnknownErrorResponse object
	 * @throws IOException Network failure
	 */
	public static UnknownErrorResponse readArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		String msg = oi.readUTF();
		return new UnknownErrorResponse(msg, sessionID);
	}
}
