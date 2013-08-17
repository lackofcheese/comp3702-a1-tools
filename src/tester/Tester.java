package tester;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import problem.Obstacle;
import problem.ProblemSpec;
import problem.ASVConfig;

public class Tester {
	/** The path for the problem file. If this is not null initially,
	 * that value will automatically be used */
	public String problemPath = null;
	/** The path for the solution file. If this is not null initially,
	 * that value will automatically be used */
	public String solutionPath = null;
	/** Remembers the specifications of the problem. */
	private ProblemSpec ps;
	
	/** The default problem file, if none is given above or
	 * in the environment variable PROBLEM_PATH */
	public static final String DEFAULT_PROBLEM_PATH = "problem.txt";
	/** The default solution file, if none is given above or
	 * in the environment variable SOLUTION_PATH */
	public static final String DEFAULT_SOLUTION_PATH = "solution.txt";
	
	/** The maximum distance any ASV can travel between two states */
	public static final double MAX_STEP = 0.001;
	/** The minimum allowable boom length */
	public static final double MIN_BOOM_LENGTH = 0.05;
	/** The maximum allowable boom length */
	public static final double MAX_BOOM_LENGTH = 0.075;
	/** The workspace bounds */
	public static final Rectangle2D BOUNDS = new Rectangle2D.Double(0, 0, 1, 1);

	/**
	 * Returns the minimum area required for the given number of ASVs.
	 * @param asvCount the number of ASVs
	 * @return the minimum area required.
	 */
	public static final double getMinimumArea(int asvCount) {
		double radius = 0.007 * (asvCount - 1);
		return Math.PI * radius * radius;
	}
	
	public static Rectangle2D grow(Rectangle2D rect, double delta) {
		return new Rectangle2D.Double(
				rect.getX() - delta,
				rect.getY() - delta,
				rect.getWidth() + delta * 2,
				rect.getHeight() + delta * 2);
	}
	
	
	
	/** The maximum allowable error in any coordinate. */
	public static final double MAX_COORD_ERROR = 0.00001;
	/** The maximum allowable error in the area. */
	public static final double MAX_AREA_ERROR = 0.00001;
	/** The workspace bounds, with allowable error. */
	public static final Rectangle2D LENIENT_BOUNDS = grow(BOUNDS, MAX_COORD_ERROR);
	
	/**
	 * Loads the problem and solution files into the ProblemSpec.
	 */
	@Before
	public void setUp() {
		ps = new ProblemSpec();
		if (problemPath == null) {
			problemPath = System.getenv("PROBLEM_PATH");
			if (problemPath != null) {
				problemPath = problemPath.replaceAll("\"", "");
			}
		}
		if (problemPath == null) {
			problemPath = DEFAULT_PROBLEM_PATH;
		}
		try {
			ps.loadProblem(problemPath);
		} catch (IOException e) {
			Assert.fail("Error loading " + problemPath + ": " + e.getMessage());
			return;
		}
		
		if (solutionPath == null) {
			solutionPath = System.getenv("SOLUTION_PATH");
			if (solutionPath != null) {
				solutionPath = solutionPath.replaceAll("\"", "");
			}
		}
		if (solutionPath == null) {
			solutionPath = DEFAULT_SOLUTION_PATH;
		}
		try {
			ps.loadSolution(solutionPath);
		} catch (IOException e) {
			Assert.fail("Error loading " + solutionPath + ": " + e.getMessage());
			return;
		}
	}
	
	/**
	 * Checks that the first configuration in the solution path is the initial configuration.
	 */
	@Test
	public void testInitialFirst() {
		List<ASVConfig> path = ps.getPath();	
		Assert.assertTrue("Solution path must start at initial state.",
				path.get(0).getMaxDistance(ps.getInitialState()) <= MAX_COORD_ERROR);
	}
	
