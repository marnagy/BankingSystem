import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Sends request to change category of payment in history
 */
public class PaymentCategoryChangeRequest extends Request {
	/**
	 * Payment to be changed
	 */
	public final Payment toChange;
	/**
	 * New category for the payment
	 */
	public final PaymentCategory newCategory;

	/**
	 * Creates a PaymentCategoryChangeRequest object
	 * @param toChange Payment to change category for
	 * @param newCategory New category of the payment
	 * @param sessionID Long identifier of session
	 */
	public PaymentCategoryChangeRequest(Payment toChange, PaymentCategory newCategory, long sessionID){
		super(RequestType.PaymentCategoryChange, sessionID);
		this.toChange = toChange;
		this.newCategory = newCategory;
	}

	/**
	 * Sends this object to ObjectOutput
	 * @param oo ObjectOutput object
	 * @throws IOException Network failure
	 */
	@Override
	public void send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(sessionID);

		toChange.send(oo);
		oo.writeInt(newCategory.ordinal());

		oo.flush();
	}

	/**
	 * Loads this object from ObjectInput
	 * @param oi ObjectInput object
	 * @return Request object
	 * @throws IOException Network failure
	 */
	public static PaymentCategoryChangeRequest ReadArgs(ObjectInput oi) throws IOException{
		long sessionID = oi.readLong();
		Payment toChange = Payment.FromObjInput(oi);
		PaymentCategory newCategory = PaymentCategory.values()[oi.readInt()];
		return new PaymentCategoryChangeRequest(toChange, newCategory, sessionID);
	}
}
