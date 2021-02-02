package uk.co.demon.mcdowella.stats;

import java.util.Arrays;
import java.util.Random;

/** We have two groups. We use linear regression to predict the
third prescription power from the first two. Under the null hypothesis
both groups have the same distribution. Under the alternative hypothesis
using two different regressions should predict better. We look at
the sum of the squared residuals. Twice the logarithm of the difference
according to chi^2 with 4 degrees of freedom, since a linear regression
of one point based on 2 others has 3 parameters plus an error variance,
and taking logs makes it a difference of log likelihoods, under the 
model that the residual is normally distributed.
*/
public class GlassesReg
{
  /** work out the deviance of the squared error for a predictor of the
      last points in a dim given the first two. This is up to a constant
      term. We also provide the fitted coefficients and an estimate
      of their variance-covariance matrix
      */
  private static double devError(int dim, int[] dataStart,
    int[] dataLen, double[][] data, double[] coefOut, double[] varCovar)
  {
    // Our predictior is (X'X)^-1(X'Y)
    double[] xy = new double[dim];
    double[] xx = new double[dim * dim];
    int d1 = dim - 1;
    for (int i = 0; i < dataStart.length; i++)
    {
      int first = dataStart[i];
      int past = first + dataLen[i];
      double[] dat = data[i];
      for (int j = first; j < past; j += dim)
      {
        double y = dat[j + d1];
	xy[0] += y;
	xx[0] += 1.0;
	for (int k = 0; k < d1; k++)
	{
	  double x = dat[j + k];
	  final int k1 = k + 1;
	  xy[k1] += y * x;
	  xx[k1] += x;
	  xx[k1 * dim] += x;
	  xx[k1 * dim + k1] += x * x;
	  for (int l = 0; l < k; l++)
	  {
	    double c = x * dat[j + l];
	    final int l1 = l + 1;
	    xx[k1 * dim + l1] += c;
	    xx[l1 * dim + k1] += c;
	  }
	}
      }
    }
    LU lu = new LU(dim, xx);
    double[] coefs = new double[dim];
    lu.solve(xy, coefs);
    // System.out.println("Coefs: " + Arrays.toString(coefs));
    double sumSq = 0.0;
    int n = 0;
    // Deviant d = new Deviant();
    for (int i = 0; i < dataStart.length; i++)
    {
      int first = dataStart[i];
      int past = first + dataLen[i];
      n += dataLen[i] / dim;
      double[] dat = data[i];
      for (int j = first; j < past; j += dim)
      {
        double y = coefs[0];
	for (int k = 0; k < d1; k++)
	{
	  y += coefs[k + 1] * dat[j + k];
	}
	double diff = y - dat[j + d1];
	// d.sample(diff);
	sumSq += diff * diff;
      }
    }
    if (coefOut != null)
    {
      System.arraycopy(coefs, 0, coefOut, 0, coefs.length);
    }
    if (varCovar != null)
    { // Work out an estimate of the variance-covariance matrix of
      // the coefficients. These are just a linear function of the
      // input Y: coef = ((X'X)^-1X) y so their variance-covariance
      // is F'F times the variance of the individual y, which
      // simplifies to (X'X)^-1 times the variance.

      // Get the inverse of (X'X)^-1 by solving for unit vectors
      double[] unit = new double[dim];
      double[] col = new double[dim];
      for (int i = 0; i < dim; i++)
      {
        unit[i] = 1.0;
	lu.solve(unit, col);
	unit[i] = 0.0;
	for (int j = 0; j < dim; j++)
	{
	  varCovar[j * dim + i] = col[j];
	}
      }
      // Here we use as an unbiassed estimate of the variance of
      // the y the divisor n - dim, to account for the dim linear
      // parameters
      double estYVar = sumSq / (n - dim);
      // System.out.println("Est y var " + estYVar);
      int past = dim * dim;
      for (int i = 0; i < past; i++)
      {
        varCovar[i] *= estYVar;
      }
    }
    // This comes from the maximum likelihood estimate for a normally
    // distributed residual, which is maximised if we divide by n
    // to work out the variance, not n - 1
    double varEst = sumSq / n;
    // System.out.println("varEst " + varEst + " n " + n);
    // System.out.println("Diff is " + d);
    return -n * Math.log(varEst);
  }
  /** Work out 2 ln probability difference between pooled mean and
      different mean theories. Also return the difference of the
      coefficients in the split theories, and the sum of their
      variance-covariance matrices.
    */
  public static double devianceDiff(int dim, double[] data1,
    int data1Start, int data1Len, double[] data2, int data2Start,
    int data2Len, double[] coefDiff, double[] diffVarCovar)
  {
    // Probability for multinomial is 
    // exp(-x'M^-1x/2)(2pi)^-d/2det(m)^-1/2
    // determinant for split means
    // This is maximised if we use the divisor n estimate for the
    // determinant and when we work out the difference of two
    // log probabilities the only thing that survives is n times
    // the log of that determinant

    int[] dataStart = new int[] {data1Start};
    int[] dataLen = new int[] {data1Len};
    double[][] datad = new double[][] {data1};
    // coefficient from fit
    double[] coef0 = new double[dim];
    double[] vc0 = new double[dim * dim];
    double splitDev = devError(dim, dataStart, dataLen,
      datad, coef0, vc0);
    dataStart[0] = data2Start;
    dataLen[0] = data2Len;
    datad[0] = data2;
    double[] coef1 = new double[dim];
    double[] vc1 = new double[dim * dim];
    splitDev += devError(dim, dataStart, dataLen, datad, coef1, vc1);
    // Pool data
    dataStart = new int[] {data1Start, data2Start};
    dataLen = new int[] {data1Len, data2Len};
    datad = new double[][] {data1, data2};
    double sharedDev = devError(dim, dataStart, dataLen,
      datad, null, null);
    for (int i = 0; i < dim; i++)
    {
      coefDiff[i] = coef0[i] - coef1[i];
    }
    int past = dim * dim;
    for (int i = 0; i < past; i++)
    {
      diffVarCovar[i] = vc0[i] + vc1[i];
    }
    return splitDev - sharedDev;
  }
  /** return tail prob of chi-squared with 4 df */
  private static double chi4(double val)
  {
    return SeqDriver.gammaq(2.0, val * 0.5);
  }
  public static void main(String[] s)
  {
    // based on standard deviation after linear fit to self
    double measurementSd = 0.4;
    double myopiaThresh = Double.MAX_VALUE;
    // If I have gone to +18 in 18 years or so high progression then
    // I am about 1 Dioptre a year. Assume this is just over 3 sigma
    double rateSd = 0.3;
    double ageFirstMeasurement = 20.0;
    boolean trouble = false;
    int tries = 1000;
    int groupSize = 100;
    double size = 0.2;
    long seed = 42;
    int s1 = s.length - 1;
    String number = "";
    try
    {
      for (int i = 0; i < s1; i++)
      {
	if ("-age".equals(s[i]))
	{
	  number = s[++i];
	  ageFirstMeasurement = Double.parseDouble(number.trim());
	}
	if ("-change".equals(s[i]))
	{
	  number = s[++i];
	  size = Double.parseDouble(number.trim());
	}
	else if ("-meas".equals(s[i]))
	{
	  number = s[++i];
	  measurementSd = Double.parseDouble(number.trim());
	}
	else if ("-rate".equals(s[i]))
	{
	  number = s[++i];
	  rateSd = Double.parseDouble(number.trim());
	}
	else if ("-seed".equals(s[i]))
	{
	  number = s[++i];
	  seed = Long.parseLong(number.trim());
	}
	else if ("-size".equals(s[i]))
	{
	  number = s[++i];
	  groupSize = Integer.parseInt(number.trim());
	}
	else if ("-thresh".equals(s[i]))
	{
	  number = s[++i];
	  myopiaThresh = Double.parseDouble(number.trim());
	}
	else if ("-tries".equals(s[i]))
	{
	  number = s[++i];
	  tries = Integer.parseInt(number.trim());
	}
	else
	{
	  System.err.println("Cannot handle flag " + s[i]);
	  ++i;
	  trouble = true;
	}
      }
      if ((s.length % 2) != 0)
      {
        System.err.println("Must have even number of arguments");
	trouble = true;
      }
    }
    catch (NumberFormatException nfe)
    {
      System.err.println("Cannot read number in " + number);
      trouble = true;
    }
    if (trouble)
    {
      System.err.println("Args are [-age #][-change #]" +
        "[-meas #][-rate #][-seed #][-size #][-thresh #][-tries #]");
      return;
    }
    double varMeas = measurementSd * measurementSd;
    double varRate = rateSd * rateSd;
    System.out.println("Age at first measurement: " +
      ageFirstMeasurement);
    System.out.println("Standard deviation of rate: " + rateSd);
    System.out.println("Standard deviation of measurement: " +
      measurementSd);
    System.out.println("Size of change in rate: " + size);
    System.out.println("Random tries; " + tries);
    System.out.println("Group size " + groupSize);
    System.out.println("seed: " + seed);
    if (myopiaThresh < Double.MAX_VALUE)
    {
      System.out.println("myopia threshold: " + myopiaThresh);
    }
    else
    {
      System.out.println("myopia threshold left at huge default value");
    }
    int gs2 = groupSize * 2;
    double[] data = new double[gs2 * 3];
    Deviant devStat = new Deviant();
    Deviant diffStat = new Deviant();
    Deviant diffSe = new Deviant();
    // double[] meanVcv = new double[9];
    // MultiDeviant md = new MultiDeviant(3);
    // Will now create random datasets and run test
    Random r = new Random(seed);
    double[] chiVals = new double[tries];
    double[] diffVals = new double[tries];
    Deviant c112 = new Deviant();
    for (int i = 0; i < tries; i++)
    {
      double[] meanX = new double[3];
      for (int j = 0; j < gs2; j++)
      {
        double rate = rateSd * r.nextGaussian();
	double base = ageFirstMeasurement;
	for (int k = 0; k < 3; k++)
	{
	  data[j * 3 + k] = rate * (base + k) +
	    measurementSd * r.nextGaussian();
	}
	if (data[j * 3] > myopiaThresh)
	{
	  j--;
	  continue;
	}
	for (int k = 0; k < 2; k++)
	{
	  meanX[k + 1] += data[j * 3 + k];
	}
	/* for testing - makes sure we get exactly a linear regression
	data[j * 3 + 2] = 1.0 + 2.0 * data[j * 3] + 3.0 *
	  data[j * 3 + 1] + measurementSd * r.nextGaussian();
	*/
      }
      for (int j = 0; j < groupSize; j++)
      {
        data[j * 3 + 2] +=  size;
      }
      int len = groupSize * 3;
      double[] coefDiff = new double[3];
      double[] coefVCV = new double[9];
      double dd = devianceDiff(3, data, 0, len, data, len, len,
        coefDiff, coefVCV);
      // md.sample(coefDiff);
      /*
      for (int j = 0; j < 9; j++)
      {
        meanVcv[j] += coefVCV[j];
      }
      */
      devStat.sample(dd);
      chiVals[i] =dd;
      meanX[0] = 1.0;
      for (int j = 1; j < 3; j++)
      {
        meanX[j] = meanX[j] / (groupSize * 2);
      }
      double expectedDiff = 0.0;
      double expectedVar = 0.0;
      for (int j = 0; j < 3; j++)
      {
        expectedDiff += meanX[j] * coefDiff[j];
	for (int k = 0; k < 3; k++)
	{
	  expectedVar += meanX[j] * meanX[k] * coefVCV[j * 3 + k];
	}
      }
      diffStat.sample(expectedDiff);
      diffVals[i] = expectedDiff;
      diffSe.sample(Math.sqrt(expectedVar));
      Deviant a112 = new Deviant();
      for (int j = 0; j < groupSize; j++)
      {
        double scalar  = data[j * 3] + data[j * 3 + 1] -
	  2.0 * data[j * 3 + 2];
        a112.sample(scalar);
      }
      Deviant b112 = new Deviant();
      for (int j = groupSize; j < gs2; j++)
      {
        double scalar = data[j * 3] + data[j * 3 + 1] -
	  2.0 * data[j * 3 + 2];
        b112.sample(scalar);
      }
      double sigmage = (a112.getSum() - b112.getSum()) /
        Math.sqrt(groupSize * (a112.getVariance() + b112.getVariance()));
      c112.sample(sigmage);
    }
    System.out.println("Deviance statistic: " + devStat);
    System.out.println("Expected difference: " + diffStat);
    System.out.println("mean S.E. of Expected difference: " + diffSe);
    Arrays.sort(chiVals);
    Arrays.sort(diffVals);
    // Want estimates of percentile values. If n values numbers 0..n-1
    // prob next <= #i is (i + 1) / n+1. Set (i+1)/(n+1) = p and
    // you get i = p(n+1) - 1
    int m05 = (int)Math.round((tries + 1) * 0.05 - 1.0);
    int m01 = (int)Math.round((tries + 1) * 0.01 - 1.0);
    int m50 = (int)Math.round((tries + 1) * 0.50 - 1.0);
    System.out.println("0.05 is offset " + m05 + " chiVal " +
      chiVals[m05] + " chi tail " + chi4(chiVals[m05]) +
        " diffVal " + diffVals[m05]);
    System.out.println("0.01 is offset " + m01 + " chiVal " +
      chiVals[m01] + " chi tail " + chi4(chiVals[m01]) +
      " diffVal " + diffVals[m01]);
    System.out.println("0.50 is offset " + m50 + " chiVal " +
      chiVals[m50] + " chi tail " + chi4(chiVals[m50]) +  " diffVal " +
      diffVals[m50]);
    System.out.println("112 sigmage: " + c112);
    /*
    for (int i = 0; i < 9; i++)
    {
      meanVcv[i] = meanVcv[i] / tries;
    }
    */
    // System.out.println("Mean expected vcv: " +
    //   Arrays.toString(meanVcv));
    // System.out.println("Coefficient vcv seen: " + md);
  }
}
