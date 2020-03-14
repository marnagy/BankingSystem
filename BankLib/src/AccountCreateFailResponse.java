import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class AccountCreateFailResponse extends Response {
	public final String mssg;
	public AccountCreateFailResponse(String errMssg, long sessionID){
		super(ResponseType.AccountCreateFailResponse, sessionID);
		this.mssg = errMssg;
	}
	@Override
	void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);
		oo.writeUTF(this.mssg);

		oo.flush();
	}
	public static AccountCreateFailResponse ReadArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		String mssg = oi.readUTF();
		return new AccountCreateFailResponse(mssg, sessionID);
	}
}
