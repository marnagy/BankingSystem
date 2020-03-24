import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class LogOutRequest extends Request {
	public LogOutRequest(long sessionID){
		super(RequestType.Logout, sessionID);
	}

	@Override
	public void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);

		oo.flush();
	}
	public static LogOutRequest ReadArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		return new LogOutRequest(sessionID);
	}
}
