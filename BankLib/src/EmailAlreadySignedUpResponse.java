import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class EmailAlreadySignedUpResponse extends Response {
	public EmailAlreadySignedUpResponse(){
		super(ResponseType.EmailAlreadySignedUp);
	}

	@Override
	void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.flush();
	}

	@Override
	public void ReadArgs(ObjectInput oi) throws IOException, ArgsException {
		return;
	}
}
