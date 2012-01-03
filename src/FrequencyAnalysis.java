/**
 * 
 * @author Oliver Frischknecht
 * 
 */
public class FrequencyAnalysis {
	double[] array;
	Complex[] spectrumData = null;
	int numberOfSamplesToAnalze = 1024; // Default: 1024
	int offset = 0; // Default 0
	private double boostLevel = 1.0; // Default 1.0
	VolumeBoost volumeBoost = new VolumeBoost();

	public FrequencyAnalysis(double[] data) {
		array = data;
	}

	public void setNumberOfSamplesToAnalyze(int numberOfSamplesToAnalyze) {
		this.numberOfSamplesToAnalze = numberOfSamplesToAnalyze;
	}

	public Complex[] getSpectrum() {
		// If the numberOfSamplesToAnalyze is higher than the size of the
		// Audio file the highest possible number of 2 will be chosen
		if (numberOfSamplesToAnalze > array.length) {
			numberOfSamplesToAnalze = 1;
			while (numberOfSamplesToAnalze < array.length) {
				numberOfSamplesToAnalze <<= 1;
			}
			numberOfSamplesToAnalze >>= 1;
		}
		// Check that the Offset isn't out of Bounds
		if (offset + numberOfSamplesToAnalze >= array.length)
			offset = array.length - numberOfSamplesToAnalze - 1;
		spectrumData = new Complex[numberOfSamplesToAnalze];
		int k = 0, i = 0;
		for (k = offset, i = 0; i < numberOfSamplesToAnalze; i++, k++) {
			// Makes Complex Values from Double and adds the multiplication with
			// the Window Function
			spectrumData[i] = new Complex(volumeBoost.multiplyWithWindow(
					array[k], boostLevel));

		}
		FFT.FFT(spectrumData, -1); // Fast Fourier Transformation Forward
		return spectrumData;
	}

	public double[] getAudioFileFromSpectrum() {
		// Check that you can't run this Method without first running
		// getSpectrum();
		if (spectrumData == null) {
			return null;
		}
		double[] data = new double[spectrumData.length];
		FFT.FFT(spectrumData, 1); // Fast Fourier Transformation Backward
		for (int i = 0; i < spectrumData.length; i++) {
			data[i] = spectrumData[i].Real();
		}
		return data;
	}
}
