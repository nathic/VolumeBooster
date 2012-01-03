import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 * 
 * @author Oliver Frischknecht
 * 
 */
public class AudioFileSaver {
	public AudioFileSaver() {
	}

	public File saveFile() {
		JFileChooser jFC = new JFileChooser();
		jFC.setDialogTitle("Save");
		jFC.setApproveButtonText("Save");
		int option = jFC.showOpenDialog(new JFrame());
		if (option == JFileChooser.APPROVE_OPTION) {
			if (jFC.getSelectedFile() != null) {
				return jFC.getSelectedFile();
			}
		} else if (option == JFileChooser.CANCEL_OPTION) {
			return new File("cancel_option");
		}
		return new File("");
	}
}
