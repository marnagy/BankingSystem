import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CloseActionListener implements ActionListener {
	private JFrame frame;
	public CloseActionListener(JFrame frame){
		this.frame = frame;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		frame.dispose();
	}
}
