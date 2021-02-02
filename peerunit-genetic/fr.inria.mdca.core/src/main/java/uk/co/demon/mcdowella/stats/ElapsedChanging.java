package uk.co.demon.mcdowella.stats;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/** Search for an Elapsed example in which it makes sense
    to change our mind.
  */
public class ElapsedChanging
{
  /** work out a best chance of hitting target, allowing us to change
    our minds after the first task
      */
  public static int getBestChangingAnswer(int numTasks, 
    int[] durations, double[] probs, int[][] bestHere, int targetValue,
    double percentile)
  {
    // save original durations for first task
    int[] savedFirst = new int[3];
    System.arraycopy(durations, 0, savedFirst, 0, 3);
    // total map from integer to probabilty
    Map<Integer, Double> probByInt = new TreeMap<Integer, Double>();
    for (int i = 0; i < 3; i++)
    {
      // fix time for first task
      for (int j = 0; j < 3; j++)
      {
        durations[j] = savedFirst[i];
      }
      // Get best answer, holding first two tasks fixed
      double pHere = ElapsedDriver.getMostChance(numTasks, durations, 
        probs, 2, numTasks - 2, bestHere[i], targetValue);
      // get probability distribution for this answer
      Elapsed e = new Elapsed(bestHere[i], probs);
      double[] distProbs = e.getDistProbs();
      int[] distValues = e.getDistValues();
      // System.out.println("Partial dist " + Arrays.toString(distProbs));
      // System.out.println("Partial values " + Arrays.toString(distValues));
      // Prob of this outcome of the first task
      double probHere = probs[i];
      // Accumulate probabilities
      for (int j = 0; j < distProbs.length; j++)
      {
	Integer v = new Integer(distValues[j]);
        Double sofar = probByInt.get(v);
	double p = distProbs[j] * probHere;
	if (sofar == null)
	{
	  probByInt.put(v, p);
	}
	else
	{
	  probByInt.put(v, sofar.doubleValue() + p);
	}
      }
    }
    // Turn map into percentile
    int[] ourValues = new int[probByInt.size()];
    double[] ourProbs = new double[ourValues.length];
    int wp = 0;
    for (Map.Entry<Integer, Double> ee: probByInt.entrySet())
    {
      ourValues[wp] = ee.getKey();
      ourProbs[wp] = ee.getValue();
      wp++;
    }
    // check
    double sofar = 0.0;
    for (double s: ourProbs)
    {
      sofar += s;
    }
    if (Math.abs(sofar - 1.0) > 1.0E-6)
    {
      throw new IllegalArgumentException(
        "Probabilities do not sum to 1");
    }
    // restore durations
    System.arraycopy(savedFirst, 0, durations, 0, 3);
    // System.out.println("Combined dist " + Arrays.toString(ourProbs));
    // System.out.println("Combined values " + Arrays.toString(ourValues));
    // return percentile of combined distribution
    return ElapsedDriver.getPercentile(percentile, ourValues, ourProbs);
  }
  /** find an example where we do better by changing our minds */
  public static void main(String[] s)
  {
    final int length = 5;
    Random r = new Random(42);
    int[] schedule = new int[length * 3];
    int[] singleBest = new int[schedule.length];
    int[][] changing = new int[3][];
    for (int i = 0; i < changing.length; i++)
    {
      changing[i] = new int[schedule.length];
    }
    final double probs[] = new double[schedule.length];
    final double percentile = 0.95;
    ElapsedDriver.do3Point(length, probs);
    for (int go = 0;; go++)
    {
      // System.out.println("Go " + go);
      ElapsedDriver.fillIn(length, schedule, r);
      // System.out.println(Arrays.toString(schedule));
      int bestSingle = ElapsedDriver.getBestAnswer(length,
        schedule, probs, percentile, 0, length, singleBest);
      // System.out.println(Arrays.toString(singleBest));
      // start from singlebest to preserve first choice
      int bestChanging = getBestChangingAnswer(length, singleBest,
	probs, changing, bestSingle, percentile);
      if (bestChanging < bestSingle)
      {
        System.out.println("changing " + bestChanging + " single " +
	  bestSingle);
	System.out.println(Arrays.toString(singleBest));
	Elapsed e = new Elapsed(singleBest, probs);
	double[] distProbs = e.getDistProbs();
	int[] distValues = e.getDistValues();
	// System.out.println("Best dist " + Arrays.toString(distProbs));
	// System.out.println("Best values " + Arrays.toString(distValues));
	System.out.println("Check percentile " +
	  ElapsedDriver.getPercentile(percentile, distValues, 
	  distProbs));
	for (int i = 0; i < changing.length; i++)
	{
	  System.out.println(Arrays.toString(changing[i]));
	}
      }
      else if (bestChanging > bestSingle)
      { // should not be here, because we optimised over chance
        // of getting bestChanging or better
	throw new IllegalArgumentException("Duff answer");
      }
    }
  }
}