	/**
	 * Checks that the last configuration in the solution path is the goal configuration.
	 */
	@Test
	public void testGoalLast() {
		List<ASVConfig> path = ps.getPath();
		Assert.assertTrue("Solution path must end at goal state.",
				path.get(path.size()-1).getMaxDistance(ps.getGoalState()) <= MAX_COORD_ERROR);
	}
	
	/**
	 * Checks that the steps in between configurations do not exceed the maximum primitive step distance.
	 */
	@Test
	public void testValidSteps() {
		Map<List<Integer>, List<ASVConfig>> badSteps = new TreeMap<List<Integer>, List<ASVConfig>>();
		List<ASVConfig> path = ps.getPath();
		ASVConfig state = path.get(0);
		for (int i = 1; i < path.size(); i++) {
			ASVConfig nextState = path.get(i);
			if (!isValidStep(state, nextState)) {
				badSteps.put(Arrays.asList(i-1, i), Arrays.asList(state, nextState));
			}
			state = nextState;
		}
		String message = String.format("Distance exceeds 0.001 for %d pair(s) of adjacent states.", badSteps.size());
		Assert.assertEquals(message, new TreeMap<List<Integer>, List<ASVConfig>>(), badSteps);
	}
	
	/**
	 * Returns whether the step from s0 to s1 is a valid primitive step.
	 * @param s0 A configuration.
	 * @param s1 Another configuration.
	 * @return whether the step from s0 to s1 is a valid primitive step.
	 */
	private boolean isValidStep(ASVConfig s0, ASVConfig s1) {
		return (s0.getMaxDistance(s1) <= MAX_STEP + MAX_COORD_ERROR);
	}
	
	/**
	 * Checks that the booms in each configuration have lengths in the allowable range.
	 */
	@Test
	public void testBoomLengths() {
		Map<Integer, ASVConfig> badStates = new TreeMap<Integer, ASVConfig>();
		List<ASVConfig> path = ps.getPath();
		for (int i = 0; i < path.size(); i++) {
			if (!hasValidBoomLengths(path.get(i))) {
				badStates.put(i, path.get(i));
			}
		}
		String message = String.format("Invalid boom length for %d state(s).", badStates.size());
		Assert.assertEquals(message, new TreeMap<Integer, ASVConfig>(), badStates);
	}
	
