package uk.co.demon.mcdowella.stats;

import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

/** Driver program for a variety of sequential tests */
public class SeqDriver
{
  /** Interface provided by a sequential test */
  public interface SeqTest
  {
    /** Absorb a value. Return <0 if decision is mean is < 0,
     *  >0 if decision is mean is > 0, 0 if no decision yet.
     */
    int eatValue(double v);
    /** Test is for a mean either + or - a given sigmage. Absorb
     *  absolutes sigmage
     */
    void configSigmage(double s);
  }
  /** Sequential testing for a normally distributed value
   *  with mean +/- sigmage and standard deviation 1.0.
   */
  public static class WaldSeq implements SeqTest
  {
    private double sum;
    private double twiceSigmage;
    private double absThresh = Math.log(99.0);
    public void configSigmage(double s)
    {
      twiceSigmage = s * 2.0;
    }
    public int eatValue(double x)
    {
      sum += x;
      double v = sum * twiceSigmage;
      if (Math.abs(v) < absThresh)
      {
        return 0;
      }
      if (v < 0)
      {
        return -1;
      }
      return 1;
    }
  }
  /** Sequential testing using only the sign of the observed data,
   *  but knowing that we expect it to be +ve with probabilty 0.5398
   *  if the mean is +ve and -ve with that probability if the mean
   *  is -ve. Theory says mean number of samples required should
   *  be about 355.
   */
  public static class WaldSignSeq implements SeqTest
  {
    private double sum;
    private double absThresh = Math.log(99.0);
    private double absScore = Math.log(0.5398/0.4602);
    public void configSigmage(double s)
    {
    }
    public int eatValue(double x)
    {
      if (x > 0)
      {
	sum += absScore;
      }
      else
      {
        sum -= absScore;
      }
      if (Math.abs(sum) < absThresh)
      {
        return 0;
      }
      if (sum < 0)
      {
        return -1;
      }
      return 1;
    }
  }
  /** Sequential testing using only the sign of the observed data,
   *  but knowing that we expect it to be +ve with probabilty 0.5398
   *  if the mean is +ve and -ve with that probability if the mean
   *  is -ve. For safety, we run two tests in parallel, against the
   *  null hypothesis that the probability is 1/2. Announce result
   *  if either test accepts prob != 1/2. If both accept probability
   *  1/2 guess 1 (in real life give up, but that isn't an option here).
   *  We are mostly interested in how many tests this takes on
   *  average, since we are not reporting don't know correctly.
   */
  public static class WaldDoubleSignSeq implements SeqTest
  {
    private double sumPlus;
    private boolean notPlus;
    private double sumMinus;
    private boolean notMinus;
    private double absThresh = Math.log(99.0);
    private double absScoreGood = Math.log(0.5398/0.5);
    private double absScoreBad = Math.log(0.4602/0.5);
    public void configSigmage(double s)
    {
    }
    public int eatValue(double x)
    {
      if (x > 0)
      {
	sumPlus += absScoreGood;
	sumMinus += absScoreBad;
      }
      else
      {
	sumPlus += absScoreBad;
	sumMinus += absScoreGood;
      }
      if (sumPlus < -absThresh)
      {
        notPlus = true;
      }
      if ((sumPlus > absThresh) && !notPlus)
      {
        return 1;
      }
      if (sumMinus < -absThresh)
      {
        notMinus = true;
      }
      if ((sumMinus > absThresh) && !notMinus)
      {
        return -1;
      }
      if (notPlus && notMinus)
      { // this amounts to guessing - in real life give up
	return 1;
      }
      return 0;
    }
  }
  /** Sequential testing looking at first 10, 20, 40, 80, 160, 320,
   *  etc values for a 1-tail error probabilty of 1/200, 1/400, ...
   *  We don't know the size of the difference between means so
   *  assume very close to zero and chose sigmage such that probability
   *  of >= sigmage at random is 1/200 if mean is 0.
   */
  private static class DoubleSeq implements SeqTest
  {
    public DoubleSeq()
    {
    }
    private double sum;
    private int num;
    private int[] when = new int[]
    { 10, 20, 40, 80, 160, 320, 640, 1280, 2560, 5120, 10240,
      20480
    };
    private double[] thresh = new double[]
    { 2.5758, 2.8070, 3.0233, 3.2272, 3.4205, 3.6047, 3.7809,
      3.9501, 4.1130, 4.2702, 4.4223, 4.5698
    };
    private int offset;
    public int eatValue(double v)
    {
      sum += v;
      num++;
      while (num > when[offset])
      {
        offset++;
      }
      if (num != when[offset])
      {
        return 0;
      }
      double sigmage = sum / Math.sqrt(num);
      if (Math.abs(sigmage) < thresh[offset])
      {
        return 0;
      }
      if (sigmage < 0)
      {
        return -1;
      }
      return 1;
    }
    public void configSigmage(double sigmage)
    {
    }
  }
  /** Sequential testing looking at first 10, 20, 40, 80, 160, 320,
   *  etc values for a 1-tail error probabilty of 1/200, 1/400, 
   *  1/800, 1/1600... Accumulate score of 1/10, 1/10, 1/20, ..
   *  if pass test, 0 otherwise. Break on total score + expected
   *  future score at random of 1 or more. Expected score at random
   *  is (1/200 + 1/400 + 1/800 + ...) = 1/100
   *  at random of prob of score of 1 or more is at most 1/100
   */
  private static class DoubleFractSeq implements SeqTest
  {
    public DoubleFractSeq()
    {
    }
    private double sum;
    private double sumScore;
    private int num;
    private double probHere = 1.0 / 200.0;
    private int[] when = new int[]
    { 10, 20, 40, 80, 160, 320, 640, 1280, 2560, 5120, 10240,
      20480
    };
    private double[] thresh = new double[]
    { 2.5758, 2.8070, 3.0233, 3.2272, 3.4205, 3.6047, 3.7809,
      3.9501, 4.1130, 4.2702, 4.4223, 4.5698
    };
    private int offset;
    public int eatValue(double v)
    {
      sum += v;
      num++;
      while (num > when[offset])
      {
	probHere = probHere * 0.5;
        offset++;
      }
      double sigmage = sum / Math.sqrt(num);
      double s;
      if (offset == 0)
      {
	s = 1.0 / when[offset];
      }
      else
      {
	s = 1.0 / (when[offset] - when[offset - 1]);
      }
      if (Math.abs(sigmage) >= thresh[offset])
      {
	sumScore += s;
      }
      double futureExpected =
        probHere * s * (when[offset] * 2 - num - 1) +
                              probHere;
      if ((sumScore + futureExpected) < 1.0)
      {
        return 0;
      }
      if (sigmage < 0)
      {
        return -1;
      }
      return 1;
    }
    public void configSigmage(double sigmage)
    {
    }
  }
  /** Score-based sequential testing but using known sigmage
   */
  private static class DoubleKnownFract implements SeqTest
  {
    public DoubleKnownFract()
    {
    }
    private double sum;
    private double sumScore;
    private int num;
    private double probHere = 1.0 / 200.0;
    private double meanSigmage;
    private int[] when = new int[]
    { 10, 20, 40, 80, 160, 320, 640, 1280, 2560, 5120, 10240,
      20480
    };
    private double[] thresh = new double[]
    { 2.5758, 2.8070, 3.0233, 3.2272, 3.4205, 3.6047, 3.7809,
      3.9501, 4.1130, 4.2702, 4.4223, 4.5698
    };
    private int offset;
    public int eatValue(double v)
    {
      sum += v;
      num++;
      while (num > when[offset])
      {
	probHere = probHere * 0.5;
        offset++;
      }
      double root = Math.sqrt(num);
      double sigmage = sum / root;
      double s;
      if (offset == 0)
      {
	s = 1.0 / when[offset];
      }
      else
      {
	s = 1.0 / (when[offset] - when[offset - 1]);
      }
      double meanAllowance = meanSigmage * root;
      if ((Math.abs(sigmage) + meanAllowance) >= thresh[offset])
      {
	sumScore += s;
      }
      double futureExpected =
        probHere * s * (when[offset] * 2 - num - 1) +
                              probHere;
      if ((sumScore + futureExpected) < 1.0)
      {
        return 0;
      }
      if (sigmage < 0)
      {
        return -1;
      }
      return 1;
    }
    public void configSigmage(double sigmage)
    {
      meanSigmage = sigmage;
    }
  }
  /** Sequential testing looking at first 10, 20, 40, 80, 160, 320,
   *  etc values for a 1-tail error probabilty of 1/200, 1/400, 
   *  1/1600, 1/6400... Accumulate score of +1 if pass test, 
   *  0 otherwise.
   *  Break on total score of 10 or more. Expected score at random
   *  is 10(1/200 + 1 * 1/400 + 2 * 1/1600 + ...)
   *  is 10(1/200 + 1/400 + 1/800 + ...) = 10/100 = 1/10 so prob
   *  at random of 10 or more is at most 1/100
   */
  private static class DoubleScoreSeq implements SeqTest
  {
    public DoubleScoreSeq()
    {
    }
    private double sum;
    private int sumScore;
    private int num;
    private int[] when = new int[]
    { 10, 20, 40, 80, 160, 320, 640, 1280, 2560, 5120, 10240,
      20480
    };
    private double[] thresh = new double[]
    { 2.5758, 2.8070, 3.2272, 3.6047, 3.9501, 4.2702, 4.5698,
      4.8522, 5.1202, 5.3757, 5.6202, 5.8551
    };
    private int offset;
    public int eatValue(double v)
    {
      sum += v;
      num++;
      while (num > when[offset])
      {
        offset++;
      }
      double sigmage = sum / Math.sqrt(num);
      if (Math.abs(sigmage) >= thresh[offset])
      {
        sumScore++;
      }
      if (sumScore < 10)
      {
        return 0;
      }
      if (sigmage < 0)
      {
        return -1;
      }
      return 1;
    }
    public void configSigmage(double sigmage)
    {
    }
  }
  /** Sequential testing looking at first 10, 20, 40, 80, 160, 320,
   *  etc values for a 1-tail error probabilty of 1/200, 1/400, ...
   *  Here we allow ourselves to know that the true mean is +/-0.1 and
   *  choose the sigmage to allow for this. If we have n numbers we
   *  calculate total/sqrt(n) so expect a mean of sqrt(n)/10 so
   *  subtract this from sigmage threshold.
   */
  private static class DoubleKnownSeq implements SeqTest
  {
    public DoubleKnownSeq()
    {
    }
    private double sum;
    private int num;
    private int[] when = new int[]
    { 10, 20, 40, 80, 160, 320, 640, 1280, 2560, 5120, 10240,
      20480
    };
    private double[] thresh = new double[]
    { 2.2596, 2.3598, 2.3909, 2.3328, 2.1556, 1.8159, 1.2511,
      0.3724, 0.0000, 0.0000, 0.0000, 0.0000
    };
    private int offset;
    public int eatValue(double v)
    {
      sum += v;
      num++;
      while (num > when[offset])
      {
        offset++;
      }
      if (num != when[offset])
      {
        return 0;
      }
      double sigmage = sum / Math.sqrt(num);
      if (Math.abs(sigmage) < thresh[offset])
      {
        return 0;
      }
      if (sigmage < 0)
      {
        return -1;
      }
      return 1;
    }
    public void configSigmage(double sigmage)
    {
    }
  }
  /** Sequential testing with scores allowing us to know the
   *  mean of +/- 0.1 sigma
   */
  private static class DoubleKnownScoreSeq implements SeqTest
  {
    public DoubleKnownScoreSeq()
    {
    }
    private double sum;
    private int sumScore;
    private int num;
    private double meanSigmage;
    private int[] when = new int[]
    { 10, 20, 40, 80, 160, 320, 640, 1280, 2560, 5120, 10240,
      20480
    };
    private double[] thresh = new double[]
    { 2.5758, 2.8070, 3.2272, 3.6047, 3.9501, 4.2702, 4.5698,
      4.8522, 5.1202, 5.3757, 5.6202, 5.8551
    };
    private int offset;
    public int eatValue(double v)
    {
      sum += v;
      num++;
      while (num > when[offset])
      {
        offset++;
      }
      double root = Math.sqrt(num);
      double sigmage = sum / root;
      double meanAllowance = meanSigmage * root;
      if ((Math.abs(sigmage) + meanAllowance) > thresh[offset])
      {
        sumScore++;
      }
      if (sumScore < 10)
      {
        return 0;
      }
      if (sigmage < 0)
      {
        return -1;
      }
      return 1;
    }
    public void configSigmage(double forSigmage)
    {
      meanSigmage = forSigmage;
    }
  }
  /** Sequential testing checking every 10 samples. Score +1
   *  over thresh, -1 below thresh, and stop when total score
   *  is > 0. Threshold starts at tail prob 1/200 and halves
   *  at 20, 40, 80...
   */
  private static class DoublePackSeq implements SeqTest
  {
    public DoublePackSeq()
    {
    }
    private double sum;
    private int sumScore;
    private int num;
    private double meanSigmage;
    private int[] when = new int[]
    { 10, 20, 40, 80, 160, 320, 640, 1280, 2560, 5120, 10240,
      20480
    };
    private double[] thresh = new double[]
    { 2.5758, 2.8070, 3.2272, 3.6047, 3.9501, 4.2702, 4.5698,
      4.8522, 5.1202, 5.3757, 5.6202, 5.8551
    };
    private int offset;
    public int eatValue(double v)
    {
      sum += v;
      num++;
      if ((num % 10) != 0)
      {
        return 0;
      }
      while (num > when[offset])
      {
        offset++;
      }
      double root = Math.sqrt(num);
      double sigmage = sum / root;
      double meanAllowance = meanSigmage * root;
      if ((Math.abs(sigmage) + meanAllowance) > thresh[offset])
      {
        sumScore++;
      }
      else
      {
        sumScore--;
      }
      if (sumScore < 1)
      {
        return 0;
      }
      if (sigmage < 0)
      {
        return -1;
      }
      return 1;
    }
    public void configSigmage(double forSigmage)
    {
      meanSigmage = forSigmage;
    }
  }
  /** Non-sequential testing using known sigmage */
  private static class NonSeq implements SeqTest
  {
    public NonSeq()
    {
    }
    private double sum;
    private int num;
    public int eatValue(double v)
    {
      sum += v;
      num++;
      if (num < 542)
      {
        return 0;
      }
      if (sum < 0)
      {
        return -1;
      }
      return 1;
    }
    public void configSigmage(double sigmage)
    {
    }
  }
  /** Sequential testing using discounted 1/sqrt(tail prob)
   */
  private static class RootSeq implements SeqTest
  {
    public RootSeq()
    {
      // Expected value of 1/sqrt(prob) is 2. Account for
      // infinite discounted sum
      expected = 2.0 / (1.0 - DISCOUNT);
    }
    private double sum;
    private double score;
    private int num;
    private double absMean;
    /** discount factor */
    private static final double DISCOUNT = 0.933;
    private double discountHere = 1.0;
    private final static double LOG2 = Math.log(2.0);
    /** Number of samples taken to double the discount factor */
    private double perDouble = 10.0;
    /** expected value at random */
    private double expected;
    public int eatValue(double v)
    {
      sum += v;
      num++;
      double root = Math.sqrt(num);
      double correction = absMean * root;
      // Simage away from mean if sign of current sum is
      // not the same as sign of true mean
      double absSigmage = Math.abs(sum / root) + correction;
      double probHere = rtlnorm(absSigmage);
      double toAdd = (1.0 / Math.sqrt(probHere)) * discountHere;
      score += toAdd;
      discountHere *= DISCOUNT;
      // Add on score expected from here on at random
      score += expected * discountHere;
      /*
      System.out.println("sum " + sum + " prob " + probHere +
        " toAdd " + toAdd + " score " + score + " expected " +
	expected + " discount " + discount);
      */
      if (score < (expected * 100.0))
      {
        return 0;
      }
      if (sum < 0)
      {
        return -1;
      }
      return 1;
    }
    public void configSigmage(double sigmage)
    {
      absMean = sigmage;
    }
  }
  /** Uses prediction-based likelihood ratio between mean 0 and 
   *  unknown mean. If the null hypothesis is true, the likelihood
   *  ratio against it forms a Martingale with expected value 1.
   *  So probability of likelihood against it > 100 at any point in
   *  a finite sequence of tests is <= 1/100
   */
  public static class PredictLike implements SeqTest
  {
    /** Use to work out estimated mean and variance for the
     *  unknown mean case.
     */
    private Deviant d = new Deviant();
    /** Sum of squared samples, to estimate variance for the
     *  mean 0 case.
     */
    private double sumSq = 0.0;
    /** Logarithm of likelihood ratio against null hypothesis.
     */
    private double logRatioAgainst = 0.0;
    /** Threshold for making a decision
     */
    private final double THRESHOLD = Math.log(100.0);
    /** Threshold before predicting
     */
    private final int PREDICT_AT = 3;
    /** Absorb a value. Return <0 if decision is mean is < 0,
     *  >0 if decision is mean is > 0, 0 if no decision yet.
     */
    public int eatValue(double v)
    {
      int samplesBefore = d.getN();
      double meanKnown = 0.0;
      double knownDifference = v - meanKnown;
      double kd2 = knownDifference * knownDifference;
      if (samplesBefore >= PREDICT_AT)
      { // Enough samples to estimate mean and standard deviation
        // we hope
        double meanUnknown = d.getMean();

	double varianceUnknown = d.getVariance();
	// allow ourselves to know variance
	varianceUnknown = 1.0;
	double varianceKnown = sumSq / samplesBefore;
	// know variance in both cases
	varianceKnown = 1.0;

	// If model holds unknown case variance is ALWAYS > 0.
	// If not, don't let NaN screw us up. If unknown mean
	// variance is > 0, known mean must be too.
	if (varianceUnknown > 0.0)
	{
	  // System.out.println("Got " + v + " unknown " +
	  //   meanUnknown + " ratio so far " + logRatioAgainst);
	  // Subtract two log likelihoods for value we have just
	  // observed. Constant factors cancel. Can also take log
	  // of variance, not log of standard deviation since that
	  // amounts to a constant term that cancels
	  double unknownDifference = v - meanUnknown;
	  double logRatioPrediction = Math.log(varianceKnown) -
	    Math.log(varianceUnknown) +
	    kd2 / (2.0 * varianceKnown) -
	    unknownDifference * unknownDifference / (2.0 * varianceUnknown);
	  logRatioAgainst += logRatioPrediction;
	}
      }
      d.sample(v);
      sumSq += kd2;
      if (logRatioAgainst >= THRESHOLD)
      {
        double mean = d.getMean();
	if (mean < 0)
	{
	  return -1;
	}
	if (mean > 0)
	{
	  return 1;
	}
      }
      return 0;
    }
    /** Test is for a mean either + or - a given sigmage. This test
     *  is for the case when we do not know the mean or standard
     *  deviation. Ignore sigmage info
     */
    public void configSigmage(double s)
    {
    }
  }
  /** Uses likelihood ratio looking only at sign of result. Alternative
   *  hypothesis is prior uniform distribution on probability.
   */
  public static class SignLike implements SeqTest
  {
    /** Logarithm of likelihood ratio against null hypothesis.
     *  Ratio is (seenPlus!)(seenMinus!)2^(seenPlus+seenMinus) /
     *   (seenPlus + seenMinus + 1)!
     */
    private double logRatioAgainst = 0.0;
    /** Threshold for making a decision
     */
    private final double THRESHOLD = Math.log(100.0);
    /** +ve seen */
    private int seenPlus = 0;
    /** -ve seen */
    private int seenMinus = 0;
    /** Absorb a value. Return <0 if decision is mean is < 0,
     *  >0 if decision is mean is > 0, 0 if no decision yet.
     */
    public int eatValue(double v)
    {
      if (v < 0)
      {
        seenMinus++;
	logRatioAgainst += Math.log(seenMinus);
      }
      else if (v > 0)
      {
        seenPlus++;
	logRatioAgainst += Math.log(seenPlus);
      }
      else
      {
        return 0;
      }
      logRatioAgainst += Math.log(2.0 /
                                  (seenPlus + seenMinus + 1.0));
      if (logRatioAgainst < THRESHOLD)
      {
        return 0;
      }
      if (seenPlus < seenMinus)
      {
        return -1;
      }
      if (seenPlus > seenMinus)
      {
        return 1;
      }
      return 0;
    }
    /** Sigmage is ignored
     */
    public void configSigmage(double s)
    {
    }
  }

