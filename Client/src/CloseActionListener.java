import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CloseActionListener implements ActionListener {
	private JFrame frame;

	/**
	 * Constructor for simple disposer of JFrame object
	 * @param frame JFrame to close
	 */
	public CloseActionListener(JFrame frame){
		this.frame = frame;
	}

	@Override
	/**
	 * Dispose method
	 */
	public void actionPerformed(ActionEvent actionEvent) {
		frame.dispose();
	}
}
