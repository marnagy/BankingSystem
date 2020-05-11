import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Main window containing payment screen, payment history, log out and exit(includes log out).
 */
public class LoggedInForm {
	private static final Pattern amountPattern = Pattern.compile("(([1-9][0-9]*)|0)(\\.[0-9]{2})?");

	// generated
	private JPanel panel1;
	private JPanel paymentPanel;
	private JPanel historyPanel;
	private JPanel leftPanel;
	private JButton paymentHistoryButton;
	private JButton exitButton;
	private JLabel AccountHeader;
	private JTextField receiverSIDTextField;
	private JTextField amountTextField;
	private JComboBox fromCurrencyComboBox;
	private JComboBox toCurrencyComboBox;
	private JTextField variableSymbolTextField;
	private JTextField specificSymbolTextField;
	private JTextField typeHereTextField;
	private JButton makePaymentButton;
	private JButton sendPaymentButton;
	private JPanel parentPanel;
	private JPanel accountBalancePanel;
	private JPanel homePanel;
	private JButton logOutButton;
	private JComboBox accountBalanceComboBox;
	private JLabel balanceLabel;
	private JComboBox monthComboBox;
	private JPanel monthHistoryPanel;
	private JComboBox hoursDelayBox;
	private JComboBox minutesDelayBox;
	private JLabel accountIDLabel;

	// custom-added
	private final ObjectInput oi;
	private final ObjectOutput oo;
	private final Account account;
	private final JFrame frame;
	private final long sessionID;
	private final ClientSession session;

	/**
	 * Constructor of LoggedInForm
	 * @param frame Used for disposing
	 * @param account Logged in account
	 * @param oi Object input
	 * @param oo Object output
	 * @param session ClientSession object
	 * @param sessionID Long identifier of session
	 */
	private LoggedInForm(JFrame frame, Account account, ObjectInput oi, ObjectOutput oo,
	                     ClientSession session, long sessionID) {
		this.account = account;
		this.oi = oi;
		this.oo = oo;
		this.frame = frame;
		this.session = session;
		this.sessionID = sessionID;
		makePaymentButton.addActionListener(actionEvent -> {
			//ClearTextBoxes();
			parentPanel.removeAll();
			parentPanel.add(paymentPanel);
			parentPanel.repaint();
			parentPanel.revalidate();
		});
		paymentHistoryButton.addActionListener(actionEvent -> {
			parentPanel.removeAll();
			parentPanel.add(historyPanel);
			parentPanel.repaint();
			parentPanel.revalidate();
			try {
				updateHistoryPanel();
			} catch (IOException e) {
				MessageForm.Show("IO Error occurred");
			}
		});
		exitButton.addActionListener(actionEvent -> {
			// close connection
			try {
				Request req = new EndRequest(sessionID);
				req.send(oo);
				ResponseType respType = ResponseType.values()[oi.readInt()];
				if (respType != ResponseType.Success) {
					MessageForm.Show("Error occurred during exiting out.");
					return;
				} else {
					Response resp = SuccessResponse.readArgs(oi);
					session.close();
				}
			} catch (IOException e) {
			}

			// close window
			frame.dispose();
		});
		sendPaymentButton.addActionListener(actionEvent -> {
			String msg = null;
			String amountText = amountTextField.getText();
			if (checkAmount(amountText)) {
				long amount = amountToLong(amountText);
				if (isValidID(receiverSIDTextField.getText())) {
					int receiverID = Integer.parseInt(receiverSIDTextField.getText());

					CurrencyType fromCurr = (CurrencyType) fromCurrencyComboBox.getSelectedItem();
					CurrencyType toCurr = (CurrencyType) toCurrencyComboBox.getSelectedItem();

					if (receiverID == account.accountID && fromCurr == toCurr) {
						msg = "You can't send yourself money to same currency. Nothing will change.";
					} else if (hasEnoughMoney(account, amount, fromCurr)) {
						try {
							String[] symbols = {variableSymbolTextField.getText(), specificSymbolTextField.getText()};
							int hoursDelay = (int) hoursDelayBox.getSelectedItem();
							int minutesDelay = (int) minutesDelayBox.getSelectedItem();
							Request req = new PaymentRequest(account.accountID, receiverID, amount, hoursDelay, minutesDelay,
									fromCurr, toCurr, symbols, typeHereTextField.getText(), sessionID);
							req.send(oo);
							SuccessPaymentResponse spresp;
							Response resp;
							ResponseType respType = ResponseType.values()[oi.readInt()];
							switch (respType) {
								case InvalidReceiverIDResponse:
									msg = "Invalid receiver ID.";
									break;
								case SuccessPaymentResponse:
									spresp = SuccessPaymentResponse.readArgs(oi);
									msg = "Payment sent and processed.";
									account.trySubtract(fromCurr, amount);
									if (receiverID == account.accountID) {
										account.tryAdd(toCurr, (long) (spresp.payment.convRate * amount));
									}
									updateBalance(account, balanceLabel, accountBalanceComboBox);
									break;
								case Success:
									SuccessResponse temp = SuccessResponse.readArgs(oi);
									msg = "Payment is about to be processed in the given time.";
									break;
								case UnknownErrorResponse:
									resp = UnknownErrorResponse.readArgs(oi);
									msg = "Unknown response error occurred.";
									break;
							}

						} catch (IOException e) {
							msg = "Network error occurred.";
						}
					} else {
						msg = "Not enough money in account.";
					}
				} else {
					msg = "Incorrect format of receiver's ID.";
				}
			} else {
				msg = "Incorrect format of number.";
			}
			MessageForm.Show(msg);
		});
		accountBalanceComboBox.addActionListener(actionEvent -> {
			double val = account.getBalance((CurrencyType) accountBalanceComboBox.getSelectedItem()) / 100D;
			balanceLabel.setText(String.format("%.2f", val));
		});
		logOutButton.addActionListener(actionEvent -> {
// close connection
			try {
				Request req = new LogOutRequest(sessionID);
				req.send(oo);
				ResponseType respType = ResponseType.values()[oi.readInt()];
				if (respType != ResponseType.Success) {
					IllegalRequestResponse resp = IllegalRequestResponse.readArgs(oi);
					MessageForm.Show("Error occurred during logging out.");
					return;
				} else {
					Response resp = SuccessResponse.readArgs(oi);
					ClientGUI.Open(session, oi, oo);
					frame.dispose();
				}
			} catch (IOException e) {
			}
		});
		monthComboBox.addActionListener(actionEvent -> {
			try {
				updateHistoryPanel();
			} catch (IOException e) {
				MessageForm.Show("IO Error occurred");
			}
		});
	}

