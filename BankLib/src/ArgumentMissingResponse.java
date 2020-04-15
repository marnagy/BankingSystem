import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ArgumentMissingResponse extends Response {
	public ArgumentMissingResponse(long sessionID){
		super(ResponseType.ArgumentMissingError, sessionID);
	}
	@Override
	void send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);
		oo.flush();
	}
	public static ArgumentMissingResponse readArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		return new ArgumentMissingResponse(sessionID);
	}
}
