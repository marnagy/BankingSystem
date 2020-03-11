import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ArgumentMissingResponse extends Response {
	public ArgumentMissingResponse(long sessionID){
		super(ResponseType.ArgumentMissingError, sessionID);
	}
	@Override
	void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);
		oo.flush();
	}
}
