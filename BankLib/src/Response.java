import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Abstract class used for response classes
 */
public abstract class Response {
	public final ResponseType type;
	public final long sessionID;

	/**
	 * Needed constructor
	 * @param type ResponseType object
	 * @param sessionID Long identifier of session
	 */
	protected Response(ResponseType type, long sessionID) {
		this.type = type;
		this.sessionID = sessionID;
	}

	/**
	 * Method used for sending this object using ObjectOutput
	 * @param oo ObjectOutput to send the object through
	 * @throws IOException Network failure
	 */
	abstract void send(ObjectOutput oo) throws IOException;

	/**
	 * Used for loading this object from ObjectInput
	 * @param oi ObjectInput object
	 * @return Response object
	 * @throws IOException Network failure
	 * @throws ClassNotFoundException LoadingClass failure
	 */
	public static Response readArgs(ObjectInput oi) throws IOException, ClassNotFoundException {
		assert false;
		return null;
	}
}
