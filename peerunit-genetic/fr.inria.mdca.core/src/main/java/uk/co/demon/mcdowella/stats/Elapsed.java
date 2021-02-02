package uk.co.demon.mcdowella.stats;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.util.HashMap;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/** This class works out the probability distribution for
  the total amount of elapsed time required by two workers
  to complete a series of tasks. A shared list gives the tasks
  to be completed, in order, with three possible durations per
  task and their probabilities. Each worker takes the next task
  on the list when they finish the previous one.
  */
public class Elapsed
{
  /** durations gives the three possible durations for each
      task together, in increasing order. Probabilities gives
      the associated probabilities. This constructor also
      works out the distribution.
      */
  public Elapsed(int[] durations, double[] probabilities)
  {
    // Check that triples of durations have probabilities that
    // sum up to something close to 1 and so on.
    if (durations.length != probabilities.length)
    {
      throw new IllegalArgumentException("Array length mismatch");
    }
    final int numTasks = durations.length / 3;
    if (numTasks * 3 != durations.length)
    {
      throw new IllegalArgumentException("Not a whole number of tasks");
    }
    // Check each triple
    for (int i = 0; i < numTasks; i++)
    {
      int to = i * 3;
      if ((probabilities[to] < 0.0) || (probabilities[to + 1] <
          0.0) || (probabilities[to + 2] < 0.0))
      {
        throw new IllegalArgumentException(
	  "-ve probability");
      }
      double sum = probabilities[to] + probabilities[to + 1] +
        probabilities[to + 2];
      if (Math.abs(sum - 1.0) > 1.0E-6)
      {
        throw new IllegalArgumentException("Sum of triple of " +
	  "probabilities too far from zero");
      }
    }
    // Now we work our way along the tasks, keeping track of the
    // probabilities of each possible state we can occupy at the
    // end of the task. The state variables are the elapsed time
    // so far, and the time remaining (if any) until the other
    // worker of the pair completes its task.
    
    // Set up initial map from state to probability
    Map<State, Double> previous = new HashMap<State, Double>();
    previous.put(new State(0, 0), 1.0);
    for (int i = 0; i < numTasks; i++)
    {
      final int off = i * 3;
      final Map<State, Double> current = new HashMap<State, Double>();
      for (Map.Entry<State, Double> ii: previous.entrySet())
      {
	final State prevState = ii.getKey();
	final double probHere = ii.getValue();
	for (int j = 0; j < 3; j++)
	{
	  double p = probHere * probabilities[off + j];
	  if (p <= 0.0)
	  {
	    continue;
	  }
	  int timeHere = prevState.nextFinishTime + durations[off + j];
	  int laterHere = prevState.laterFinishTime;
	  if (laterHere < timeHere)
	  {
	    int t = laterHere;
	    laterHere = timeHere;
	    timeHere = t;
	  }
	  State now = new State(timeHere, laterHere);
	  Double sofar = current.get(now);
	  if (sofar == null)
	  {
	    current.put(now, p);
	  }
	  else
	  {
	    current.put(now, p + sofar);
	  }
	}
      }
      previous = current;
      if (CHECK_PROBS)
      {
	double sum = 0.0;
	for (Double contrib: previous.values())
	{
	  sum += contrib;
	}
	if (Math.abs(sum - 1.0) > 1.0E-6)
	{
	  throw new IllegalStateException("Probs sum to " + sum);
	}
      }
    }
    // produce the final distribution
    Map<Integer, Double> probByValue = new TreeMap<Integer, Double>();
    for (Map.Entry<State, Double> e: previous.entrySet())
    {
      State s = e.getKey();
      Integer time = s.laterFinishTime;
      Double p = probByValue.get(time);
      if (p == null)
      {
        probByValue.put(time, e.getValue());
      }
      else
      {
        probByValue.put(time, p.doubleValue() + e.getValue());
      }
    }
    probs = new double[probByValue.size()];
    values = new int[probs.length];
    int wp = 0;
    for (Map.Entry<Integer, Double> e: probByValue.entrySet())
    {
      values[wp] = e.getKey();
      probs[wp] = e.getValue();
      wp++;
    }
  }
  // Class representing state
  private static class State
  {
    /** construct from info stored */
    State(int forFinish, int forLater)
    {
      nextFinishTime = forFinish;
      laterFinishTime = forLater;
    }
    /** time next task finishes */
    public final int nextFinishTime;
    /** time task of other worker finshes; >= nextFinishTime */
    public final int laterFinishTime;
    @Override
    public int hashCode()
    {
      return nextFinishTime * 131 + laterFinishTime;
    }
    @Override
    public boolean equals(Object other)
    {
      if (!(other instanceof State))
      {
        return false;
      }
      State otherState = (State) other;
      return (nextFinishTime == otherState.nextFinishTime) &&
             (laterFinishTime == otherState.laterFinishTime);
    }
  }
  /** switches on sanity checking */
  private static boolean CHECK_PROBS = true;
  /** array of probabilities */
  private final double[] probs;
  /** matching array of values */
  private final int[] values;
  /** returns the probabilities of the possible (integer) 
      durations, starting with that for the minimum possible
      duration */
  public double[] getDistProbs()
  {
    return probs.clone();
  }
  /** returns a matching array of values for the probabilities,
      in increasing order */
  public int[] getDistValues()
  {
    return values.clone();
  }
  /** Main routine reads in lines of form 
    <prob1> <duration1> <prob2> <duration2> <prob3> <duration3> 
    and works out probability distribution
    */
  public static void main(String[] s) throws Exception
  {
    BufferedReader br = new BufferedReader(new InputStreamReader(
      System.in));
    List<Double> probs = new ArrayList<Double>();
    List<Integer> durations = new ArrayList<Integer>();
    String num = null;
    String line = null;
    try
    {
      for (;;)
      {
	line = br.readLine();
	if (line == null)
	{
	  break;
	}
	// comment
	int index = line.indexOf('#');
	if (index >= 0)
	{
	  line = line.substring(0, index);
	}
	line = line.trim();
	if (line.length() <= 0)
	{ // blank line or comment
	  continue;
	}
	StringTokenizer st = new StringTokenizer(line);
	double totalProb = 0.0;
	for (int i = 0; i < 3; i++)
	{
	  if (!st.hasMoreTokens())
	  {
	    System.err.println(
	      "Could not read 3 pairs of prob and duration from " +
	      line);
	    return;
	  }
	  num = st.nextToken();
	  double prob = Double.parseDouble(num.trim());
	  if ((prob < 0.0) || (prob > 1.0))
	  {
	    System.err.println("Prob " + prob + " out of range");
	    return;
	  }
	  totalProb += prob;
	  probs.add(prob);
	  if (!st.hasMoreTokens())
	  {
	    System.err.println(
	      "Could not read 3 pairs of prob and duration from " +
	      line);
	    return;
	  }
	  num = st.nextToken();
	  durations.add(Integer.parseInt(num.trim()));
	}
	if (Math.abs(totalProb - 1.0) > 1.0E-6)
	{
	  System.err.println("Probs do not add to 1 in " + line);
	  return;
	}
      }
    }
    catch (NumberFormatException nfe)
    {
      System.err.println("Could not read number in " + num);
      return;
    }
    double[] pv = new double[probs.size()];
    int[] dv = new int[pv.length];
    for (int i = 0; i < pv.length; i++)
    {
      pv[i] = probs.get(i);
      dv[i] = durations.get(i);
    }
    Elapsed e = new Elapsed(dv, pv);
    double[] pp = e.getDistProbs();
    int[] vv = e.getDistValues();
    for (int i = 0; i < pp.length; i++)
    {
      System.out.println("Time " + vv[i] + " prob " + pp[i]);
    }
  }
}