  /** This class stores info per test */
  private static class TestInfo
  {
    private Class testClass;
    private Deviant forLengths = new Deviant();
    private int numSamples;
    private int correct;
    private int wrong;
    private SeqTest test;
    private double sign;
    TestInfo(Class forClass)
    {
      testClass = forClass;
    }
    boolean finished;
    public void startTest(double forSign) throws InstantiationException,
      IllegalAccessException
    {
      finished = false;
      test = (SeqTest)testClass.newInstance();
      test.configSigmage(Math.abs(forSign));
      sign = forSign;
    }
    /** eat value. Return true if finished */
    boolean eatValue(double v)
    {
      if (finished)
      {
        return true;
      }
      int result = test.eatValue(v);
      numSamples++;
      if (result == 0)
      {
        return false;
      }
      forLengths.sample(numSamples);
      numSamples = 0;
      finished = true;
      if ((result < 0) == (sign < 0.0))
      {
        correct++;
      }
      else
      {
        wrong++;
      }
      return true;
    }
    void printResult()
    {
      System.out.println("Class " + testClass.getName() + 
        " Total samples " + numSamples + " correct " + correct +
	" wrong " + wrong + " mean samples " + forLengths.getMean() +
	" sd " + Math.sqrt(forLengths.getVariance()));
    }
  }
  public static void main(String[] s) throws Exception
  {
    int goes = 10000;
    long seed = 425;
    // Pre-sample sigmage
    double sigmage = 0.1;
    System.out.println("Goes " + goes + " seed " + seed + " sigmage " +
      sigmage);
    // For sigmage 0.1 and alpha = beta = 1/100 expect
    // roughly 225 samples per test for SPRT
    TestInfo[] st = new TestInfo[]
    {
      new TestInfo(WaldSeq.class),
      new TestInfo(WaldSignSeq.class),
      new TestInfo(DoubleSeq.class),
      // new TestInfo(DoubleScoreSeq.class),
      // new TestInfo(DoubleFractSeq.class),
      new TestInfo(DoubleKnownSeq.class),
      new TestInfo(DoubleKnownScoreSeq.class),
      // new TestInfo(DoubleKnownFract.class),
      // new TestInfo(DoublePackSeq.class),
      // new TestInfo(RootSeq.class), too slow!
      new TestInfo(NonSeq.class),
      // new TestInfo(PredictLike.class),
      // new TestInfo(SignLike.class)
      new TestInfo(WaldDoubleSignSeq.class)
    };

    Random r = new Random(seed);
    for (int i = 0; i < goes; i++)
    {
      double bias = (r.nextInt(2) * 2 - 1) * sigmage;
      for (int j = 0; j < st.length; j++)
      {
        st[j].startTest(bias);
      }
      for (;;)
      {
        boolean finished = true;
	double sample = r.nextGaussian() + bias;
	for (int j = 0; j < st.length; j++)
	{
	  finished &= st[j].eatValue(sample);
	}
	if (finished)
	{
	  break;
	}
      }
    }
    for (int i = 0; i < st.length; i++)
    {
      st[i].printResult();
    }
  }
  private static double stp=2.5066282746310005;
  private static double cof[]={
   76.18009172947146,
   -86.50532032941677,
   24.01409824083091,
   -1.231739572450155,
   0.1208650973866179e-2,
   -0.5395239384953e-5};