	/**
	 * Returns whether the booms in the given configuration have valid lengths.
	 * @param s the configuration to test.
	 * @return whether the booms in the given configuration have valid lengths.
	 */
	private boolean hasValidBoomLengths(ASVConfig s) {
		List<Point2D> points = s.getASVPositions();
		for (int i = 1; i < points.size(); i++) {
			Point2D p0 = points.get(i-1);
			Point2D p1 = points.get(i);
			double boomLength = p0.distance(p1);
			if (boomLength < MIN_BOOM_LENGTH - MAX_COORD_ERROR) {
				return false;
			} else if (boomLength > MAX_BOOM_LENGTH + MAX_COORD_ERROR) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks that each configuration in the path is convex (and hence also non-self-intersecting).
	 */
	@Test
	public void testConvexity() {
		Map<Integer, ASVConfig> badStates = new TreeMap<Integer, ASVConfig>();
		List<ASVConfig> path = ps.getPath();
		for (int i = 0; i < path.size(); i++) {
			if (!isConvex(path.get(i))) {
				badStates.put(i, path.get(i));
			}
		}
		String message = String.format("%d state(s) not convex.", badStates.size());
		Assert.assertEquals(message, new TreeMap<Integer, ASVConfig>(), badStates);
	}
	
	/**
	 * Returns whether the given configuration is convex.
	 * @param s the configuration to test.
	 * @return whether the given configuration is convex.
	 */
	private boolean isConvex(ASVConfig s) {
		List<Point2D> points = s.getASVPositions();
		points.add(points.get(0));
		points.add(points.get(1));
		
		double sgn = 0;
		for (int i = 2; i < points.size(); i++) {
			Point2D p0 = points.get(i-2);
			Point2D p1 = points.get(i-1);
			Point2D p2 = points.get(i);
			double dx0 = p1.getX() - p0.getX();
			double dy0 = p1.getY() - p0.getY();
			double dx1 = p2.getX() - p1.getX();
			double dy1 = p2.getY() - p1.getY();
			double zcp = dx0 * dy1 - dy0 * dx1;
			if (zcp * sgn < 0) {
				return false;
			} else if (zcp > 0) {
				sgn = 1;
			} else if (zcp < 0) {
				sgn = -1;
			} else if (dx0 * dx1 + dy0 * dy1 < 0) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks whether each configuration has sufficient internal area.
	 */
	@Test
	public void testAreas() {
		Map<Integer, ASVConfig> badStates = new TreeMap<Integer, ASVConfig>();
		List<ASVConfig> path = ps.getPath();
		for (int i = 0; i < path.size(); i++) {
			if (!hasEnoughArea(path.get(i))) {
				badStates.put(i, path.get(i));
			}
		}
		String message = String.format("%d state(s) with insufficient area.", badStates.size());
		Assert.assertEquals(message, new TreeMap<Integer, ASVConfig>(), badStates);
	}
	
	/**
	 * Returns whether the given configuration has sufficient area.
	 * @param s the configuration to test.
	 * @return whether the given configuration has sufficient area.
	 */
	private boolean hasEnoughArea(ASVConfig s) {
		double total = 0;
		List<Point2D> points = s.getASVPositions();
		points.add(points.get(0));
		points.add(points.get(1));
		for (int i = 1; i < points.size() - 1; i++) {
			total += points.get(i).getX() * (points.get(i+1).getY() - points.get(i-1).getY());
		}
		double area = Math.abs(total) / 2;
		return (area >= getMinimumArea(s.getASVCount()) - MAX_AREA_ERROR);
	}
	
	/**
	 * Checks that each configuration fits within the workspace bounds.
	 */
	@Test
	public void testBounds() {
		Map<Integer, ASVConfig> badStates = new TreeMap<Integer, ASVConfig>();
		List<ASVConfig> path = ps.getPath();
		for (int i = 0; i < path.size(); i++) {
			if (!(path.get(i)).fitsBounds(LENIENT_BOUNDS)) {
				badStates.put(i, path.get(i));
			}
		}
		String message = String.format("%d state(s) go out of the workspace bounds.", badStates.size());
		Assert.assertEquals(message, new TreeMap<Integer, ASVConfig>(), badStates);
	}
	
	/**
	 * Checks that each configuration does not collide with any of the obstacles.
	 */
	@Test
	public void testCollisions() {
		Map<Integer, ASVConfig> badStates = new TreeMap<Integer, ASVConfig>();
		List<ASVConfig> path = ps.getPath();
		for (int i = 0; i < path.size(); i++) {
			for (Obstacle o : ps.getObstacles()) {
				if (hasCollision(path.get(i), o)) {
					badStates.put(i, path.get(i));
					break;
				}
			}
		}
		String message = String.format("%d state(s) collide with obstacles.", badStates.size());
		Assert.assertEquals(message, new TreeMap<Integer, ASVConfig>(), badStates);
	}
	
	/**
	 * Returns whether the given configuration collides with the given obstacle.
	 * @param s the configuration to test.
	 * @param o the obstacle to test against.
	 * @return whether the given configuration collides with the given obstacle.
	 */
	public boolean hasCollision(ASVConfig s, Obstacle o) {
		Rectangle2D lenientRect = grow(o.getRect(), -MAX_COORD_ERROR);
		List<Point2D> points = s.getASVPositions();
		for (int i = 1; i < points.size(); i++) {
			if (new Line2D.Double(points.get(i-1), points.get(i)).intersects(lenientRect)) {
				return true;
			}
		}
		return false;
	}
}