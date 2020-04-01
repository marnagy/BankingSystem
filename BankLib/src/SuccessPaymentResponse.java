import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class SuccessPaymentResponse extends Response {
	public final Payment payment;
	public SuccessPaymentResponse(Payment payment, long sessionID){
		super(ResponseType.SuccessPaymentResponse, sessionID);
		this.payment = payment;
	}
	@Override
	void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);

		payment.Send(oo);

		oo.flush();
	}
	public static SuccessPaymentResponse ReadArgs(ObjectInput oi) throws IOException{
		long sessionID = oi.readLong();
		Payment payment = Payment.FromObjInput(oi);
		return new SuccessPaymentResponse(payment, sessionID);
	}
}
