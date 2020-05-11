import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Used for confirmation of payment being processed
 */
public class SuccessPaymentResponse extends Response {
	public final Payment payment;

	/**
	 * Constructor of SuccessPaymentResponse object
	 * @param payment Payment object
	 * @param sessionID Long identifier of session
	 */
	public SuccessPaymentResponse(Payment payment, long sessionID){
		super(ResponseType.SuccessPaymentResponse, sessionID);
		this.payment = payment;
	}

	/**
	 * Method used for sending this object using ObjectOutput
	 * @param oo ObjectOutput to send the object through
	 * @throws IOException Network failure
	 */
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

	/**
	 * Used for loading this object from ObjectInput
	 * @param oi ObjectInput object
	 * @return SuccessPaymentResponse object
	 * @throws IOException Network failure
	 */
	public static SuccessPaymentResponse readArgs(ObjectInput oi) throws IOException{
		long sessionID = oi.readLong();
		Payment payment = null;
		if (oi.readBoolean()) {
			payment = Payment.FromObjInput(oi);
		}
		return new SuccessPaymentResponse(payment, sessionID);
	}
}
