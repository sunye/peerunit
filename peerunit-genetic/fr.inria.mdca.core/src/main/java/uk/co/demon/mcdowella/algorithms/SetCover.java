package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.util.Collections;
import java.util.HashMap;
import java.io.InputStreamReader;
import java.util.List;
import uk.co.demon.mcdowella.stats.LU;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

/** This class includes code to do a backtracking search to
    solve the set coverage problem, albeit in worst case
    exorbitant amounts of cpu.
    */
public class SetCover implements InOutSet.PerMember
{
  /** Create a class ready to do a backtracking search. It
      receives an array of arrays of ints. Our search will be
      to find the smallest subset of arrays that covers all
      the ints mentioned in any array. The ints should be
      small numbers >= 0. Each subset has an associated cost
      and we search for the smallest cost solution, ignoring
      anything of cost >= forTooCostly.
      */
  public SetCover(int[][] coverings, double[] costs, 
    double forTooCostly, SolutionReceiver answerCatcher)
  {
    if (costs.length != coverings.length)
    {
      throw new IllegalArgumentException("Cost array does not match coverings array");
    }
    callWithSolution = answerCatcher;
    tooCostly = forTooCostly;
    // set up possibilities
    possa = new Possibility[coverings.length];
    answerSoFar = new int[coverings.length];
    score = new MaxInRange(coverings.length);
    int[] forActivePoss = new int[coverings.length];
    removed = new int[coverings.length];
    for (int i = 0; i < forActivePoss.length; i++)
    {
      forActivePoss[i] = i;
    }
    scoreChanged = new InOutSet(forActivePoss);
    activePoss = new InOutSet(forActivePoss);
    int maxPoint = 0;
    for (int i = 0; i < coverings.length; i++)
    {
      int[] points = coverings[i];
      possa[i] = new Possibility(points, i, costs[i]);
      score.set(i, possa[i].getScore());
      scoreChanged.takeOut(i);
      for (int x: points)
      {
	if (x > maxPoint)
	{
	  maxPoint = x;
	}
      }
    }
    // Set up points. First want arrays saying which possibilities
    // carry that given point
    List<ArrayList<Integer>> pointIn = new ArrayList<ArrayList<Integer>>(
      maxPoint + 1);
    for (int i = 0; i <= maxPoint; i++)
    {
      pointIn.add(null);
    }
    for (int i = 0; i < coverings.length; i++)
    {
      final Integer iInt = new Integer(i);
      int[] points = coverings[i];
      for (int j = 0; j < points.length; j++)
      {
	int x = points[j];
	ArrayList<Integer> pl = pointIn.get(x);
        if (pl == null)
	{
	  pl = new ArrayList<Integer>();
	  pointIn.set(x, pl);
	}
	pl.add(iInt);
      }
    }
    pointa = new Point[maxPoint + 1];
    int countp = 0;
    for (int i = 0; i <= maxPoint; i++)
    {
      ArrayList<Integer> poss = pointIn.get(i);
      if (poss == null)
      {  // nobody uses this point
        continue;
      }
      countp++;
      int[] pi = new int[poss.size()];
      for (int j = 0; j < pi.length; j++)
      {
        pi[j] = poss.get(j).intValue();
      }
      Point pp = new Point(pi, i);
      pointa[i] = pp;
    }
    numPoints = countp;
  }
  /** array of Possibilities */
  private final Possibility[] possa;
  /** active Possibilities */
  private final InOutSet activePoss;
  /** possibilities whose score we should update */
  private final InOutSet scoreChanged;
  /** possibilities removed during backtrack */
  private final int[] removed;
  /** number of possibilities removed */
  int numRemoved;
  /** score array holds number of points each possibility could cover */
  private final MaxInRange score;
  /** array of points */
  private final Point[] pointa;
  /** Don't consider anything that costs this or more */
  private double tooCostly;
  /** Can change tooCostly e.g. in callback when answer found
      to decrease it before default of value of answer to
      speed up search at cost of skipping slightly better
      answers */
  public void setTooCostly(double newCost)
  {
    tooCostly = newCost;
  }
  /** answer so far */
  private final int[] answerSoFar;
  /** number of points actually present */
  private final int numPoints;
  /** number of points currently covered */
  private int numCovered;
  /** score so far */
  private double costSoFar;
  /** for each possibility we keep track of the set of active
    points it could cover
  */
  private class Possibility implements InOutSet.PerMember
  {
    /** points we could cover, with the uncovered points set active */
    private final InOutSet cover;
    /** request callback from cover */
    public void callFromCover(InOutSet.PerMember pm)
    {
      cover.callMe(pm);
    }
    /** integer labelling this possibility */
    private final int myNum;
    /** cost of this possibility */
    private final double cost;
    /** get the cost */
    public double getCost()
    {
      return cost;
    }
    /** get a score used to choose the next possibility to try */
    public long getScore()
    {
      int numHere = cover.getNumIn();
      if (numHere <= 0)
      { // no good at all
        return Long.MIN_VALUE;
      }
      return MaxInRange.toOrderedLong(numHere / cost);
    }
    /** get a lower bound on the expected cost including the cost of this 
	possibility to cover the given number of points, assuming that we are 
	the highest scoring possibility */
    public double getEstimate(int toCover)
    {
      int numHere = cover.getNumIn();
      // We are the highest scoring answer, so nobody has a better value of
      // number per cost than us
      return cost + (toCover - numHere) * cost / numHere;
    }
    /** construct points we cover and our number. After construction
        everything is active */
    Possibility(int[] points, int num, double forCost)
    {
      cover = new InOutSet(points);
      myNum = num;
      cost = forCost;
    }
    int numCovered()
    {
      return cover.getNumIn();
    }
    /** called when possibility is selected */
    void selected()
    {
      // System.out.println("Selected possibility " + myNum);
      // set not active
      // System.out.println("Take out active point " + myNum);
      activePoss.takeOut(myNum);
      // Go over every active point
      cover.callMe(this);
    }
    /** Called when selected for every active point */
    public void callback(int point)
    {
      // For this point, want callback pointing it at every
      // active possibility containing it, so we can remove
      // it from the set of active points for those possibilities
      pointa[point].callMe(pointa[point]);
    }
    /** called when possibility is unselected, after a backtrack */
    void unselected()
    {
      // go over every active point
      cover.callMe(forUnselection);
      // set active
      // System.out.println("Put in active point " + myNum);
      activePoss.putIn(myNum);
    }
    /** Object to receive callbacks after unselection */
    private final InOutSet.PerMember forUnselection =
      new InOutSet.PerMember()
    {
      /** called when unselected for every active point */
      public void callback(int point)
      {
	// For each active possibility covering that point we will
	// have removed it, so we need to put it back again
        pointa[point].callMe(pointa[point].forUnselection);
      }
    };
  }
  /** For each active point, we have a set of unused possibilities
      that cover it. Keep track of both links from possibility ->
      point and point -> possibility so once we chose a possibility
      we can forget about it and its points while we backtrack below
      that combination
      */
  private class Point implements InOutSet.PerMember
  {
    /** possibilities covering us, with the ones still not selected
        set active */
    private InOutSet possibilitiesX;
    /** iterate over remaining possibilities */
    public void callMe(InOutSet.PerMember callThis)
    {
      possibilitiesX.callMe(callThis);
    }
    /** number labelling us */
    final int myNum;
    /** construct given the possibilities covering us and our number.
        After construction everything is active. */
    Point(int[] possible, int num)
    {
      possibilitiesX = new InOutSet(possible);
      myNum = num;
    }
    /** return number of possibility that must be included, if exactly
        one choice. Else return number < 0 */
    public int getForced()
    {
      if (possibilitiesX.getNumIn() != 1)
      {
        return -1;
      }
      final int[] only = new int[1];
      possibilitiesX.callMe(new InOutSet.PerMember()
      {
        public void callback(int mem)
	{
	  only[0] = mem;
	}
      });
      // System.out.println("Forced " + only[0]);
      return only[0];
    }
    /** called after a containing possibility has been selected,
      for each possibility covering us */
    public void callback(int cover)
    {
      if (!activePoss.isIn(cover))
      { // this cover is the one that has just been made inactive
        // and we want to preserve its selection of points
	// or has been ruled out
	return;
      }
      // Tell this possibility not to consider this point any more
      possa[cover].cover.takeOut(myNum);
      // so its score has changed
      if (!scoreChanged.isIn(cover))
      {
        scoreChanged.putIn(cover);
      }
    }
    private final InOutSet.PerMember forUnselection =
      new InOutSet.PerMember()
    {
      /** called when unselected for every active cover */
      public void callback(int cover)
      {
	if (!activePoss.isIn(cover))
	{ // this cover is the one that has just been unselected
	  // or has been ruled out
	  return;
	}
	// Tell this cover we are active again
        possa[cover].cover.putIn(myNum);
	// so its score has changed
	if (!scoreChanged.isIn(cover))
	{
	  scoreChanged.putIn(cover);
	}
      }
    };
  }
  /** called in search to update scores */
  public void callback(int cover)
  {
    score.set(cover, possa[cover].getScore());
    scoreChanged.takeOut(cover);
  }
  /** Trigger update of every changed score */
  private void recomputeScores()
  {
    scoreChanged.callMe(this);
  }
  /** number of choices made so far */
  private int numChoices = 0;
  /** Called when new best answer selected. Updates
    cost so far,
    answer so far, state of possibilities,
    and number of points covered */
  void choosePossibility(int selected)
  {
    Possibility best = possa[selected];
    best.selected();
    costSoFar += best.getCost();
    answerSoFar[numChoices++] = selected;
    recomputeScores();
    // no longer available for further selection, but retains old
    // points so would otherwise still score high. Set it low
    score.set(selected, Long.MIN_VALUE);
    numCovered += best.numCovered(); // number of points currently covered
  }
  /** do backtracing search. Run only immediately after construction */
  public void backtrack()
  {
    // For each point, if there is only one set containing it, chose
    // that set. The number of options for each point does not then
    // decrease except when sets are chosen, and such choices cause
    // all points related to those sets to vanish, so we don't care
    // about that
    for (int i = 0; i < pointa.length; i++)
    {
      final Point p = pointa[i];
      if (p == null)
      {
        continue;
      }
      int forced = p.getForced();
      // Choose if forced and not already chosen. Can otherwise be forced
      // multiple times because we don't bother checking the activity or
      // otherwise of points
      if ((forced >= 0) && activePoss.isIn(forced))
      {
	System.out.println("Choose " + forced + " for " + i);
	choosePossibility(forced);
      }
      // else nothing forced or we have already chosen
      // forced
    }
    // proper backtrack
    innerBacktrack();
  }
  /** Answer object is called back with solution */
  public interface SolutionReceiver
  {
    void gotSolution(int[] answer, double cost);
  }
  /** answer object to call */
  private SolutionReceiver callWithSolution;
  /** Set answer catcher */
  public void setAnswerCatcher(SolutionReceiver answerCatcher)
  {
    callWithSolution = answerCatcher;
  }
  /** Whether to use lp bound */
  private boolean useLpBound = false;
  /** set whether to use lp bound */
  public void setUseLpBound(boolean useBound)
  {
    useLpBound = useBound;
  }
  /** get whether to use lp bound */
  public boolean getLpBound()
  {
    return useLpBound;
  }
  /** run recursive part of depth first backtracking search. */
  private void innerBacktrack()
  {
    // save current number of possibilities removed
    int oldRemoved = numRemoved;
    for (;;)
    {
      // System.out.println("Covered " + numCovered + " of " + numPoints);
      if (numCovered >= numPoints)
      { // Hurray!
	int[] answer = new int[numChoices];
	for (int i = 0; i < answer.length; i++)
	{
	  answer[i] = answerSoFar[i];
	}
	// Do this before firing off callback so it can
	// change tooCostly if it wants
	if (costSoFar <= tooCostly)
	{ // want better answer next time
	  tooCostly = costSoFar;
	}
	callWithSolution.gotSolution(answer, costSoFar);
	break;
      }
      // Find best possibility
      int bestOffset = score.getMaxIndex(0, possa.length);
      long bestScore = score.get(bestOffset);
      if (bestScore == Long.MIN_VALUE)
      {
        // nothing left to try
	break;
      }
      if (costSoFar >= tooCostly)
      { // don't try extending this
	break;
      }
      // save state we are about to update
      int oldCovered = numCovered;
      int oldChoices = numChoices;
      double oldCost = costSoFar;
      // select best possibility
      choosePossibility(bestOffset);
      if (useLpBound)
      {
	// System.out.println("Before pre");
	// possibleAll(); (was just to check that possibleLP is
	// no worse a bound than possible all)
	// System.out.println("After pre");
	if (possibleLp())
	{ // Could extend this cover in time, given best possible continuation
	  innerBacktrack();
	}
      }
      else
      {
	if (possibleAll())
	{ // Could extend this cover in time, given best possible continuation
	  innerBacktrack();
	}
      }
      // backtrack
      costSoFar = oldCost;
      numChoices = oldChoices;
      numCovered = oldCovered;
      possa[bestOffset].unselected();
      recomputeScores();
      // So as not to get all possible permutations of every answer,
      // make sure that this selection is not considered again 
      // within this loop.
      // (it won't be immediately recomputed because it is not in
      // scoreChanged and it won't get into scoreChanged because
      // it is not active any more)
      // System.out.println("take out of " + bestOffset);
      activePoss.takeOut(bestOffset);
      removed[numRemoved++] = bestOffset;
      score.set(bestOffset, Long.MIN_VALUE);
    }
    // Now put back stuff we removed from within the loop
    while (numRemoved > oldRemoved)
    {
      int wasRemoved = removed[--numRemoved];
      // System.out.println("loop put back of " + wasRemoved);
      activePoss.putIn(wasRemoved);
      score.set(wasRemoved, possa[wasRemoved].getScore());
    }
  }
  /** See if extending the current partial solution could produce
      a good enough answer */
  private boolean possible()
  {
      int nextBestOffset = score.getMaxIndex(0, possa.length);
      long nextBestScore = score.get(nextBestOffset);
      double estimate;
      if (nextBestScore > Long.MIN_VALUE)
      {
        estimate = costSoFar + possa[nextBestOffset].getEstimate(numPoints - numCovered);
	// System.out.println("Estimate " + estimate + " cost barrier " + tooCostly);
      }
      else if (numCovered >= numPoints)
      { // nothing left, but don't need anything
        estimate = costSoFar;
      }
      else
      {
        estimate = Double.MAX_VALUE;
      }
      return estimate < tooCostly;
  }
  /** as possible(), but use linear programming to get better bound on minimum
      cost solution. The linear programming bound relaxes the problem by making it
      permissible to give possibilities fractional weights between 0 and 1. The possibleAll()
      bound assumes a best case in which each chosen possibility provides a different subset
      of points, and there are no knapsack-packing end effects. It chooses the possibilities
      in sorted order, with the most productive in points per cost first. If the LP bound is
      lower than the possibleAll() bound then consider the possible() points as they are chosen
      and look at the LP weight. Because the LP stuff is in the range [0, 1] we reach a
      contradiction - the LP solution cannot cover all the points at a cost less than the
      possible() bound. So the LP bound is always at least as sharp as the possibleAll() bound.
      We can have equality when the solution is the best possible case for the possibleAll() 
      bound.
      */
  private boolean possibleLp()
  {
    // We have one equation per point, saying that the total weight assigned to that
    // point is at least one: MijXj - Si = 1, where Si is slack, i ranges across points,
    // and j ranges across possibilities, and Mij is set to 1 if point i is covered by
    // possibility j. To make it easy to get a feasible solution to start with we add a
    // final variable that we can initially have set to 1
    final int numEqns = numPoints - numCovered;
    if (numEqns <= 0)
    { // Already done it - cheap enough?
      // System.out.println("Already covered");
      return costSoFar < tooCostly;
    }
    // need to count number of possibilities that still have any points
    final int[] numPoss = new int[1];
    // For each active possibility
    activePoss.callMe(new InOutSet.PerMember()
    {
      public void callback(int here)
      {
        // find the active possibility
	Possibility p = possa[here];
	if (p.numCovered() <= 0)
	{
	  return;
	}
	numPoss[0]++;
      }
    });
    if (numPoss[0] <= 0)
    { // nothing left, but not already done it
      // System.out.println("Nothing left");
      return false;
    }
    final int numVars = numEqns + numPoss[0] + 1;
    final double[] mRowPointColPoss = new double[numEqns * numVars];
    // Numbers possibilities as we come across them
    final int[] possibilityNumber = new int[1];
    // This map allows us to assign eqns to new points as we come across them
    final Map<Integer, Integer> pointToEqn = new HashMap<Integer, Integer>();
    // For each point in the possibilities we are about to work over
    final InOutSet.PerMember perPoint = new InOutSet.PerMember()
    {
      public void callback(int here)
      {
	int pointNumber;
        // get our number for this point
	Integer hi = new Integer(here);
	Integer pn = pointToEqn.get(hi);
	if (pn == null)
	{ // assign a new point number
	  pointNumber = pointToEqn.size();
	  pn = new Integer(pointNumber);
	  pointToEqn.put(hi, pn);
	}
	else
	{
	  pointNumber = pn.intValue();
	}
	// System.out.println("Set row " + pointNumber + " col " + possibilityNumber[0]);
	mRowPointColPoss[pointNumber * numVars + possibilityNumber[0]] = 1.0;
      }
    };
    // objective vector is cost of each remaining possibility, then 0
    // for the slack variables
    final double[] obj = new double[numVars];
    // For each active possibility
    activePoss.callMe(new InOutSet.PerMember()
    {
      public void callback(int here)
      {
        // find the active possibility
	Possibility p = possa[here];
	if (p.numCovered() <= 0)
	{
	  return;
	}
	p.callFromCover(perPoint);
	obj[possibilityNumber[0]] = p.getCost();
	possibilityNumber[0]++;
      }
    });
    // Fill in the coefficients for the slack variables
    for (int i = 0; i < numEqns; i++)
    {
      mRowPointColPoss[i * numVars + numPoss[0] + i] = -1;
      mRowPointColPoss[i * numVars + numVars - 1] = 1.0;
    }
    // Want the end slack variable more expensive than everything else
    // put together, so never chosen
    double totalCost = 0.0;
    for (int i = 0; i < numEqns; i++)
    {
      totalCost += obj[i];
    }
    obj[numVars - 1] = 2.0 * (totalCost + 1.0);
    // rhs is a vector of 1s
    double[] rhs = new double[numEqns];
    Arrays.fill(rhs, 1.0);
    final double small = 1.0e-6;
    // including the final variable and all but one of the other
    // slack variables is feasible
    int[] nonZero = new int[numEqns];
    for (int i = 0; i < nonZero.length; i++)
    {
      nonZero[i] = numVars - i - 1;
    }
    if (false)
    {
      System.out.println("obj " + Arrays.toString(obj));
      System.out.println("rhs " + Arrays.toString(rhs));
      System.out.println("nonZero " + Arrays.toString(nonZero));
      System.out.println("Matrix");
      LU.print(numVars, mRowPointColPoss);
    }
    SimplexSparse st = new SimplexSparse(obj, mRowPointColPoss, rhs,
      42, small);
    double[] xValues = new double[numVars];
    double[] discrepancy = new double[1];
    double additional = st.driver(nonZero, xValues, discrepancy, System.out);
    if (discrepancy[0] > small)
    {
      // LP problem - hopefully just rounding error
      System.err.println("Problem with LP " + discrepancy[0]);
      System.out.println("Problem with LP " + discrepancy[0]);
      return possibleAll();
    }
    double estimate = additional + costSoFar;
    // System.out.println("LP estimate " + estimate);
    return (estimate < tooCostly);
  }

