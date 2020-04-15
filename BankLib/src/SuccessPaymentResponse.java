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
	void send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);
		oo.writeBoolean(payment != null);
		if (payment != null) {
			payment.send(oo);
		}

		oo.flush();
	}
	public static SuccessPaymentResponse readArgs(ObjectInput oi) throws IOException{
		long sessionID = oi.readLong();
		Payment payment = null;
		if (oi.readBoolean()) {
			payment = Payment.FromObjInput(oi);
		}
		return new SuccessPaymentResponse(payment, sessionID);
	}
}
