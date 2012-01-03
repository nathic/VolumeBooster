import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * 
 * @author Oliver Frischknecht
 * 
 */
public interface AudioFileReader {

	/**
	 * This function reads the sound data from the audiofile.
	 * 
	 * @return signed PCM audio
	 * @throws UnsupportedAudioFileException
	 */
	public double[] readAudioData() throws UnsupportedAudioFileException;

	/**
	 * This Function returns the samplingRate of the sound data
	 * 
	 * @return int samplingRate
	 */
	public int getSamplingRate();
}
