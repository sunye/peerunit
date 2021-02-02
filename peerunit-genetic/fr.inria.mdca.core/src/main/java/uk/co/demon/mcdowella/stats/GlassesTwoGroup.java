package uk.co.demon.mcdowella.stats;

import java.util.Arrays;
import java.util.Random;

/** We have two groups. Under the null hypothesis they share the
same multivariate normal distribution. We test this against the
alternative hypothesis that there is a shift in mean, using a
likelihood ratio test.
*/
public class GlassesTwoGroup
{
  /** work out some of the contributions to the log probability 
      given the dimension of the data and the data itself, as
      contiguous chunks appended together
      */
  private static double devianceContrib(int dim, int[] dataStart,
    int[] dataLen, double[][] data)
  {
    // Probability for multinomial is 
    // exp(-x'M^-1x/2)(2pi)^-d/2det(m)^-1/2
    // This is maximised if we use the divisor n estimate for the
    // determinant and when we work out the difference of two
    // log probabilities the only thing that survives is n times
    // the log of that determinant. The term at the top goes away
    // becase x'y = tr(yx'). For y = Nx we have SUM tr(Nxx') and the
    // maximising N is (SUM xx'^/n)^-1 so we get tr(In) = dim*n
    // which will cancel out

    // Start off by working out n-1 estimate of variance-coveriance
    MultiDeviant md = new MultiDeviant(dim);
    double[] sample = new double[dim];
    for (int i = 0; i < dataStart.length; i++)
    {
      int off = dataStart[i];
      int past = off + dataLen[i];
      double[] dat = data[i];
      for (int j = off; j < past; j+= dim)
      {
	System.arraycopy(dat, j, sample, 0, dim);
	md.sample(sample);
      }
    }
    double[] vc = new double[dim * dim];
    md.getVariance(vc);
    // That is the n-1 divisor variance. The log probability
    // is maximised using the n divisor variance
    int n = md.getN();
    double factor = (n - 1.0) / n;
    for (int i = 0; i < vc.length; i++)
    {
      vc[i] *= factor;
    }
    // Want log of determinant
    LU lu = new LU(dim, vc);
    // This drops a multiplication by 1/2 we don't want anyway
    return -n * Math.log(lu.getDeterminant());
  }
  /** Work out 2 ln probability difference between pooled mean and
      different mean theories.
    */
  public static double devianceDiff(int dim, double[] data1,
    int data1Start, int data1Len, double[] data2, int data2Start,
    int data2Len)
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
    double splitDeviance = devianceContrib(dim, dataStart, dataLen,
      datad);
    dataStart[0] = data2Start;
    dataLen[0] = data2Len;
    datad[0] = data2;
    splitDeviance += devianceContrib(dim, dataStart, dataLen, datad);
    // Pool data
    dataStart = new int[] {data1Start, data2Start};
    dataLen = new int[] {data1Len, data2Len};
    datad = new double[][] {data1, data2};
    double sharedDeviance = devianceContrib(dim, dataStart, dataLen,
      datad);
    return splitDeviance - sharedDeviance;
  }
  public static void main(String[] s)
  {
    // based on standard deviation after linear fit to self
    double measurementSd = 0.4;
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
        "[-meas #][-rate #][-seed #][-size #][-tries #]");
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
    int gs2 = groupSize * 2;
    double[] data = new double[gs2 * 3];
    Deviant d = new Deviant();
    // Will now create random datasets and run test
    Random r = new Random(seed);
    for (int i = 0; i < tries; i++)
    {
      for (int j = 0; j < gs2; j++)
      {
        double rate = rateSd * r.nextGaussian();
	double base = ageFirstMeasurement;
	for (int k = 0; k < 3; k++)
	{
	  data[j * 3 + k] = rate * (base + k) +
	    measurementSd * r.nextGaussian();
	}
      }
      for (int j = 0; j < groupSize; j++)
      {
        data[j * 3 + 2] -=  size;
      }
      int len = groupSize * 3;
      double dd = devianceDiff(3, data, 0, len, data, len, len);
      d.sample(dd);
    }
    System.out.println("Deviance statistic: " + d);
  }
}
