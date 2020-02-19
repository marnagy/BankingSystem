import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public abstract class Response {
	ResponseType type;
	protected Response(ResponseType type) {
		this.type = type;
	}
	// used for sending object
	// used by server
	abstract void Send(ObjectOutput oo) throws IOException;
	// used for reading arguments of the response
	// used by client
	public static Response ReadArgs(ObjectInput oi) throws IOException, ArgsException{
		assert false;
		return null;
	}
}
