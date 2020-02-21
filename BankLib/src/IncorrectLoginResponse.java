import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class IncorrectLoginResponse extends Response {
	public IncorrectLoginResponse(){
		super(ResponseType.IncorrectLoginError);
	}
	@Override
	public void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.flush();
	}
	public static Response ReadArgs(ObjectInput oi){
		return new IncorrectLoginResponse();
	}
}
