import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class PaymentCategoryChangeRequest extends Request {
	public final Payment toChange;
	public final PaymentCategory newCategory;
	public PaymentCategoryChangeRequest(Payment toChange, PaymentCategory newCategory, long sessionID){
		super(RequestType.PaymentCategoryChange, sessionID);
		this.toChange = toChange;
		this.newCategory = newCategory;
	}
	@Override
	public void send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(sessionID);

		toChange.send(oo);
		oo.writeInt(newCategory.ordinal());

		oo.flush();
	}
	public static PaymentCategoryChangeRequest ReadArgs(ObjectInput oi) throws IOException{
		long sessionID = oi.readLong();
		Payment toChange = Payment.FromObjInput(oi);
		PaymentCategory newCategory = PaymentCategory.values()[oi.readInt()];
		return new PaymentCategoryChangeRequest(toChange, newCategory, sessionID);
	}
}
