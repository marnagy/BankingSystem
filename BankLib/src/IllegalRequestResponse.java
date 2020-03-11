import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class IllegalRequestResponse extends Response {
	public IllegalRequestResponse(long sessionID) {
		super(ResponseType.IllegalRequestResponse, sessionID);
	}

	@Override
	void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);
		oo.flush();
	}
	public static IllegalRequestResponse ReadArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		return new IllegalRequestResponse(sessionID);
	}
}
