package tester;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import problem.ASVConfig;
import problem.Obstacle;
import problem.ProblemSpec;

public class Tester {
	/** The maximum distance any ASV can travel between two states */
	public static final double MAX_STEP = 0.001;
	/** The minimum allowable boom length */
	public static final double MIN_BOOM_LENGTH = 0.05;
	/** The maximum allowable boom length */
	public static final double MAX_BOOM_LENGTH = 0.075;
	/** The workspace bounds */
	public static final Rectangle2D BOUNDS = new Rectangle2D.Double(0, 0, 1, 1);
	/** The default value for maximum error */
	public static final double DEFAULT_MAX_ERROR = 1e-5;

	/**
	 * Returns the minimum area required for the given number of ASVs.
	 * 
	 * @param asvCount
	 *            the number of ASVs
	 * @return the minimum area required.
	 */
	public static final double getMinimumArea(int asvCount) {
		double radius = 0.007 * (asvCount - 1);
		return Math.PI * radius * radius;
	}

	/**
	 * Creates a new Rectangle2D that is grown by delta in each direction
	 * compared to the given Rectangle2D.
	 * 
	 * @param rect
	 *            the Rectangle2D to expand.
	 * @param delta
	 *            the amount to expand by.
	 * @return a Rectangle2D expanded by delta in each direction.
	 */
	public static Rectangle2D grow(Rectangle2D rect, double delta) {
		return new Rectangle2D.Double(rect.getX() - delta, rect.getY() - delta,
				rect.getWidth() + delta * 2, rect.getHeight() + delta * 2);
	}

	/** Remembers the specifications of the problem. */
	private ProblemSpec ps = new ProblemSpec();
	/** The maximum error allowed by this Tester */
	private double maxError;
	/** The workspace bounds, with allowable error. */
	private Rectangle2D lenientBounds;

	/**
	 * Constructor. Creates a Tester with the default value for maximum error.
	 */
	public Tester() {
		this(DEFAULT_MAX_ERROR);
	}

	/**
	 * Constructor. Creates a Tester with the given maximum error.
	 * 
	 * @param maxError
	 *            the maximum allowable error.
	 */
	public Tester(double maxError) {
		this.maxError = maxError;
		lenientBounds = grow(BOUNDS, maxError);
	}

	/**
	 * Checks that the first configuration in the solution path is the initial
	 * configuration.
	 */
	public boolean testInitialFirst(int testNo, boolean verbose) {
		System.out.println(String.format("Test #%d: Initial state", testNo));
		if (!hasInitialFirst()) {
			System.out.println("FAILED: "
					+ "Solution path must start at initial state.");
			return false;
		} else {
			System.out.println("Passed.");
			return true;
		}
	}

	/**
	 * Returns whether the first cfg is the initial cfg.
	 * 
	 * @return whether the first cfg is the initial cfg.
	 */
	public boolean hasInitialFirst() {
		return ps.getPath().get(0).maxDistance(ps.getInitialState()) <= maxError;
	}

	/**
	 * Checks that the last configuration in the solution path is the goal
	 * configuration.
	 */
	public boolean testGoalLast(int testNo, boolean verbose) {
		System.out.println(String.format("Test #%d: Goal state", testNo));
		if (!hasGoalLast()) {
			System.out.println("FAILED: Solution path must end at goal state.");
			return false;
		} else {
			System.out.println("Passed.");
			return true;
		}
	}

	/**
	 * Returns whether the last cfg is the goal cfg.
	 * 
	 * @return whether the last cfg is the goal cfg.
	 */
	public boolean hasGoalLast() {
		List<ASVConfig> path = ps.getPath();
		return path.get(path.size() - 1).maxDistance(ps.getGoalState()) <= maxError;
	}

	/**
	 * Returns a copy of list where each value is incremented by delta.
	 * 
	 * @param list
	 *            the list of integers to add to.
	 * @return a copy of list where each value is incremented by delta.
	 */
	public List<Integer> addToAll(List<Integer> list, int delta) {
		List<Integer> result = new ArrayList<Integer>();
		for (int i : list) {
			result.add(i + delta);
		}
		return result;
	}

	/**
	 * Checks that the steps in between configurations do not exceed the maximum
	 * primitive step distance.
	 */
	public boolean testValidSteps(int testNo, boolean verbose) {
		System.out.println(String.format("Test #%d: Step sizes", testNo));
		List<Integer> badSteps = getInvalidSteps();
		if (!badSteps.isEmpty()) {
			System.out.println(String.format(
					"FAILED: Distance exceeds 0.001 for %d of %d step(s).",
					badSteps.size(), ps.getPath().size() - 1));
			if (verbose) {
				System.out.println("Starting line for each invalid step:");
				System.out.println(addToAll(badSteps, 2));
			}
			return false;
		} else {
			System.out.println("Passed.");
			return true;
		}
	}

