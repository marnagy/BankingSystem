import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class UnknownErrorResponse extends Response {
	public final String msg;
	public UnknownErrorResponse(String msg, long sessionID){
		super(ResponseType.UnknownErrorResponse, sessionID);
		this.msg = msg;
	}

	@Override
	void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);
		oo.writeUTF(this.msg);

		oo.flush();
	}
	public static UnknownErrorResponse ReadArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		String msg = oi.readUTF();
		return new UnknownErrorResponse(msg, sessionID);
	}
}
