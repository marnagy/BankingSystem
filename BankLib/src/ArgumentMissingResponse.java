import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ArgumentMissingResponse extends Response {
	public ArgumentMissingResponse(){
		super(ResponseType.ArgumentMissingError);
	}
	@Override
	void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(ResponseType.ArgumentMissingError.ordinal());
		oo.flush();
	}

	public static Response ReadArgs(ObjectInput oi) {
		return new ArgumentMissingResponse();
	}
}
