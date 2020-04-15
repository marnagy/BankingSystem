import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.ZonedDateTime;

public class PaymentHistorySubpanel extends JPanel {

	public PaymentHistorySubpanel(int accountID, Payment payment) throws InvalidFormatException {
		super(new GridLayout(1, 5));
		this.setPreferredSize(new Dimension(-1, 40));
		this.setMaximumSize(new Dimension(-1, 40));
		this.setMinimumSize(new Dimension(-1, 40));
		if ( accountID == payment.senderAccountID){
			this.add(new JLabel("Sent: " + dateTimeToString(payment.sendingDateTime)));
			this.add(new JLabel("To: " + payment.receiverAccountID));
			this.add(new JLabel("Amount: " + String.format("%.2f", -payment.amount / 100D)));
		}
		else if ( accountID == payment.receiverAccountID){
			this.add(new JLabel("Received: " + dateTimeToString(payment.receivedDateTime)));
			this.add(new JLabel("From: " + payment.senderAccountID));
			this.add(new JLabel("Amount sent: " + String.format("%.2f", payment.amount / 100D)));
		}
		else{
			throw new InvalidFormatException("Invalid information in payment: account " + accountID +
					" is not sender neither receiver.");
		}
		this.add(new JLabel(payment.fromCurr.name() + " -> " + payment.toCurr.name()));
		//this.add(new JLabel("Category: " + payment.category));
		var comboBox = new JComboBox(new DefaultComboBoxModel(PaymentCategory.values()));
		comboBox.setSelectedItem(payment.category);
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				if ((PaymentCategory)comboBox.getSelectedItem() != payment.category){

				}
			}
		});
		this.add(comboBox);
	}

	private String dateTimeToString(ZonedDateTime datetime){
		return String.format("%02d:%02d:%02d %02d.%02d.%04d",
				datetime.getHour(), datetime.getMinute(), datetime.getSecond(),
				datetime.getDayOfMonth(), datetime.getMonthValue(), datetime.getYear());
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
