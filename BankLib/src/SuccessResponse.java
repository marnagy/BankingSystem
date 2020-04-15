import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class SuccessResponse extends Response {
	public SuccessResponse(long  sessionID) {
		super(ResponseType.Success, sessionID);
	}

	@Override
	void send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);
		oo.flush();
	}

	public static SuccessResponse readArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		return new SuccessResponse(sessionID);
	}
}