	/**
	 * Returns the preceding path indices of any invalid steps.
	 * 
	 * @return the preceding path indices of any invalid steps.
	 */
	private List<Integer> getInvalidSteps() {
		List<Integer> badSteps = new ArrayList<Integer>();
		List<ASVConfig> path = ps.getPath();
		ASVConfig state = path.get(0);
		for (int i = 1; i < path.size(); i++) {
			ASVConfig nextState = path.get(i);
			if (!isValidStep(state, nextState)) {
				badSteps.add(i - 1);
			}
			state = nextState;
		}
		return badSteps;
	}

	/**
	 * Returns whether the step from s0 to s1 is a valid primitive step.
	 * 
	 * @param s0
	 *            A configuration.
	 * @param s1
	 *            Another configuration.
	 * @return whether the step from s0 to s1 is a valid primitive step.
	 */
	private boolean isValidStep(ASVConfig s0, ASVConfig s1) {
		return (s0.maxDistance(s1) <= MAX_STEP + maxError);
	}

	/**
	 * Checks that the booms in each configuration have lengths in the allowable
	 * range.
	 */
	public boolean testBoomLengths(int testNo, boolean verbose) {
		System.out.println(String.format("Test #%d: Boom lengths", testNo));
		List<Integer> badStates = getInvalidBoomStates();
		if (!badStates.isEmpty()) {
			System.out.println(String.format(
					"FAILED: Invalid boom length for %d of %d state(s).",
					badStates.size(), ps.getPath().size()));
			if (verbose) {
				if (verbose) {
					System.out.println("Line for each invalid cfg:");
					System.out.println(addToAll(badStates, 2));
				}
			}
			return false;
		} else {
			System.out.println("Passed.");
			return true;
		}
	}

	/**
	 * Returns the path indices of any states with invalid booms.
	 * 
	 * @return the path indices of any states with invalid booms.
	 */
	public List<Integer> getInvalidBoomStates() {
		List<Integer> badStates = new ArrayList<Integer>();
		List<ASVConfig> path = ps.getPath();
		for (int i = 0; i < path.size(); i++) {
			if (!hasValidBoomLengths(path.get(i))) {
				badStates.add(i);
			}
		}
		return badStates;
	}

