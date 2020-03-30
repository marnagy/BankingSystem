import java.io.IOException;
import java.io.ObjectOutput;
import java.util.List;

public class PaymentHistoryResponse extends Response {
	public final List<Payment> history;
	public PaymentHistoryResponse(List<Payment> history, long sessionID){
		super(ResponseType.PaymentHistoryResponse, sessionID);
		this.history = history;
	}
	@Override
	void Send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(super.sessionID);

		oo.writeInt(history.size());
		Payment temp;
		for (int i = 0; i < history.size(); i++) {
			history.get(i).Send(oo);
		}

		oo.flush();
	}
}
