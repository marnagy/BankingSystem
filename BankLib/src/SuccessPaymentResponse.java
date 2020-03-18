import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class SuccessPaymentResponse extends Response {
	public final String paymentFileName;
	public SuccessPaymentResponse(String paymentFileName, long sessionID){
		super(ResponseType.SuccessPaymentResponse, sessionID);
		this.paymentFileName = paymentFileName;
	}
	@Override
	void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);

		oo.writeUTF(paymentFileName);

		oo.flush();
	}
	public static SuccessPaymentResponse ReadArgs(ObjectInput oi) throws IOException{
		int sessionID = oi.readInt();
		String paymentFileName = oi.readUTF();
		return new SuccessPaymentResponse(paymentFileName, sessionID);
	}
}
