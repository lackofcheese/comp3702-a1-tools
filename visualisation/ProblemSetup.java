package visualisation;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProblemSetup {
	private boolean problemLoaded = false;
	private boolean solutionLoaded = false;
	
	private int asvCount;
	private State initialState;
	private State goalState;
	private List<Obstacle> obstacles;
	
	private List<State> path;
	
	private boolean isEmpty;
	private double minX;
	private double maxX;
	private double minY;
	private double maxY;
	
	public void expandToInclude(double x, double y) {
		if (isEmpty) {
			minX = x;
			maxX = x;
			minY = y;
			maxY = y;
			isEmpty = false;
		} else {
			if (x < minX) {
				minX = x;
			} else if (x > maxX) {
				maxX = x;
			}
			if (y < minY) {
				minY = y;
			} else if (y > maxY) {
				maxY = y;
			}
		}
	}
	
	public void expandToInclude(State s) {
		for (Point2D p : s.getASVPositions()) {
			expandToInclude(p.getX(), p.getY());
		}
	}
	
	public void loadProblem(String filename) throws IOException {
		problemLoaded = false;
		solutionLoaded = false;
		isEmpty = true;
		BufferedReader input = new BufferedReader(new FileReader(filename));
		try {
			asvCount = Integer.valueOf(input.readLine().trim());
			initialState = new State(asvCount, input.readLine().trim());
			goalState = new State(asvCount, input.readLine().trim());
			
			int numObstacles = Integer.valueOf(input.readLine().trim());
			obstacles = new ArrayList<Obstacle>();
			for (int i = 0; i < numObstacles; i++) {
				obstacles.add(new Obstacle(input.readLine().trim()));
			}
			input.close();
			
			expandToInclude(initialState);
			expandToInclude(goalState);
			for (Obstacle obs : obstacles) {
				Rectangle2D rect = obs.getRect();
				expandToInclude(rect.getX(), rect.getY());
				expandToInclude(rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight());
			}
			problemLoaded = true;
		} catch (NumberFormatException e) {
			throw new IOException("Invalid number format.");
		} catch (IndexOutOfBoundsException e) {
			throw new IOException("Invalid format; not enough tokens in a line.");
		}
	}
	
	public void loadSolution(String filename) throws IOException {
		if (!problemLoaded) {
			return;
		}
		solutionLoaded = false;
		BufferedReader input = new BufferedReader(new FileReader(filename));
		try {
			String[] tokens = input.readLine().trim().split("\\s+");
			int pathLength = Integer.valueOf(tokens[0]);
			path = new ArrayList<State>();
			for (int i = 0; i < pathLength; i++) {
				State s = new State(asvCount, input.readLine().trim());
				expandToInclude(s);
				path.add(s);
			}
			input.close();
			solutionLoaded = true;
		} catch (NumberFormatException e) {
			throw new IOException("Invalid number format.");
		} catch (IndexOutOfBoundsException e) {
			throw new IOException("Invalid format; not enough tokens in a line.");
		}
	}
	
	public void assumeDirectSolution() {
		if (!problemLoaded) {
			return;
		}
		path = new ArrayList<State>();
		path.add(initialState);
		path.add(goalState);
		solutionLoaded = true;
	}
	
	public int getASVCount() {
		return asvCount;
	}
	
	public State getInitialState() {
		return initialState;
	}
	
	public State getGoalState() {
		return goalState;
	}
	
	public List<State> getPath() {
		return new ArrayList<State>(path);
	}
	
	public List<Obstacle> getObstacles() {
		return new ArrayList<Obstacle>(obstacles);
	}
	
	public boolean problemLoaded() {
		return problemLoaded;
	}
	
	public boolean solutionLoaded() {
		return solutionLoaded;
	}
	
	public Rectangle2D getBounds() {
		return new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY);
	}
}
