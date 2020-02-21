import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

public class AccountInfoResponse extends Response {
	public AccountInfoResponse(Object... objects){
		super(ResponseType.AccountInfo);
	}
	@Override
	void Send(ObjectOutput oo) throws IOException {

	}
	public static Response ReadArgs(ObjectInput oi) {
		return new ArgumentMissingResponse();
	}
}
