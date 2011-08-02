package uk.co.demon.mcdowella.stats;

/** This smoother fits a straight line within a window, but it
 *  applies more weight to the point at the centre of the window.
 *  If you keep track of the previous n values it is easy to maintain
 *  a total of the last n values with work per value received
 *  independent of n. If you put two of these filters together, with
 *  the output of the first being the input of the second, you get
 *  a filter of length 2n-1, with a pattern of coeffients
 *  1, 2, 3, 4, ... n-1, n, n-1, .... 4, 3, 2, 1. We use that
 *  filter pattern to weight our linear fits
 */
public class TriangleSmoother implements Smoother
{
  /** x values */
  private final double[] x;
  /** y values */
  private final double[] y;
  /** fitted slope at point */
  private double[] slope;
  /** fitted constant at point */
  private double[] constant;
  /** create from data to be smoothed. Must have at least 3 points */
  TriangleSmoother(double[] xValues, double[] yValues)
  {
    Smooth.Point[] p = Smooth.createPoints(xValues, yValues);
    x = new double[p.length];
    y = new double[p.length];
    for (int i = 0; i < p.length; i++)
    {
      x[i] = p[i].getX();
      y[i] = p[i].getY();
    }
    p = null;
  }
  public double autoFit()
  {
    return Smooth.innerAutoFit(3, x.length, this);
  }
  /** last window length or -1 */
  private int lastFit = -1;
  /** return last window length or -1 */
  public int getWindowLength()
  {
    return lastFit;
  }
  /** fit given a window parameter and return sum of squared
   *  errors from Cross-Validation
   */
  public double fit(int windowLength)
  {
    if ((windowLength < 3) || (windowLength > x.length))
    {
      throw new IllegalArgumentException("Bad window length");
    }
    lastFit = windowLength;
    slope = new double[x.length];
    constant = new double[x.length];
    TriangleFilter xf = new TriangleFilter(windowLength);
    TriangleFilter x2 = new TriangleFilter(windowLength);
    TriangleFilter c = new TriangleFilter(windowLength);
    TriangleFilter yf = new TriangleFilter(windowLength);
    TriangleFilter xy = new TriangleFilter(windowLength);
    double[] fit = new double[2];
    double sum = 0.0;
    for (int i = 0;; i++)
    {
      // These will be dummy values when we start running off the
      // end of the data
      double xHere = 0.0;
      double yHere = 0.0;
      double cHere = 0.0;
      if (i < x.length)
      {
        xHere = x[i];
	yHere = y[i];
	cHere = 1.0;
      }
      double xyHere = xHere * yHere;
      double x2Here = xHere * xHere;
      // System.out.println(" X2 " + x2Here);
      double xFiltered = xf.accept(xHere);
      double x2Filtered = x2.accept(x2Here);
      // System.out.println(" x2Filtered " + x2Filtered);
      double cFiltered = c.accept(cHere);
      double yFiltered = yf.accept(yHere);
      double xyFiltered = xy.accept(xyHere);
      // This is the index of the point with most weight in our
      // filter
      int wp = i - windowLength + 1;
      if (wp >= 0)
      {
        if (wp >= x.length)
	{ // Have finished
	  break;
	}
	/*
	System.out.println("wp " + wp + " x " + xFiltered + " y " +
	  yFiltered + " c " + cFiltered);
	*/
	dofit(xFiltered, x2Filtered, cFiltered, yFiltered, xyFiltered, 
	  fit);
        slope[wp] = fit[0];
	constant[wp] = fit[1];
	xHere = x[wp];
	yHere = y[wp];
	xyHere = xHere * yHere;
	x2Here = xHere * xHere;
	dofit(xFiltered - xHere * windowLength, x2Filtered -
	  x2Here * windowLength, cFiltered - windowLength,
	  yFiltered - yHere * windowLength,
	  xyFiltered - xyHere * windowLength, fit);
	double fitted = fit[0] * xHere + fit[1];
        double error = fitted - yHere;
	/*
	if (fit[0] == 0.0)
	{
	  System.out.println(" 0 at " + wp + " len " + windowLength +
	    " cSum " + cFiltered + " xFiltered " + xFiltered +
	    " x2Filtered " + x2Filtered + " xHere " + xHere +
	    " yFiltered " + yFiltered + " yHere " + yHere +
	    " xyFiltered " + xyFiltered + " xyHere " + xyHere);
	}
	*/
	sum += error * error;
      }
    }
    return sum;
  }
  /** For weighting to work, we treat this as minimising a total
   *  of terms (y_i - mx_i - cz_i)^2 where in the original data z
   *  is always 1.0. The solution of this can be stated in terms
   *  of sums of products x_i*x_i, x_i*y_i and so on.
   */
  void dofit(double xSum, double x2Sum, double cSum, double ySum,
    double xySum, double[] fit)
  {
    double base = x2Sum * cSum - xSum * xSum;
    if (base <= 0.0)
    { // all x values are the same
      /*
      System.out.println(" X2Sum " + x2Sum + " cSum " + cSum +
        " xSum " + xSum);
      */
      fit[0] = 0.0;
      fit[1] = ySum / cSum;
      return;
    }
    fit[0] = (xySum * cSum - xSum * ySum) / base;
    fit[1] = (x2Sum * ySum - xySum * xSum) / base;
    /*
    if (fit[0] == 0)
    {
      System.out.println(" xySum " + xySum + " cSum " + cSum +
        " xSum " + xSum + " ySum " + ySum);
    }
    */
  }
  /** return the value at a given x point */
  public double getValue(double target)
  {
    return Smooth.innerGetValue(target, x, slope, constant);
  }
  /** Does single level of window filter */
  private static class Filter
  {
    Filter(int len)
    {
      window = new double[len];
    }
    private final double[] window;
    private double sum;
    private int wp;
    double accept(double input)
    {
      double oldest = window[wp];
      sum += input - oldest;
      window[wp] = input;
      if ((++wp) >= window.length)
      {
        wp = 0;
      }
      return sum;
    }
  }
  /** Does double level of window filtering */
  private static class TriangleFilter
  {
    private final Filter first;
    private final Filter second;
    TriangleFilter(int windowLength)
    {
      first = new Filter(windowLength);
      second = new Filter(windowLength);
    }
    double accept(double input)
    {
      return second.accept(first.accept(input));
    }
  }
}
