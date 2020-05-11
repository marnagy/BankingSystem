import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

/**
 * Used for sending payment history for requested month
 */
public class PaymentHistoryResponse extends Response {
	public final Payment[] history;

	/**
	 * Constructor used when creating this object on server
	 * @param history List of Payment objects
	 * @param sessionID Long identifier of session
	 */
	public PaymentHistoryResponse(List<Payment> history, long sessionID){
		super(ResponseType.PaymentHistoryResponse, sessionID);
		this.history = new Payment[history.size()];
		history.toArray(this.history);
	}

	/**
	 * Constructor used when loading from ObjectInput
	 * @param history Payment[] in requested YearMonth
	 * @param sessionID Long identifier of session
	 */
	private PaymentHistoryResponse(Payment[] history, long sessionID){
		super(ResponseType.PaymentHistoryResponse, sessionID);
		this.history = history;
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

		oo.writeInt(history.length);
		for (int i = 0; i < history.length; i++) {
			history[i].send(oo);
		}

		oo.flush();
	}

	/**
	 * Used for loading this object from ObjectInput
	 * @param oi ObjectInput object
	 * @return Response object
	 * @throws IOException Network failure
	 */
	public static PaymentHistoryResponse readArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		int size = oi.readInt();
		Payment[] history = new Payment[size];
		for (int i = 0; i < size; i++) {
			history[i] = Payment.FromObjInput(oi);
		}
		return new PaymentHistoryResponse(history, sessionID);
	}
}
