import java.io.File;

/**
 * 
 * @author Oliver Frischknecht
 * 
 */
public interface AudioFileWriter {

	/**
	 * This function writes the sound data to an audiofile.
	 * 
	 * @param File
	 * @param array
	 *            signed PCM audio
	 */
	public void writeAudioData(Double[] array, File file);
}
