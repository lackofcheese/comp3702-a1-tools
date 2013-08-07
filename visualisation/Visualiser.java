package visualisation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Visualiser {
	private Container container;
	
	private VisualisationPanel vp;
	
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenuItem loadProblemItem, loadSolutionItem, assumeDirectSolutionItem, exitItem;
	private JMenu animationMenu;
	private JMenuItem initialiseItem, playPauseItem, stopItem;
	
	private JPanel animationControls;
	private JSlider manualSlider;
	private JSlider framerateSlider;
	private JSpinner resolutionSpinner;
	
	protected ImageIcon createImageIcon(String path, String description) {
		java.net.URL imgURL = getClass().getResource(path);
	    if (imgURL != null) {
	        return new ImageIcon(imgURL, description);
	    } else {
	    	return new ImageIcon(path, description);
	    }
	}
	
	private JButton playPauseButton, stopButton;
	private ImageIcon playIcon = createImageIcon("play.gif", "Play");
	private ImageIcon pauseIcon = createImageIcon("pause.gif", "Pause");
	private ImageIcon stopIcon = createImageIcon("stop.gif", "Stop");
	
	private boolean animating;
	private boolean wasPlaying = false;
	private boolean playing;
	private boolean hasProblem;
	private boolean hasSolution;
	
	private static final int FRAMERATE_MIN = 1;
	private static final int FRAMERATE_MAX = 100;
	private static final int FRAMERATE_INIT = 50;
	
	private static final int RESOLUTION_INIT = 50;
	private int resolution = RESOLUTION_INIT;
	
	private File defaultPath;
	
	private class MenuListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd.equals("Load problem")) {
				loadProblem();
			} else if (cmd.equals("Load solution")) {
				loadSolution();
			} else if (cmd.equals("Assume direct solution")) {
				assumeDirectSolution();
			} else if (cmd.equals("Exit")) {
				container.setVisible(false);
				System.exit(0);
			} else if (cmd.equals("Initialise")) {
				setAnimating(true);
			} else if (cmd.equals("Play")) {
				playPause();
			} else if (cmd.equals("Pause")) {
				playPause();
			} else if (cmd.equals("Stop")) {
				setAnimating(false);
			}
		}
	}

	private MenuListener menuListener = new MenuListener();
	
	private ChangeListener manualSliderListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			if (!manualSlider.getValueIsAdjusting() && wasPlaying) {
				wasPlaying = false;
				if (manualSlider.getValue() < manualSlider.getMaximum()) {
					vp.playPauseAnimation();
				}
			}
			vp.gotoFrame(manualSlider.getValue());
		}
	};
	
	private MouseListener manualSliderClickListener = new MouseListener() {
		@Override
		public void mousePressed(MouseEvent e) {
			if (playing) {
				wasPlaying = true;
				vp.playPauseAnimation();
			}
		}
		@Override
		public void mouseReleased(MouseEvent e) {}
		@Override
		public void mouseClicked(MouseEvent e) {}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}
	};
	
	private ChangeListener framerateListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			vp.setFramerate(framerateSlider.getValue());
		}
	};
	
	private ChangeListener resolutionListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			resolution = (Integer)resolutionSpinner.getValue();
			vp.setResolution(resolution);
		}
	};
	
	private ActionListener playPauseListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			playPause();
		}
	};
	
	private ActionListener stopListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			setAnimating(false);
		}
	};
	
	
	public Visualiser(Container container, File defaultPath) {
		this.container = container;
		this.defaultPath = defaultPath;
		createComponents();
	}

	public Visualiser(Container container) {
		this.container = container;
		try {
			this.defaultPath = new File(".").getCanonicalFile();
		} catch (IOException e) {
			this.defaultPath = null;
		}
		createComponents();
	}
	
	private void createComponents() {
		vp = new VisualisationPanel(this);
		container.setLayout(new BorderLayout());
		container.add(vp, BorderLayout.CENTER);
		
		createMenus();
		createAnimationControls();
	}
	
	private void createMenus() {
		menuBar = new JMenuBar();
		createFileMenu();
		createAnimationMenu();
		if (container instanceof JFrame) {
			((JFrame)container).setJMenuBar(menuBar);
		} else if (container instanceof JApplet) {
			((JApplet)container).setJMenuBar(menuBar);
		}
	}
	
	private void createFileMenu() {
		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.getAccessibleContext().setAccessibleDescription(
				"Load configs or close the app.");
		menuBar.add(fileMenu);
		
		loadProblemItem = new JMenuItem("Load problem");
		loadProblemItem.setMnemonic(KeyEvent.VK_P);
		loadProblemItem.addActionListener(menuListener);
		fileMenu.add(loadProblemItem);
		
		loadSolutionItem = new JMenuItem("Load solution");
		loadSolutionItem.setMnemonic(KeyEvent.VK_S);
		loadSolutionItem.addActionListener(menuListener);
		loadSolutionItem.setEnabled(false);
		fileMenu.add(loadSolutionItem);
		
		assumeDirectSolutionItem = new JMenuItem("Assume direct solution");
		assumeDirectSolutionItem.setMnemonic(KeyEvent.VK_D);
		assumeDirectSolutionItem.addActionListener(menuListener);
		assumeDirectSolutionItem.setEnabled(false);
		fileMenu.add(assumeDirectSolutionItem);
		
		fileMenu.addSeparator();
		exitItem = new JMenuItem("Exit");
		exitItem.setMnemonic(KeyEvent.VK_X);
		exitItem.addActionListener(menuListener);
		fileMenu.add(exitItem);
	}
	
	private void createAnimationMenu() {
		animationMenu = new JMenu("Animation");
		animationMenu.setMnemonic(KeyEvent.VK_A);
		fileMenu.getAccessibleContext().setAccessibleDescription(
				"Manage the animation.");
		menuBar.add(animationMenu);
		animationMenu.setEnabled(false);
		
		initialiseItem = new JMenuItem("Initialise");
		initialiseItem.setMnemonic(KeyEvent.VK_I);
		initialiseItem.addActionListener(menuListener);
		animationMenu.add(initialiseItem);
		
		playPauseItem = new JMenuItem("Play");
		playPauseItem.setMnemonic(KeyEvent.VK_P);
		playPauseItem.addActionListener(menuListener);
		animationMenu.add(playPauseItem);
		
		stopItem = new JMenuItem("Stop");
		stopItem.setMnemonic(KeyEvent.VK_T);
		stopItem.addActionListener(menuListener);
		stopItem.setEnabled(false);
		animationMenu.add(stopItem);
	}
	
	private void createAnimationControls() {
		Font sliderFont = new Font("Arial", Font.PLAIN, 12);
		
		animationControls = new JPanel();
		animationControls.setLayout(new BoxLayout(animationControls, BoxLayout.PAGE_AXIS));
		
		JLabel manualLabel = new JLabel("Frame #");
		manualLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		manualSlider = new JSlider(JSlider.HORIZONTAL);
		manualSlider.setPaintTicks(true);
		manualSlider.setPaintLabels(true);
		manualSlider.setFont(sliderFont);
		manualSlider.addChangeListener(manualSliderListener);
		manualSlider.addMouseListener(manualSliderClickListener);
	
		JLabel framerateLabel = new JLabel("Framerate");
		framerateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		framerateSlider = new JSlider(JSlider.HORIZONTAL, FRAMERATE_MIN, FRAMERATE_MAX, FRAMERATE_INIT);
		framerateSlider.setMajorTickSpacing(10);
		framerateSlider.setMinorTickSpacing(1);	
		framerateSlider.setPaintTicks(true);
		framerateSlider.setPaintLabels(true);
		framerateSlider.setLabelTable(framerateSlider.createStandardLabels(10, 10));
		framerateSlider.setFont(sliderFont);
		framerateSlider.addChangeListener(framerateListener);
		JPanel frameratePanel = new JPanel();
		frameratePanel.setLayout(new BoxLayout(frameratePanel, BoxLayout.PAGE_AXIS));
		frameratePanel.add(framerateLabel);
		frameratePanel.add(Box.createRigidArea(new Dimension(0, 2)));
		frameratePanel.add(framerateSlider);
		
		JLabel resolutionLabel = new JLabel("Resolution");
		resolutionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		resolutionSpinner = new JSpinner(new SpinnerNumberModel(RESOLUTION_INIT, 1, null, 10));
		resolutionSpinner.addChangeListener(resolutionListener);
		resolutionSpinner.setMaximumSize(new Dimension(200, 25));
		JPanel resolutionPanel = new JPanel();
		resolutionPanel.setLayout(new BoxLayout(resolutionPanel, BoxLayout.PAGE_AXIS));
		resolutionPanel.add(resolutionLabel);
		resolutionPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		resolutionPanel.add(resolutionSpinner);
		
		playPauseButton = new JButton(playIcon);
		playPauseButton.addActionListener(playPauseListener);
		stopButton = new JButton(stopIcon);
		stopButton.addActionListener(stopListener);
		
		animationControls.add(new JSeparator(JSeparator.HORIZONTAL));
		animationControls.add(Box.createRigidArea(new Dimension(0, 2)));
		animationControls.add(manualLabel);
		animationControls.add(Box.createRigidArea(new Dimension(0, 2)));
		animationControls.add(manualSlider);
		JPanel p2 = new JPanel();
		p2.setLayout(new BoxLayout(p2, BoxLayout.LINE_AXIS));
		p2.add(playPauseButton);
		p2.add(Box.createRigidArea(new Dimension(10, 0)));
		p2.add(stopButton);
		p2.add(frameratePanel);
		p2.add(Box.createRigidArea(new Dimension(20, 0)));
		p2.add(resolutionPanel);
		animationControls.add(p2);
		animationControls.setVisible(false);
		animationControls.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		container.add(animationControls, BorderLayout.SOUTH);
	}
		
	
	private File askForFile() {
		JFileChooser fc = new JFileChooser(defaultPath);
		int returnVal = fc.showOpenDialog(container);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		return fc.getSelectedFile();
	}
	
	private void showFileError(File f) {
		JOptionPane.showMessageDialog(container,
				"Error loading " + f.getName(),
				"File I/O Error",
				JOptionPane.ERROR_MESSAGE);
	}

	private void loadProblem(File f) {
		try {
			vp.getProblemSetup().loadProblem(f.getPath());
			setHasProblem(true);
		} catch (IOException e1) {
			showFileError(f);
			setHasProblem(false);
		}
		vp.updateProblemSetup();
	}
	
	private void loadProblem() {
		File f = askForFile();
		if (f == null) {
			return;
		}
		loadProblem(f);	
	}
	
	private void loadSolution(File f) {
		try {
			vp.getProblemSetup().loadSolution(f.getPath());
			setHasSolution(true);
		} catch (IOException e1) {
			showFileError(f);
			setHasSolution(false);
		}
	}
	
	private void loadSolution() {
		File f = askForFile();
		if (f == null) {
			return;
		}
		loadSolution(f);
	}

	private void assumeDirectSolution() {
		vp.getProblemSetup().assumeDirectSolution();
		setHasSolution(true);
	}
	
	
	private void playPause() {
		if (!animating) {
			setAnimating(true);
		}
		vp.playPauseAnimation();
	}

	private void setHasProblem(boolean hasProblem) {
		this.hasProblem = hasProblem;
		loadSolutionItem.setEnabled(hasProblem);
		assumeDirectSolutionItem.setEnabled(hasProblem);
		setHasSolution(false);
	}

	private void setHasSolution(boolean hasSolution) {
		this.hasSolution = hasSolution;
		animationMenu.setEnabled(hasSolution);
		setAnimating(hasSolution);
	}
	
	private void setAnimating(boolean animating) {
		if (animating) {
			vp.initAnimation();
		} else {
			vp.stopAnimation();
		}
		
		if (this.animating == animating) {
			return;
		}
		this.animating = animating;
		loadProblemItem.setEnabled(!animating);
		loadSolutionItem.setEnabled(!animating);
		assumeDirectSolutionItem.setEnabled(!animating);
		stopItem.setEnabled(animating);
		animationControls.setVisible(animating);
		container.validate();
		vp.calculateTransform();
		vp.repaint();
	}

	

	
	public void setPlaying(boolean playing) {
		if (this.playing == playing) {
			return;
		}
		this.playing = playing;
		if (playing) {
			playPauseItem.setText("Pause");
			playPauseButton.setIcon(pauseIcon);
		} else {
			playPauseItem.setText("Play");
			playPauseButton.setIcon(playIcon);
		}
		playPauseButton.repaint();
	}
	
	public void updateManualTicks() {
		manualSlider.setMaximum(resolution * (vp.getProblemSetup().getPath().size() - 1));
		manualSlider.setMajorTickSpacing(resolution);
		manualSlider.setMinorTickSpacing(1);
		manualSlider.setLabelTable(manualSlider.createStandardLabels(resolution));
	}
	
	public void setFrameNumber(int frameNumber) {
		manualSlider.setValue(frameNumber);
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Assignment 1 visualiser");
		Visualiser vis = new Visualiser(frame);
		if (args.length > 0) {
			vis.loadProblem(new File(args[0]));
			if (vis.hasProblem && args.length >= 2) {
				vis.loadSolution(new File(args[1]));
				if (vis.hasSolution) {
					vis.vp.initAnimation();
				}
			}
		}
		frame.setSize(800, 600);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.setVisible(true);
	}
}
