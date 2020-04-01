import javax.swing.*;
import java.awt.*;

public class PaymentHistorySubpanel extends JPanel {

	public PaymentHistorySubpanel(Account account, Payment payment) throws InvalidFormatException {
		super(new GridLayout(1, 5));
		this.setPreferredSize(new Dimension(-1, 40));
		if (account.accountID == payment.senderAccountID){
			this.add(new JLabel("To: " + payment.receiverAccountID));
			this.add(new JLabel("Amount: " + String.format("%.2f", -payment.amount / 100D)));
		}
		else if (account.accountID == payment.receiverAccountID){
			this.add(new JLabel("From: " + payment.senderAccountID));
			this.add(new JLabel("Amount: " + String.format("%.2f", payment.amount / 100D)));
		}
		else{
			throw new InvalidFormatException("Invalid information in payment: account " + account.accountID +
					" is not sender neither receiver.");
		}

	}

	// for test
//	public PaymentHistorySubpanel(LayoutManager layout, String msg) {
//		super(layout);
//
//		this.add(new JLabel("TestLeft"));
//		JLabel label = new JLabel("msg");
//		label.setHorizontalAlignment(SwingConstants.CENTER);
//		this.add(label);
//		label = new JLabel("TestRight");
//		label.setHorizontalAlignment(SwingConstants.RIGHT);
//		this.add(label);
//	}

}
