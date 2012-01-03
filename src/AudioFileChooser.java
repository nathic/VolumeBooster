import java.io.File;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

/**
 * 
 * @author Oliver Frischknecht
 * 
 */
public class AudioFileChooser {

	public AudioFileChooser() {

	}

	public AudioFileReader getFile() throws UnsupportedAudioFileException {
		AudioFileReader audioFile = null;
		JFileChooser jFC = new JFileChooser();
		UIManager.put("FileChooser.readOnly", true);
		jFC.setCurrentDirectory(new File("."));

		jFC.setFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				return "Audio Files";
			}

			@Override
			public boolean accept(File f) {
				// Here we can change the filtering options. For Example mp3
				// files can be accepted by adding ||
				// f.getName().toLowerCase().endsWith(".mp3")
				return f.getName().toLowerCase().endsWith(".wav")
						|| f.isDirectory();
			}
		});

		int r = jFC.showOpenDialog(new JFrame());
		if (r == JFileChooser.APPROVE_OPTION) {
			// This is the check if we have a .wav file or not
			if (jFC.getSelectedFile().getName().toLowerCase().endsWith(".wav")) {
				audioFile = new WaveFileReader(jFC.getSelectedFile());
			} else {
				throw new UnsupportedAudioFileException();
			}
		}
		return audioFile;
	}
}
