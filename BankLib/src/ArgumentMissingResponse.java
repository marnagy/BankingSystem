import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Used to send information about missing arguments from server to client
 */
public class ArgumentMissingResponse extends Response {
	/**
	 * Constructor of ArgumentMissingResponse object
	 * @param sessionID Long identifier of session
	 */
	public ArgumentMissingResponse(long sessionID){
		super(ResponseType.ArgumentMissingError, sessionID);
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
	 * Used for loading ArgumentMissingResponse from ObjectInput
	 * @param oi ObjectInput object
	 * @return ArgumentMissingResponse object
	 * @throws IOException Network failure
	 */
	public static ArgumentMissingResponse readArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		return new ArgumentMissingResponse(sessionID);
	}
}