	/**
	 * Used for getting history of account for selected month
	 * @throws IOException Network failure
	 */
	private void updateHistoryPanel() throws IOException {
		YearMonth selectedMonth = (YearMonth) monthComboBox.getSelectedItem();
		monthHistoryPanel.removeAll();

		Request req = new PaymentHistoryRequest((YearMonth) monthComboBox.getSelectedItem(), account, sessionID);
		req.send(oo);
		ResponseType respType = ResponseType.values()[oi.readInt()];
		String msg = null;
		switch (respType) {
			case PaymentHistoryResponse:
				PaymentHistoryResponse resp = PaymentHistoryResponse.readArgs(oi);
				account.updatePaymentHistory(selectedMonth, resp.history);
				monthHistoryPanel.setLayout(new GridLayout(resp.history.length, 1));
				for (int j = resp.history.length - 1; j >= 0; j--) {
					try {
						JPanel subpanel = new PaymentHistorySubpanel(account.accountID, resp.history[j], oi, oo, sessionID);
						monthHistoryPanel.add(subpanel);
					} catch (InvalidFormatException e) {
						System.err.println("Invalid payment received");
					}
				}
				break;
			case IllegalRequestResponse:
				msg = "Illegal request received by server";
				break;
			default:
				msg = "Unknown response from server";
				break;
		}
		if (msg != null) { // true, when problem occurs
			MessageForm.Show(msg);
		} else {
			monthHistoryPanel.revalidate();
			monthHistoryPanel.repaint();
		}
	}

	/**
	 * Used to update label showing balance after payment
	 * @param account Logged in Account object
	 * @param balanceLabel Label showing current balance for CurrencyType
	 * @param accountBalanceComboBox ComboBox where currency is selected
	 */
	private void updateBalance(Account account, JLabel balanceLabel, JComboBox accountBalanceComboBox) {
		double val = account.getBalance((CurrencyType) accountBalanceComboBox.getSelectedItem()) / 100D;
		balanceLabel.setText(String.format("%.2f", val));
	}

