import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public abstract class Request {
	public final RequestType type;
	public final long sessionID;

	/**
	 * Needed constructor
	 * @param type RequestType object
	 * @param sessionID Long identifier of session
	 */
	protected Request(RequestType type, long sessionID){
		this.type = type;
		this.sessionID = sessionID;
	}

	/**
	 * Used to send this object to ObjectOutput
	 * @param oo ObjectOutput object
	 * @throws IOException Network failure
	 */
	public abstract void send(ObjectOutput oo) throws IOException;

	/**
	 * Used to load this object from ObjectInput
	 * @param oi ObjectInput object
	 * @return Request object
	 * @throws IOException Network failure
	 * @throws ClassNotFoundException LoadingClass failure
	 */
	public static Request readArgs(ObjectInput oi) throws IOException, ClassNotFoundException {
		assert false;
		return null;
	}
}
