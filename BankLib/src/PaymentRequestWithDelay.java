import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class PaymentRequestWithDelay extends Request {
	public final PaymentRequest req;
	public final int hours, minutes;
	public PaymentRequestWithDelay(PaymentRequest req, int hours, int minutes, long sessionID){
		super(RequestType.PaymentWithDelay, sessionID);
		this.req = req;
		this.hours = hours;
		this.minutes = minutes;
	}

	@Override
	public void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);

		req.Send(oo);
		oo.writeInt(hours);
		oo.writeInt(minutes);

		oo.flush();
	}
	public static PaymentRequestWithDelay ReadArgs(ObjectInput oi) throws IOException, ClassNotFoundException {
		long sessionID = oi.readLong();
		// reading og Request type of req
		oi.readInt();
		PaymentRequest req = PaymentRequest.ReadArgs(oi);
		int hours = oi.readInt();
		int minutes = oi.readInt();
		return new PaymentRequestWithDelay(req, hours, minutes, sessionID);
	}
}
