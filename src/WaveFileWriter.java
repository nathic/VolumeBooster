import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * 
 * @author Oliver Frischknecht
 * 
 */
public class WaveFileWriter implements AudioFileWriter {

	public WaveFileWriter() {

	}

	/**
	 * This function writes the sound data to an audiofile. Currently writes
	 * data mono
	 * 
	 * @param array
	 *            signed PCM audio
	 */
	@Override
	public void writeAudioData(final Double[] array, File file) {
		final int sampleSizeInBytes = 2;
		// set audioformat
		AudioFormat format = new AudioFormat(44100, sampleSizeInBytes * 8, 1,
				true, false);
		// maxValue will be used to scale back the values to their original
		// amplitude
		final double maxValue = Math.pow(2, sampleSizeInBytes * 8 - 1) - 1;
		// make an InputStream which will be used to make an AudioInputStream
		InputStream inputStream = new InputStream() {
			private int i = 0;
			private int j = 0;

			@Override
			public int read() throws IOException {
				if (j >= sampleSizeInBytes) {
					j = 0;
					i++;
				}
				if (i >= array.length - 1) {
					return -1;
				} else {
					double d = array[i] * maxValue;
					long l = (long) d;
					// returns each Byte of our now long Value and changes the
					// byte order that we have little endian in the end
					return (int) ((l >> j++ * 8) & 0xff);
				}
			}
		};

		AudioInputStream ais = new AudioInputStream(inputStream, format,
				array.length);

		File fileOut = file;
		AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
		try {
			AudioSystem.write(ais, fileType, fileOut);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
