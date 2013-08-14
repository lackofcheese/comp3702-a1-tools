package problem;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the specifications of a given problem and solution;
 * that is, it provides a structured representation of the contents of 
 * a problem text file and associated solution text file, as described
 * in the assignment specifications.
 * 
 * This class doesn't do any validity checking - see the code in tester.Tester for this.
 * @author lackofcheese
 */
public class ProblemSpec {
	/** True iff a problem is currently loaded */
	private boolean problemLoaded = false;
	/** True iff a solution is currently loaded */
	private boolean solutionLoaded = false;
	
	/** The number of ASVs in each configuration */
	private int asvCount;
	/** The initial configuration */
	private ASVConfig initialState;
	/** The goal configuration */
	private ASVConfig goalState;
	/** The obstacles */
	private List<Obstacle> obstacles;
	
	/** The path taken in the solution */
	private List<ASVConfig> path;
	/** The cost of the solution */
	private double solutionCost;
	
	/**
	 * Loads a problem from a problem text file.
	 * @param filename the text file to load.
	 * @throws IOException if the text file doesn't exist or doesn't meet
	 * the assignment specifications.
	 */
	public void loadProblem(String filename) throws IOException {
		problemLoaded = false;
		solutionLoaded = false;
		BufferedReader input = new BufferedReader(new FileReader(filename));
		String line;
		int lineNo = 0;
		try {
			line = input.readLine().trim();
			lineNo++;
			asvCount = Integer.valueOf(line);
			line = input.readLine().trim();
			lineNo++;
			initialState = new ASVConfig(asvCount, line);
			line = input.readLine().trim();
			lineNo++;
			goalState = new ASVConfig(asvCount, line);
			line = input.readLine().trim();
			lineNo++;
			int numObstacles = Integer.valueOf(line);
			obstacles = new ArrayList<Obstacle>();
			for (int i = 0; i < numObstacles; i++) {
				line = input.readLine().trim();
				lineNo++;
				obstacles.add(new Obstacle(line));
			}
			input.close();
			problemLoaded = true;
		} catch (NumberFormatException e) {
			throw new IOException(String.format("Invalid number format on line %d. %s", lineNo, e.getMessage()));
		} catch (IndexOutOfBoundsException e) {
			throw new IOException(String.format("Not enough tokens on line %d - %d required", lineNo, asvCount*2));
		} catch (NullPointerException e) {
			throw new IOException(String.format("Line %d expected, but file ended.", lineNo));
		}
	}
	
	/**
	 * Loads a solution from a solution text file.
	 * @param filename the text file to load.
	 * @throws IOException if the text file doesn't exist or doesn't meet
	 * the assignment specifications.
	 */
	public void loadSolution(String filename) throws IOException {
		if (!problemLoaded) {
			return;
		}
		solutionLoaded = false;
		BufferedReader input = new BufferedReader(new FileReader(filename));
		String line;
		int lineNo = 0;
		try {
			line = input.readLine().trim();
			lineNo++;
			String[] tokens = line.split("\\s+");
			int pathLength = Integer.valueOf(tokens[0]);
			solutionCost = Double.valueOf(tokens[1]);
			path = new ArrayList<ASVConfig>();
			for (int i = 0; i < pathLength; i++) {
				line = input.readLine().trim();
				lineNo++;
				ASVConfig s = new ASVConfig(asvCount, line);
				path.add(s);
			}
			input.close();
			solutionLoaded = true;
		} catch (NumberFormatException e) {
			throw new IOException(String.format("Invalid number format on line %d. %s", lineNo, e.getMessage()));
		} catch (IndexOutOfBoundsException e) {
			throw new IOException(String.format("Not enough tokens on line %d - %d required", lineNo, asvCount*2));
		} catch (NullPointerException e) {
			throw new IOException(String.format("Line %d expected, but file ended.", lineNo));
		}
	}
	
	/**
	 * Returns the number of ASVs in each configuration.
	 * @return the number of ASVs in each configuration.
	 */
	public int getASVCount() {
		return asvCount;
	}
	
	/**
	 * Returns the initial configuration.
	 * @return the initial configuration.
	 */
	public ASVConfig getInitialState() {
		return initialState;
	}
	
	/**
	 * Returns the goal configuration.
	 * @return the goal configuration.
	 */
	public ASVConfig getGoalState() {
		return goalState;
	}
	
	/**
	 * Returns the solution path.
	 * @return the solution path.
	 */
	public List<ASVConfig> getPath() {
		return new ArrayList<ASVConfig>(path);
	}
	
	/**
	 * Returns the list of obstacles.
	 * @return the list of obstacles.
	 */
	public List<Obstacle> getObstacles() {
		return new ArrayList<Obstacle>(obstacles);
	}
	
	/**
	 * Returns the cost of the solution.
	 * @return the cost of the solution.
	 */
	public double getSolutionCost() {
		return solutionCost;
	}
	
	/**
	 * Returns whether a problem is currently loaded.
	 * @return whether a problem is currently loaded.
	 */
	public boolean problemLoaded() {
		return problemLoaded;
	}
	
	/**
	 * Returns whether a solution is currently loaded.
	 * @return whether a solution is currently loaded.
	 */
	public boolean solutionLoaded() {
		return solutionLoaded;
	}
}
