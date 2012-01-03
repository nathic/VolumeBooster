import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;

/**
 * 
 * @author Oliver Frischknecht
 * 
 */
public class GUI {
	protected static final int ChartRenderingInfo = 0;
	private JFrame jFrame = new JFrame("Sound Machine");
	private JPanel toolbarPanel;
	private JPanel timeDomainPanel;
	private JPanel frequencyDomainPanel;
	private JLabel timeDomainLabel = new JLabel("");
	private JLabel frequencyDomainLabel = new JLabel("");
	private JComboBox sampleComboBox;
	private JComboBox windowFunctionComboBox;
	private JLabel sampleComboBoxLabel;
	private JLabel windowFunctionComboBoxLabel;
	private Slider slider;
	private AudioFileReader audioFileReader;
	private FrequencyAnalysis fA;
	private DefaultComboBoxModel sampleComboxModel;
	private JButton save;

	public GUI() {

	}

	/**
	 * This Method visualizes the sound data from the time domain
	 * 
	 * @throws UnsupportedAudioFileException
	 */
	public void visualizeWaveform() throws UnsupportedAudioFileException {
		AudioChannelDataset dataset = new AudioChannelDataset(
				audioFileReader.readAudioData(),
				audioFileReader.getSamplingRate());
		JFreeChart chart = Chart.getChart(dataset, "Waveform (Left Channel)",
				"Time (Seconds)", "Amplitude");

		ChartRenderingInfo chartRenderingInfo = new ChartRenderingInfo();
		BufferedImage bImage = chart.createBufferedImage(400, 400,
				chartRenderingInfo);
		timeDomainLabel.removeAll();
		timeDomainLabel.setIcon(new ImageIcon(bImage));
		Slider slider = setSliderArea(chart, chartRenderingInfo);
		slider.setOffset(0);
		timeDomainLabel.add(slider);
		timeDomainLabel.updateUI();
	}

