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
	// used for sending object
	// used by server
	abstract void Send(ObjectOutput oo) throws IOException;
	// used for reading arguments of the response
	// used by client
	public static Response ReadArgs(ObjectInput oi) throws IOException, ArgsException, InvalidFormatException, ClassNotFoundException {
		assert false;
		return null;
	}
}
