import javax.swing.*;
import java.awt.*;

public class PaymentHistorySubpanel extends JPanel {

	public PaymentHistorySubpanel(Account account, Payment payment) {
		super(new GridLayout(1, 5));
		if ()
		this.add(new JLabel("Sender:" + payment.senderAccountID));
		this.add(new JLabel("Receiver:" + payment.receiverAccountID));
		String temp = "Amount " + (payment.amount / 100D) + " " + payment.fromCurr + " to " + payment.toCurr;
		this.add(new JLabel(temp));
		this.add(new JLabel("Category: " + payment.category));
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