	/**
	 * This Method generates the slider, which will be used for area selection
	 * 
	 * @param chart
	 * @param info
	 * @return Slider
	 */
	private Slider setSliderArea(JFreeChart chart, ChartRenderingInfo info) {
		// Get Info about the dataArea
		Rectangle2D dataArea = info.getPlotInfo().getDataArea();
		Rectangle bounds = dataArea.getBounds();
		XYPlot xyPlot = chart.getXYPlot();
		// Calculate lastDataPoint
		Double lastDataPoint = xyPlot.getDataRange(xyPlot.getDomainAxis())
				.getUpperBound();
		double xxx = xyPlot.getDomainAxis().valueToJava2D(lastDataPoint,
				dataArea, xyPlot.getDomainAxisEdge());
		xxx -= bounds.getX();

		bounds.setSize((int) xxx, (int) bounds.getHeight() - 2);
		bounds.setLocation((int) bounds.getX() + 1, (int) bounds.getY() + 1);

		slider = new Slider(audioFileReader.getSamplingRate(), lastDataPoint,
				bounds);

		slider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				try {
					if (slider.getOffsetDataPoint() >= 0.0) {
						// Must be set to avoid that the slider goes out of area
						// while changing its size
						fA.setOffset((int) slider.getOffsetDataPoint());
					} else {
						fA.setOffset(0);
					}
					fA.setNumberOfSamplesToAnalyze(Integer
							.parseInt((String) sampleComboBox.getSelectedItem()));
					doFrequencyAnalysis();

				} catch (NullPointerException e1) {
					System.out.println("Doesn't work for you");
					e1.printStackTrace();
				} catch (UnsupportedAudioFileException e1) {
					e1.printStackTrace();
				}
			}
		});

		return slider;
	}

	/**
	 * This Method will run the Frequency Analysis and plot the Spectrum
	 * 
	 * @throws UnsupportedAudioFileException
	 */
	public void doFrequencyAnalysis() throws UnsupportedAudioFileException {
		// run Fourier transformation
		Complex[] analyzed = fA.getSpectrum();
		SpectrumDataset dataset = new SpectrumDataset(analyzed,
				audioFileReader.getSamplingRate());
		JFreeChart chart = Chart.getChart(dataset, "FFT (Left Channel)",
				"Frequency (Hz)", "Amplitude (dB)");
		ChartRenderingInfo chartRenderingInfo = new ChartRenderingInfo();
		BufferedImage bImage = chart.createBufferedImage(400, 400,
				chartRenderingInfo);
		frequencyDomainLabel.removeAll();
		frequencyDomainLabel.setIcon(new ImageIcon(bImage));
		frequencyDomainLabel.updateUI();
	}

	/**
	 * initializes the user interface
	 */
	public void init() {

		jFrame.setSize(new Dimension(820, 600));
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setResizable(false);

		// create JMenuBar and add it to the JFrame
		JMenuBar menuBar = new JMenuBar();
		jFrame.setJMenuBar(menuBar);

		// create JMenus
		JMenu mnFile = new JMenu("File");
		JMenu mnInfo = new JMenu("About");

		// creates JMenuItems
		JMenuItem mntmOpen = new JMenuItem("Open");
		JMenuItem mntmExit = new JMenuItem("Exit");
		final JMenuItem mntmSave = new JMenuItem("Save as...");
		mntmSave.setEnabled(false);
		JMenuItem mntmAboutAudioProject = new JMenuItem("About Audio Project");

		// map keybord shortcuts
		setMnemonicsAndAccelerators(mnFile, mnInfo, mntmOpen, mntmSave,
				mntmExit);

		// Implementation of ActionListeners of the GUI-Elements

		mntmAboutAudioProject.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String message = "This is Oliver Frischknechts Sound Maschine for his IPA! \nPlease contact me if you're having problems. \n(oliver.frischknecht@hsr.ch)";
				JOptionPane.showMessageDialog(jFrame, message);
			}
		});

		ActionListener openFileAction = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Chose a file
				AudioFileChooser fC = new AudioFileChooser();
				try {
					audioFileReader = fC.getFile();
					if (audioFileReader != null) {
						// visualize Waveform
						visualizeWaveform();
						fA = new FrequencyAnalysis(
								audioFileReader.readAudioData());
						// calculate values in the samples to Analyze ComboBox
						updateSampleComboBox(audioFileReader);
						windowFunctionComboBox.setSelectedIndex(0);
						// enable save option and windowFunction ComboBox
						windowFunctionComboBox.setEnabled(true);
						mntmSave.setEnabled(true);
						save.setEnabled(true);

					}
				} catch (UnsupportedAudioFileException e1) {
					String message = "Fileformat not supported";
					JOptionPane.showMessageDialog(jFrame, message);
				}
			}

			private void updateSampleComboBox(AudioFileReader audioFile)
					throws UnsupportedAudioFileException {
				sampleComboBox.setEnabled(true);
				sampleComboxModel.removeAllElements();
				// calculate values in the samples to Analyze ComboBox
				int i = 1024;
				for (; i < Math.min(524288, audioFile.readAudioData().length); i <<= 1) {
					sampleComboxModel.addElement(Integer.toString(i));
				}
				sampleComboxModel.addElement(Integer.toString(i << 1));
				sampleComboBox.setSelectedIndex(0);
			}
		};
		mntmOpen.addActionListener(openFileAction);

		mntmExit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("closing the program");
				System.exit(1);
			}
		});

		ActionListener saveFileAction = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				File f = new File("");
				// chose where to save the file and under which name
				AudioFileSaver audioFileSaver = new AudioFileSaver();
				while (f.exists() || f.getName() == "") {
					f = audioFileSaver.saveFile();
					// Checks if the file already exists
					if (f.exists()) {
						String message = "File already exists! Do you really want to overwrite it?";
						// Checks weather you want to overwrite the file or if
						// you want to change the name
						if (JOptionPane.showConfirmDialog(jFrame, message,
								"File already exists", 1) == 0) {
							if (f.getName().endsWith(".wav."))
								break;
						}
					}
				}
				if (f.getName() != "cancel_option") {
					if (!f.getName().endsWith(".wav")) {
						f = new File(f.getAbsolutePath() + ".wav");
					}
					Double[] audioFileFromSpectrum = fA
							.getAudioFileFromSpectrum();
					AudioFileWriter af = new WaveFileWriter();
					af.writeAudioData(audioFileFromSpectrum, f);
				}
			}
		};

		// Actually adding the listeners
		mntmOpen.addActionListener(openFileAction);
		mntmSave.addActionListener(saveFileAction);

		// adding the menuitems to the menu and the menus to the menu bar
		menuBar.add(mnFile);
		menuBar.add(mnInfo);
		mnFile.add(mntmOpen);
		mnFile.add(mntmSave);
		mnFile.add(mntmExit);
		mnInfo.add(mntmAboutAudioProject);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 155, 282, 155, 0 };
		gridBagLayout.rowHeights = new int[] { 40, 0, 511, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 1.0,
				Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		jFrame.getContentPane().setLayout(gridBagLayout);

		toolbarPanel = new JPanel();
		GridBagConstraints gbc_toolbarPanel = new GridBagConstraints();
		gbc_toolbarPanel.anchor = GridBagConstraints.NORTH;
		gbc_toolbarPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_toolbarPanel.insets = new Insets(0, 0, 5, 0);
		gbc_toolbarPanel.gridwidth = 3;
		gbc_toolbarPanel.gridx = 0;
		gbc_toolbarPanel.gridy = 0;
		jFrame.getContentPane().add(toolbarPanel, gbc_toolbarPanel);

		GridBagLayout gbl_toolbarPanel = new GridBagLayout();
		gbl_toolbarPanel.columnWidths = new int[] { 108, 0 };
		gbl_toolbarPanel.rowHeights = new int[] { 40, 0 };
		gbl_toolbarPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_toolbarPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		toolbarPanel.setLayout(gbl_toolbarPanel);

		JToolBar toolbar = new JToolBar();
		JButton open = new JButton();
		save = new JButton();
		save.setEnabled(false);
		sampleComboBoxLabel = new JLabel("  Samples to analyze: ");
		windowFunctionComboBoxLabel = new JLabel("  Windowfunction: ");
		sampleComboBox = new JComboBox();
		sampleComboBox.setEnabled(false);
		sampleComboxModel = new DefaultComboBoxModel(new String[] { "Samples" });
		sampleComboBox.setModel(sampleComboxModel);
		windowFunctionComboBox = new JComboBox();
		windowFunctionComboBox.setEnabled(false);
		windowFunctionComboBox.setModel(new DefaultComboBoxModel(new String[] {
				"None", "Hamming" }));

		// add action listener for the sampleComboBox
		sampleComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					// the selected value will be parsed and the slider width
					// will be set
					int sliderWidth = Integer.parseInt(sampleComboBox
							.getSelectedItem().toString());
					slider.setWidth(sliderWidth);
				} catch (NullPointerException e1) {
					e1.getStackTrace();
				}
			}

		});

		// add action listener for the windowFunctionComboBox
		windowFunctionComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Changes the windowFunction and plots the new Spectrum
				String windowName = windowFunctionComboBox.getSelectedItem()
						.toString();
				WindowFunction windowFunction = null;
				if (windowName == "Hamming") {
					windowFunction = new Hamming();
				} else {
					windowFunction = new NoneWindow();
				}
				fA.setWindowFunction(windowFunction);
				try {
					doFrequencyAnalysis();
				} catch (UnsupportedAudioFileException e1) {
					e1.printStackTrace();
				}
			}
		});

		save.addActionListener(saveFileAction);
		open.addActionListener(openFileAction);
		save.setIcon(new ImageIcon(this.getClass().getResource(
				"toolbarButtonGraphics/general/Save24.gif")));
		open.setIcon(new ImageIcon(this.getClass().getResource(
				"toolbarButtonGraphics/general/Open24.gif")));

		GridBagConstraints gbc_open_label = new GridBagConstraints();
		gbc_open_label.fill = GridBagConstraints.HORIZONTAL;
		gbc_open_label.anchor = GridBagConstraints.LINE_START;
		gbc_open_label.gridx = 0;
		gbc_open_label.gridy = 0;

		GridBagConstraints gbc_save_label = new GridBagConstraints();
		gbc_save_label.fill = GridBagConstraints.HORIZONTAL;
		gbc_save_label.anchor = GridBagConstraints.LINE_START;
		gbc_save_label.gridx = 1;
		gbc_save_label.gridy = 0;

		GridBagConstraints gbc_sampleComboBoxLabel = new GridBagConstraints();
		gbc_sampleComboBoxLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_sampleComboBoxLabel.anchor = GridBagConstraints.LINE_START;
		gbc_sampleComboBoxLabel.gridx = 2;
		gbc_sampleComboBoxLabel.gridy = 0;

		GridBagConstraints gbc_sampleComboBox = new GridBagConstraints();
		gbc_sampleComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_sampleComboBox.anchor = GridBagConstraints.LINE_START;
		gbc_sampleComboBox.gridx = 3;
		gbc_sampleComboBox.gridy = 0;

		GridBagConstraints gbc_windowFunctionComboBoxLabel = new GridBagConstraints();
		gbc_windowFunctionComboBoxLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_windowFunctionComboBoxLabel.anchor = GridBagConstraints.LINE_START;
		gbc_windowFunctionComboBoxLabel.gridx = 4;
		gbc_windowFunctionComboBoxLabel.gridy = 0;

		GridBagConstraints gbc_windowFunctionComboBox = new GridBagConstraints();
		gbc_windowFunctionComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_windowFunctionComboBox.anchor = GridBagConstraints.LINE_START;
		gbc_windowFunctionComboBox.gridx = 5;
		gbc_windowFunctionComboBox.gridy = 0;

		toolbar.setLayout(new GridBagLayout());
		toolbar.setFloatable(false);

		toolbar.add(open, gbc_open_label);
		toolbar.add(save, gbc_save_label);
		toolbar.add(sampleComboBoxLabel, gbc_sampleComboBoxLabel);
		toolbar.add(sampleComboBox, gbc_sampleComboBox);
		toolbar.add(windowFunctionComboBoxLabel,
				gbc_windowFunctionComboBoxLabel);
		toolbar.add(windowFunctionComboBox, gbc_windowFunctionComboBox);

		GridBagConstraints gbc_toolbar = new GridBagConstraints();
		gbc_toolbar.fill = GridBagConstraints.VERTICAL;
		gbc_toolbar.anchor = GridBagConstraints.WEST;
		gbc_toolbar.gridx = 0;
		gbc_toolbar.gridy = 0;
		toolbarPanel.add(toolbar, gbc_toolbar);

		timeDomainPanel = new JPanel();

		GridBagConstraints gbc_timeDomainPanel = new GridBagConstraints();
		gbc_timeDomainPanel.anchor = GridBagConstraints.WEST;
		gbc_timeDomainPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_timeDomainPanel.insets = new Insets(0, 0, 0, 5);
		gbc_timeDomainPanel.gridx = 0;
		gbc_timeDomainPanel.gridy = 2;
		jFrame.getContentPane().add(timeDomainPanel, gbc_timeDomainPanel);
		GridBagLayout gbl_timeDomainPanel = new GridBagLayout();
		gbl_timeDomainPanel.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0 };
		gbl_timeDomainPanel.columnWeights = new double[] { 1.0, 0.0, 0.0, 0.0,
				0.0, 0.0 };
		gbl_timeDomainPanel.columnWidths = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_timeDomainPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		timeDomainPanel.setLayout(gbl_timeDomainPanel);

		GridBagConstraints gbc_timeDomainLabel = new GridBagConstraints();
		gbc_timeDomainLabel.insets = new Insets(0, 0, 5, 5);
		gbc_timeDomainLabel.gridy = 0;
		gbc_timeDomainLabel.gridx = 0;

		timeDomainPanel.add(timeDomainLabel, gbc_timeDomainLabel);
		frequencyDomainPanel = new JPanel();

		GridBagConstraints gbc_frequencyDomainPanel = new GridBagConstraints();
		gbc_frequencyDomainPanel.insets = new Insets(0, 0, 0, 5);
		gbc_frequencyDomainPanel.anchor = GridBagConstraints.WEST;
		gbc_frequencyDomainPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_frequencyDomainPanel.gridx = 1;
		gbc_frequencyDomainPanel.gridy = 2;
		jFrame.getContentPane().add(frequencyDomainPanel,
				gbc_frequencyDomainPanel);
		GridBagLayout gbl_frequencyDomainPanel = new GridBagLayout();
		gbl_frequencyDomainPanel.rowWeights = new double[] { 1.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		gbl_frequencyDomainPanel.columnWeights = new double[] { 1.0, 0.0, 0.0,
				0.0, 0.0, 0.0 };
		gbl_frequencyDomainPanel.columnWidths = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_frequencyDomainPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0,
				0, 0 };
		frequencyDomainPanel.setLayout(gbl_frequencyDomainPanel);

		GridBagConstraints gbc_frequencyDomainLabel = new GridBagConstraints();
		gbc_frequencyDomainLabel.insets = new Insets(0, 0, 5, 5);
		gbc_frequencyDomainLabel.gridy = 0;
		gbc_frequencyDomainLabel.gridx = 0;
		frequencyDomainPanel
				.add(frequencyDomainLabel, gbc_frequencyDomainLabel);

		jFrame.setVisible(true);
		jFrame.setLocationRelativeTo(null);

	}

	// Keyboard shortcuts will be mapped here
	private void setMnemonicsAndAccelerators(JMenu mnFile, JMenu mnInfo,
			final JMenuItem mntmOpen, JMenuItem mntmSave, JMenuItem mntmExit) {
		mnFile.setMnemonic('F');
		mnInfo.setMnemonic('A');
		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.CTRL_MASK));
		mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				InputEvent.CTRL_MASK));
		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
				InputEvent.CTRL_MASK));
	}

	public static void main(String args[]) {
		GUI gui = new GUI();
		gui.init();
	}
}
