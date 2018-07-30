package cop5556sp18;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class demo {

	public static void main (String args[]) {


JFrame frame = new JFrame();
ImageIcon icon = new ImageIcon("image.png");
frame.add(new JLabel(icon));
frame.pack();
frame.setVisible(true);
	}
	
}
