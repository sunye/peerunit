package uk.co.demon.mcdowella.stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.io.StreamTokenizer;

/** A very simple smoother. Fits a line by least squares within a
 *  window
 */
public class Smooth implements Smoother
{
  /** reusable method to return sorted array of Points */
  static Point[] createPoints(double[] xValues, double[] yValues)
  {
    if (xValues.length != yValues.length)
    {
      throw new IllegalArgumentException("lengths do not match");
    }
    if (xValues.length < 3)
    {
      throw new IllegalArgumentException("Must have at least 3 points");
    }
    // Now sort by x value
    Point[] p = new Point[xValues.length];
    for (int i = 0; i < p.length; i++)
    {
      p[i] = new Point(xValues[i], yValues[i]);
    }
    Arrays.sort(p);
    return p;
  }
  /** Create from x and y values */
  public Smooth(double[] xValues, double[] yValues)
  {
    Point[] p = createPoints(xValues, yValues);
    x = new double[p.length];
    y = new double[p.length];
    for (int i = 0; i < p.length; i++)
    {
      x[i] = p[i].getX();
      y[i] = p[i].getY();
    }
    p = null;
  }
  /** Fit line to each point. Return estimate of total squared error
   *  from cross-validation. This automatically chooses the best fit
   *  and returns the sum of cross-validated errors.
   */
  public double autoFit()
  {
    return innerAutoFit(3, x.length, this);
  }
  /** Reusable auto fit. Tries a range of values */
  static double innerAutoFit(int first, int last, Smoother sm)
  {
    int best = -1;
    double error = Double.MAX_VALUE;
    boolean goingUp = false;
    boolean haveDirection = false;
    double lastError = 0.0;
    boolean haveLastError = false;
    int changes = 0;
    for (int i = first; i <= last; i++)
    {
      double errorHere = sm.fit(i);
      // System.out.println("Error for " + i + " is " + errorHere);
      if (errorHere < error)
      {
	error = errorHere;
	best = i;
      }
      if (haveLastError)
      {
        if (errorHere < lastError)
	{
	  if (haveDirection & goingUp)
	  {
	    // System.out.println("CHANGE");
	    changes++;
	  }
	  goingUp = false;
	  haveDirection = true;
	}
	else if (errorHere > lastError)
	{
	  if (haveDirection && !goingUp)
	  {
	    changes++;
	    // System.out.println("CHANGE");
	  }
	  goingUp = true;
	  haveDirection = true;
	}
      }
      lastError = errorHere;
      haveLastError = true;
    }
    System.err.println(changes + " changes in direction");
    return sm.fit(best);
  }
  /** last window length or -1 */
  private int lastFit = -1;
  /** return last window length or -1 */
  public int getWindowLength()
  {
    return lastFit;
  }
  /** Fit line to each point. Return estimate of total squared error
   *  from cross-validation.
   * @param windowLength fit to window of this many different x values.
   *  Must be >= 3.
   */
  public double fit(int windowLength)
  {
    if (windowLength < 3)
    {
      throw new IllegalArgumentException("Window too short");
    }
    if (windowLength > x.length)
    {
      throw new IllegalArgumentException("Window too long");
    }
    lastFit = windowLength;
    // Will keep running totals here
    double xxRun = 0.0;
    double xyRun = 0.0;
    double yRun = 0.0;
    double xRun = 0.0;
    // initialise by fitting first windowLength points
    for (int i = 0; i < windowLength; i++)
    {
      double xHere = x[i];
      double yHere = y[i];
      xxRun += xHere * xHere;
      xyRun += xHere * yHere;
      xRun += xHere;
      yRun += yHere;
    }
    slope = new double[x.length];
    constant = new double[x.length];
    double[] fit = new double[2];
    double result = 0.0;
    int youngest = windowLength - 1;
    int oldest = 0;
    int firstChange = windowLength / 2;
    for (int i = 0; i < x.length; i++)
    { // use current fit
      doFit(xxRun, xyRun, xRun, yRun, windowLength, fit);
      slope[i] = fit[0];
      constant[i] = fit[1];
      double xHere = x[i];
      double yHere = y[i];
      doFit(xxRun - xHere * xHere, xyRun - xHere * yHere,
        xRun - xHere, yRun - yHere, windowLength - 1, fit);
      double error = fit[0] * xHere + fit[1] - yHere;
      result += error * error;
      if ((i >= firstChange) && (youngest < (x.length - 1)))
      { // here => ditch oldest point in window and add newer one
        double xLost = x[oldest];
	double yLost = y[oldest];
	oldest++;
	youngest++;
	double xGained = x[youngest];
	double yGained = y[youngest];
	xxRun = xGained * xGained + (xxRun - xLost * xLost);
	xyRun = xGained * yGained + (xyRun - xLost * yLost);
	xRun = xGained + (xRun - xLost);
	yRun = yGained + (yRun - yLost);
      }
    }
    return result;
  }
  /** fit a straight line */
  private void doFit(double xxTotal, double xyTotal, double xTotal,
    double yTotal, int len, double[] result)
  {
    double base = xTotal * xTotal - len * xxTotal;
    if (base >= 0)
    { // all x values are the same
      result[0] = 0.0;
      result[1] = yTotal / len;
      return;
    }
    result[0] = (yTotal * xTotal - xyTotal * len) / base;
    result[1] = (xyTotal * xTotal - yTotal * xxTotal) / base;
  }
  /** return the value at a given x point */
  public double getValue(double target)
  {
    return innerGetValue(target, x, slope, constant);
  }
  /** get value using binary chop then linear interpolation */
  static double innerGetValue(double target, double[] x,
    double slope[], double constant[])
  {
    if (target < x[0])
    {
      return slope[0] * target + constant[0];
    }
    int last = x.length - 1;
    if (target > x[last])
    {
      return slope[last] * target + constant[last];
    }
    // find leftmost value >= target. Must be such or would
    // have finished by now
    int first = 0;
    while (first < last)
    { // here with answer somewhere among [first, last]
      int probe = (first + last) / 2;
      // first <= probe < last
      double val = x[probe];
      if (val < target)
      { // probe too far to left
        first = probe + 1;
	continue;
      }
      if (val >= target)
      { // cut down possibilities since probe < last
        last = probe;
      }
    }
    double val = x[last];
    if (val > target)
    {
      double xRight = x[last];
      // recursive call ensures continuity when multiple x with
      // same value
      double fromRight = innerGetValue(xRight, x, slope, constant);
      last--;
      double xLeft = x[last];
      double fromLeft = innerGetValue(xLeft, x, slope, constant);
      return (fromLeft * (xRight - target) +
              fromRight * (target - xLeft)) / (xRight - xLeft);
    }
    int num = 0;
    double sum = 0.0;
    for (;(last < x.length) && (x[last] == target); last++)
    {
      num++;
      sum += slope[last] * target + constant[last];
    }
    return sum / num;
  }
  /** x values */
  private final double[] x;
  /** y values */
  private final double[] y;
  /** slope of linear fit at each point */
  private double[] slope;
  /** constant for linear fit at each point */
  private double[] constant;
  /** Info per point */
  static class Point implements Comparable
  {
    private final double x;
    private final double y;
    Point(double forX, double forY)
    {
      x = forX;
      y = forY;
    }
    public double getX()
    {
      return x;
    }
    public double getY()
    {
      return y;
    }
    public int compareTo(Object o)
    {
      Point p = (Point)o;
      if (x < p.x)
      {
        return -1;
      }
      if (x > p.x)
      {
        return 1;
      }
      return 0;
    }
  }
  public static void main(String[] s) throws IOException
  {
    int span = -1;
    boolean trouble = false;
    int s1 = s.length - 1;
    int argp = 0;
    boolean tri = true;
    List xValues = new ArrayList();
    try
    {
      for (; argp < s.length; argp++)
      {
	if ("-rect".equals(s[argp]))
	{
	  tri = false;
	}
	else if ((argp < s1) && ("-span".equals(s[argp])))
	{
	  argp++;
	  span = Integer.parseInt(s[argp].trim());
	}
	else if ((argp < s1) && "-x".equals(s[argp]))
	{
	  argp++;
	  xValues.add(new Double(s[argp].trim()));
	}
	else
	{
	  System.err.println("Could not handle flag " + s[argp]);
	  trouble = true;
	}
      }
    }
    catch (NumberFormatException nfe)
    {
      System.err.println("Could not read number in " + s[argp]);
    }
    if (trouble)
    {
      System.err.println("args are [-rect] [-span #] [-x #]*");
      return;
    }
    // read x and y values
    BufferedReader br = new BufferedReader(new InputStreamReader(
      System.in));
    StreamTokenizer st = new StreamTokenizer(br);
    st.resetSyntax();
    st.commentChar('#');
    // Have to read numbers as words because StreamTokenizers don't
    // handle exponentials
    st.wordChars('0', '9');
    st.wordChars('-', '-');
    st.wordChars('+', '+');
    st.wordChars('.', '.');
    st.wordChars('e', 'e');
    st.wordChars('E', 'E');
    st.slashSlashComments(true);
    st.slashStarComments(true);
    List l = new ArrayList();
    for (;;)
    {
      int tok = st.nextToken();
      if (tok == StreamTokenizer.TT_EOF)
      {
        break;
      }
      else if (tok == StreamTokenizer.TT_WORD)
      {
        try
	{
	  l.add(new Double(st.sval.trim()));
	}
	catch (NumberFormatException nfe)
	{
	  System.err.println("Cannot read number in " + st.sval);
	  return;
	}
      }
      else if (tok != StreamTokenizer.TT_EOL)
      {
	if (tok >= 0)
	{
	  char here = (char)tok;
	  if (Character.isWhitespace(here))
	  {
	    continue;
	  }
	  System.err.println("Could not handle char " + here +
	    " in input");
	  return;
	}
        System.err.println("Could not handle funny token in input");
	return;
      }
    }
    int len = l.size();
    if ((len & 1) != 0)
    {
      System.err.println("No matching y for final x value ");
      return;
    }
    double[] xv = new double[len / 2];
    double[] yv = new double[xv.length];
    int row = 0;
    for (Iterator i = l.iterator(); i.hasNext();)
    {
      xv[row] = ((Double)i.next()).doubleValue();
      yv[row] = ((Double)i.next()).doubleValue();
      row++;
    }
    if (xv.length < 3)
    {
      System.err.println("Only " + xv.length + " rows");
      return;
    }
    Smoother sm;
    if (tri)
    {
      System.err.println("Using triangle smoother");
      sm = new TriangleSmoother(xv, yv);
    }
    else
    {
      System.err.println("Using rectangle smoother");
      sm = new Smooth(xv, yv);
    }
    if ((span < 3) || (span > xv.length))
    {
      double error = sm.autoFit();
      System.err.println("Error for autoFit is " + error +
        " from len " + sm.getWindowLength());
    }
    else
    {
      double error = sm.fit(span);
      System.err.println("Error for span " + span + " is " +
        error);
    }
    if (xValues.isEmpty())
    {
      for (int i = 0; i < xv.length; i++)
      {
	double here = xv[i];
	double fitted = sm.getValue(here);
	System.out.println(here + ", " + yv[i] + ", " + fitted +
	  ", " + (yv[i] - fitted));
      }
    }
    else
    {
      for (Iterator i = xValues.iterator(); i.hasNext();)
      {
        Double d = (Double)i.next();
	double x = d.doubleValue();
	System.out.println(x + ", " + sm.getValue(x));
      }
    }
  }
}
