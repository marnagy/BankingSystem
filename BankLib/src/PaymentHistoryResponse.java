import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

public class PaymentHistoryResponse extends Response {
	public final Payment[] history;
	public PaymentHistoryResponse(List<Payment> history, long sessionID){
		super(ResponseType.PaymentHistoryResponse, sessionID);
		this.history = new Payment[history.size()];
		history.toArray(this.history);
	}
	private PaymentHistoryResponse(Payment[] history, long sessionID){
		super(ResponseType.PaymentHistoryResponse, sessionID);
		this.history = history;
	}
	@Override
	void send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);

		oo.writeInt(history.length);
		for (int i = 0; i < history.length; i++) {
			history[i].Send(oo);
		}

		oo.flush();
	}
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
