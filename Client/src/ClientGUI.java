import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.regex.Pattern;

public class ClientGUI {
	private JPanel MyPanel;
	private JTextField emailTextField;
	private JPasswordField passwordPasswordField;
	private JButton createAccountButton;
	private JButton logInButton;

	private ClientSession session;
	private ObjectInput oi;
	private ObjectOutput oo;
	private long sessionID;
	private Account account;
	private JFrame frame;

	private static final Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

	public ClientGUI(JFrame frame, ClientSession session, ObjectInput oi, ObjectOutput oo) {
		this.frame = frame;
		this.session = session;
		this.sessionID = session.sessionID;
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
		createAccountButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				String msg = null;
				try {
					if (!CheckEmail(emailTextField.getText())) {
						MessageForm.Show("Incorrect email format.");
						return;
					}
					Request req = new AccountCreateRequest(emailTextField.getText(), passwordPasswordField.getPassword(), sessionID);
					req.Send(oo);
					ResponseType respType = ResponseType.values()[oi.readInt()];

					switch (respType) {
						case AccountCreateFailResponse:
							msg = "Failed to create account.";
							break;
						case Success:
							SuccessResponse sr = SuccessResponse.ReadArgs(oi);
							msg = "Account created.";
							break;
						case EmailAlreadySignedUp:
							Response resp = EmailAlreadySignedUpResponse.ReadArgs(oi);
							msg = "Email already exists.";
							break;
						default:
							msg = "Unexpected response from server.";
							break;
					}
				} catch (IOException e) {
					msg = "Network error occured.";
				}
				MessageForm.Show(msg);
			}
		});
		logInButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				String msg = null;
				try {
					Response resp;
					Request req = new LoginRequest(emailTextField.getText(), passwordPasswordField.getPassword(), sessionID);
					req.Send(oo);
					int val = oi.readInt();
					ResponseType respType = ResponseType.values()[val];
					switch (respType) {
						case AccountInfo:
							try {
								resp = AccountInfoResponse.ReadArgs(oi);
								account = Account.fromAccountInfoResponse((AccountInfoResponse) resp);
							} catch (ClassNotFoundException e) {
								msg = "Received incorrect format of payment history.";
							}
							break;
						case IncorrectLoginError:
							msg = "Email or password are not correct. Try again.";
							break;
						default:
							msg = "Unexpected response from server.";
							break;
					}
				} catch (IOException e) {
					msg = "Network error occured.";
				}
				if (msg != null) { // error
					MessageForm.Show(msg);
				} else { // successful login
					frame.dispose();
					LoggedInForm.Open(account, oi, oo, session, sessionID);
				}
			}
		});
	}

	private boolean CheckEmail(String text) {
		return emailPattern.matcher(text).matches();
	}

	private long GetSessionID(ObjectInput oi) throws IOException {
		return oi.readLong();
	}

	public static void main(String[] args) throws IOException {
		JFrame frame = new JFrame("ClientGUI");
		ClientSession session = new ClientSession();
		ObjectInput oi = new ObjectInputStream(session.getInputStream());
		ObjectOutput oo = new ObjectOutputStream(session.getOutputStream());
		session.getID(oi);
		frame.setContentPane(new ClientGUI(frame, session, oi, oo).MyPanel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	public static void Open(ClientSession session, ObjectInput oi, ObjectOutput oo) {
		JFrame frame = new JFrame("ClientGUI");
		frame.setContentPane(new ClientGUI(frame, session, oi, oo).MyPanel);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
		MyPanel = new JPanel();
		MyPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(10, 3, new Insets(20, 20, 20, 20), -1, -1));
		final JLabel label1 = new JLabel();
		label1.setText("Email");
		MyPanel.add(label1, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		emailTextField = new JTextField();
		MyPanel.add(emailTextField, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setDoubleBuffered(false);
		label2.setEnabled(true);
		label2.setFocusable(false);
		Font label2Font = this.$$$getFont$$$("Consolas", Font.BOLD, 20, label2.getFont());
		if (label2Font != null) label2.setFont(label2Font);
		label2.setHorizontalAlignment(0);
		label2.setHorizontalTextPosition(0);
		label2.setText("BankingApp");
		label2.putClientProperty("html.disable", Boolean.FALSE);
		MyPanel.add(label2, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label3 = new JLabel();
		label3.setText("Password");
		MyPanel.add(label3, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		passwordPasswordField = new JPasswordField();
		MyPanel.add(passwordPasswordField, new com.intellij.uiDesigner.core.GridConstraints(7, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		createAccountButton = new JButton();
		createAccountButton.setText("Create Account");
		MyPanel.add(createAccountButton, new com.intellij.uiDesigner.core.GridConstraints(9, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		logInButton = new JButton();
		logInButton.setText("Log in");
		MyPanel.add(logInButton, new com.intellij.uiDesigner.core.GridConstraints(9, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
		MyPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(2, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 50), null, null, 0, false));
		final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
		MyPanel.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 10), null, null, 0, false));
		final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
		MyPanel.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(8, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 20), null, null, 0, false));
		final com.intellij.uiDesigner.core.Spacer spacer4 = new com.intellij.uiDesigner.core.Spacer();
		MyPanel.add(spacer4, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(150, -1), null, null, 0, false));
		final com.intellij.uiDesigner.core.Spacer spacer5 = new com.intellij.uiDesigner.core.Spacer();
		MyPanel.add(spacer5, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(150, -1), null, null, 0, false));
		label1.setLabelFor(emailTextField);
		label3.setLabelFor(passwordPasswordField);
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
		return MyPanel;
	}

}
