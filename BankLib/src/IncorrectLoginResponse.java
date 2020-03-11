import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class IncorrectLoginResponse extends Response {
	public IncorrectLoginResponse(long sessionID){
		super(ResponseType.IncorrectLoginError, sessionID);
	}
	@Override
	public void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);
		oo.flush();
	}
	public static IncorrectLoginResponse ReadArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		return new IncorrectLoginResponse(sessionID);
	}
}