	/**
	 * Returns whether the booms in the given configuration have valid lengths.
	 * 
	 * @param s
	 *            the configuration to test.
	 * @return whether the booms in the given configuration have valid lengths.
	 */
	private boolean hasValidBoomLengths(ASVConfig s) {
		List<Point2D> points = s.getASVPositions();
		for (int i = 1; i < points.size(); i++) {
			Point2D p0 = points.get(i - 1);
			Point2D p1 = points.get(i);
			double boomLength = p0.distance(p1);
			if (boomLength < MIN_BOOM_LENGTH - maxError) {
				return false;
			} else if (boomLength > MAX_BOOM_LENGTH + maxError) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks that each configuration in the path is convex (and hence also
	 * non-self-intersecting).
	 */
	public boolean testConvexity(int testNo, boolean verbose) {
		System.out.println(String.format("Test #%d: Convexity", testNo));
		List<Integer> badStates = getNonConvexStates();
		if (!badStates.isEmpty()) {
			System.out.println(String.format(
					"FAILED: %d of %d state(s) are not convex.",
					badStates.size(), ps.getPath().size()));
			if (verbose) {
				System.out.println("Line for each invalid cfg:");
				System.out.println(addToAll(badStates, 2));
			}
			return false;
		} else {
			System.out.println("Passed.");
			return true;
		}
	}

	/**
	 * Returns the path indices of any non-convex states.
	 * 
	 * @return the path indices of any non-convex states.
	 */
	public List<Integer> getNonConvexStates() {
		List<Integer> badStates = new ArrayList<Integer>();
		List<ASVConfig> path = ps.getPath();
		for (int i = 0; i < path.size(); i++) {
			if (!isConvex(path.get(i))) {
				badStates.add(i);
			}
		}
		return badStates;
	}

	/**
	 * Returns whether the given configuration is convex.
	 * 
	 * @param s
	 *            the configuration to test.
	 * @return whether the given configuration is convex.
	 */
	private boolean isConvex(ASVConfig s) {
		List<Point2D> points = s.getASVPositions();
		points.add(points.get(0));
		points.add(points.get(1));

		double required_sign = 0;
		for (int i = 2; i < points.size(); i++) {
			Point2D p0 = points.get(i - 2);
			Point2D p1 = points.get(i - 1);
			Point2D p2 = points.get(i);
			double dx0 = p1.getX() - p0.getX();
			double dy0 = p1.getY() - p0.getY();
			double dx1 = p2.getX() - p1.getX();
			double dy1 = p2.getY() - p1.getY();
			double zcp = dx0 * dy1 - dy0 * dx1;
			if (zcp == 0 && dx0*dx1 + dy0*dy1 < 0) {
				return false;
			}
			
			double sgn;
			if (zcp < -maxError) {
				sgn = -1;
			} else if (zcp > maxError) {
				sgn = 1;
			} else {
				sgn = 0;
			}
			
			if (sgn * required_sign < 0) {
				return false;
			} else if (sgn != 0) {
				required_sign = sgn;
			}
		}
		return true;
	}

	/**
	 * Checks whether each configuration has sufficient internal area.
	 */
	public boolean testAreas(int testNo, boolean verbose) {
		System.out.println(String.format("Test #%d: Areas", testNo));
		List<Integer> badStates = getInvalidAreaStates();
		if (!badStates.isEmpty()) {
			System.out.println(String.format(
					"FAILED: %d of %d state(s) have insufficient area.",
					badStates.size(), ps.getPath().size()));
			if (verbose) {
				System.out.println("Line for each invalid cfg:");
				System.out.println(addToAll(badStates, 2));
			}
			return false;
		} else {
			System.out.println("Passed.");
			return true;
		}
	}

	/**
	 * Returns the path indices of any states with insufficient area.
	 * 
	 * @return the path indices of any states with insufficient area.
	 */
	public List<Integer> getInvalidAreaStates() {
		List<ASVConfig> path = ps.getPath();
		List<Integer> badStates = new ArrayList<Integer>();
		for (int i = 0; i < path.size(); i++) {
			if (!hasEnoughArea(path.get(i))) {
				badStates.add(i);
			}
		}
		return badStates;
	}

	/**
	 * Returns whether the given configuration has sufficient area.
	 * 
	 * @param s
	 *            the configuration to test.
	 * @return whether the given configuration has sufficient area.
	 */
	private boolean hasEnoughArea(ASVConfig s) {
		double total = 0;
		List<Point2D> points = s.getASVPositions();
		points.add(points.get(0));
		points.add(points.get(1));
		for (int i = 1; i < points.size() - 1; i++) {
			total += points.get(i).getX()
					* (points.get(i + 1).getY() - points.get(i - 1).getY());
		}
		double area = Math.abs(total) / 2;
		return (area >= getMinimumArea(s.getASVCount()) - maxError);
	}

	/**
	 * Checks that each configuration fits within the workspace bounds.
	 */
	public boolean testBounds(int testNo, boolean verbose) {
		System.out.println(String.format("Test #%d: Bounds", testNo));
		List<Integer> badStates = getOutOfBoundsStates();
		if (!badStates.isEmpty()) {
			System.out.println(String.format("FAILED: %d of %d"
					+ " state(s) go out of the workspace bounds.",
					badStates.size(), ps.getPath().size()));
			if (verbose) {
				System.out.println("Line for each invalid cfg:");
				System.out.println(addToAll(badStates, 2));
			}
			return false;
		} else {
			System.out.println("Passed.");
			return true;
		}
	}

	/**
	 * Returns the path indices of any states that are out of bounds.
	 * 
	 * @return the path indices of any states that are out of bounds.
	 */
	public List<Integer> getOutOfBoundsStates() {
		List<ASVConfig> path = ps.getPath();
		List<Integer> badStates = new ArrayList<Integer>();
		for (int i = 0; i < path.size(); i++) {
			if (!(path.get(i)).fitsBounds(lenientBounds)) {
				badStates.add(i);
			}
		}
		return badStates;
	}

	/**
	 * Checks that each configuration does not collide with any of the
	 * obstacles.
	 */
	public boolean testCollisions(int testNo, boolean verbose) {
		System.out.println(String.format("Test #%d: Collisions", testNo));
		List<Integer> badStates = getCollidingStates();
		if (!badStates.isEmpty()) {
			System.out.println(String.format(
					"FAILED: %d of %d state(s) collide with obstacles.",
					badStates.size(), ps.getPath().size()));
			if (verbose) {
				System.out.println("Line for each invalid cfg:");
				System.out.println(addToAll(badStates, 2));
			}
			return false;
		} else {
			System.out.println("Passed.");
			return true;
		}
	}

	/**
	 * Returns the path indices of any states that collide with obstacles.
	 * 
	 * @return the path indices of any states that collide with obstacles.
	 */
	public List<Integer> getCollidingStates() {
		List<ASVConfig> path = ps.getPath();
		List<Integer> badStates = new ArrayList<Integer>();
		for (int i = 0; i < path.size(); i++) {
			for (Obstacle o : ps.getObstacles()) {
				if (hasCollision(path.get(i), o)) {
					badStates.add(i);
					break;
				}
			}
		}
		return badStates;
	}

	/**
	 * Returns whether the given config collides with the given obstacle.
	 * 
	 * @param s
	 *            the configuration to test.
	 * @param o
	 *            the obstacle to test against.
	 * @return whether the given config collides with the given obstacle.
	 */
	public boolean hasCollision(ASVConfig s, Obstacle o) {
		Rectangle2D lenientRect = grow(o.getRect(), -maxError);
		List<Point2D> points = s.getASVPositions();
		for (int i = 1; i < points.size(); i++) {
			if (new Line2D.Double(points.get(i - 1), points.get(i))
					.intersects(lenientRect)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks that the total cost of the solution is correctly calculated.
	 */
	public boolean testTotalCost(int testNo, boolean verbose) {
		System.out.println(String.format("Test #%d: Solution cost", testNo));
		double cost = ps.getSolutionCost();
		double actualCost = ps.calculateTotalCost();
		if (Math.abs(cost - actualCost) > maxError) {
			System.out.println(String.format(
					"FAILED: Incorrect solution cost; was %f but should be %f",
					cost, actualCost));
			return false;
		} else {
			System.out.println("Passed.");
			return true;
		}
	}

	/**
	 * Runs a specific test based on its name.
	 */
	public boolean testByName(String testName, int testNo, boolean verbose) {
		switch (testName.toLowerCase()) {
		case "initial":
			return testInitialFirst(testNo, verbose);
		case "goal":
			return testGoalLast(testNo, verbose);
		case "steps":
			return testValidSteps(testNo, verbose);
		case "booms":
			return testBoomLengths(testNo, verbose);
		case "convexity":
			return testConvexity(testNo, verbose);
		case "areas":
			return testAreas(testNo, verbose);
		case "bounds":
			return testBounds(testNo, verbose);
		case "collisions":
			return testCollisions(testNo, verbose);
		case "cost":
			return testTotalCost(testNo, verbose);
		default:
			return true;
		}
	}

	/**
	 * Runs all 9 test cases from the command line.
	 * 
	 * @param args
	 *            the command line arguments.
	 */
	public static void main(String[] args) {
		double maxError = DEFAULT_MAX_ERROR;
		boolean verbose = false;
		String problemPath = null;
		String solutionPath = null;
		for (int i = 0; i < args.length; i++) {
			String arg = args[i].trim();
			if (arg.equals("-e")) {
				i++;
				if (i < args.length) {
					maxError = Double.valueOf(args[i]);
				}
			} else if (arg.equals("-v")) {
				verbose = true;
			} else {
				if (problemPath == null) {
					problemPath = arg;
				} else {
					solutionPath = arg;
				}
			}
		}
		if (problemPath == null) {
			System.out.println("Usage: tester [-e maxError] [-v] "
					+ "problem-file [solution-file]");
			return;
		}
		System.out.println("Test #0: Loading files");
		Tester tester = new Tester(maxError);
		try {
			tester.ps.loadProblem(problemPath);
		} catch (IOException e1) {
			System.out.println("FAILED: Invalid problem file");
			return;
		}

		if (solutionPath != null) {
			try {
				tester.ps.loadSolution(solutionPath);
			} catch (IOException e1) {
				System.out.println("FAILED: Invalid solution file");
				return;
			}
		} else {
			tester.ps.assumeDirectSolution();
		}
		System.out.println("Passed.");

		List<String> testsToRun = new ArrayList<String>();
		if (solutionPath != null) {
			testsToRun.addAll(Arrays.asList(new String[] { "initial", "goal",
					"steps", "cost" }));
		}
		testsToRun.addAll(Arrays.asList(new String[] { "booms", "convexity",
				"areas", "bounds", "collisions" }));
		int testNo = 1;
		for (String name : testsToRun) {
			tester.testByName(name, testNo, verbose);
			testNo++;
		}
	}
}