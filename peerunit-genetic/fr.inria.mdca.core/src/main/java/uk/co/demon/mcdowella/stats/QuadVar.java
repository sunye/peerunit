package uk.co.demon.mcdowella.stats;

import java.util.Arrays;

/** This class is used to set confidence limits on the value of
 *  a linear function of binomially distributed variables. We work
 *  out the maximum variance possible to a statistic given its expected
 *  value. This turns out to be a quadratic in the expected value,
 *  which we return - as the a, b, and c in ax^2 + bx + c.
 *  <br>
 *  We want to maximise SUM_i AiPi(1-Pi) subject to SUM_i BiPi = x.
 *  In the real world 0 &le; Pi &le; 1, but for the purposes of putting
 *  an upper bound on the possible variance we can neglect this and so
 *  make our life easy. 
 *  <br>
 *  We form the lagrangian SUM_i AiPi(1-Pi) + L (SUM_i BiPi - x)
 * <br>
 *  which tells us that Ai(1-2Pi) + LBi = 0
 *  <br>
 *  or Pi = 1/2 + LBi/2Ai
 *  and summing up BiPi we have
 *  <br>
 *  SUM_i(Bi/2 + LBi^2/2Ai) = x
 *  <br>
 *  so L = (2x - SUM_iBi) / (SUM_i Bi^2/Ai)
 */
public class QuadVar
{
  /** No public constructor since only static method is useful */
  private QuadVar()
  {
  }
  /** Does maximisation using a and b vectors and puts coefficients
   *  of ax^2 + bx + c into abcCoeffs[] = {a, b, c}
   */
  public static void quadVar(double[] a, double[] b, double[] abcCoeffs)
  {
    // System.out.println("A " + Arrays.toString(a));
    // System.out.println("B " + Arrays.toString(b));
    if (a.length != b.length)
    {
      throw new IllegalArgumentException("Argument vector mismatch");
    }
    double sumB = 0.0;
    double sumB2OverA = 0.0;
    for (int i = 0; i < a.length; i++)
    {
      final double bi = b[i];
      sumB += bi;
      final double ai = a[i];
      if (bi == 0.0)
      { // early exit to ignore cases with ai = bi = 0 because
        // some spline is not affected by some inputs
        continue;
      }
      if (ai == 0.0)
      {
        throw new IllegalArgumentException("zero variance weight");
      }
      sumB2OverA += bi * bi / ai;
    }
    // Work out L as a linear function of x
    double lx = 2.0 / sumB2OverA;
    double lc = - sumB / sumB2OverA;
    // Work out L^2 as a quadratic function of x
    double qx2 = lx * lx;
    double qx = 2.0 * lx * lc;
    double qc = lc * lc;
    // Now add up the terms from Pi = 1/2 + LBi/2Ai
    // so Pi(1-Pi) = 1/4 - L^2Bi^2/(4Ai^2)
    double fa = 0.0;
    double fb = 0.0;
    double fc = 0.0;
    for (int i = 0; i < a.length; i++)
    {
      final double bi = b[i];
      final double ai = a[i];
      if (ai == 0.0)
      {
        continue;
      }
      final double s = bi * bi / (4.0 * ai * ai);
      fc += ai * (0.25 - qc * s);
      final double ais = ai * s;
      fb -= qx * ais;
      fa -= qx2 * ais;
    }
    abcCoeffs[0] = fa;
    abcCoeffs[1] = fb;
    abcCoeffs[2] = fc;
  }
}
