package uk.co.demon.mcdowella.stats;

import java.util.Arrays;
import java.util.Random;

/** Test/experiment harness. Generate probabilities for log odds
 *  based probabilities. Generate test data and work out significance
 *  with MultiBin
 */
public class MbTest
{
  /** work out probabilities given log odds for bit differences of
   *  0,1,2,3
   */
  private static void setUpProbs(double prob, int maxDiff,
    double[] probOut)
  {
    double odds = prob / (1.0 - prob);
    double now = 1.0;
    for (int i = 0; i <= maxDiff; i++)
    {
      probOut[i] = now / (1.0 + now);
      now *= odds;
    }
  }
  /** produce a random table using comparisons at random */
  private static void fillInDiffs(Random r, int numCompares,
    int[] comparisons, int[] diffs, double probs[], int mask)
  {
    Arrays.fill(diffs, 0);
    int maskCount = Integer.bitCount(mask);
    int choices = comparisons.length / 2;
    for (int i = 0; i < numCompares; i++)
    {
      // choose a comparison
      int diff = r.nextInt(choices);
      int offset = diff * 2;
      // which bits differ between two sides of comparison and
      // actually matter
      // System.out.println("Compare " + comparisons[offset] + " and " +
      //   comparisons[offset + 1] + " mask " + mask);
      int changed = mask & (comparisons[offset] ^ comparisons[offset + 1]);
      // System.out.println("Changed " + changed);
      // bits achieved by lhs - mask with changed not mask because
      // we want to compare bits set this side with bits under change
      int lo = changed & comparisons[offset];
      // bits on lo side - bits on hi side
      int bitDiff = Integer.bitCount(lo) * 2 - Integer.bitCount(changed);
      // System.out.println("bitDiff " + bitDiff);
      double p;
      if (bitDiff < 0)
      {
	bitDiff = -bitDiff;
	p = 1.0 - probs[bitDiff];
      }
      else
      {
	p = probs[bitDiff];
      }
      // System.out.println("p " + p);
      if (r.nextDouble() < p)
      {
	diffs[offset]++;
      }
      else
      {
	diffs[offset + 1]++;
      }
    }
  }
  /** produce a random table using even comparisons */
  private static void fillInEvenDiffs(Random r, int numCompares,
    int[] comparisons, int[] diffs, double probs[], int mask)
  {
    Arrays.fill(diffs, 0);
    int maskCount = Integer.bitCount(mask);
    int choices = comparisons.length / 2;
    int perSlot = numCompares / choices;
    for (int diff = 0; diff < choices; diff++)
    {
      // choose a comparison
      int offset = diff * 2;
      // which bits differ between two sides of comparison and
      // actually matter
      // System.out.println("Compare " + comparisons[offset] + " and " +
      //   comparisons[offset + 1] + " mask " + mask);
      int changed = mask & (comparisons[offset] ^ comparisons[offset + 1]);
      // System.out.println("Changed " + changed);
      // bits achieved by lhs - mask with changed not mask because
      // we want to compare bits set this side with bits under change
      int lo = changed & comparisons[offset];
      // bits on lo side - bits on hi side
      int bitDiff = Integer.bitCount(lo) * 2 - Integer.bitCount(changed);
      // System.out.println("bitDiff " + bitDiff);
      double p;
      if (bitDiff < 0)
      {
	bitDiff = -bitDiff;
	p = 1.0 - probs[bitDiff];
      }
      else
      {
	p = probs[bitDiff];
      }
      for (int j = 0; j < perSlot; j++)
      {
	// System.out.println("p " + p);
	if (r.nextDouble() < p)
	{
	  diffs[offset]++;
	}
	else
	{
	  diffs[offset + 1]++;
	}
      }
    }
  }
  /** single factor comparisons */
  private static int[] ONE = {0, 1};
  /** comparisons for all 2-factors */
  private static int[] ALL2 = {0, 1, 0, 2, 0, 3, 1, 2, 1, 3, 2, 3};
  /** comparisons for density 1 2-factors */
  private static int[] SINGLE2 = {0, 1, 0, 2, 1, 3, 2, 3};
  /** comparisons for all 3-factors */
  private static int[] ALL3 = {
    0, 1, 0, 2, 0, 3, 0, 4, 0, 5, 0, 6, 0, 7,
          1, 2, 1, 3, 1, 4, 1, 5, 1, 6, 1, 7,
	        2, 3, 2, 4, 2, 5, 2, 6, 2, 7,
		      3, 4, 3, 5, 3, 6, 3, 7,
		            4, 5, 4, 6, 4, 7,
			          5, 6, 5, 7,
				        6, 7};
  /** comparisons for density 1 3-factors */
  private static int[] SINGLE3 = {
    0, 1, 0, 2, 0, 4,
    1, 3, 1, 5, 2, 3, 2, 6, 4, 5, 4, 6,
    3, 7, 5, 7, 6, 7};
  /** comparisons walking along the grey code 
    000/001/011/010/110/111/101/100 */
  private static int[] GREY =
    {0, 1, 1, 3, 3, 2, 2, 6, 6, 7, 7, 5, 5, 4};
  public static void main(String[] s) throws Exception
  {
    long seed = 42;
    int runs = 100000;
    int numCompares = 32;
    double p = 0.8;
    int factors = 3;
    int bits = 3;
    boolean trouble = false;
    boolean even = false;
    int[] comparisons = ONE;

    int s1 = s.length - 1;
    for (int i = 0; i < s.length; i++)
    {
      if ("-all2".equals(s[i]))
      {
        comparisons = ALL2;
      }
      else if ("-all3".equals(s[i]))
      {
        comparisons = ALL3;
      }
      else if (("-bits".equals(s[i])) && (i < s1))
      {
        bits = Integer.parseInt(s[++i].trim());
      }
      else if (("-compares".equals(s[i])) && (i < s1))
      {
        numCompares = Integer.parseInt(s[++i].trim());
      }
      else if ("-even".equals(s[i]))
      {
        even = true;
      }
      else if ("-grey".equals(s[i]))
      {
        comparisons = GREY;
      }
      else if (("-p".equals(s[i])) && (i < s1))
      {
        p = Double.parseDouble(s[++i].trim());
      }
      else if (("-runs".equals(s[i])) && (i < s1))
      {
        runs = Integer.parseInt(s[++i].trim());
      }
      else if (("-seed".equals(s[i])) && (i < s1))
      {
        seed = Long.parseLong(s[++i].trim());
      }
      else if ("-single2".equals(s[i]))
      {
        comparisons = SINGLE2;
      }
      else if ("-single3".equals(s[i]))
      {
        comparisons = SINGLE3;
      }
      else
      {
        System.err.println("Cannot handle flag " + s[i]);
	trouble = true;
      }
    }

    if (trouble)
    {
      System.err.println("Args are [-all2] [-all3] [-bits #] [-compares #] " +
        " [-even] [-p #] [-runs #] [-seed #] [-single2] [-single3]");
      return;
    }
    
    System.out.println("seed " + seed + " compares " + numCompares +
      " p " + p + " bits " + bits + " even " + even);
    System.out.println("Comparisons " + Arrays.toString(comparisons));
    System.out.println("runs = " + runs);
    double[] probs = new double[factors + 1];
    setUpProbs(p, factors, probs);
    Random r = new Random(seed);
    int[] diffs = new int[comparisons.length];
    double[] allProbs = new double[runs];
    int mask = (1 << bits) - 1;
    double[] mean = new double[comparisons.length];
    for (int i = 0; i < runs; i++)
    {
      if (even)
      {
	fillInEvenDiffs(r, numCompares, comparisons, diffs, probs, mask);
      }
      else
      {
	fillInDiffs(r, numCompares, comparisons, diffs, probs, mask);
      }
      for (int j = 0; j < comparisons.length; j++)
      {
        mean[j] = mean[j] + diffs[j];
      }
      SigProb sp = MultiBin.computeMultiBin(0.5, diffs, true);
      // System.out.println(Arrays.toString(diffs));
      double tail = sp.getEq() + sp.getGt();
      // System.out.println("Tail prob " + tail);
      allProbs[i] = tail;
    }
    Arrays.sort(allProbs);
    // Want estimates of percentile values. If n values numbers 0..n-1
    // prob next <= #i is (i + 1) / (n+1). Set (i+1)/(n+1) = p and
    // you get i = p(n+1) - 1
    System.out.println("Median tail " + allProbs[(runs + 1) / 2 - 1]);
    System.out.println("5% " + allProbs[(int)((runs + 1) * 0.95) - 1]);
    System.out.println("95% " + allProbs[(int)((runs + 1) * 0.05) - 1]);
    for (int j = 0; j < comparisons.length; j++)
    {
      mean[j] = mean[j] / runs;
    }
    System.out.println("Means " + Arrays.toString(mean));
  }
}
