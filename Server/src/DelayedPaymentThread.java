import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintWriter;
import java.util.Dictionary;

public class DelayedPaymentThread extends Thread {

	public DelayedPaymentThread(int hours, int minutes, PaymentRequest paymentRequest, ObjectInput oi, ObjectOutput oo,
	                            PrintWriter outPrinter, PrintWriter errWriter, Dictionary<Integer, Account> accounts,
	                            long sessionID){

	}
	public boolean isValid(){
		boolean res = false;

		// CONTINUE HERE

		return res;
	}
	@Override
	public void run(){

	}
}
