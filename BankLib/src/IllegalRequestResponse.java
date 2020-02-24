import java.io.IOException;
import java.io.ObjectOutput;

public class IllegalRequestResponse extends Response {
	public IllegalRequestResponse() {
		super(ResponseType.IllegalRequestResponse);
	}

	@Override
	void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.flush();
	}
}
