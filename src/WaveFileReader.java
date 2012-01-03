import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFormat.Encoding;

/**
 * 
 * @author Oliver Frischknecht
 * 
 */
public class WaveFileReader implements AudioFileReader {

	private File file;
	private double[] leftChannel = null;
	private double[] rightChannel = null;
	private int samplingRate;
	private int audioChannels = 0;
	private int sampleSizeInBytes = 0;
	private AudioFileFormat audioFileFormat;
	private AudioInputStream audioInputStream;

	public WaveFileReader(File file) {
		this.file = file;
	}

	/**
	 * This function reads the sound data from the audiofile. Currently returns
	 * only the left channel.
	 * 
	 * @return signed PCM audio
	 * @throws UnsupportedAudioFileException
	 */
	@Override
	public double[] readAudioData() throws UnsupportedAudioFileException {
		// if the data is already read then we don't have to read it again, we
		// can just return it
		if (leftChannel != null) {
			return leftChannel;
		}
		ArrayList<Byte> audioDataInBytes = new ArrayList<Byte>();
		Double normFactor = 1.0;
		try {
			// get InputStream and FileFormat
			audioInputStream = AudioSystem.getAudioInputStream(file);
			audioFileFormat = AudioSystem.getAudioFileFormat(file);
			// determine sampleSizeInBits
			int sampleSizeInBits = audioFileFormat.getFormat()
					.getSampleSizeInBits();
			Double maxValue = Math.pow(2, sampleSizeInBits - 1) - 1;
			Encoding encoding = audioFileFormat.getFormat().getEncoding();
			// check whether or not the file is PCM_SIGNED
			if (!encoding.equals(Encoding.PCM_SIGNED)) {
				throw new UnsupportedAudioFileException();
			}
			// this ensures that the values are between 1 and -1
			normFactor = 1 / maxValue;
			samplingRate = (int) audioFileFormat.getFormat().getSampleRate();

			// Bytes will be read from the file and added to the ByteArray
			byte[] b = new byte[2048];
			int l;
			while ((l = audioInputStream.read(b)) > 0) {
				for (int i = 0; i < l; i++) {
					audioDataInBytes.add(b[i]);
				}
			}
			audioInputStream.close();

			audioChannels = audioFileFormat.getFormat().getChannels();
			sampleSizeInBytes = sampleSizeInBits / 8;
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		int sampleSizeTimesChannels = sampleSizeInBytes * audioChannels;
		leftChannel = new double[audioDataInBytes.size()
				/ sampleSizeTimesChannels];
		rightChannel = new double[audioDataInBytes.size()
				/ sampleSizeTimesChannels];
		// Loops over all samples
		for (int k = 0, m = 0; k < audioDataInBytes.size()
				- sampleSizeTimesChannels; k += sampleSizeTimesChannels, m++) {
			leftChannel[m] = 0.0;
			rightChannel[m] = 0.0;
			// Loops over all Bytes inside a sample and shifts the Bytes because
			// they are saved in little endian
			// and we need them in big endian
			for (int i = 0, shift = 1; i < sampleSizeInBytes; i++, shift *= 256) {
				leftChannel[m] += audioDataInBytes.get(k + i) * (shift);
				rightChannel[m] += audioDataInBytes.get(k + i
						+ sampleSizeInBytes)
						* (shift);
			}
			leftChannel[m] *= normFactor;
			rightChannel[m] *= normFactor;
		}
		return leftChannel;
	}

	/**
	 * This Function returns the samplingRate of the sound data
	 * 
	 * @return int samplingRate
	 */
	@Override
	public int getSamplingRate() {
		return samplingRate;
	}
}