  /**
   * log of gamma function, from Numerical Recipies.
   * Note that n!=gamma(n+1)
   */
  public static double gammaln(double xx)
  {
    int j;
    double ser,tmp,x,y;
    x=xx;
    y=x;
    tmp=x+5.5;
    tmp=(x+0.5)*Math.log(tmp)-tmp;
    ser=1.000000000190015;
    for(j=0;j<6;j++)
    {
      y=y+1.0;

      // System.out.println("Cof["+j+"]="+cof[j]);
      ser=ser+cof[j]/y;
    }
    return(tmp+Math.log(stp*ser/x));
  }
  private static final int MAXITERS = 100;
  private static final double EPS = 1.0e-9;
  private static double gser(double a, double x)
  {
    double gln=gammaln(a);
    //  cout<<"gser"<<endl;
   if(x<0.0)
    {
      throw new ArithmeticException("-ve x in gser");
    }
    if(x==0.0)
      return 0.0;
    double ap=a;
    double sum=1.0/a;
    double del=sum;

    int i;
    for(i=0;i<MAXITERS;i++)
    {
      ap=ap+1.0;
      del=del*x/ap;
      sum=sum+del;
      if(Math.abs(del)<(Math.abs(sum)*EPS))
        break;
    }
    if(i==MAXITERS)
    {
      throw new ArithmeticException("Gser did not converge");
    }
    return sum*Math.exp(-x+a*Math.log(x)-gln);
  }

