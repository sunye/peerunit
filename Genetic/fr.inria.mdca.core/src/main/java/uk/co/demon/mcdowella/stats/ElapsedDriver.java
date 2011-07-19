package uk.co.demon.mcdowella.stats;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/** Drives Elapsed to work out 95% length of time taken for
    work under different ordering strategies. We use the
    Extended Pearson-Tukey Method to assign probabilities
    for the lengths of individual tasks. This is supposed to
    approximate a variety of continuous distributions. Take
    the values for 5%, 50%, and 95% percentiles and produce
    a discrete distribution with those values and with
    probabilities 0.185, 0.630, and 0.185
    */
public class ElapsedDriver
{
  public static final double PROB_LOW = 0.185;
  public static final double PROB_MIDDLE = 0.630;
  public static final double PROB_HIGH = 0.185;
  /** create sets of lengths for different tasks using normal
      distribution */
  public static void fillIn(int numTasks, int[] len,
    Random r)
  {
    for (int i = 0; i < numTasks; i++)
    {
      double v = r.nextGaussian();
      // v is probably around +/- 3
      v = (v + 4) * 10.0;
      if (v < 0.0)
      {
	v = 0.0;
      }
      // v is probably between about 0 and 70
      double d = r.nextDouble();
      len[i * 3] = (int)Math.round(v * (1.0 - d));
      len[i * 3 + 1] = (int)Math.round(v);
      len[i * 3 + 2] = (int)Math.round(v * (1.0 + d));
    }
  }
  /** Simulate one instance and return length of time */
  public static int simulate(int numTasks, int[] len, Random r)
  {
    int timeFirst = 0;
    int timeLast = 0;
    RoughRandom rr = new RoughRandom(new double[] {PROB_LOW, PROB_MIDDLE,
      PROB_HIGH}, r);
    for (int i = 0; i < numTasks; i++)
    {
      int duration = len[i * 3 + rr.next()];
      timeFirst += duration;
      if (timeFirst > timeLast)
      {
        int t = timeFirst;
	timeFirst = timeLast;
	timeLast = t;
      }
    }
    return timeLast;
  }
  /** produce 3-point probabilities */
  public static void do3Point(int numTasks, double[] probs)
  {
    for (int i = 0; i < numTasks; i++)
    {
      probs[i * 3] = PROB_LOW;
      probs[i * 3 + 1] = PROB_MIDDLE;
      probs[i * 3 + 2] = PROB_HIGH;
    }
  }
  /** run tests to check Elapsed vs simulation */
  public static void checkWithSim(int numTasks, Random r, int numGoes)
  {
    // task durations
    int[] t1 = new int[numTasks * 3];
    fillIn(numTasks, t1, r);
    // countByTime holds counts produced from simulation
    Map<Integer, Integer> countByTime = new TreeMap<Integer, Integer>();
    for (int i = 0; i < numGoes; i++)
    {
      int t = simulate(numTasks, t1, r);
      Integer v = countByTime.get(t);
      if (v == null)
      {
        countByTime.put(t, 1);
      }
      else
      {
        countByTime.put(t, new Integer(1 + v));
      }
    }
    // Put probabilities in here for Elapsed calculation
    double[] probs = new double[t1.length];
    do3Point(numTasks, probs);
    // calculated probabilities to be checked against simulation
    Elapsed e = new Elapsed(t1, probs);
    double[] ep = e.getDistProbs();
    int[] ev = e.getDistValues();
    // Use this to work through simulation results, in order
    Iterator<Map.Entry<Integer, Integer>> ii =
      countByTime.entrySet().iterator();
    // holds current simulation result
    Map.Entry<Integer, Integer> me = null;
    if (ii.hasNext())
    {
      me = ii.next();
    }
    int numDistinct = ev.length;
    // chi-squared statistic
    double chi = 0.0;
    for (int erp = 0; erp < ep.length; erp++)
    { // here with erp pointing at a calculation result and
      // me, if not null, pointing at a simulation result at
      // least as far along
      int eval = ev[erp];
      double eprob = ep[erp];
      if ((me != null) && (me.getKey() < eval))
      {
	throw new IllegalArgumentException(
	  "Simulation value not in calculation");
      }
      int countHere = 0;
      if ((me != null) && (me.getKey().intValue() == eval))
      { // calculated value matches next value seen in simulation
	countHere = me.getValue();
	if (ii.hasNext())
	{ // can move on to next simulated value
	  me = ii.next();
	}
	else
	{
	  me = null;
	}
      }
      double expected = numGoes * eprob;
      double diff = countHere - expected;
      chi += diff * diff / expected;
      // Use flattened probabilities: add 1 to everything,
      // including values we know only from calculation that
      // didn't show up in the simulation
      /*
      System.out.println(eval + ", " + eprob + ", " +
	((countHere + 1.0) / (numGoes + numDistinct)) +
	", " + countHere);
	*/
    }
    int df = numDistinct - 1;
    // chi-squared with n degrees of freedom has mean n
    // variance 2n - is of course not normal, but we can use
    // that as a check
    double sigmage = (chi - df) / Math.sqrt(2.0 * df);
    System.out.println("Chi-Squared " + chi + " with " + df +
      " degrees of freedom sigmage " + sigmage);
    if (me != null)
    {
      throw new IllegalArgumentException(
	"Result in simulation not in calculation");
    }
  }
  /** sort tasks into decreasing order of duration at given offset */
  public static void sortByOffset(int[] durations, int offset)
  {
    TaskInfo[] ti = new TaskInfo[durations.length / 3];
    for (int i = 0; i < ti.length; i++)
    {
      ti[i] = new TaskInfo(3, durations, durations[i * 3 + offset],
        i * 3);
    }
    Arrays.sort(ti);
    for (int i = 0; i < ti.length; i++)
    {
      ti[i].write(i * 3, durations);
    }
  }
  /** sort tasks into decreasing order of range */
  public static void sortByRange(int[] durations)
  {
    TaskInfo[] ti = new TaskInfo[durations.length / 3];
    for (int i = 0; i < ti.length; i++)
    {
      ti[i] = new TaskInfo(3, durations, durations[i * 3 + 2] -
        durations[i * 3], i * 3);
    }
    Arrays.sort(ti);
    for (int i = 0; i < ti.length; i++)
    {
      ti[i].write(i * 3, durations);
    }
  }
  /** return percentile requested from durations */
  public static int getPercentile(double required, int[] durations,
    double[] probs)
  {
    double sofar = 0.0;
    // won't fall off the end if good data
    for (int i = 0;;i++)
    {
      sofar += probs[i];
      if (sofar >= required)
      {
        return durations[i];
      }
    }
  }
  /** return chance of making deadline */
  public static double getChance(int[] durations, double[] probs,
    int targetValue)
  {
    double sofar = 0;
    for (int i = 0; i < durations.length; i++)
    {
      if (durations[i] <= targetValue)
      {
        sofar += probs[i];
      }
      else
      { // values are in ascending order
        return sofar;
      }
    }
    return sofar;
  }
  /** recursive routine to search for best answer making variations
      from the specified order. For speed, assumes that all probs
      are the same and does not reorder them to match durations.
      @param numTasks number of tasks in list
      @param durations durations of tasks
      @param probs probabilities of matching durations
      @param percentile Which percentile to optimise
      @param reorderHere allowed to change order of tasks from here on
      @param depthToGo depth to search. Must be at least 1 => swap task
        reorderHere with all tasks to its right
      @param bestHere if non-null gets copy of best answer
      @return value of percentile at best answer
    */
  public static int getBestAnswer(int numTasks, int[] durations,
    double[] probs, double percentile, int reorderHere, int depthToGo,
    int[] bestHere)
  {
    int ourOff = reorderHere * 3;
    if (depthToGo == 1)
    { // swap reorderHere with everything to its right and return the
      // best answer, restoring order when finished.
      // System.out.println("First Try " + Arrays.toString(durations));
      int bestSoFar = getCalculatedPercentile(numTasks, durations,
        probs, percentile);
      if (bestHere != null)
      {
	System.arraycopy(durations, 0, bestHere, 0, bestHere.length);
      }
      for (int swapWith = reorderHere + 1; swapWith < numTasks; swapWith++)
      {
	int theirOff = swapWith * 3;
        for (int j = 0; j < 3; j++)
	{
	  int t = durations[ourOff + j];
	  durations[ourOff + j] = durations[theirOff + j];
	  durations[theirOff + j] = t;
	}
	// System.out.println("Try " + Arrays.toString(durations));
	int attempt = getCalculatedPercentile(numTasks, durations,
	  probs, percentile);
	// System.out.println("Attempt " + attempt + " best " + bestSoFar);
	if (attempt < bestSoFar)
	{
	  // System.out.println("Better");
	  bestSoFar = attempt;
	  if (bestHere != null)
	  {
	    System.arraycopy(durations, 0, bestHere, 0, bestHere.length);
	  }
	}
	// restore array
        for (int j = 0; j < 3; j++)
	{
	  int t = durations[ourOff + j];
	  durations[ourOff + j] = durations[theirOff + j];
	  durations[theirOff + j] = t;
	}
      }
      return bestSoFar;
    }
    // as before, but make recursive calls
    int bestSoFar = getBestAnswer(numTasks, durations,
      probs, percentile, reorderHere + 1, depthToGo - 1, bestHere);
    // use this instead of bestHere to get best answer in recursive
    // calls to preserve true best answer found so far
    int[] bestBelow;
    if (bestHere != null)
    {
      bestBelow = new int[bestHere.length];
    }
    else
    {
      bestBelow = null;
    }
    for (int swapWith = reorderHere + 1; swapWith < numTasks; swapWith++)
    {
      int theirOff = swapWith * 3;
      for (int j = 0; j < 3; j++)
      {
	int t = durations[ourOff + j];
	durations[ourOff + j] = durations[theirOff + j];
	durations[theirOff + j] = t;
      }
      // There is some redundancy in this search because the order of
      // the first two tasks is irrelevant, which means that we are
      // computing some values twice. Ignore this: increased 
      // complication and risk of getting the wrong answer in return
      // for a factor of two in a program that won't be run more
      // than half a dozen times is a bad bargain
      int attempt = getBestAnswer(numTasks, durations,
	probs, percentile, reorderHere + 1, depthToGo - 1, bestBelow);
      // System.out.println("Attempt " + attempt + " best " + bestSoFar);
      if (attempt < bestSoFar)
      {
	// System.out.println("Better 2 at " + reorderHere);
	bestSoFar = attempt;
	if (bestHere != null)
	{
	  System.arraycopy(bestBelow, 0, bestHere, 0, bestHere.length);
	}
      }
      // restore array
      for (int j = 0; j < 3; j++)
      {
	int t = durations[ourOff + j];
	durations[ourOff + j] = durations[theirOff + j];
	durations[theirOff + j] = t;
      }
    }
    return bestSoFar;
  }
  /** recursive routine to search for best answer making variations
      from the specified order. For speed, assumes that all probs
      are the same and does not reorder them to match durations.
      Maximises probability <= some value
    */
  public static double getMostChance(int numTasks, int[] durations,
    double[] probs, int reorderHere, int depthToGo,
    int[] bestHere, int targetValue)
  {
    int ourOff = reorderHere * 3;
    if (depthToGo == 1)
    { // swap reorderHere with everything to its right and return the
      // best answer, restoring order when finished
      double bestSoFar = getChance(numTasks, durations,
        probs, targetValue);
      if (bestHere != null)
      {
	System.arraycopy(durations, 0, bestHere, 0, bestHere.length);
      }
      for (int swapWith = reorderHere + 1; swapWith < numTasks; swapWith++)
      {
	int theirOff = swapWith * 3;
        for (int j = 0; j < 3; j++)
	{
	  int t = durations[ourOff + j];
	  durations[ourOff + j] = durations[theirOff + j];
	  durations[theirOff + j] = t;
	}
	double attempt = getChance(numTasks, durations,
	  probs, targetValue);
	if (attempt > bestSoFar)
	{
	  bestSoFar = attempt;
	  if (bestHere != null)
	  {
	    System.arraycopy(durations, 0, bestHere, 0, bestHere.length);
	  }
	}
	// restore array
        for (int j = 0; j < 3; j++)
	{
	  int t = durations[ourOff + j];
	  durations[ourOff + j] = durations[theirOff + j];
	  durations[theirOff + j] = t;
	}
      }
      return bestSoFar;
    }
    // as before, but make recursive calls
    double bestSoFar = getMostChance(numTasks, durations,
      probs, reorderHere + 1, depthToGo - 1, bestHere, targetValue);
    int[] bestBelow;
    if (bestHere != null)
    {
      bestBelow = new int[bestHere.length];
    }
    else
    {
      bestBelow = null;
    }
    for (int swapWith = reorderHere + 1; swapWith < numTasks; swapWith++)
    {
      int theirOff = swapWith * 3;
      for (int j = 0; j < 3; j++)
      {
	int t = durations[ourOff + j];
	durations[ourOff + j] = durations[theirOff + j];
	durations[theirOff + j] = t;
      }
      double attempt = getMostChance(numTasks, durations,
	probs, reorderHere + 1, depthToGo - 1, bestBelow,
	targetValue);
      if (attempt > bestSoFar)
      {
	bestSoFar = attempt;
	System.arraycopy(bestBelow, 0, bestHere, 0, bestHere.length);
      }
      // restore array
      for (int j = 0; j < 3; j++)
      {
	int t = durations[ourOff + j];
	durations[ourOff + j] = durations[theirOff + j];
	durations[theirOff + j] = t;
      }
    }
    return bestSoFar;
  }
  /** get calculated percentile produced from given tasks in
      specified order
      */
  public static int getCalculatedPercentile(int numTasks,
    int[] durations, double[] probs, double percentile)
  {
    Elapsed e = new Elapsed(durations, probs);
    return getPercentile(percentile, e.getDistValues(),
      e.getDistProbs());
  }
  /** get probability of being <= target value */
  public static double getChance(int numTasks, int[] durations,
    double[] probs, int targetValue)
  {
    Elapsed e = new Elapsed(durations, probs);
    return getChance(e.getDistValues(), e.getDistProbs(), targetValue);
  }
  /** used to sort tasks */
  private static class TaskInfo implements Comparable<TaskInfo>
  {
    /** data we hold giving durations of task */
    private final int[] data;
    /** data to sort by */
    private final int key;
    public int compareTo(TaskInfo t)
    {
      if (key < t.key)
      { // want to sort into decreasing order so reverse compare
        return 1;
      }
      if (key > t.key)
      {
        return -1;
      }
      return 0;
    }
    /** create holding a chunk of length len, comparing at offset
        within it, from data in array, starting at startHere */
    public TaskInfo(int len, int[] forData, int forKey, int startHere)
    {
      data = new int[len];
      key = forKey;
      System.arraycopy(forData, startHere, data, 0, len);
    }
    /** write out data held */
    public void write(int where, int[] dest)
    {
      System.arraycopy(data, 0, dest, where, data.length);
    }
  }
  /** work out 95% percentile elapsed time for original array
      in random order and on sorting into decreasing order of
      each of the 3 3-point offsets
      */
  public static void compareStrategies(Random r, int numTasks)
  {
    int[] t1 = new int[numTasks * 3];
    fillIn(numTasks, t1, r);
    double[] probs = new double[t1.length];
    do3Point(numTasks, probs);
    String sep = "";
    double percentile = 0.50;
    int depth = 2;
    for (int i = -1; i < 4; i++)
    {
      if (i == -1)
      {
      }
      else if (i == 3)
      {
        sortByRange(t1);
      }
      else
      {
	sortByOffset(t1, i);
      }
      int elapsed = getCalculatedPercentile(numTasks,
	t1, probs, percentile);
      System.out.print(sep);
      System.out.print(elapsed);
      sep = ", ";
    }
    // base strategy for search on worst case values sorted
    sortByOffset(t1, 2);
    int[] bestIs = new int[t1.length];
    int searched = getBestAnswer(numTasks, t1, probs, percentile, 0, 
      depth, bestIs);
    System.out.print(sep);
    System.out.print(searched);
    sep = ", ";
    System.out.println();
  }
  public static void main(String[] s)
  {
    Random r = new Random(42);
    if (true)
    {
      for (int i = 0; i < 1000; i++)
      {
	int numTasks = 2 + r.nextInt(15);
	// checkWithSim(numTasks, r, 1000000);
	compareStrategies(r, numTasks);
      }
    }
  }
}
