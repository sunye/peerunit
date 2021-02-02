package uk.co.demon.mcdowella.stats;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.util.HashMap;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/** This class is used to work out the number of ways of choosing
 *  different vectors from a list, subject to constraints on the
 *  sum of those vectors. It is intended to be used to work out
 *  probabilities.
 */
public class NumChoices
{
  /** Prepare to answer questions about numbers of vectors,
   *  subject to marginals. Each vector has a
   *  score as its first component, and the other components will
   *  be required to have specific values
   */
  public NumChoices(int[][] vectors)
  {
    vecs = new int[vectors.length][];
    if (vecs.length == 0)
    {
      return;
    }
    vecLength = vectors[0].length;
    for (int i = 0; i < vecs.length; i++)
    {
      vecs[i] = vectors[i].clone();
      if (vecLength != vecs[i].length)
      {
        throw new IllegalArgumentException("Inconsistent length");
      }
    }
    if (vecLength < 1)
    {
      return;
    }
    // We will want to know the minimum and maximum possible sums
    // in the Nth dimension, using any of the first M vectors
    maxSumByComponentFrom = new int[vecLength][];
    minSumByComponentFrom = new int[vecLength][];
    for (int i = 0; i < vecLength; i++)
    {
      maxSumByComponentFrom[i] = new int[vecs.length];
      minSumByComponentFrom[i] = new int[vecs.length];
      int maxSoFar = 0;
      int minSoFar = 0;
      for (int j = 0; j < vecs.length; j++)
      {
	maxSumByComponentFrom[i][j] = maxSoFar;
	minSumByComponentFrom[i][j] = minSoFar;
	int contendor = vecs[j][i];
	if (contendor < 0)
	{
	  minSoFar += contendor;
	}
	else
	{
	  maxSoFar += contendor;
	}
      }
    }
  }
  /** Length of each possible vector */
  private int vecLength;
  /** possible vectors. 0th element is score. The rest
   *  is marginal
   */
  private int[][] vecs;
  /** maxSumByComponentFrom[i][j] holds the maximum
   *  possible sum using the first j vectors for the
   *  ith component
   */
  private int[][] maxSumByComponentFrom;
  /** minSumByComponentFrom[i][j] holds the minimum
   *  possible sum using the first j vectors for the
   *  ith component
   */
  private int[][] minSumByComponentFrom;
  /** scaled number < target[0] */
  private double belowTarget;
  /** return scaled number < target[0] */
  public double getProbBelowTarget()
  {
    return belowTarget;
  }
  /** scaled number == target[0] */
  private double atTarget;
  /** return scaled number == target[0] */
  public double getProbAtTarget()
  {
    return atTarget;
  }
  /** scaled number > target[0] */
  private double aboveTarget;
  /** return scaled number == target[0] */
  public double getProbAboveTarget()
  {
    return aboveTarget;
  }
  /** log of scaling factor */
  private double logScalingFactor;
  /** return log of scaling factor */
  public double getLogScalingFactor()
  {
    return logScalingFactor;
  }
  /** Get significance tail probability */
  public SigProb getSigProb()
  {
    return new SigProb(belowTarget, atTarget, aboveTarget);
  }
  /** target value - score followed by marginals */
  private int[] target;
  /** work out the number of sums less than, equal to, or greater than,
   *  target, subject to the sum of all other dimensions equalling
   *  the value in the target. Each vector is treated as different
   *  in computing the number of distinct sums, even if the values
   *  of the two vectors are identical
   */
  public void computeFor(int[] forTarget)
  {
    target = forTarget.clone();
    logScalingFactor = 0.0;
    if (target.length != vecLength)
    {
      throw new IllegalArgumentException("Target length mismatch");
    }
    if ((vecLength == 0) || (vecs.length == 0))
    {
      belowTarget = 0.0;
      atTarget = 1.0;
      if ((vecLength > 0) && (forTarget[0] != 0))
      {
        atTarget = 0.0;
      }
      aboveTarget = 0.0;
      logScalingFactor = vecs.length * Math.log(2.0);
      return;
    }
    target = forTarget.clone();
    Map<Marginal, Counts> current = new HashMap<Marginal, Counts>();
    // there is one way of reaching 0 using 0 items
    Counts start = new Counts();
    start.counts = new double[] {1.0};
    start.valueAtZero = 0;
    start.countsBelow = 0;
    start.countsAbove = 0;
    current.put(new Marginal(new int[vecLength - 1]), start);
    // This vector represents not using a particular choice
    int[] notUsed = new int[vecLength];
    for (int i = vecs.length - 1; i >= 0; i--)
    {
      int[] include = vecs[i];
      Map<Marginal, Counts> next = new HashMap<Marginal, Counts>();
      for (Map.Entry<Marginal, Counts> me: current.entrySet())
      {
	Counts oldCounts = me.getValue();
	Marginal m = me.getKey();
        updateMap(i, include, oldCounts, m.getArrayCopy(),
	  next);
        updateMap(i, notUsed, oldCounts, m.getArrayCopy(),
	  next);
      }
      // System.err.println("Done pos " + i);
      current = next;
      double largest = 0.0;
      for (Counts c: current.values())
      {
        if (c.countsBelow > largest)
	{
	  largest = c.countsBelow;
	}
	if (c.countsAbove > largest)
	{
	  largest = c.countsAbove;
	}
	for (int j = 0; j < c.counts.length; j++)
	{
	  double count = c.counts[j];
	  if (count > largest)
	  {
	    largest = count;
	  }
	}
      }
      if (largest == 0.0)
      {
        continue;
      }
      logScalingFactor += Math.log(largest);
      largest = 1.0 / largest;
      for (Counts c: current.values())
      {
        c.countsBelow *= largest;
        c.countsAbove *= largest;
	for (int j = 0; j < c.counts.length; j++)
	{
	  c.counts[j] *= largest;
	}
      }
    }
    int cs = current.size();
    if (cs == 0)
    {
      belowTarget = atTarget = aboveTarget = 0.0;
      return;
    }
    if (cs > 1)
    {
      throw new IllegalStateException(
        "More than one match to marginals");
    }
    Counts survivor = current.values().iterator().next();
    if (survivor.counts.length > 1)
    {
      throw new IllegalStateException("Survivor is of length > 1");
    }
    belowTarget = survivor.countsBelow;
    aboveTarget = survivor.countsAbove;
    if (survivor.counts.length == 1)
    {
      atTarget = survivor.counts[0];
    }
    else
    {
      atTarget = 0.0;
    }
    double total = belowTarget + aboveTarget + atTarget;
    if (total > 0.0)
    {
      logScalingFactor += Math.log(total);
      total = 1.0 / total;
      belowTarget *= total;
      aboveTarget *= total;
      atTarget *= total;
    }
  }
  /** Use Marginal-Count pair and a array 
   * to update a map from Marginals to Counts. Can
   * update oldMarginals to produce new marginals
   */
  private void updateMap(int pos, int[] contribution, 
    Counts oldCounts, int[] oldMarginals, Map<Marginal, Counts> newMap)
  {
    for (int j = 0; j < oldMarginals.length; j++)
    {
      int vecCoord = j + 1;
      int atM = oldMarginals[j] + contribution[vecCoord];
      if ((atM + maxSumByComponentFrom[vecCoord][pos]) <
	  target[vecCoord])
      {
	return;
      }
      if ((atM + minSumByComponentFrom[vecCoord][pos]) >
	  target[vecCoord])
      {
	return;
      }
      oldMarginals[j] = atM;
    }
    Marginal newMarg = new Marginal(oldMarginals);
    int score = contribution[0];
    Counts newCounts = newMap.get(newMarg);
    if (newCounts == null)
    { // produce dummy counts
      newCounts = new Counts();
      newCounts.counts = new double[0];
      newCounts.countsBelow = 0.0;
      newCounts.countsAbove = 0.0;
      newCounts.valueAtZero = 0;
      newMap.put(newMarg, newCounts);
    }
    /*
    System.err.println("Before Counts at zero is " +
      newCounts.valueAtZero +
      " below " + newCounts.countsBelow +
      " above " + newCounts.countsAbove);
    for (int i = 0; i < newCounts.counts.length; i++)
    {
      System.err.println(i + ": " + newCounts.counts[i]);
    }
    */
    // Add in first part of the contribution from the old counts and
    // the score
    newCounts.countsBelow += oldCounts.countsBelow;
    newCounts.countsAbove += oldCounts.countsAbove;
    // Add in values now known to be to one side or other of target
    int pastBelow = target[0] - score - 
      maxSumByComponentFrom[0][pos] - oldCounts.valueAtZero;
    if (pastBelow > oldCounts.counts.length)
    {
      pastBelow = oldCounts.counts.length;
    }
    for (int i = 0; i < pastBelow; i++)
    {
      newCounts.countsBelow += oldCounts.counts[i];
    }
    int firstAbove = target[0] - score -
      minSumByComponentFrom[0][pos] - oldCounts.valueAtZero + 1;
    if (firstAbove < 0)
    {
      firstAbove = 0;
    }
    for (int i = firstAbove; i < oldCounts.counts.length; i++)
    {
      newCounts.countsAbove += oldCounts.counts[i];
    }
    if ((pastBelow == oldCounts.counts.length) || (firstAbove == 0))
    { // taken account of everything here
      return;
    }
    if (pastBelow < 0)
    {
      pastBelow = 0;
    }
    if (firstAbove > oldCounts.counts.length)
    {
      firstAbove = oldCounts.counts.length;
    }
    // Work out range we produce by adding new vector in
    int lowValue = oldCounts.valueAtZero + score + pastBelow;
    int highValue = oldCounts.valueAtZero + score + firstAbove - 1;
    if (newCounts.counts.length == 0)
    { // brand new
      newCounts.valueAtZero = lowValue;
      newCounts.counts = new double[highValue - lowValue + 1];
      for (int i = pastBelow; i < firstAbove; i++)
      {
        newCounts.counts[i - pastBelow] = oldCounts.counts[i];
      }
      return;
    }
    int oldLow = newCounts.valueAtZero;
    int oldHigh = newCounts.valueAtZero + newCounts.counts.length - 1;
    // Each movement at least doubles the range covered
    // so cost of copies is linear in final length
    while (oldLow > lowValue)
    {
      oldLow = oldLow - (oldHigh - oldLow + 1);
    }
    // except that we know we never have to go below this
    if ((oldLow + maxSumByComponentFrom[0][pos]) < target[0])
    {
      oldLow = target[0] - maxSumByComponentFrom[0][pos];
    }
    while (oldHigh < highValue)
    {
      oldHigh = oldHigh + (oldHigh - oldLow + 1);
    }
    if ((oldHigh + minSumByComponentFrom[0][pos]) > target[0])
    {
      oldHigh = target[0] - minSumByComponentFrom[0][pos];
    }
    int oldLen = oldHigh - oldLow + 1;
    if ((oldLow != newCounts.valueAtZero) ||
        (oldLen != newCounts.counts.length))
    {
      double[] newData = new double[oldLen];
      for (int i = 0; i < newCounts.counts.length; i++)
      {
        newData[i + newCounts.valueAtZero - oldLow] =
	  newCounts.counts[i];
      }
      newCounts.valueAtZero = oldLow;
      newCounts.counts = newData;
    }
    for (int i = pastBelow; i < firstAbove; i++)
    {
      newCounts.counts[oldCounts.valueAtZero + i + score -
	newCounts.valueAtZero] += oldCounts.counts[i];
    }
    /*
    System.err.println("After Counts at zero is " +
      newCounts.valueAtZero +
      " below " + newCounts.countsBelow +
      " above " + newCounts.countsAbove);
    for (int i = 0; i < newCounts.counts.length; i++)
    {
      System.err.println(i + ": " + newCounts.counts[i]);
    }
    */
  }
  /** count info for a distribution: counts of number of combinations
   *  reaching target value
   */
  private static class Counts
  {
    double[] counts;
    int valueAtZero;
    double countsBelow;
    double countsAbove;
  }
  /** key holds the marginals */
  private static class Marginal
  {
    final int hash;
    final int[] marginal;
    int[] getArrayCopy()
    {
      return marginal.clone();
    }
    Marginal(int[] values)
    {
      marginal = values.clone();
      int h = 0;
      for (int i = 0; i < marginal.length; i++)
      {
        h += marginal[i] * (i + 1);
      }
      hash = h;
    }
    public int hashCode()
    {
      return hash;
    }
    public boolean equals(Object other)
    {
      if (!(other instanceof Marginal))
      {
        return false;
      }
      Marginal m = (Marginal) other;
      if (m.hash != hash)
      {
        return false;
      }
      if (m.marginal.length != marginal.length)
      {
        return false;
      }
      for (int i = 0; i < marginal.length; i++)
      {
        if (marginal[i] != m.marginal[i])
	{
	  return false;
	}
      }
      return true;
    }
  }
  /** read a line of ints, or return null */
  private static int[] readInts(BufferedReader br)
    throws IOException
  {
    String readThis = "";
    try
    {
      for (;;)
      {
	String s = br.readLine();
	if (s == null)
	{
	  return null;
	}
	int index = s.indexOf('#');
	if (index >= 0)
	{
	  s = s.substring(0, index);
	}
	s = s.trim();
	if ("".equals(s))
	{
	  continue;
	}
	StringTokenizer st = new StringTokenizer(s);
	List<Integer> l = new ArrayList<Integer>();
	while (st.hasMoreTokens())
	{
	  readThis = st.nextToken().trim();
	  l.add(Integer.parseInt(readThis));
	}
	int[] result = new int[l.size()];
	for (int i = 0; i < result.length; i++)
	{
	  result[i] = l.get(i).intValue();
	}
	return result;
      }
    }
    catch (NumberFormatException nfe)
    {
      System.err.println("Could not read number in " + readThis);
      return null;
    }
  }
  public static void main(String[] s) throws Exception
  {
    if (s.length != 0)
    {
      System.err.println("No arguments - everything read from input");
      System.err.println(
        "# starts a comment and blank lines are ignored");
      System.err.println(
        "First line should be target score then marginals");
      System.err.println("other lines are contribution to score");
      System.err.println("then contribution to marginals");
      return;
    }
    BufferedReader br = new BufferedReader(new InputStreamReader(
      System.in));
    int[] target = readInts(br);
    if (target == null)
    {
      System.err.println(
        "Must have at least a first line and a data line");
      return;
    }
    List<int[]> data = new ArrayList<int[]>();
    for (;;)
    {
      int[] dataLine = readInts(br);
      if (dataLine == null)
      {
        break;
      }
      if (dataLine.length != target.length)
      {
        System.err.println(
    "All data vectors must be the same length as the target vector");
      }
      data.add(dataLine);
    }
    int numVecs = data.size();
    if (numVecs < 1)
    {
      System.err.println("Must have at least one data line");
      return;
    }
    System.out.println("Target score is " + target[0]);
    System.out.print("Required marginals");
    for (int i = 1; i < target.length; i++)
    {
      System.out.print(' ');
      System.out.print(target[i]);
    }
    System.out.println();
    int[][] vecs = new int[numVecs][];
    vecs = data.toArray(vecs);
    System.out.println("Individual vectors are:");
    for (int i = 0; i < vecs.length; i++)
    {
      int[] row = vecs[i];
      String sp = "";
      for (int j = 0; j < row.length; j++)
      {
        System.out.print(sp);
	System.out.print(row[j]);
	sp = " ";
      }
      System.out.println();
    }
    NumChoices nc = new NumChoices(vecs);
    nc.computeFor(target);
    double total = nc.getProbBelowTarget() + nc.getProbAboveTarget() +
      nc.getProbAtTarget();
    if (total <= 0.0)
    {
      System.out.println("Marginals unachievable");
      return;
    }
    total *= Math.exp(nc.getLogScalingFactor());
    System.out.println(total + " combinations");
    System.out.println("Sigprob " + nc.getSigProb());
  }
}