	/**
	 * Used when sending money so user can (but doesn't have to) specify decimal places
	 * @param text String containing amount
	 * @return Long representation of number with 2 decimal places
	 */
	private long amountToLong(String text) {
		long res;
		if (text.contains(".")) {
			StringBuilder send = new StringBuilder();
			for (int i = 0; i < text.length(); i++) {
				if (text.charAt(i) != '.') {
					send.append(text.charAt(i));
				}
			}
			res = Long.parseLong(send.toString());
		} else {
			res = 100 * Long.parseLong(text);
		}
		return res;
	}

	/**
	 * Used for checking if the amount to send isn't too big
	 * @param text String containing number
	 * @return
	 */
	private boolean isValidID(String text) {
		try {
			int temp = Integer.parseInt(text);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Check if enough money is in the bank so account won't go to negative numbers
	 * @param account Account object
	 * @param amount Amount to send in Long with 2 decimal places
	 * @param curr CurrencyType to check
	 * @return
	 */
	private boolean hasEnoughMoney(Account account, long amount, CurrencyType curr) {
		return account.getBalance(curr) - amount > 0;
	}

	/**
	 * Check if String satisfies amount format
	 * @param text String to test
	 * @return
	 */
	private boolean checkAmount(String text) {
		return amountPattern.matcher(text).matches();
	}

	/**
	 * Setup of the Form
	 * @param account Account that is logged in
	 * @param oi Object input
	 * @param oo Object output
	 * @param session ClientSession object
	 * @param sessionID Long identifier of session
	 */
	public static void open(Account account, ObjectInput oi, ObjectOutput oo, ClientSession session, long sessionID) {
		JFrame frame = new JFrame("LoggedInForm");

		// center frame
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);

		LoggedInForm loggedInForm = new LoggedInForm(frame, account, oi, oo, session, sessionID);
		JPanel parent = loggedInForm.parentPanel;
		parent.removeAll();
		parent.add(loggedInForm.homePanel);
		parent.repaint();
		parent.revalidate();

		//set combo boxes values
		loggedInForm.accountBalanceComboBox.setModel(new DefaultComboBoxModel(CurrencyType.values()));
		loggedInForm.accountBalanceComboBox.setSelectedItem(CurrencyType.EUR);
		loggedInForm.balanceLabel.setText(String.format("%.2f", account.getBalance(CurrencyType.EUR) / 100D));

		loggedInForm.fromCurrencyComboBox.setModel(new DefaultComboBoxModel(CurrencyType.values()));

		loggedInForm.toCurrencyComboBox.setModel(new DefaultComboBoxModel(CurrencyType.values()));

		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < 24; i++) {
			list.add(i);
		}
		loggedInForm.hoursDelayBox.setModel(new DefaultComboBoxModel(list.toArray()));

		list = new ArrayList<>();
		for (int i = 0; i < 60; i++) {
			list.add(i);
		}
		loggedInForm.minutesDelayBox.setModel(new DefaultComboBoxModel(list.toArray()));

		YearMonth monthYear = YearMonth.now();
		YearMonth created = YearMonth.of(account.created.getYear(), account.created.getMonth());
		ArrayList<YearMonth> monthYearList = new ArrayList<YearMonth>();
		while (!monthYear.equals(created)) {
			monthYearList.add(monthYear);
			monthYear = monthYear.minusMonths(1);
		}
		monthYearList.add(created);

		loggedInForm.monthComboBox.setModel(new DefaultComboBoxModel(monthYearList.toArray()));

		loggedInForm.accountIDLabel.setText("Your account ID: " + account.accountID);


		frame.setContentPane(loggedInForm.panel1);
		// when GUI uses exit, it kills process resulting in Exception on server
		// don't know how to launch custom method on dispose/exit, so this is the solution
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	{
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		panel1 = new JPanel();
		panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 1, new Insets(0, 10, 10, 10), -1, -1));
		panel1.setEnabled(true);
		final JSplitPane splitPane1 = new JSplitPane();
		panel1.add(splitPane1, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(500, -1), null, null, 0, false));
		leftPanel = new JPanel();
		leftPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
		leftPanel.setBackground(new Color(-8882056));
		splitPane1.setLeftComponent(leftPanel);
		paymentHistoryButton = new JButton();
		paymentHistoryButton.setBorderPainted(false);
		paymentHistoryButton.setContentAreaFilled(false);
		paymentHistoryButton.setFocusPainted(false);
		paymentHistoryButton.setText("Payment History");
		leftPanel.add(paymentHistoryButton, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		exitButton = new JButton();
		exitButton.setBorderPainted(false);
		exitButton.setContentAreaFilled(false);
		exitButton.setFocusPainted(false);
		exitButton.setText("Exit");
		leftPanel.add(exitButton, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		makePaymentButton = new JButton();
		makePaymentButton.setBorderPainted(false);
		makePaymentButton.setContentAreaFilled(false);
		makePaymentButton.setFocusPainted(false);
		makePaymentButton.setFocusTraversalPolicyProvider(false);
		makePaymentButton.setHideActionText(false);
		makePaymentButton.setInheritsPopupMenu(false);
		makePaymentButton.setOpaque(false);
		makePaymentButton.setText("Make Payment");
		leftPanel.add(makePaymentButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		logOutButton = new JButton();
		logOutButton.setBorderPainted(false);
		logOutButton.setContentAreaFilled(false);
		logOutButton.setFocusPainted(false);
		logOutButton.setText("Log out");
		leftPanel.add(logOutButton, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JScrollPane scrollPane1 = new JScrollPane();
		splitPane1.setRightComponent(scrollPane1);
		parentPanel = new JPanel();
		parentPanel.setLayout(new CardLayout(0, 0));
		scrollPane1.setViewportView(parentPanel);
		paymentPanel = new JPanel();
		paymentPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(7, 5, new Insets(10, 10, 10, 10), -1, -1));
		paymentPanel.setVisible(false);
		parentPanel.add(paymentPanel, "Card1");
		final JLabel label1 = new JLabel();
		label1.setText("Receiver's ID");
		paymentPanel.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		receiverSIDTextField = new JTextField();
		paymentPanel.add(receiverSIDTextField, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setText("Amount");
		paymentPanel.add(label2, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 2, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		amountTextField = new JTextField();
		paymentPanel.add(amountTextField, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 2, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label3 = new JLabel();
		label3.setText("From Currency");
		paymentPanel.add(label3, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label4 = new JLabel();
		label4.setText("To Currency");
		paymentPanel.add(label4, new com.intellij.uiDesigner.core.GridConstraints(2, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		fromCurrencyComboBox = new JComboBox();
		fromCurrencyComboBox.setEditable(true);
		fromCurrencyComboBox.setInheritsPopupMenu(true);
		fromCurrencyComboBox.setLightWeightPopupEnabled(true);
		final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
		fromCurrencyComboBox.setModel(defaultComboBoxModel1);
		paymentPanel.add(fromCurrencyComboBox, new com.intellij.uiDesigner.core.GridConstraints(1, 3, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		toCurrencyComboBox = new JComboBox();
		toCurrencyComboBox.setEditable(true);
		final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
		toCurrencyComboBox.setModel(defaultComboBoxModel2);
		paymentPanel.add(toCurrencyComboBox, new com.intellij.uiDesigner.core.GridConstraints(2, 3, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label5 = new JLabel();
		label5.setText("Variable Symbol");
		paymentPanel.add(label5, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		variableSymbolTextField = new JTextField();
		paymentPanel.add(variableSymbolTextField, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label6 = new JLabel();
		label6.setText("Specific Symbol");
		paymentPanel.add(label6, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		specificSymbolTextField = new JTextField();
		paymentPanel.add(specificSymbolTextField, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label7 = new JLabel();
		label7.setText("Information for receiver");
		paymentPanel.add(label7, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		typeHereTextField = new JTextField();
		typeHereTextField.setText("");
		typeHereTextField.setToolTipText("Optional");
		paymentPanel.add(typeHereTextField, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		sendPaymentButton = new JButton();
		sendPaymentButton.setText("Send Payment");
		paymentPanel.add(sendPaymentButton, new com.intellij.uiDesigner.core.GridConstraints(6, 3, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label8 = new JLabel();
		label8.setText("With Delay");
		paymentPanel.add(label8, new com.intellij.uiDesigner.core.GridConstraints(3, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		hoursDelayBox = new JComboBox();
		paymentPanel.add(hoursDelayBox, new com.intellij.uiDesigner.core.GridConstraints(3, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		minutesDelayBox = new JComboBox();
		paymentPanel.add(minutesDelayBox, new com.intellij.uiDesigner.core.GridConstraints(3, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label9 = new JLabel();
		label9.setText("hours");
		paymentPanel.add(label9, new com.intellij.uiDesigner.core.GridConstraints(4, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label10 = new JLabel();
		label10.setText("minutes");
		paymentPanel.add(label10, new com.intellij.uiDesigner.core.GridConstraints(4, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		historyPanel = new JPanel();
		historyPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
		historyPanel.setVisible(true);
		parentPanel.add(historyPanel, "Card2");
		final JLabel label11 = new JLabel();
		label11.setText("Month");
		historyPanel.add(label11, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JScrollPane scrollPane2 = new JScrollPane();
		historyPanel.add(scrollPane2, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		monthHistoryPanel = new JPanel();
		monthHistoryPanel.setLayout(new BorderLayout(0, 0));
		scrollPane2.setViewportView(monthHistoryPanel);
		final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
		historyPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		monthComboBox = new JComboBox();
		historyPanel.add(monthComboBox, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		homePanel = new JPanel();
		homePanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		homePanel.setBackground(new Color(-11775918));
		homePanel.setEnabled(false);
		parentPanel.add(homePanel, "Card3");
		final JLabel label12 = new JLabel();
		Font label12Font = this.$$$getFont$$$("Comic Sans MS", Font.BOLD | Font.ITALIC, 48, label12.getFont());
		if (label12Font != null) label12.setFont(label12Font);
		label12.setText("Home Screen");
		homePanel.add(label12, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		accountBalancePanel = new JPanel();
		accountBalancePanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 5, new Insets(0, 0, 0, 0), -1, -1));
		panel1.add(accountBalancePanel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(500, -1), null, null, 0, false));
		final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
		accountBalancePanel.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 50), null, 0, false));
		AccountHeader = new JLabel();
		AccountHeader.setText("Account Balance");
		accountBalancePanel.add(AccountHeader, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		balanceLabel = new JLabel();
		balanceLabel.setText("current balance");
		accountBalancePanel.add(balanceLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(64, 16), null, 0, false));
		accountBalanceComboBox = new JComboBox();
		accountBalancePanel.add(accountBalanceComboBox, new com.intellij.uiDesigner.core.GridConstraints(1, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
		accountBalancePanel.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		accountIDLabel = new JLabel();
		accountIDLabel.setText("Your account's ID -> ");
		accountBalancePanel.add(accountIDLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final com.intellij.uiDesigner.core.Spacer spacer4 = new com.intellij.uiDesigner.core.Spacer();
		accountBalancePanel.add(spacer4, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final com.intellij.uiDesigner.core.Spacer spacer5 = new com.intellij.uiDesigner.core.Spacer();
		panel1.add(spacer5, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, new Dimension(500, -1), null, null, 0, false));
		final com.intellij.uiDesigner.core.Spacer spacer6 = new com.intellij.uiDesigner.core.Spacer();
		panel1.add(spacer6, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, new Dimension(500, -1), null, null, 0, false));
		label1.setLabelFor(receiverSIDTextField);
		label2.setLabelFor(amountTextField);
		label3.setLabelFor(fromCurrencyComboBox);
		label4.setLabelFor(toCurrencyComboBox);
		label5.setLabelFor(variableSymbolTextField);
		label6.setLabelFor(specificSymbolTextField);
		label11.setLabelFor(monthComboBox);
	}

	/**
	 * @noinspection ALL
	 */
	private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
		if (currentFont == null) return null;
		String resultName;
		if (fontName == null) {
			resultName = currentFont.getName();
		} else {
			Font testFont = new Font(fontName, Font.PLAIN, 10);
			if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
				resultName = fontName;
			} else {
				resultName = currentFont.getName();
			}
		}
		return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return panel1;
	}

}
