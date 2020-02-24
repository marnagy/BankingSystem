import java.io.IOException;
import java.io.ObjectOutput;

public class IllegalResponse extends Response {
	public IllegalResponse(){
		super(ResponseType.IllegalServerResponse);
	}
	@Override
	void Send(ObjectOutput oo) throws IOException {
		throw new IOException();
	}
}
