package uk.co.demon.mcdowella.stats;

import java.util.Arrays;
import java.util.Random;

/** This class simulates running a large number of experiments
    to test for significant differences amongst a number of 
    alternatives. The model is that each observation is a sum of
    scores from factors plus random noise. Alternatives are ranked
    based on these totals and the ranks go on to produce the result
    of the experiment.
    */
public class MbPerm
{
  /** produce scores for all combinations of factors, using
      noise of standard deviation one */
  private static void scorer(double[] means, Random r, double[] results)
  {
    for (int i = 0; i < means.length; i++)
    {
      results[i] = means[i] + r.nextGaussian();
    }
  }
  /** means for 3-factor, all significant */
  private static double[] ALL3 = {
  /* 000 001 010 011 100 101 110 111 */
    -3.0, -1.0, -1.0, 1.0, -1.0, 1.0, 1.0, 3.0
  };
  /** means for 3-factor, only low order bit significant */
  private static double[] SINGLE3 = {
  /* 000 001 010 011 100 101 110 111 */
    -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 1.0
  };
  /** means for 2-factor, all significant */
  private static double[] ALL2 = {
  /** 00 01 10 11 */
    -2.0, 0.0, 0.0, 2.0};
  /** means for 2-factor, only low order bit significant */
  private static double[] SINGLE2 = {
  /** 00 01 10 11 */
    -1.0, 1.0, -1.0, 1.0};
  /** means for 1 factor */
  private static double[] JUST_BIT = {-1.0, 1.0};
  /** class holding score and initial position */
  private static class ScorePos implements Comparable<ScorePos>
  {
    private double score;
    private int rank;
    public int compareTo(ScorePos other)
    {
      if (score < other.score)
      {
        return -1;
      }
      if (score > other.score)
      {
        return 1;
      }
      return 0;
    }
  }
  /** Turn vector of scores into ranks using ScorePos as temp */
  private static void toRank(
    double[] scores, int[] ranks, ScorePos[] temp)
  {
    for (int i = 0; i < scores.length; i++)
    {
      temp[i].score = scores[i];
      temp[i].rank = i;
    }
    Arrays.sort(temp);
    for (int i = 0; i < temp.length; i++)
    {
      ranks[temp[i].rank] = i;
    }
  }
  /** run experiment, returning sum of odd ranks */
  private static int runExperiment(double[] means, double[] scores,
    int[] ranks, ScorePos[] temp, int numRankings, Random r)
  {
    int total = 0;
    for (int i = 0; i < numRankings; i++)
    {
      scorer(means, r, scores);
      toRank(scores, ranks, temp);
      for (int j = 1; j < ranks.length; j+= 2)
      {
        total += ranks[j];
      }
    }
    return total;
  }
  /** Get significance of given percentile stat */
  private static double probGe(double percentile, int[] ranked,
    double[] probs, int base)
  {
    // Want estimates of percentile values. If n values numbers 0..n-1
    // prob next <= #i is (i + 1) / (n+1). Set (i+1)/(n+1) = p and
    // you get i = p(n+1) - 1
    int pos = (int)((ranked.length + 1) * percentile) - 1;
    int found = ranked[pos];
    double probGe = 0.0;
    for (int i = found - base; i < probs.length; i++)
    {
      probGe += probs[i];
    }
    return probGe;
  }
  public static void main(String[] s)
  {
    long seed = 42;
    int numRankings = 4;
    // Want single-factor comparison to have probability 0.8
    // by default. If factor here is f we subtract -f + noise from
    // f + noise to get 2f + noise + noise. Var(noise) = 1 so we
    // have mean 2f and variance 2 or sigmage of 2f / sqrt(2) =
    // f * sqrt(2). We want sigmage 0.8146 to get probability 0.8
    double meanFactor = 0.8416 * Math.sqrt(0.5);
    int numChecks = 100000;
    int numExperiments = 1000;
    double[] chosenMeans = ALL3;
    boolean trouble = false;
    int s1 = s.length - 1;
    String num = "";
    try
    {
      for (int i = 0; i < s.length; i++)
      {
	if ("-all2".equals(s[i]))
	{
	  chosenMeans = ALL2;
	}
	else if ("-bit".equals(s[i]))
	{
	  chosenMeans = JUST_BIT;
	}
	else if ("-checks".equals(s[i]) && (i < s1))
	{
	  num = s[++i];
	  numChecks = Integer.parseInt(num.trim());
	}
	else if ("-mean".equals(s[i]) && (i < s1))
	{
	  num = s[++i];
	  meanFactor = Double.parseDouble(num.trim());
	}
	else if ("-ranks".equals(s[i]) && (i < s1))
	{
	  num = s[++i];
	  numRankings = Integer.parseInt(num.trim());
	}
	else if ("-runs".equals(s[i]) && (i < s1))
	{
	  num = s[++i];
	  numExperiments = Integer.parseInt(num.trim());
	}
	else if ("-seed".equals(s[i]) && (i < s1))
	{
	  num = s[++i];
	  seed = Long.parseLong(num.trim());
	}
	else if ("-single2".equals(s[i]))
	{
	  chosenMeans = SINGLE2;
	}
	else if ("-single3".equals(s[i]))
	{
	  chosenMeans = SINGLE3;
	}
	else
	{
	  System.err.println("Cannot handle flag " + s[i]);
	  trouble = true;
	}
      }
    }
    catch (NumberFormatException nfe)
    {
      System.err.println("Cannot read number in " + num);
      trouble = true;
    }
    if (trouble)
    {
      System.err.println("Args are [-all2] [-bit] [-checks #] " +
        "[-ranks #] [-runs #] [-seed #] [-single2] [-single3]");
      return;
    }

    // Check probability in single-factor case
    System.out.println("mean factor is " + meanFactor);
    System.out.println("Check with " + numChecks + " compares");
    double[] meanCheck = new double[] {-meanFactor, meanFactor};
    double[] scoreCheck = new double[2];
    int[] rankCheck = new int[2];
    ScorePos[] spCheck = new ScorePos[2];
    for (int i = 0; i < spCheck.length; i++)
    {
      spCheck[i] = new ScorePos();
    }
    Random r = new Random(seed);
    int checkSum = runExperiment(meanCheck, scoreCheck, rankCheck,
      spCheck, numChecks, r);
    System.out.println("Estimated single-factor comparison prob is " +
      ((double)checkSum) / numChecks);
    
    System.out.println("Comparison means are " +
      Arrays.toString(chosenMeans));
    System.out.println("Experiments " + numExperiments + " of " +
      numRankings + " each");
    // Run experiments as planned and save off scores
    double[] ourMeans = chosenMeans.clone();
    ScorePos[] ourSp = new ScorePos[ourMeans.length];
    for (int i = 0; i < ourMeans.length; i++)
    {
      ourMeans[i] *= meanFactor;
      ourSp[i] = new ScorePos();
    }
    double[] ourScore = new double[ourMeans.length];
    int[] ourRank = new int[ourMeans.length];
    int[] results = new int[numExperiments];
    for (int i = 0; i < numExperiments; i++)
    {
      results[i] = runExperiment(ourMeans, ourScore, ourRank,
        ourSp, numRankings, r);
    }
    Arrays.sort(results);
    // Form an array to work out the significances. For this
    // purpose, all the experiments are basically the same
    int[][] sigRanks = new int[numRankings][];
    for (int i = 0; i < numRankings; i++)
    {
      int[] rr = new int[ourMeans.length];
      for (int j = 0; j < rr.length; j++)
      {
        rr[j] = j;
      }
      sigRanks[i] = rr;
    }
    Columns forSig = new Columns(ourMeans.length / 2, sigRanks);
    int base = forSig.getBase();
    double[] probs = forSig.getProbs();
    System.out.println("Median Prob >= observed is "  +
      probGe(0.5, results, probs, base));
    System.out.println("5% >= observed is "  +
      probGe(0.05, results, probs, base));
    System.out.println("95% >= observed is "  +
      probGe(0.95, results, probs, base));
  }
}
