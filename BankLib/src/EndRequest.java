import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class EndRequest extends Request {
	public EndRequest(long sessionID){
		super(RequestType.End, sessionID);
	}
	@Override
	public void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);

		oo.flush();
	}
	public static EndRequest ReadArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		return new EndRequest(sessionID);
	}
}
