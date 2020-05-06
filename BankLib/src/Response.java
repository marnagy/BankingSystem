import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public abstract class Response {
	public final ResponseType type;
	public final long sessionID;
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
	 * @ClassNotFoundException LoadingClass failure
	 * @IOException Network failure
	 */
	public static Response readArgs(ObjectInput oi) throws IOException, ClassNotFoundException {
		assert false;
		return null;
	}
}
