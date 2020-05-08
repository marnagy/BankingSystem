import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.ZonedDateTime;

public class PaymentHistorySubpanel extends JPanel {
	final ObjectInput oi;
	final ObjectOutput oo;
	final long sessionID;
	final int accountID;
	final Payment payment;

	/**
	 * Construct the subpanel
	 * @param accountID ID of your account
	 * @param payment Payment object
	 * @param oi Object Input
	 * @param oo Object Output
	 * @param sessionID Long identifier of session
	 * @throws InvalidFormatException Some failure
	 */
	public PaymentHistorySubpanel(int accountID, Payment payment, ObjectInput oi, ObjectOutput oo, long sessionID) throws InvalidFormatException {
		super(new GridLayout(1, 5));
		this.setPreferredSize(new Dimension(-1, 40));
		this.setMaximumSize(new Dimension(-1, 40));
		this.setMinimumSize(new Dimension(-1, 40));
		this.oi = oi;
		this.oo = oo;
		this.payment = payment;
		this.sessionID = sessionID;
		this.accountID = accountID;

		if ( accountID == payment.senderAccountID){
			this.add(new JLabel("Sent: " + dateTimeToString(payment.sendingDateTime)));
			if (accountID == payment.receiverAccountID){
				this.add(new JLabel("Self Payment"));
			}
			else{
				this.add(new JLabel("To: " + payment.receiverAccountID));
			}
			this.add(new JLabel("Amount sent: " + String.format("%.2f", -payment.amount / 100D)));
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
		var comboBox = new JComboBox(new DefaultComboBoxModel(PaymentCategory.values()));
		comboBox.setSelectedItem(payment.category);
		comboBox.addActionListener(actionEvent -> {
			if (comboBox.getSelectedItem() != payment.category) {
				Request req = new PaymentCategoryChangeRequest(payment, (PaymentCategory) comboBox.getSelectedItem(), sessionID);
				try {
					req.send(oo);
					ResponseType respType = ResponseType.values()[oi.readInt()];
					String msg = "!Serious error occured!";
					Response resp;
					switch (respType) {
						case Success:
							resp = SuccessResponse.readArgs(oi);
							msg = "Category changed successfully.";
							break;
						case UnknownErrorResponse:
							UnknownErrorResponse UEResponse = UnknownErrorResponse.readArgs(oi);
							msg = UEResponse.msg;
							break;
					}
					MessageForm.Show(msg);
				} catch (IOException e) {
					String msg = "Category failed to change due to network error.";
					comboBox.setSelectedItem(payment.category);
					MessageForm.Show(msg);
				}

			}
		});
		this.add(comboBox);
	}

	/**
	 * Method for constructing datetime String
	 * @param datetime ZonedDateTime object
	 * @return Formated DateTime String
	 */
	private String dateTimeToString(ZonedDateTime datetime){
		return String.format("%02d:%02d:%02d %02d.%02d.%04d",
				datetime.getHour(), datetime.getMinute(), datetime.getSecond(),
				datetime.getDayOfMonth(), datetime.getMonthValue(), datetime.getYear());
	}
}
