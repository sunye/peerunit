package uk.co.demon.mcdowella.stats;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import uk.co.demon.mcdowella.algorithms.Swatch;

/** Class for working with the Mann-Whitney statistic; sum of
    ranks of one group within the set of values produced by
    combining two groups
    */
public class MannWhitney
{
  /** Constructor does most of the work. We are given a vector
      of ranks (needed because ties mean they aren't necessarily
      1,2,3...n) and the number of observations in the group
      we use when we work out a sum of ranks. This has to work
      out the distribution of that sum.
      */
  public MannWhitney(int[] ranks, int numRanksInGroup)
  {
    sortedRanks = ranks.clone();
    Arrays.sort(sortedRanks);
    numRanks = numRanksInGroup;
    int min = 0;
    for (int i = 0; i < numRanksInGroup; i++)
    {
      min += sortedRanks[i];
    }
    minSum = min;
    // We work over sortedRanks from left to right. At each
    // stage we have a distribution for the sums that can be
    // formed using the left hand portion of sortedRanks, using
    // n different values, for all relevant n. We can then compute
    // the distribution we get if we include, or don't include
    // the next value in sortedRanks.

    // minimum number of different values considered
    int minDifferentConsidered = 0;
    // If we consider minDifferentConsidered, 
    // minDifferentConsidered + 1,.. different values from the
    // sortedRanks values seen so far, the minimum possible sum
    // is as given in minValue[i]
    int[] minValue = new int[] {0};
    // And for each possible value from minValue[i] on, the number
    // of ways of reaching that value is mumWays[i][j]
    double[][] numWays = new double[1][];
    numWays[0] = new double[] {1.0};
    
    for (int i = 0; i < sortedRanks.length; i++)
    {
      // Minimum number of different values summed so far to consider
      // here. For i = sortedRanks.length - 1 (the last value of i) this
      // should be numRanksInGroup
      int nextMinDifferent = numRanksInGroup -
        sortedRanks.length + i + 1;
      if (nextMinDifferent < 0)
      {
        nextMinDifferent = 0;
      }
      // Work out factor to add on to offset as e.g. x as in
      // nextMinValue[x] to get the equivalent x in minValue[x]
      final int toPrevOffset =  nextMinDifferent -
        minDifferentConsidered;
      // System.out.println("To prev offset " + toPrevOffset);
      // maximum number of contributions we can handle here
      int maxCanDo = numRanksInGroup;
      if (maxCanDo > (i + 1))
      {
        maxCanDo = i + 1;
      }
      final int[] nextMinValue = new int[maxCanDo -
        nextMinDifferent + 1];
      final int valueHere = sortedRanks[i];
      final double[][] nextNumWays = new double[nextMinValue.length][];
      // Here to extend our calculation by using, or not using,
      // sortedRanks[i]
      for (int j = 0; j < nextMinValue.length; j++)
      { // work out the result for a particular number of items taken
        // from the first i + 1.
        if ((j == 0) && (nextMinDifferent == 0))
	{ // minimum possible value from sum of 0 ranks is 0
	  nextMinValue[j] = 0;
	  nextNumWays[j] = new double[] {1.0};
	  continue;
	}
	final int offUsing = j + toPrevOffset - 1;
	if (offUsing >= 0)
	{ // can find min value for using one less item before
	  // and picking up this item
	  final int usingMin = minValue[offUsing];
	  int minv = usingMin + valueHere;
	  double[] waysUsing = numWays[offUsing];
	  int numDifferent = waysUsing.length;
	  int maxv = minv + numDifferent - 1;
	  final int offNotUsing = j + toPrevOffset;
	  if (offNotUsing < minValue.length)
	  { // can find min value for not using this item
	    final int notUsingMin = minValue[offNotUsing];
	    if (notUsingMin < minv)
	    {
	      minv = notUsingMin;
	    }
	    double[] waysNotUsing = numWays[offNotUsing];
	    int o = notUsingMin + waysNotUsing.length - 1;
	    if (o > maxv)
	    {
	      maxv = o;
	    }
	    nextMinValue[j] = minv;
	    final double[] target = new double[maxv - minv + 1];
	    nextNumWays[j] = target;
	    for (int k = 0; k < waysUsing.length; k++)
	    {
	      final int total = usingMin + valueHere + k;
	      target[total - minv] += waysUsing[k];
	    }
	    for (int k = 0; k < waysNotUsing.length; k++)
	    {
	      final int total = notUsingMin + k;
	      target[total - minv] += waysNotUsing[k];
	    }
	  }
	  else
	  { // here if forced to take the item offered here
	    nextMinValue[j] = minv;
	    nextNumWays[j] = waysUsing;
	  }
	}
	else
	{ // min value for not using this item must be available,
	  // as we got here somehow, so our only option here
	  // is not to take the offered value
	  final int off = j + toPrevOffset;
	  nextMinValue[j] = minValue[off];
	  nextNumWays[j] = numWays[off];
	}
      }
      minDifferentConsidered = nextMinDifferent;
      minValue = nextMinValue;
      numWays = nextNumWays;
    }
    if (minSum != minValue[0])
    {
      throw new IllegalStateException("MinSum conflict minSum " +
        minSum + " minValue " + minValue[0]);
    }
    numWaysRank = numWays[0];
    double t = 0.0;
    for (int i = 0; i < numWaysRank.length; i++)
    {
      t += numWaysRank[i];
    }
    totalNumWays = t;
    double le = 0.0;
    double ge = 0.0;
    probLe = new double[numWaysRank.length];
    probGe = new double[numWaysRank.length];
    for (int i = 0; i < numWaysRank.length; i++)
    {
      le += numWaysRank[i];
      probLe[i] = le / totalNumWays;
      int off = numWaysRank.length - 1 - i;
      ge += numWaysRank[off];
      probGe[off] = ge / totalNumWays;
    }
  }
  /** sorted ranks. They don't actually need to be in sorted order
      but it might help us use a little less store
      */
  private final int[] sortedRanks;
  /** number in group */
  private final int numRanks;
  /** minimum possible sum of ranks */
  private final int minSum;
  /** number of ways of getting the possible sums, starting out
      at minSum
      */
  private final double[] numWaysRank;
  /** total number of ways of getting the possible sums */
  private final double totalNumWays;
  /** return the total number of ways of getting the possible sums */
  public double getTotalNumWays()
  {
    return totalNumWays;
  }
  /** for debugging, produce the theoretical total number of ways.
      This is just sortedRanks.length CHOSE numRanks
      */
  public double getTheoreticalNumWays()
  {
    double logAnswer = LogFact.lF(sortedRanks.length) -
      LogFact.lF(numRanks) - LogFact.lF(sortedRanks.length - numRanks);
    return Math.exp(logAnswer);
  }
  /** return the mean score */
  public double getMeanScore()
  {
    double sofar = 0.0;
    for (int i = 0; i < numWaysRank.length; i++)
    {
      double score = i + minSum;
      sofar += score * numWaysRank[i];
    }
    return sofar / totalNumWays;
  }
  /** return the theoretical mean score */
  public double getMeanTheoryScore()
  {
    double totalRank = 0.0;
    for (int i = 0; i < sortedRanks.length; i++)
    {
      totalRank += sortedRanks[i];
    }
    return totalRank * numRanks / sortedRanks.length;
  }
  /** return the mean squared score, mostly for checking */
  public double getMeanSqScore()
  {
    double sofar = 0.0;
    for (int i = 0; i < numWaysRank.length; i++)
    {
      double score = i + minSum;
      sofar += score * score * numWaysRank[i];
    }
    return sofar / totalNumWays;
  }
  /** return the theoretical mean squared score */
  public double getMeanSqTheoryScore()
  {
    // Use that fact that E(a + b) = E(a) + E(b)
    // (SUM_i Xi)^2 = SUM_i Xi^2 + 2SUM_i,j XiXj
    double total = 0.0;
    // probability of each particular squared term turning up
    final double probEach = numRanks / (double)sortedRanks.length;
    // Work out contribution from squared terms
    for (int i = 0; i < sortedRanks.length; i++)
    {
      double contrib = sortedRanks[i];
      total += contrib * contrib * probEach;
    }
    // probability of i,j term turning up
    double probPair = 2.0 * numRanks * (numRanks - 1) /
      (sortedRanks.length * (sortedRanks.length - 1.0));
    for (int i = 1; i < sortedRanks.length; i++)
    {
      double ti = sortedRanks[i] * probPair;
      for (int j = 0; j < i; j++)
      {
        total += ti * sortedRanks[j];
      }
    }
    return total;
  }
  /** turn scores into integer ranks. Fill in set of
      ranks and return total rank of A side. Uses 0, 2, 4...
      counting of ranks if required to cope with ties.
      */
  public static int toIntegerRanks(double[] aSide, double[] bSide,
    int[] forRanks, boolean[] usesDoubleCounting)
  {
    double[] together = new double[aSide.length + bSide.length];
    System.arraycopy(aSide, 0, together, 0, aSide.length);
    System.arraycopy(bSide, 0, together, aSide.length, bSide.length);
    Arrays.sort(together);
    boolean usingDouble = false;
    // turn into ranks, assuming ties
    for (int i = 0; i < together.length;)
    {
      double val = together[i];
      // Find region of ties
      int j = i + 1;
      for (; j < together.length; j++)
      {
        if (together[j] != val)
	{
	  break;
	}
      }
      int rank = i + j - 1;
      if ((rank & 1) != 0)
      {
        usingDouble = true;
      }
      for (int k = i; k < j; k++)
      {
        forRanks[k] = rank;
      }
      i = j;
    }
    if (usingDouble)
    {
      usesDoubleCounting[0] = true;
    }
    else
    {
      usesDoubleCounting[0] = false;
      for (int i = 0; i < together.length; i++)
      {
        forRanks[i] = forRanks[i] / 2;
      }
    }
    // now work out total score
    aSide = aSide.clone();
    Arrays.sort(aSide);
    int p = 0;
    int total = 0;
    for (int i = 0; i < aSide.length; i++)
    {
      while (together[p] < aSide[i])
      {
        p++;
      }
      total += forRanks[p];
    }
    return total;
  }
  /** probability of getting &le; the corresponding value */
  private final double[] probLe;
  /** probability of getting &ge; the corresponding value */
  private final double[] probGe;
  /** work out confidence limits and true coverage. Works out values
   range[0], range[1] such that the probability of x being
   range[0] &lt; x &lt; range[1] is at least probWithinRequired and
   puts actual probabilities of being outside to the left and right
   in actualTail
   */
  public void confidence(double probWithinRequired, 
    int[] range, double[] actualTail)
  {
    double targetLt = (1.0 - probWithinRequired) / 2.0;
    targetLt *= totalNumWays;
    // Slow but sure for now
    double probToLeft = numWaysRank[0];
    int n1 = numWaysRank.length - 1;
    int n2 = n1 - 1;
    for (int i = 0; i < n1; i++)
    {
      double newProb = probToLeft + numWaysRank[i + 1];
      if ((newProb > targetLt) || (i == n2))
      {
	range[0] = i + minSum;
	actualTail[0] = probToLeft / totalNumWays;
        break;
      }
      probToLeft = newProb;
    }
    double targetGt = 1.0 - probWithinRequired - actualTail[0];
    targetGt *= totalNumWays;
    double probToRight = numWaysRank[numWaysRank.length - 1];
    for (int i = numWaysRank.length - 1; i > 0; i--)
    {
      double newProb = probToRight + numWaysRank[i - 1];
      if ((newProb > targetGt) || (i == 1))
      {
        range[1] = i + minSum;
	actualTail[1] = probToRight / totalNumWays;
	break;
      }
      probToRight = newProb;
    }
  }
  /** display a MannWhitney and run some consistency checks */
  private static void showCheck(MannWhitney mw)
  {
      double totalNumWays = mw.getTotalNumWays();
      double theory = mw.getTheoreticalNumWays();
      double td = theory - totalNumWays;
      System.out.println("Total " + totalNumWays + " in theory " +
        theory + " diff " + td);
      double parts = Math.abs(td / theory);
      if ((Math.abs(td) > 1.0E-1) && (parts > 1.0E-6))
      {
        throw new IllegalStateException("Total " + totalNumWays +
	  " in theory " + theory + " diff " + td);
      }
      double ms = mw.getMeanScore();
      double mts = mw.getMeanTheoryScore();
      double mtd = Math.abs(ms - mts);
      System.out.println("mean score " + ms + " in theory " + mts);
      if (mtd > 1.0E-6)
      {
        throw new IllegalStateException("mean score " + ms +
	  " in theory " + mts);
      }
      double msq = mw.getMeanSqScore();
      double mtsq = mw.getMeanSqTheoryScore();
      double mtsqd = Math.abs(msq - mtsq);
      System.out.println("mean square score " + msq + " in theory " +
        mtsq);
      if (mtsqd > 1.0E-6)
      {
        throw new IllegalStateException("mean square score " + msq +
	  " in theory " + mtsq);
      }
  }
  /** compute the rank score of the second array in the group
      formed by merging the two arrays
   */
  public static double scoreArrays(double[] first, double[] scoreThis)
  { 
    int[] forRanks = new int[first.length + scoreThis.length];
    boolean[] hasTies = new boolean[2];
    int intResult = toIntegerRanks(scoreThis, first, forRanks,
      hasTies);
    if (hasTies[0])
    {
      return intResult / 2.0;
    }
    else
    {
      return intResult;
    }
  }
  /** need Memo key for getSig */
  public static class SigMemoKey
  {
    private final int[] ranks;
    private final int numRanks;
    private final int hash;
    private SigMemoKey(int[] forRanks, int forNumRanks)
    {
      ranks = forRanks.clone();
      numRanks = forNumRanks;
      hash = numRanks * 131 + Arrays.hashCode(ranks);
    }
    @Override
    public boolean equals(Object o)
    {
      if (!(o instanceof SigMemoKey))
      {
        return false;
      }
      SigMemoKey other = (SigMemoKey)o;
      if (other.numRanks != numRanks)
      {
        return false;
      }
      return Arrays.equals(ranks, other.ranks);
    }
    @Override
    public int hashCode()
    {
      return hash;
    }
  }
  /** Work out the rank score for the b side and its significance
      given the values as two arrays. Writes score to forScore[0].
      If memo is not null, uses this to try and avoid recomputing
      the significance
      */
  public static SigProb getSig(double[] ad, double[] bd, 
    double[] forScore, Map<SigMemoKey, MannWhitney> memo)
  { 
    int[] forRanks = new int[ad.length + bd.length];
    boolean[] hasTies = new boolean[2];
    int intResult = toIntegerRanks(bd, ad, forRanks,
      hasTies);
    if (hasTies[0])
    {
      forScore[0] = intResult / 2.0;
    }
    else
    {
      forScore[0] = intResult;
    }
    MannWhitney mw;
    if (memo == null)
    {
      mw = new MannWhitney(forRanks, bd.length);
    }
    else
    {
      SigMemoKey key = new SigMemoKey(forRanks, bd.length);
      mw = memo.get(key);
      if (mw == null)
      {
        mw = new MannWhitney(forRanks, bd.length);
	memo.put(key, mw);
      }
    }
    int offset = intResult - mw.minSum;
    double probEq = mw.numWaysRank[offset] / mw.totalNumWays;
    double probLt;
    if (offset == 0)
    {
      probLt = 0.0;
    }
    else
    {
      probLt = mw.probLe[offset - 1];
    }
    double probGt;
    if (offset == (mw.probGe.length - 1))
    {
      probGt = 0.0;
    }
    else
    {
      probGt = mw.probGe[offset + 1];
    }
    return new SigProb(probLt, probEq, probGt);
  }
  /** display and compute rank sum */
  private static void sumDisplay(double[] first, double[] scoreThis)
  {
    System.out.println("First: " + Arrays.toString(first));
    System.out.println("ScoreThis: " + Arrays.toString(scoreThis));
    System.out.println("Score: " + scoreArrays(first, scoreThis));
  }
  /** Here we want to count the number of differences of the form
  Ai - Bj that are &lt;, &eq;, or &gt; the argument. The two input
  arrays must be sorted. The cost of this pass is at most a.length +
  b.length, because we move pointers over both arrays only forwards
  */
  public static void countDifferences(double[] a, double[] b,
    double aMinusB, int[] forCount)
  {
    if ((a.length <= 0) || (b.length <= 0))
    { // no differences to count
      forCount[0] = 0;
      forCount[1] = 0;
      forCount[2] = 0;
      return;
    }
    // First find p and q such that a[p] - b[0] < aMinusB and
    // a[q] - b[0] > aMinusB and any intermediate values x have
    // a[x] - b[0] = aMinus
    int p = -1;
    int q = a.length;
    double b0 = b[0];
    for (int i = 0; i < a.length; i++)
    {
      double v = a[i] - b0;
      if (v < aMinusB)
      {
        p = i;
      }
      else if (v > aMinusB)
      {
        q = i;
	break;
      }
    }
    // Set count for differences of form a[.] - b[0]
    forCount[0] = p + 1;
    forCount[1] = q - p - 1;
    forCount[2] = a.length - q;
    // System.out.println("ForCount " + Arrays.toString(forCount));
    int al1 = a.length - 1;
    for (int i = 1; i < b.length; i++)
    {
      double bi = b[i];
      // adjust pointers. B has not decreased, so we may
      // be able to move p and q to the right
      while (p < al1)
      {
        if ((a[p + 1] - bi) < aMinusB)
	{
	  p++;
	}
	else
	{
	  break;
	}
      }
      while (q < a.length)
      {
        if ((a[q] - bi) <= aMinusB)
	{
	  q++;
	}
	else
	{
	  break;
	}
      }
      forCount[0] = forCount[0] + p + 1;
      forCount[1] = forCount[1] + q - p - 1;
      forCount[2] = forCount[2] + a.length - q;
      // System.out.println("ForCount " + Arrays.toString(forCount));
    }
  }
  /** here we want a random sample from the differences a[i] - b[j]
      between two points, and not equal to either of them. As before,
      a and b must be sorted, and the cost is linear in the input size.
    */
  private static double getRandomDifference(double[] a, double[] b,
    double tooLow, double tooHigh, Random r)
  {
    // System.out.println("GetRandom A " + Arrays.toString(a));
    // System.out.println("GetRandom B " + Arrays.toString(b));
    // System.out.println("Low " + tooLow + " high " + tooHigh);
    if ((a.length <= 0) || (b.length <= 0))
    {
      throw new NoSuchElementException("No elements");
    }
    // First find p and q to mark points just to the left
    // and right of the range of possible valus of a[i] in
    // a[i] - b[0]
    int p = -1;
    int q = a.length;
    double b0 = b[0];
    for (int i = 0; i < a.length; i++)
    {
      double v = a[i] - b0;
      if (v <= tooLow)
      {
        p = i;
      }
      else if (v >= tooHigh)
      {
        q = i;
	break;
      }
    }
    int numSoFar = q - p - 1;
    double result = 0.0;
    if (numSoFar > 0)
    {
      result = a[p + 1 + r.nextInt(numSoFar)] - b0;
    }
    int a1 = a.length - 1;
    for (int i = 1; i < b.length; i++)
    {
      double bi = b[i];
      // first adjust p and q. b[i] has not decreased, so we
      // may be able to move them right
      while (p < a1)
      {
        if ((a[p + 1] - bi) <= tooLow)
	{
	  p++;
	}
	else
	{
	  break;
	}
      }
      while (q < a.length)
      {
        if ((a[q] - bi) < tooHigh)
	{
	  q++;
	}
	else
	{
	  break;
	}
      }
      int numHere = q - p - 1;
      if (numHere <= 0)
      { // nothing in range here
        continue;
      }
      // make random unbiased choice of result from
      // range 0..numSofar-1/numSofar..numSofar + numHere - 1
      // and use that to accept a value from the new range or not
      numSoFar += numHere;
      int choice = r.nextInt(numSoFar);
      if (choice >= numHere)
      { // reject
        continue;
      }
      if (numSoFar > 0)
      {
	result = a[p + 1 + choice] - bi;
      }
    }
    if (numSoFar <= 0)
    {
      throw new NoSuchElementException("No elements");
    }
    return result;
  }
  /** Get element of Ai - Bj in given rank order, counting from 0. Uses
    randomised algorithm, hence random number. */
  public static double aMinusBRank(double[] a, double[] b, 
    int rankFromZero, Random r)
  {
    // Might as well copy and sort a and b, since this is going to take
    // time about n log n anyway
    final double[] aa = a.clone();
    Arrays.sort(aa);
    final double[] bb = b.clone();
    Arrays.sort(bb);
    return aMinusBRankSorted(aa, bb, rankFromZero, r);
  }
  /** Get element of Ai - Bj in given rank order, counting from 0. Uses
    randomised algorithm, hence random number. Assume aa and bb sorted */
  private static double aMinusBRankSorted(double[] aa, double[] bb,
    int rankFromZero, Random r)
  {
    // smallest element of Ai - Bj
    // System.out.println("Want rank " + rankFromZero);
    double low = aa[0] - bb[bb.length - 1];
    int[] counts = new int[3];
    countDifferences(aa, bb, low, counts);
    // System.out.println("Counts " + Arrays.toString(counts));
    if (rankFromZero < (counts[0] + counts[1]))
    { // want smallest element
      return low;
    }
    // highest element
    double high = aa[aa.length - 1] - bb[0];
    countDifferences(aa, bb, high, counts);
    // System.out.println("Counts " + Arrays.toString(counts));
    if (rankFromZero >= counts[0])
    { // want highest element
      return high;
    }
    for (;;)
    {
      if (low >= high)
      { // range contains only a single value
        return low;
      }
      // get random difference within range. Since this is a random
      // point within the range, on average we cut the number of points
      // within the range by a big enough factor each time round that
      // we go round only log n times
      final double probe = getRandomDifference(aa, bb, low, high, r);
      // System.out.println("probe " + probe + " for " + rankFromZero);
      // and work out counts for it
      countDifferences(aa, bb, probe, counts);
      // System.out.println("Counts " + Arrays.toString(counts));
      if (counts[0] <= rankFromZero)
      { // probe was not too high
        if ((counts[0] + counts[1]) > rankFromZero)
	{
	  return probe;
	}
	low = probe;
	continue;
      }
      high = probe;
    }
  }
  /** Slow version of aMinusRank, for debugging: computes all n^2 
   *  differences, sorts, and returns the requested element of the 
   *  sorted array
   */
  public static double n2AMinusBRank(double[] a, double[] b, 
    int rankFromZero)
  {
    double[] diffs = new double[a.length * b.length];
    int wp = 0;
    for (int i = 0; i < a.length; i++)
    {
      double ai = a[i];
      for (int j = 0; j < b.length; j++)
      {
        diffs[wp++] = ai - b[j];
      }
    }
    Arrays.sort(diffs);
    return diffs[rankFromZero];
  }
  /** test difference ranking */
  private static void testDifference(int maxALen, int maxBLen,
    Random forGeneration, Random forSampling, Swatch fast, Swatch slow)
  {
    double[] a = new double[forGeneration.nextInt(maxALen) + 1];
    for (int i = 0; i < a.length; i++)
    {
      a[i] = forGeneration.nextGaussian();
    }
    int pairs = forGeneration.nextInt(a.length);
    for (int i = 0; i < pairs; i++)
    {
      a[forGeneration.nextInt(a.length)] = a[forGeneration.nextInt(a.length)];
    }
    double[] b = new double[forGeneration.nextInt(maxBLen) + 1];
    for (int i = 0; i < b.length; i++)
    {
      b[i] = forGeneration.nextGaussian();
    }
    pairs = forGeneration.nextInt(b.length);
    for (int i = 0; i < pairs; i++)
    {
      b[forGeneration.nextInt(b.length)] = b[forGeneration.nextInt(b.length)];
    }
    int rank = forGeneration.nextInt(a.length * b.length);
    fast.start();
    double ourValue = aMinusBRank(a, b, rank, forSampling);
    fast.stop();
    slow.start();
    double slowValue = n2AMinusBRank(a, b, rank);
    slow.stop();
    if (ourValue != slowValue)
    {
      System.err.println("A: " + Arrays.toString(a));
      System.err.println("B: " + Arrays.toString(a));
      System.err.println("Rank " + rank + " fast " + ourValue + " slow " +
        slowValue);
      throw new IllegalStateException("Mismatch");
    }
  }
  /** ranks and result of location confidence interval depend only on
      Alen, Blen, and confidence required, so use memo-isation */
  public static class MemoKey
  {
    private final int aSize;
    private final int bSize;
    private final double confidence;
    private final int hash;
    private MemoKey(int a, int b, double c)
    {
      aSize = a;
      bSize = b;
      confidence = c;
      hash = (aSize + bSize * 131) ^ 
        (int)(131 * Double.doubleToLongBits(c));
    }
    @Override
    public int hashCode()
    {
      return hash;
    }
    @Override
    public boolean equals(Object o)
    {
      if (!(o instanceof MemoKey))
      {
        return false;
      }
      MemoKey other = (MemoKey) o;
      if (aSize != other.aSize)
      {
	return false;
      }
      if (bSize != other.bSize)
      {
	return false;
      }
      return confidence == other.confidence;
    }
  }
  /** result of memo-ised calculation */
  public static class MemoResult
  {
    private int[] ranks;
    private double[] tails;
  }
  /** create Memo table for setLocationTails Memo */
  public static Map<MemoKey, MemoResult> createLocationMemo()
  {
    return new HashMap<MemoKey, MemoResult>();
  }
  /** Memoised version of: Given two samples of size aLen and bLen 
    from the same distribution set ranks[0] and ranks[1] to ranks, 
    counting from zero, such that Ai - Bj at some rank within ranks[0] 
    and ranks[1] is likely to include 0. We are assuming no ties. 
    Memo argument can be null or the result of createLocationMemo(),
    in which case it accumulates information over calls that reduces
    the number of times it has to do an expensive significance
    calculation
    */
  public static void setLocationTails(int aLen, int bLen, 
    int[] ranks, double[] tails, double probWithinRequired,
    Map<MemoKey, MemoResult> memo)
  {
    MemoKey mk = new MemoKey(aLen, bLen, probWithinRequired);
    if (memo != null)
    {
      MemoResult mr = memo.get(mk);
      if (mr != null)
      {
        ranks[0] = mr.ranks[0];
        ranks[1] = mr.ranks[1];
	tails[0] = mr.tails[0];
	tails[1] = mr.tails[1];
	return;
      }
    }
    setLocationTailsInner(aLen, bLen, ranks, tails, probWithinRequired);
    if (memo != null)
    {
      // System.out.println("Put for " + aLen + ", " + bLen + ", " +
      //   probWithinRequired);
      MemoResult mr = new MemoResult();
      mr.ranks = new int[] {ranks[0], ranks[1]};
      mr.tails = new double[] {tails[0], tails[1]};
      memo.put(mk, mr);
    }
  }
  /** Given two samples of size aLen and bLen from the same distribution
      set ranks[0] and ranks[1] to ranks, counting from zero, such that
      Ai - Bj at some rank within ranks[0] and ranks[1] is likely to
      include 0. We are assuming no ties. The confidence intervals are
      conservative in the presence of ties, by continuity. */
  private static void setLocationTailsInner(int aLen, int bLen,
    int[] ranks, double[] tails, double probWithinRequired)
  {
    // System.out.println("Location tails " + aLen + " and " + bLen);
    // get confidence interval for Mann-Whitney rank sum scores
    int[] allRanks = new int[aLen + bLen];
    for (int i = 0; i < allRanks.length; i++)
    {
      allRanks[i] = i;
    }
    MannWhitney mw = new MannWhitney(allRanks, aLen);
    mw.confidence(probWithinRequired, ranks, tails);
    // Then Mann-Whitney rank sum score for a, counting ranks from
    // zero, is the sum, over all a, of the number of other elements
    // less than it (we have outlawed ties). This is the number of such
    // comparisons within a plus the number of terms of the form Ai - Bi
    // that are greater than zero. So turn this into a confidence 
    // interval on counts of +ve Ai - Bi by subtracting comparisons 
    // within A
    int winsWithinA = (aLen * (aLen - 1)) / 2;
    ranks[0] -= winsWithinA;
    ranks[1] -= winsWithinA;
  }
  /** given two arrays of samples and and b, return a confidence interval 
      for the location difference a - b and upper bound tail estimates */
  public static void locationRange(double[] a, double[] b, double[] interval,
    double[] tails, Random r, double probWithinRequired,
    Map<MemoKey, MemoResult> memo)
  {
    // We presume that a and b are two samples from a common distribution,
    // different only by a location shift. Pretend that we break ties
    // by adding random fuzz. Under this hypothesis the Mann-Whitney test
    // gives us a distribution on the Mann-Whitney statistic which we can
    // translate as a distribution on the place of 0 in the differences
    // Ai - Bj with the location shift taken away. Suppose that we find
    // out that 0 is likely to end up between rank x and rank y of the
    // true Ai - Bj. Then if, in the real data, rank x of Ai - Bj is xV
    // and rank y of Ai - Bj is Yv, the location difference a - b must
    // be between xV and Yv
    int[] ranks = new int[2];
    setLocationTails(a.length, b.length, ranks, tails,
      probWithinRequired, memo);
    final double[] aa = a.clone();
    Arrays.sort(aa);
    final double[] bb = b.clone();
    Arrays.sort(bb);
    interval[0] = aMinusBRankSorted(aa, bb, ranks[0], r);
    interval[1] = aMinusBRankSorted(aa, bb, ranks[1], r);
  }
  /** produce median of A minus B, as a measure of location */
  public static double medianAMinusB(double[] a, double[] b,
    Random r)
  {
    final double[] aa = a.clone();
    Arrays.sort(aa);
    final double[] bb = b.clone();
    Arrays.sort(bb);
    return aMinusBRankSorted(aa, bb, (aa.length * bb.length) / 2, r);
  }
  /** Run significance tests with drop outs */
  private static void checkSig(Random forGen, int aLen, int bLen,
    double sd, double shift, int numTests, int numDrops)
  {
    System.out.println("CheckSig aLen " + aLen + " blen " + bLen +
      " drops " + numDrops + " sd " + sd + " shift " + shift);
    double[] allTails = new double[numTests];
    Map<SigMemoKey, MannWhitney> memo =
      new HashMap<SigMemoKey, MannWhitney>();
    Deviant testDifference = new Deviant();
    Deviant ad = new Deviant();
    Deviant bd = new Deviant();
    for (int i = 0; i < numTests; i++)
    {
      int aHere = aLen;
      int bHere = bLen;
      for (int j = 0; j < numDrops; j++)
      {
        if (forGen.nextInt(2) == 0)
	{
	  aHere++;
	}
	else
	{
	  bHere++;
	}
      }
      double[] aSamples = new double[aHere];
      double[] bSamples = new double[bHere];
      for (int j = 0; j < aLen; j++)
      {
        aSamples[j] = forGen.nextGaussian() * sd;
      }
      for (int j = 0; j < bLen; j++)
      {
        bSamples[j] = forGen.nextGaussian() * sd + shift;
      }
      for (int j = aLen; j < aHere; j++)
      {
        aSamples[j] = LARGE;
      }
      for (int j = bLen; j < bHere; j++)
      {
        bSamples[j] = LARGE;
      }
      Deviant d1 = new Deviant();
      for (int j = 0; j < aSamples.length; j++)
      {
        d1.sample(aSamples[j]);
        ad.sample(aSamples[j]);
      }
      Deviant d2 = new Deviant();
      for (int j = 0; j < bSamples.length; j++)
      {
        d2.sample(bSamples[j]);
        bd.sample(bSamples[j]);
      }
      double t = (d1.getMean() - d2.getMean()) /
        Math.sqrt(d1.getVariance() / aHere + d2.getVariance() / bHere);
      testDifference.sample(t);
      double[] score = new double[1];
      SigProb sp = getSig(aSamples, bSamples, score, memo);
      // System.out.println("Score " + score[0] + " sig " + sp);
      double lt = sp.getLt();
      double gt = sp.getGt();
      double tail;
      if (lt < gt)
      {
        tail = sp.getEq() + lt;
      }
      else
      {
        tail = sp.getEq() + gt;
      }
      // Bonferroni correction as don't predict direction
      allTails[i] = tail * 2.0;
    }
    Arrays.sort(allTails);
    System.out.println("Median tail probability is " + 
      allTails[allTails.length / 2]);
    System.out.println("95% tail probability is " + 
      allTails[allTails.length - (int) (allTails.length / 20)]);
    System.out.println("TestDifference " + testDifference);
    System.out.println("Ad " + ad + " bd " + bd);
  }
  /** Check confidence interval for locations against Monte Carlo */
  private static void checkConfidenceLocation(Random forGen, 
    Random forSample, int aLen, int bLen, int numSamples, 
    double confidence)
  {
    double[] aValues = new double[aLen];
    double[] bValues = new double[bLen];
    // take values from random pool to thrown in some ties
    double[] randomPool = new double[aLen + bLen];
    int toLeft = 0;
    int within = 0;
    int toRight = 0;
    double[] interval = new double[2];
    double[] trueTails = new double[2];
    Deviant d = new Deviant();
    Map<MemoKey, MemoResult> memo = new HashMap<MemoKey, MemoResult>();
    for (int i = 0; i < numSamples; i++)
    {
      double trueAMinusB = forGen.nextGaussian();
      if (false)
      { // don't use ties at the moment so we can match up the
	// claimed and achieved confidence level (ties make it
	// conservative)
	for (int j = 0; j < randomPool.length; j++)
	{
	  randomPool[j] = forGen.nextGaussian();
	}
	for (int j = 0; j < aLen; j++)
	{
	  aValues[j] = randomPool[forGen.nextInt(randomPool.length)] +
	    trueAMinusB;
	}
	for (int j = 0; j < bLen; j++)
	{
	  bValues[j] = randomPool[forGen.nextInt(randomPool.length)];
	}
      }
      else
      {  // just fill with random values - should get exact probability
         // if no ties (else is conservative)
	for (int j = 0; j < aLen; j++)
	{
	  aValues[j] = forGen.nextGaussian() +
	    trueAMinusB;
	}
	for (int j = 0; j < bLen; j++)
	{
	  bValues[j] = forGen.nextGaussian();
	}
      }
      locationRange(aValues, bValues, interval, trueTails, forSample,
        confidence, memo);
      if (trueAMinusB < interval[0])
      {
        toLeft++;
      }
      else if (trueAMinusB > interval[1])
      {
        toRight++;
      }
      else
      {
        within++;
      }
      d.sample(interval[1] - interval[0]);
    }
    System.out.println("True tails " + Arrays.toString(trueTails));
    double left = (toLeft + 1.0) / (numSamples + 3.0);
    double centre = (within + 1.0) / (numSamples + 3.0);
    double right = (toRight + 1.0) / (numSamples + 3.0);
    System.out.println("Estimated left " + left + " centre " + centre +
      " right " + right);
    System.out.println("Confidence Interval widths: " + d);
  }
  /** use this to signal drop out */
  private static final double LARGE = 1.0e6;
  /** This version of checkConfidenceLocation simulates dropouts
      scored as maximal values */
  private static void checkConfidenceLocationDropOut(Random forGen, 
    Random forSample, int aLen, int bLen, int numSamples, 
    double confidence, int aDropsContrib, int bDropsContrib)
  {
    System.out.println("Alen " + aLen + " drops contrib " + aDropsContrib);
    System.out.println("Blen " + bLen + " drops contrib " + bDropsContrib);
    Map<MemoKey, MemoResult> memo = new HashMap<MemoKey, MemoResult>();
    // take values from random pool to thrown in some ties
    double[] randomPool = new double[aLen + bLen];
    int toLeft = 0;
    int within = 0;
    int toRight = 0;
    double[] interval = new double[2];
    double[] trueTails = new double[2];
    Deviant d = new Deviant();
    Deviant medianD = new Deviant();
    // count of failures, when the large value used to signal a
    // dropout emerges as part of the confidence interval
    int failures = 0;
    for (int i = 0; i < numSamples; i++)
    {
      int aDrops = 0;
      int bDrops = 0;
      int totalContrib = aDropsContrib + bDropsContrib;
      for (int j = 0; j < totalContrib; j++)
      {
        if (forGen.nextInt(2) == 0)
	{
	  aDrops++;
	}
	else
	{
	  bDrops++;
	}
      }
      double[] aValues = new double[aLen + aDrops];
      double[] bValues = new double[bLen + bDrops];
      double trueAMinusB = forGen.nextGaussian();
      {  // just fill with random values - should get exact probability
         // if no ties (else is conservative)
	for (int j = 0; j < aLen; j++)
	{
	  aValues[j] = forGen.nextGaussian() +
	    trueAMinusB;
	}
	for (int j = 0; j < aDrops; j++)
	{
	  aValues[aLen + j] = LARGE;
	}
	for (int j = 0; j < bLen; j++)
	{
	  bValues[j] = forGen.nextGaussian();
	}
	for (int j = 0; j < bDrops; j++)
	{
	  bValues[bLen + j] = LARGE;
	}
      }
      medianD.sample(medianAMinusB(aValues, bValues, forSample));
      locationRange(aValues, bValues, interval, trueTails, forSample,
        confidence, memo);
      if ((Math.abs(interval[0]) > 100.0) || 
          (Math.abs(interval[1]) > 100.0))
      {
        failures++;
	continue;
      }
      if (trueAMinusB < interval[0])
      {
        toLeft++;
      }
      else if (trueAMinusB > interval[1])
      {
        toRight++;
      }
      else
      {
        within++;
      }
      d.sample(interval[1] - interval[0]);
    }
    System.out.println("Failures: " + failures + " of " + numSamples);
    System.out.println("True tails " + Arrays.toString(trueTails));
    double left = (toLeft + 1.0) / (numSamples + 3.0);
    double centre = (within + 1.0) / (numSamples + 3.0);
    double right = (toRight + 1.0) / (numSamples + 3.0);
    System.out.println("Estimated left " + left + " centre " + centre +
      " right " + right);
    System.out.println("Confidence Interval widths: " + d);
    System.out.println("Median estimates: " + medianD);
  }
  /** sort both arrays in place and compute rank */
  public static void main(String[] s)
  {
    Random gen = new Random(42);
    Swatch fast = new Swatch();
    Swatch slow = new Swatch();
    for (int i = 0; i < 10000; i++)
    {
      Random sample = new Random(1000);
      testDifference(100, 100, gen, sample, fast, slow);
    }
    System.out.println("Fast " + fast);
    System.out.println("Slow " + slow);
    for (int i = 1; i <= 50; i++)
    {
      int[] ranks = new int[i * 2];
      for (int j = 0; j < ranks.length; j++)
      {
        ranks[j] = j;
      }
      MannWhitney mw = new MannWhitney(ranks, i);
      int[] range = new int[2];
      double[] actual = new double[2];
      mw.confidence(0.95, range, actual);
      System.out.println("95% for 2x" + i);
      System.out.println(Arrays.toString(range));
      System.out.println(Arrays.toString(actual));
      int sub = i * (i - 1) / 2;
      System.out.println("U: " + (range[0] - sub) + ", " +
        (range[1] - sub));
      setLocationTails(i, i, range, actual, 0.95, null);
      System.out.println("Ranks for Ai -Bj are " + Arrays.toString(range) +
        " actual " + Arrays.toString(actual));
      showCheck(mw);
      // Check the asymmetric case
      int other = i + 5;
      if (other >= ranks.length)
      {
        other = ranks.length;
      }
      MannWhitney mw2 = new MannWhitney(ranks, other);
      showCheck(mw2);
    }
    sumDisplay(new double[] {1.0, 2.0, 3.0},
      new double[] {4.0, 5.0, 6.0});
    sumDisplay(new double[] {10.0, 12.0, 13.0},
      new double[] {4.0, 5.0, 6.0});
    sumDisplay(new double[] {1.0, 3.0, 5.0, 7.0},
      new double[] {2.0, 4.0, 6.0});
    sumDisplay(new double[] {1.0, 1.0, 1.0},
      new double[] {1.0, 1.0, 1.0, 1.0});
    sumDisplay(new double[] {1.0, 3.0, 3.0, 5.0, 6.0, 7.0, 7.0, 8.0},
      new double[] {2.0, 4.0, 6.0, 7.0, 7.0, 10.0});
    // checkConfidenceLocationDropOut(new Random(778), new Random(42),
    //   60, 40, 100000, 0.95, 0, 0);
    // checkConfidenceLocationDropOut(new Random(778), new Random(42),
    //   50, 50, 100000, 0.95, 0, 0);
    // checkConfidenceLocationDropOut(new Random(778), new Random(42),
    //    47, 47, 100000, 0.95, 0, 0);
    // checkConfidenceLocationDropOut(new Random(778), new Random(42),
    //   44, 44, 100000, 0.95, 6, 6);
    // checkConfidenceLocationDropOut(new Random(778), new Random(42),
    //   47, 47, 100000, 0.95, 6, 6);
    // checkSig(new Random(432), 47, 47, 0.25, 0.2, 1000000, 0);
    checkSig(new Random(432), 55, 55, 0.25, 0.2, 1000000, 0);
    // checkSig(new Random(432), 50, 50, 0.25, 0.0, 1000000, 0);
  }
}
