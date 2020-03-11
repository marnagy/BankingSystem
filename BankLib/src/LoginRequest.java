import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

public class LoginRequest extends Request {
	public final String email;
	public final char[] passwd;
	public LoginRequest(String email, char[] passwd, long sessionID){
		super(RequestType.Login, sessionID);
		this.email = email;
		// make copy
		this.passwd = passwd;
		//this.passwd = Arrays.copyOfRange(passwd, 0, passwd.length);
	}
	@Override
	public void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(sessionID);

		oo.writeUTF(email);
		oo.writeObject(passwd);
		oo.flush();
	}
	public static Request ReadArgs(ObjectInput oi){
		try {
			long sessionID = oi.readLong();
			String email = oi.readUTF();
			char[] passwd = (char[])oi.readObject();
			return new LoginRequest(email, passwd, sessionID);
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}

	}
}
