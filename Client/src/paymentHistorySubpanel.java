import javax.swing.*;
import java.awt.*;

public class paymentHistorySubpanel extends JPanel {

	public paymentHistorySubpanel(LayoutManager layout, Payment payment) {
		super(layout);

		this.add(new JLabel("Sender:" + payment.senderAccountID));
		this.add(new JLabel("Receiver:" + payment.receiverAccountID));
		String temp = "Amount " + (payment.amount / 100D) + " " + payment.fromCurr + " to " + payment.toCurr;
		this.add(new JLabel(temp));
		this.add(new JLabel("Category: " + payment.category));
	}
	public paymentHistorySubpanel(LayoutManager layout, String msg) {
		super(layout);

		this.add(new JLabel("TestLeft"));
		JLabel label = new JLabel("msg");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(label);
		label = new JLabel("TestRight");
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		this.add(label);
	}

}