  /** See if extending the current partial solution would work.
      With this version we pull out as many of the active possibilities
      as we need to cover everything, but we assume that no every point
      in every possibility we pull out can be used. This gives us
      a lower bound to the cost, because if this was true then just
      using the topmost answers would be the best answer (we reduce
      the cost of an answer if it covers more than we need).
      */
  private boolean possibleAll()
  {
    double estimate = costSoFar;
    int ourRemoved = numRemoved;
    int ourCovered = numCovered;
    boolean answer;
    for (;;)
    {
      if (estimate >= tooCostly)
      {
        answer = false;
	// System.out.println("Stop all estimate at " + estimate);
	break;
      }
      if (ourCovered >= numPoints)
      {
	answer = true;
	// System.out.println("Finish all estimate at " + estimate);
	break;
      }
      int nextBestOffset = score.getMaxIndex(0, possa.length);
      if (score.get(nextBestOffset) <= Long.MIN_VALUE)
      {
        // nothing left
	// System.out.println("Exhaust all estimate at " + estimate);
	answer = false;
	break;
      }
      Possibility p = possa[nextBestOffset];
      int covers = p.cover.getNumIn();
      double cost = p.cost;
      int toCover = numPoints - ourCovered;
      ourCovered += covers;
      if (covers <= toCover)
      {
        estimate += cost;
      }
      else
      {
        estimate += (cost * toCover) / covers;
      }
      // Remove from top
      score.set(nextBestOffset, Long.MIN_VALUE);
      // and record to reinstate
      removed[ourRemoved++] = nextBestOffset;
    }
    // reinstate answers
    while (--ourRemoved >= numRemoved)
    {
      int x = removed[ourRemoved];
      score.set(x, possa[x].getScore());
    }
    return answer;
  }
  /** Read input and run. Each possibility is given by a cost, followed by a series of numbers.
    Possibilities are separated by '*' characters */
  public static void main(String[] s) throws Exception
  {
    double tooCostly = -1.0;
    int s1 = s.length - 1;
    boolean trouble = false;
    // for debugging
    int numRand = 1;
    long seed = 42;
    String num = "";
    boolean showAll = false;
    boolean useLpBound = false;
    try
    {
      for (int i = 0; i < s.length; i++)
      {
	if ("-cost".equals(s[i]) && (i < s1))
	{
	  num = s[++i];
	  tooCostly = Double.parseDouble(num.trim());
	}
	else if ("-lp".equals(s[i]))
	{
	  useLpBound = true;
	}
	else if ("-passes".equals(s[i]) && (i < s1))
	{
	  num = s[++i];
	  numRand = Integer.parseInt(num.trim());
	}
	else if ("-all".equals(s[i]))
	{
	  showAll = true;
	}
	else
	{
	  System.err.println("Cannot handle flag " + s[i]);
	  trouble = true;
	}
      }
    }
    catch (NumberFormatException nf)
    {
      System.err.println("Cannot read number in " + num);
      trouble = true;
    }
    if (trouble)
    {
      System.err.println("Args are [-cost #] [-lp] [-passes #] [-all]");
      return;
    }
    ArrayList<int[]> poss = new ArrayList<int[]>();
    ArrayList<Double> cost = new ArrayList<Double>();
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    ArrayList<Integer> current = new ArrayList<Integer>();
    boolean foundEof = false;
    boolean gotCost = false;
    while (!foundEof)
    {
      String lin = br.readLine();
      if (lin == null)
      {
        foundEof = true;
	lin = "*"; // trigger cleanup
      }
      // System.out.println("Line " + lin);
      for (StringTokenizer st = new StringTokenizer(lin, "\t\n\r\f #", 
        true); st.hasMoreTokens();)
      {
        String tok = st.nextToken();
	// System.out.println("Got token " + tok);
	if ("#".equals(tok))
	{ // comment till end of line
	  break;
	}
	if (Character.isWhitespace(tok.charAt(0)))
	{ // white space
	  continue;
	}
	if ("*".equals(tok))
	{
	  int[] pointsHere = new int[current.size()];
	  for (int i = 0; i < pointsHere.length; i++)
	  {
	    pointsHere[i] = current.get(i).intValue();
	  }
	  poss.add(pointsHere);
	  current.clear();
	  gotCost = false;
	  continue;
	}
	try
	{
	  // System.out.println("Token " + tok + " gotCost " + gotCost);
	  if (!gotCost)
	  {
	    cost.add(new Double(tok));
	    gotCost = true;
	  }
	  else
	  {
	    current.add(new Integer(tok));
	  }
	}
	catch (NumberFormatException nf)
	{
	  System.err.println("Could not read number " + tok +
	    " in input line " + lin);
	  return;
	}
      }
    }
    if (cost.size() != poss.size())
    {
      System.err.println("Input leaves costs out of step");
      return;
    }
    final int[][] ppoints = new int[poss.size()][];
    double[] costs = new double[ppoints.length];
    for (int i = 0; i < costs.length; i++)
    {
      costs[i] = cost.get(i).doubleValue();
    }
    final int[][] points = poss.toArray(ppoints);
    double totalCost = 0.0;
    for (double d: costs)
    {
      totalCost += d;
    }
    if (tooCostly < 0)
    {
      tooCostly = totalCost * 2.0 + 1.0;
    }
    System.out.println("Got " + points.length + " sets to cover with");
    for (int i = 0; i < points.length; i++)
    {
      System.out.println("Set " + i + " cost " + costs[i]);
      String sep = "";
      for (int p: points[i])
      {
        System.out.print(sep);
	System.out.print(p);
	sep = " ";
      }
      System.out.println();
    }
    final SetCover toSolve = new SetCover(points, costs, tooCostly,
      null);
    toSolve.setUseLpBound(useLpBound);
    final boolean sa = showAll;
    final double tc = tooCostly;
    final SolutionReceiver showAnswer = new SolutionReceiver()
    {
      public void gotSolution(int[] answer, double cost)
      {
	String sep = "Solution: ";
        for (int i = 0; i < answer.length; i++)
	{
	  System.out.print(sep);
	  System.out.print(answer[i]);
	  sep = " ";
	}
	System.out.println(" cost " + cost);
	for (int i = 0; i < answer.length; i++)
	{
	  System.out.println(Arrays.toString(points[answer[i]]));
	}
	if (sa)
	{
	  toSolve.setTooCostly(tc);
	}
      }
    };
    toSolve.setAnswerCatcher(showAnswer);
    toSolve.backtrack();
    Random r = new Random(seed);
    // To check, try with rows permuted
    for (int go = 1; go < numRand; go++)
    {
      System.out.println("Reordered");
      int[][] np = new int[points.length][];
      double[] nc = new double[costs.length];
      List<Integer> perm = new ArrayList<Integer>();
      for (int i = 0; i < costs.length; i++)
      {
        perm.add(i);
      }
      Collections.shuffle(perm, r);
      for (int i = 0; i < costs.length; i++)
      {
	int ni = perm.get(i);
	np[i] = points[ni];
	nc[i] = costs[ni];
      }
      SetCover toSolvep = new SetCover(np, nc, tooCostly,
	showAnswer);
      toSolvep.backtrack();
    }
  }
}