  private static double gcf2(double a, double x)
  { // Based on Numerical Recipies V.2
    double gln=gammaln(a);
    double b=x+1.0-a;
    double c=1.0/Double.MIN_VALUE;
    double d=1.0/b;
    double h=d;

    int i;
    for(i=1;i<=MAXITERS;i++)
    {
      double an= -i*(i-a);
      b=b+2.0;
      d=an*d+b;
      if(Math.abs(d)<Double.MIN_VALUE)
        d=Double.MIN_VALUE;
      c=b+an/c;
      if(Math.abs(c)<Double.MIN_VALUE)
        c=Double.MIN_VALUE;
      d=1.0/d;
      double del=d*c;
      h=h*del;
      if(Math.abs(del-1.0)<EPS)
        break;
    }
    if(i>MAXITERS)
    {
      throw new ArithmeticException("Gcf did not converge");
    }
    return Math.exp(-x+a*Math.log(x)-gln)*h;
  }
  private static double gcf(double a, double x)
  {
    double gln=gammaln(a);
    double gold=0.0; // Previous value - test for convergence with this
    double a0=1.0;
    double a1=x;   // Here setting up a recurrence relation for
    double b0=0.0; // An and Bn s.t. value of continued fraction is An/Bn
    double b1=1.0;
    double fac=1.0;
    double g=1.0;
    //  cout<<"GCF1"<<endl;

    int i;
    for(i=1;i<=MAXITERS;i++)
    {
      double an=i;
      double ana=an-a;
      a0=(a1+a0*ana)*fac; // One step
      b0=(b1+b0*ana)*fac;
      double anf=an*fac;
      a1=x*a0+anf*a1;     // Second step
      b1=x*b0+anf*b1;
      if(a1!=0.0)
      { // Renormalise
        fac=1.0/a1;
        g=b1*fac;
        if(Math.abs((g-gold)/g)<EPS)
          break;
        gold=g;
      }
    }
    if(i>MAXITERS)
    {
      throw new ArithmeticException("Gcf did not converge");
    }
    return Math.exp(-x+a*Math.log(x)-gln)*g;
  }
  // Right tail of gamma distribution:
  //
  // 1/G(a) INT_x^inf exp(-t)t^(a-1) dt
  // Where G is the gamma function
  //
  static double gammaq(double a, double x)
  {
    if(x<0.0)
    {
      throw new ArithmeticException("Gammaq called with x<0");
    }
    if(x==0.0)
      return 1.0;
    if(a<=0.0)
    {
      throw new ArithmeticException("Gammaq called with a <=0");
    }
    if(x<(a+1.0))
      return 1.0-gser(a,x);
    else
      return gcf(a,x);
  }
  static double rtlnorm(double x) // We need 1/2 of erfc(x/sqrt(2))
  {                        // Equivalently, we square x and return half
                           // the tail area of chi-sq with 1 df
    return 0.5*gammaq(0.5,x*x*0.5);
  }
}
