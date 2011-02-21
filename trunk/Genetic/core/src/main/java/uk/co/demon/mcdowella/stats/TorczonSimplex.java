package uk.co.demon.mcdowella.stats;
/** This is the multidimensional simplex search described and proved
 *  convergent in Torczon "On the convergence of Multidirectional
 *  Search". There is evidence: "Convergence properties of the
 *  Nelder-Mead Simplex Method in Low Dimensions" that it is possible
 *  to construct counterexamples for which the Nelder-Mead simplex
 *  does not converge, even in dimension 2.
 */
public class TorczonSimplex
{
  private TorczonSimplex()
  {
  }
  public interface Function
  {
    public double f(double[] v);
  }
  private static void showAt(double v, double[] x, int first, int past)
  {
    System.out.print("got " + v + " at");
    for (int i = first; i < past; i++)
    {
      System.out.print(" " + x[i]);
    }
    System.out.println();
  }
  /** Returns best value found, with corresponding x in
   * startpoint[0]. Other members of startpoint will also
   * be modified - that is perhaps
   * startpoint[x] != original startpoint[x] for all x.
   */
  public static double min(double[][]startpoint, Function f,
    double ftol)
  {
    boolean gotBest = false;
    double[][] originalStartpoint = startpoint;
    double[] values = new double[startpoint.length];
    double[][] tryHere = new double[startpoint.length][];
    double[] tryValues = new double[startpoint.length];
    double[][] expTry = new double[startpoint.length][];
    double[] expValues = new double[startpoint.length];
    int bestAt = 0;
    double best = 0.0;
    int len = startpoint[0].length;
    // System.out.println("Starting point");
    for (int i = 0; i < startpoint.length; i++)
    {
      tryHere[i] = new double[len];
      expTry[i] = new double[len];
      if (startpoint[i].length != len)
      {
        throw new IllegalArgumentException("Uneven lengths");
      }
      values[i] = f.f(startpoint[i]);
      // showAt(values[i], startpoint[i], 0, len);
      if (i == 0)
      {
        best = values[0];
      }
      else if (values[i] < best)
      {
         best = values[i];
	 bestAt = i;
      }
    }
    values[bestAt] = values[0];
    values[0] = best;
    double[] t = startpoint[0];
    startpoint[0] = startpoint[bestAt];
    startpoint[bestAt] = t;
    outer:for (;;)
    {
      // Here with values corresponding to startpoint, best
      // answer at index 0, and startpoint is a scaled, translated,
      // and rotated (but not distorted) version of the original
      // set of points. Since the same shape, we should find any
      // gradient as soon as we shrink small enough, assuming
      // the original shape was even vaguely reasonable (recommend
      // a simplex).
      best = values[0];
      for (;;)
      {
	// Check for convergence
	boolean difference = false;
	for (int i = 0; i < len; i++)
	{
	  double min = startpoint[0][i];
	  double max = min;
	  for (int j = 1; j < startpoint.length; j++)
	  {
	    double v = startpoint[j][i];
	    if (v < min)
	    {
	      min = v;
	    }
	    else if (v > max)
	    {
	      max = v;
	    }
	  }
	  double ma = Math.abs(min);
	  if (ma < Math.abs(max))
	  {
	    ma = Math.abs(max);
	  }
	  if (ma < ftol)
	  {
	    ma = ftol;
	  }
	  if ((max - min) > ma * ftol)
	  {
	    difference = true;
	    break;
	  }
	}
	if (!difference)
	{
	  break outer;
	}
	// Reflect everything in the minimum point.
	// System.out.println("Rotate");
	double rotated = tryTowards(startpoint,
	  tryHere, tryValues, 2.0, f, values[0]);
	if (rotated < best)
	{
	  // Here => successfully reduced minimum.
	  // Go as far again in the same direction.
	  // System.out.println("Expand");
	  double expBest = tryTowards(startpoint,
	    expTry, expValues, 3.0, f, values[0]);
	  if (expBest < rotated)
	  {
	    double[] tx = values;
	    values = expValues;
	    expValues = tx;
	    double[][] tt = startpoint;
	    startpoint = expTry;
	    expTry = tt;
	  }
	  else
	  {
	    double[] tx = values;
	    values = tryValues;
	    tryValues = tx;
	    double[][] tt = startpoint;
	    startpoint = tryHere;
	    tryHere = tt;
	  }
	  break;
	}
	else
	{
	  // Contract towards minimum point.
	  // System.out.println("Contract");
	  double contracted = tryTowards(startpoint,
	    tryHere, tryValues, 0.5, f, values[0]);
	  // will accept this, whether or not any good.
	  double[] tx = values;
	  values = tryValues;
	  tryValues = tx;
	  double[][] tt = startpoint;
	  startpoint = tryHere;
	  tryHere = tt;
	  if (contracted < best)
	  {
	    break;
	  }
	  // If here, no reduction but have contracted points.
	  // Will eventually either hit improvement or converge
	  // out of whole routine.
	}
      }
    }
    if (startpoint != originalStartpoint)
    {
      System.arraycopy(startpoint, 0, originalStartpoint, 0,
        startpoint.length);
    }
    return best;
  }
  /** Try at a point current[i][].(1-moveBy) + current[0][].moveBy.
   * Return
   * best value, and move around so that corresponds to index 0.
   */
  private static double tryTowards(double[][] current, 
    double[][] tryHere, double tryValues[],
    double moveBy, Function f, double zeroValue)
  {
    double m1 = 1.0 - moveBy;
    int bestAt = 0;
    double best = zeroValue;
    double[] towards = current[0];
    // Want to pick up value computed for previous best rather
    // than recomputing it at zero point (which stays the same).
    // Do this for consistency as well as efficiency.
    // showAt(zeroValue, towards, 0, towards.length);
    tryValues[0] = zeroValue;
    System.arraycopy(towards, 0, tryHere[0], 0, towards.length);
    for (int i = 1; i < current.length; i++)
    {
      double[] target = tryHere[i];
      double[] from = current[i];
      for (int j = 0; j < towards.length; j++)
      {
        target[j] = from[j] * m1 + towards[j] * moveBy;
      }
      tryValues[i] = f.f(target);
      // showAt(tryValues[i], target, 0, target.length);
      if (tryValues[i] < best)
      {
        best = tryValues[i];
	bestAt = i;
      }
    }
    tryValues[bestAt] = tryValues[0];
    tryValues[0] = best;
    double[] t = tryHere[0];
    tryHere[0] = tryHere[bestAt];
    tryHere[bestAt] = t;
    return best;
  }
  public static void fit(double[] x, double[] y, int first, int past,
    double[] constant, double[] coefficient, double[] meanAbDev,
    double tol)
  {
    // Calculate least squares fit
    double sx = 0.0;
    double sy = 0.0;
    double sxy = 0.0;
    double sxx = 0.0;
    for (int i = first; i < past; i++)
    {
      double xx = x[i];
      double yy = y[i];
      sx += xx;
      sy += yy;
      sxy += xx * yy;
      sxx += xx * xx;
    }
    int num = past - first;
    double del = num * sxx - sx * sx;
    if (del <= 0.0)
    {
      throw new IllegalArgumentException("X args degenerate");
    }
    double a = (sxx * sy - sx * sxy) / del;
    double b = (num * sxy - sx * sy) / del;
    double da = Math.abs(a) * 0.1 + 0.1;
    double db = Math.abs(b) * 0.1 + 0.1;
    double[][] simplex = new double[3][];
    simplex[0] = new double[]{a, b};
    simplex[1] = new double[]{a + da, b - db};
    simplex[2] = new double[]{a + da, b + db};
    MeanAbDev ma = new MeanAbDev(x, y, first, past);
    meanAbDev[0] = min(simplex, ma, tol);
    constant[0] = simplex[0][0];
    coefficient[0] = simplex[0][1];
  }
  private static class MeanAbDev implements Function
  {
    private double[] x;
    private double[] y;
    private int first;
    private int past;
    MeanAbDev(double[] x, double[] y, int first, int past)
    {
      this.x = x;
      this.y = y;
      this.first = first;
      this.past = past;
    }
    public double f(double[] arg)
    {
      double a = arg[0];
      double b = arg[1];
      double sum = 0.0;
      for (int i = first; i < past; i++)
      {
        double diff = y[i] - a - b * x[i];
	sum += Math.abs(diff);
      }
      return sum / (past - first);
    }
  }
}
