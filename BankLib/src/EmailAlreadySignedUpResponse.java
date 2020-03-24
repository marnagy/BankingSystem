import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class EmailAlreadySignedUpResponse extends Response {
	public EmailAlreadySignedUpResponse(long sessionID){
		super(ResponseType.EmailAlreadySignedUp, sessionID);
	}

	@Override
	void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(sessionID);
		oo.flush();
	}

	public static EmailAlreadySignedUpResponse ReadArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		return new EmailAlreadySignedUpResponse(sessionID);
	}
}
