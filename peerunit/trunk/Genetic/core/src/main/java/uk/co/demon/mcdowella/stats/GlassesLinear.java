package uk.co.demon.mcdowella.stats;

import java.util.Arrays;
import java.util.Random;

/** Suppose that we know the variance-covariance matrix of an
observation and wish to find the linear function that highlights
a difference in mean most clearly. If the matrix is M and the
difference in means is a, then we need to find x maximising a.x
subject to x'Mx = 1. Using a Lagrangian we need to maximise
a.x + k(x'Mx - 1) which has a derivative of
a + 2kMx, so the x we want is M^-1a.
<br>
For our glasses survey we have 3 dimensions corresponding to the
strength of prescription at the ends and middle of a two-year period.
a = (0,0,1) to highlight the effect of an intervention which reduces
the increase in myopia during the second year. If Myopia progresses
linearly at a random rate with known variance and is measured with known
variance then we can work out M
*/
public class GlassesLinear
{
  /** Work out sigmage of linear function */
  public static double sigmage(double[] diff, double[] function,
    double[] mat)
  {
    double dot = 0.0;
    for (int i = 0; i < diff.length; i++)
    {
      dot += diff[i] * function[i];
    }
    double var = 0.0;
    for (int i = 0; i < function.length; i++)
    {
      for (int j = 0; j < function.length; j++)
      {
        var += function[i] * mat[i * function.length + j] *
	  function[j];
      }
    }
    return dot / Math.sqrt(var);
  }
  public static void main(String[] s)
  {
    // based on standard deviation after linear fit to self
    double measurementSd = 0.4;
    // If I have gone to +18 in 18 years or so high progression then
    // I am about 1 Dioptre a year. Assume this is just over 3 sigma
    double rateSd = 0.3;
    double ageFirstMeasurement = 20.0;
    double target = 5.0;
    boolean trouble = false;
    int tries = 1000;
    double groupSize = 100.0;
    int s1 = s.length - 1;
    double size = 0.2;
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
	else if ("-change".equals(s[i]))
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
	else if ("-size".equals(s[i]))
	{
	  number = s[++i];
	  groupSize = Double.parseDouble(number.trim());
	}
	else if ("-target".equals(s[i]))
	{
	  number = s[++i];
	  target = Double.parseDouble(number.trim());
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
      System.err.println("Args are [-age #][-change #][-meas #]" +
        "[-rate #][-size #][-target #][-tries #]");
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
    System.out.println("Target sigmage " + target);
    double[] mat = new double[9];
    // Variance = age^2 * var(rate) + var(meas)
    for (int i = 0; i < 3; i++)
    {
      double age = ageFirstMeasurement + i;
      // variance
      double var = age * age * varRate + varMeas;
      mat[i * 3 + i] = var * groupSize * 2.0;
      for (int j = 0; j < i; j++)
      {
        // covariance
	double cv = age * (ageFirstMeasurement + j) * varRate * 
	  groupSize * 2.0;
	mat[i * 3 + j] = cv;
	mat[j * 3 + i] = cv;
      }
    }
    System.out.println("Mat is " + Arrays.toString(mat));
    LU lu = new LU(3, mat);
    double det = lu.getDeterminant();
    System.out.println("Determinant is " + det);
    if (det <= 0.0)
    {
      return;
    }
    double[] diff = new double[]{0.0, 0.0, size * groupSize};
    double[] best = new double[3];
    lu.solve(diff, best);
    System.out.println("Best linear function to reveal difference is " +
      Arrays.toString(best));
    double optimal = sigmage(diff, best, mat);
    System.out.println("Optimal sigmage: " + optimal);
    double factor = target / optimal;
    System.out.println("group size for target: " + (groupSize * factor *
      factor));
    double s112 =  sigmage(diff, new double[] {-1.0, -1.0, 2.0}, mat);
    System.out.println("112 sigmage: " + s112);
    double s011 =  sigmage(diff, new double[] {0.0, -1.0, 1.0}, mat);
    System.out.println("011 sigmage: " + s011);
    factor = target / s112;
    System.out.println("group size for target: " + (groupSize * factor *
      factor));
    System.out.println("Constant sigmage: " +
      sigmage(diff, new double[] {1.0, 1.0, 1.0}, mat));
    System.out.println("Linear sigmage: " +
      sigmage(diff, new double[] {-1.0, 0.0, 1.0}, mat));
    System.out.println("Quad sigmage: " +
      sigmage(diff, new double[] {1.0, -2.0, 1.0}, mat));
    double bestR = 0.0;
    double[] bestAt = new double[3];
    double[] tryThis = new double[3];
    Random r = new Random(42);
    for (int i = 0; i < tries; i++)
    {
      for (int j = 0; j < tryThis.length; j++)
      {
        tryThis[j] = r.nextGaussian();
      }
      double sigHere = Math.abs(sigmage(diff, tryThis, mat));
      if (sigHere > bestR)
      {
        bestR = sigHere;
	System.arraycopy(tryThis, 0, bestAt, 0, 3);
      }
    }
    System.out.println("Best random " + bestR + " at " +
      Arrays.toString(bestAt));
  }
}
