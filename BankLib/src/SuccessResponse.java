import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class SuccessResponse extends Response {
	public SuccessResponse() {
		super(ResponseType.Success);
	}

	@Override
	void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(ResponseType.Success.ordinal());
		oo.flush();
	}

	@Override
	public void ReadArgs(ObjectInput oi) throws IOException, ArgsException {
		return;
	}
}
