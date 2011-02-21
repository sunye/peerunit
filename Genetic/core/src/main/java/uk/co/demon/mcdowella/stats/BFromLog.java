package uk.co.demon.mcdowella.stats;
import java.util.Random;

/** This class is to work out the b-values from experiments where
 *  several 0/1 factors are varied at the same time and the log
 *  odds of success or failure from the experiment depends on the
 *  sum of the factors
 */
public class BFromLog
{
  /** turn p into b */
  public static double pToB(double p)
  {
    return 2.0 * p - 1.0;
  }
  /** turn b into p */
  public static double bToP(double b)
  {
    return (1.0 + b) / 2.0;
  }
  /** turn b into log odds */
  public static double bToLogOdds(double b)
  {
    if (b <= 0.0)
    {
      return Math.log((1.0 + b) / (1.0 - b));
    }
    return -Math.log((1.0 - b) / (1.0 + b));
  }
  /** turn log odds into b */
  public static double logOddsToB(double l)
  {
    if (l <= 0.0)
    {
      double d = Math.exp(l);
      return (d - 1.0) / (d + 1.0);
    }
    else
    {
      double d = Math.exp(-l);
      return (1.0 - d) / (d + 1.0);
    }
  }
  /** Do some random conversions */
  private static void checkConversions(Random r)
  {
    double p = r.nextDouble();
    double b = pToB(p);
    double pp = bToP(b);
    double l = bToLogOdds(b);
    double bb = logOddsToB(l);
    // System.out.println("P = " + p + " b = " + b + " pp = " + pp +
    //   " l = " + l + " bb = " + bb);
    double d1 = Math.abs(p - pp);
    double d2 = Math.abs(b - bb);
    // System.out.println("d1 " + d1 + " d2 " + d2);
    if ((d1 > 1.0E-15) || (d2 > 1.0e-15))
    {
      System.err.println("d1 " + d1 + " d2 " + d2);
      System.exit(1);
    }
  }
  /** work out b-value when n of them are combined */
  public static double combinedB(double b, int n)
  {
    if (n < 1)
    {
      return b;
    }
    double l = bToLogOdds(b);
    // There are 2^n combinations for the other b-values
    int choicesLeft = n - 1;
    int numCombs = 1 << choicesLeft;
    double sumB = 0.0;
    // (slowly!) go through all possible combinations
    // Horrible but hopefully simple and less likely to be wrong
    for (int i = 0; i < numCombs; i++)
    {
      // Vote 1 for our experiment. Each remaining bit is either
      // for us or against us. - so 3 bits can vote 3, 1, -1, or -3
      int bias = 1 + 2 * Integer.bitCount(i) - choicesLeft;
      double bHere = logOddsToB(bias * l);
      sumB += bHere;
    }
    return sumB / numCombs;
  }
  public static void main(String[] s)
  {
    Random r = new Random(42);
    for (int i = 0; i < 10000; i++)
    {
      checkConversions(r);
    }
    if (s.length != 1)
    {
      System.out.println("Expect b");
    }
    double b = Double.parseDouble(s[0].trim());
    System.out.println("B = " + b + " is p = " + bToP(b));
    for (int i = 1; i < 10; i++)
    {
      double resultB = combinedB(b, i);
      System.out.println("I = " + i + " b = " + resultB +
        " p = " + bToP(resultB));
    }
  }
}
