package visualisation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.Timer;

public class VisualisationPanel extends JPanel {
	/** UID, as required by Swing */
	private static final long serialVersionUID = -4286532773714402501L;
	private static final double PAD_FACTOR = 0.05;
	
	private ProblemSetup problemSetup = new ProblemSetup();
	private Visualiser visualiser;
	
	private double minWorldX;
	private double maxWorldY;
	private double worldWidth;
	private double worldHeight;
	private AffineTransform translation = null;
	private AffineTransform transform = null;
	
	private State currentState;
	private boolean animating = false;
	private Timer animationTimer;
	private int framePeriod = 20; // 50 FPS
	private int resolution = 50; // 50 frames per solution step.
	private Integer frameNumber = null;
	private int maxFrameNumber;
	private double stepIndex = 0;
	
	private class VisualisationListener implements ComponentListener {
		@Override
		public void componentResized(ComponentEvent e) {
			calculateTransform();
		}
		@Override
		public void componentHidden(ComponentEvent e) {}
		@Override
		public void componentMoved(ComponentEvent e) {}
		@Override
		public void componentShown(ComponentEvent e) {}
	}
	
	public VisualisationPanel(Visualiser visualiser) {
		super();
		this.visualiser = visualiser;
		this.addComponentListener(new VisualisationListener());
	}
	
	public void setFramerate(int framerate) {
		this.framePeriod = 1000 / framerate;
		if (animationTimer != null) {
			animationTimer.setDelay(framePeriod);
		}
	}
	
	public void setResolution(int resolution) {
		int oldResolution = this.resolution;
		this.resolution = resolution;
		maxFrameNumber = resolution * (problemSetup.getPath().size() - 1);
		int newFrameNumber = (int)Math.round((double)frameNumber * resolution / oldResolution);
		if (newFrameNumber > maxFrameNumber) {
			newFrameNumber = maxFrameNumber;
		}
		if (resolution > oldResolution) {
			visualiser.updateManualTicks();
			gotoFrame(newFrameNumber);
		} else {
			gotoFrame(newFrameNumber);
			visualiser.updateManualTicks();
		}
	}

	public void initAnimation() {
		if (!problemSetup.solutionLoaded()) {
			return;
		}
		if (animationTimer != null) {
			animationTimer.stop();
		}
		animating = true;
		gotoFrame(0);
		maxFrameNumber = resolution * (problemSetup.getPath().size() - 1);
		animationTimer = new Timer(framePeriod, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int newFrameNumber = frameNumber+1;
				if (newFrameNumber >= maxFrameNumber) {
					animationTimer.stop();
					visualiser.setPlaying(false);
				}
				if (newFrameNumber <= maxFrameNumber) {
					gotoFrame(newFrameNumber);
				}
			}
		});
		visualiser.setPlaying(false);
		visualiser.updateManualTicks();
	}
	
	public void gotoFrame(int frameNumber) {
		if (!animating || (this.frameNumber != null && this.frameNumber == frameNumber)) {
			return;
		}
		this.frameNumber = frameNumber;
		visualiser.setFrameNumber(frameNumber);
		stepIndex = ((double)frameNumber) / resolution;
		int flooredIndex = (int)Math.floor(stepIndex);
		if (flooredIndex == stepIndex) {
			currentState = problemSetup.getPath().get(flooredIndex);
		} else {
			State s0 = problemSetup.getPath().get(flooredIndex);
			State s1 = problemSetup.getPath().get(flooredIndex + 1);
			double t = stepIndex - flooredIndex;
			currentState = State.interpolate(s0, s1, t); 
		}
		repaint();
	}
	
	public int getFrameNumber() {
		return frameNumber;
	}
	
	public void playPauseAnimation() {
		if (animationTimer.isRunning()) {
			animationTimer.stop();
			visualiser.setPlaying(false);
		} else {
			if (stepIndex >= problemSetup.getPath().size() - 1) {
				gotoFrame(0);
			}
			animationTimer.start();
			visualiser.setPlaying(true);
		}
	}
	
	public void stopAnimation() {
		if (animationTimer != null) {
			animationTimer.stop();
		}
		animating = false;
		visualiser.setPlaying(false);
		frameNumber = null;
	}
	
	
	public ProblemSetup getProblemSetup() {
		return problemSetup;
	}
	
	public void updateProblemSetup() {
		if (!problemSetup.problemLoaded()) {
			repaint();
			return;
		}
		
		Rectangle2D bounds = problemSetup.getBounds();
		double probWidth = bounds.getWidth();
		double probHeight = bounds.getHeight();
		minWorldX = bounds.getX() - PAD_FACTOR * probWidth;
		maxWorldY = bounds.getY() +  (1 + PAD_FACTOR) * probHeight;
		worldWidth = probWidth * (1 + 2*PAD_FACTOR);
		worldHeight = probHeight * (1 + 2*PAD_FACTOR);
		translation = AffineTransform.getTranslateInstance(
				-minWorldX,
				-maxWorldY);
		calculateTransform();
		repaint();
	}
	
	public void calculateTransform() {
		if (!problemSetup.problemLoaded()) {
			return;
		}
		transform = AffineTransform.getScaleInstance(
				getWidth() / worldWidth,
				- getHeight() / worldHeight);
		transform.concatenate(translation);
	}
	
	public void paintState(Graphics g, State s) {
		if (s == null) {
			return;
		}
		
		Graphics2D g2 = (Graphics2D)g;
		Path2D.Float path = new Path2D.Float();
		
		List<Point2D> points = s.getASVPositions();
		Point2D p = points.get(0);
		path.moveTo(p.getX(), p.getY());
		for (int i = 1; i < points.size(); i++) {
			p = points.get(i);
			path.lineTo(p.getX(), p.getY());
		}
		path.transform(transform);
		g2.draw(path);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (!problemSetup.problemLoaded()) {
			return;
		}
		Graphics2D g2 = (Graphics2D)g;
		List<Obstacle> obstacles = problemSetup.getObstacles();
		if (obstacles != null) {
			g2.setColor(Color.red);
			g2.setStroke(new BasicStroke(2));
			for (Obstacle obs : problemSetup.getObstacles()) {
				Shape transformed = transform.createTransformedShape(obs.getRect());
				g2.fill(transformed);
			}
		}
		
		if (!animating) {
			g2.setColor(Color.blue);
			g2.setStroke(new BasicStroke(5));
			paintState(g, problemSetup.getInitialState());
			
			g2.setColor(Color.green);
			g2.setStroke(new BasicStroke(5));
			paintState(g, problemSetup.getGoalState());
		} else {
			g2.setColor(Color.blue);
			g2.setStroke(new BasicStroke(5));
			paintState(g, currentState);
		}
	}
}