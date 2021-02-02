package uk.co.demon.mcdowella.stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.StringTokenizer;

/** This class is used to provide fitted values and bounds
    for a function at points and its derivative, based on
    an exponentially weighted least squares fit.
  */
public class WeightLS
{
  // Let's do the linear algebra first. We wish to minimise
  // SUM Wi(Yi - aXi - b)^2
  // so we want
  // SUM XiWi(Yi - aXi - b) = 0      SUM Wi(Yi - aXi - b) = 0
  // SUM XiWiYi - aSUM Xi^2Wi - bSUM XiWi = 0
  // SUM WiYi - aSUM XiWi - bSUM Wi = 0
  // R - aS -bT = 0
  // V - aT -bZ = 0
  // a = (VT - RZ) / (T^2 - ZS)
  // b = (RT - VS) / (T^2 - ZS)
  // where V = SUM WiYi R = SUM XiWiYi are the only contributions via Yi
  // T = SUM WiXi S = SUM WiXi^2 Z = SUM Wi
  // We are interested in ax + b which is therefore linear in Yi
  // and in d(ax + b)/dx which is more complicated than it looks
  // because everything involves Wi which is a function of x but is
  // also linear in Yi
  /** Given x, the Wi, Xi, and Yi, and dWi/dx, compute the linear
      functions giving f(x) and df(x)/dx. Return determinant, which
      should not be zero */
  public static double computeLinears(double xVal, double[] w,
    double[] dw, double[] x, double[] ylin, double[] ydlin)
  {
    // S, T, and Z are easy, as are their derivatives
    double s = 0.0;
    double t = 0.0;
    double z = 0.0;
    double dsdx = 0.0;
    double dtdx = 0.0;
    double dzdx = 0.0;
    for (int i = 0; i < w.length; i++)
    {
      final double wHere = w[i];
      z += wHere;
      final double dwHere = dw[i];
      dzdx += dwHere;
      final double xHere = x[i];
      final double xw = wHere * xHere;
      t += xw;
      final double dxw = dwHere * xHere;
      dtdx += dxw;
      s += xHere * xw;
      dsdx += xHere * dxw;
    }
    // System.out.println("z " + z + " t " + t + " s " + s);
    // System.out.println("dz " + dzdx + " dt " + dtdx + " ds " + dsdx);
    // value at x is ax + b = (V(Tx - S)  + R(T - Zx)) / (T^2 - ZS)
    // V provides terms as WiYi and R terms as WiXiYi
    double bottom = t * t - z * s;
    double txs = t * xVal - s;
    double tzx = t - z * xVal;
    double dBottomDx = 2 * t * dtdx - dzdx * s - z * dsdx;
    for (int i = 0; i < w.length; i++)
    {
      // First work out value as a linear function of y, which we can
      // do by simply ignoring the multiplicative contribution of y
      final double wHere = w[i];
      final double xHere = x[i];
      final double wx = wHere * xHere;
      ylin[i] = (wHere * txs + wx * tzx) / bottom;

      // Work out easy component of derivative, again pretending to ignore y
      double sofar = (wHere * t - wx * z) / bottom;
      // Work out how Tx - S and T - Zx change due to changes in T, S
      // and Z. This gets multiplied by wHere soon enough
      final double contrib = dtdx * xVal - dsdx + dtdx * xHere -
        dzdx * xHere * xVal;
      sofar += contrib * wHere / bottom;
      // work out how V(Tx-S) + R(T-Zx) change due to changes in W 
      // within V and R, which are WiYi and WiYiXi respectively
      final double contrib2 = txs + xHere * tzx;
      sofar += contrib2 * dw[i] / bottom;
      // now add in component due to change in bottom
      sofar -= ylin[i] * dBottomDx / bottom;
      ydlin[i] = sofar;
    }
    return bottom;
  }
  /** Produce Gaussian weights and their derivative. These have lots of
      nice properties; for one thing they have everywhere continuous
      derivatives. If you were convolving with the weights you would also
      have the nice property that convolving twice with two different sets
      of weights is equivalent to convolving once with a different set of
      Gaussian weights.
      @param centre the centre point, with maximum weight
      @param width the distance from the centre to 1-sigma points of 
        the underlying gaussian distribution, which is also the 
	distance to inflection points of the gaussian; the area
	between them gets just under 70% of the weights.
      @param x the points at which we want weights.
      @param w weights get written here.
      @param dw derivative of the weights w.r.t centre get written here.
      */
  public static void buildWeights(double centre, double width, double[] x,
    double[] w, double[] dw)
  {
      for (int j = 0; j < x.length; j++)
      {
	// Natural scale is Math.exp(-x^2/2), which has a width of
	// two
	double divisor = width * width * 2.0;
	double dist = x[j] - centre;
        w[j] = Math.exp(-dist * dist / divisor);
	// derivative w.r.t here
	dw[j] = 2.0 * dist * w[j] / divisor;
      }
  }
  /** test linear fit */
  public static void testFit(Random r)
  {
    double a = r.nextGaussian();
    double b = r.nextGaussian();
    final int len = 3 + r.nextInt(10);
    double[] w = new double[len];
    double[] x = new double[len];
    double[] y = new double[len];
    double[] ylin = new double[len];
    double[] ydlin = new double[len];
    double[] dw = new double[len];
    for (int i = 0; i < len; i++)
    {
      final double xh = r.nextGaussian();
      x[i] = xh;
      y[i] = a * xh + b; // + r.nextGaussian() / 10.0;
    }
    for (int i = 0; i < 10; i++)
    {
      double here = r.nextGaussian();
      buildWeights(here , 4.0, x, w, dw);
      double bottom = computeLinears(here, w, dw, x, ylin, ydlin);
      double fromLinear = 0.0;
      double derivLinear = 0.0;
      for (int j = 0; j < len; j++)
      {
        fromLinear += y[j] * ylin[j];
        derivLinear += y[j] * ydlin[j];
      }
      double answer = a * here + b;
      double delta = here * 0.0001;
      System.out.println("Bottom " + bottom + " guess " + fromLinear +
        " answer " + answer + " error " + (fromLinear - answer) + " delta " + delta);
      double h = here + delta;
      buildWeights(here + delta, 4.0, x, w, dw);
      computeLinears(here + delta, w, dw, x, ylin, ydlin);
      double fromLineard = 0.0;
      for (int j = 0; j < len; j++)
      {
        fromLineard += y[j] * ylin[j];
      }
      h = here - delta;
      buildWeights(here - delta, 4.0, x, w, dw);
      computeLinears(h, w, dw, x, ylin, ydlin);
      double fromLineard2 = 0.0;
      for (int j = 0; j < len; j++)
      {
        fromLineard2 += y[j] * ylin[j];
      }
      double d1 = (fromLineard - fromLinear) / delta;
      double d2 = (fromLineard2 - fromLinear) / -delta;
      System.out.println("Deriv " + derivLinear + " a1 " + d1 +
        " a2 " + d2);
    }
  }
  /** linF[i] is the value underlying point i as a linear function of
    the observed values with mean aMean[i]p[i] */
  private final double[][] linF;
  /** linDF[i] is the value at point i of the underlying slope as a linear
      function of the observed values with mean aMean[i]p[i] */
  private final double[][] lindF;
  /** copy of underlying a[] mean factor */
  private final double[] aMean;
  /** copy of underlying b[] variance factor */
  private final double[] bVar;
  /** WeightsLS object works out linear functions giving smoothed
      value and derivative at every point. Construct from points,
      a, b, and width. Width is a measure of the width of the gaussian
      smoother. a and b are arrays where we expect that with underlying
      value p[i] we have mean a[i]p[i] and variance b[i]p[i](1-p[i])
    */
  public WeightLS(double[] points, double[] a, double[] b,
    double width)
  {
    aMean = a.clone();
    bVar = b.clone();
    linF = new double[points.length][];
    lindF = new double[points.length][];
    double[] w = new double[points.length];
    double[] dw = new double[points.length];
    for (int i = 0; i < points.length; i++)
    {
      linF[i] = new double[points.length];
      lindF[i] = new double[points.length];
      // Work out a priori weights, which come from smoothing
      buildWeights(points[i], width, points, w, dw);
      // Our hypothesis is an underlying linear trend, but we have
      // a[i]y[i] instead of y[i] so we need to divide the observations
      // by a[i] This means that the variance of the ammended y[i] is 
      // not b[i] but b[i] / a[i]^2, and for the most efficient fits 
      // we want the weights to be proportional to the inverse of
      // the variance
      for (int j = 0; j < points.length; j++)
      {
	double aHere = aMean[j];
	if (aHere == 0.0)
	{
	  continue;
	}
	double scale = aHere * aHere / bVar[j];
        w[j] = w[j] * scale;
	// the weights are at our disposal; all we have to do is
	// keep dw[i][j] showing how the weight for a particular point
	// changes as we change the position at which we evaluate
	// the best fit, and we can do this by multiplying w by the same factor.
	dw[j] = dw[j] * scale;
      }
      computeLinears(points[i], w, dw, points, linF[i], lindF[i]);
      // This gives us functions of input y[i] / a[i]; we want them as
      // functions of y[i]
      for (int j = 0; j < points.length; j++)
      {
        double aHere = aMean[j];
	if (aHere == 0.0)
	{
	  continue;
	}
	final double[] f1 = linF[i];
	f1[j] = f1[j] / aHere;
	final double[] f2 = lindF[i];
	f2[j] = f2[j] / aHere;
      }
    }
  }
  /** work out fitted points for underlying curve given y */
  public void getFitted(double[] y, double[] fitted)
  {
    for (int i = 0; i < linF.length; i++)
    {
      double sofar = 0.0;
      double[] lf = linF[i];
      for (int j = 0; j < linF.length; j++)
      {
        sofar += lf[j] * y[j];
      }
      fitted[i] = sofar;
    }
  }
  /** work out value for underlying slope given y */
  public void getSlope(double[] y, double[] slope)
  {
    for (int i = 0; i < lindF.length; i++)
    {
      double sofar = 0.0;
      double[] lf = lindF[i];
      for (int j = 0; j < lindF.length; j++)
      {
        sofar += lf[j] * y[j];
      }
      slope[i] = sofar;
    }
  }
  /** produce bounds for linear function of values with mean
      a[i]p[i] and variance b[i]p[i](1-p[i]). given the sigmage for the
      bounds, the linear function of the observed values, a place
      to put the answer, and the observed value of the linear function applied
      to the real data.
    */
  public void doBounds(double sigmage, double[] fun,
    double[] twoBounds, double obs)
  {
    // make coefTemp a quadratic which tells us the maximum variance of
    // a linear function, fun, of the underlying P[i], subject to a
    // constraint on its value.
    double[] abcCoeffs = new double[3];
    double[] conFun = new double[fun.length];
    double[] varFun = new double[fun.length];
    for (int i = 0; i < fun.length; i++)
    {
      final double fHere = fun[i];
      final double am = aMean[i];
      // We have a function of the observed values with mean aMean[i]p[i]
      // but our constraint should be a function of the underlying p[i]
      conFun[i] = fHere * am;
      // Variance of observed value is bVar[i]p[i](1-p[i]), so variance
      // of fHere times this is fHere^2 that
      varFun[i] = bVar[i] * fHere * fHere;
    }
    // QuadVar maximises SUM_i varFun[i]p[i](1-p[i]) subject to
    // SUM_i conFun[i]p[i] = x, returning the coefficients for a quadratic
    // in x, so we are finding the p[i] that maximise the variance of
    // the observed value subject to the constraint that the observed
    // value is x and returning the result as a quadratic in x
    QuadVar.quadVar(varFun, conFun, abcCoeffs);
    // System.err.println("Quadratic " + Arrays.toString(abcCoeffs));
    // now need to solve
    // (obs - x)^2 = sigmage^2 * (ax^2 + bx + c)
    // lets work out aax^2 + bbx + cc = 0
    // System.out.println("Obs => " + obs);
    final double s2 = sigmage * sigmage;
    final double aa = s2 * abcCoeffs[0] - 1.0;
    final double bb = s2 * abcCoeffs[1] + 2 * obs;
    final double cc = s2 * abcCoeffs[2] - obs * obs;
    // we want (-bb +/- sqrt(bb^2 - 4 aa cc)) / 2 aa
    final double discrim = bb * bb - 4.0 * aa * cc;
    if (discrim < 0.0)
    { // no solution - this should not happen as we always have a
      // solution for sigmage = 0 from the linear fit and we can go
      // as far away from that as we like
      System.err.println("conFun " + Arrays.toString(conFun));
      System.err.println("varFun " + Arrays.toString(varFun));
      System.err.println("aMean " + Arrays.toString(aMean));
      throw new IllegalArgumentException("No solution");
    }
    if (aa == 0.0)
    {
      // I don't think we can ever be here
      // because I think aa is always -ve
      throw new IllegalArgumentException("aa == 0.0");
    }
    if (discrim == 0.0)
    {
      final double x =  -bb / (2.0 * aa);
      twoBounds[0] = x;
      twoBounds[1] = x;
      return;
    }
    final double rootDiscrim = Math.sqrt(discrim);
    // standard trick to avoid cancellation
    double root1;
    if (bb < 0.0)
    {
      root1 = (-bb + rootDiscrim) / (2.0 * aa);
    }
    else
    {
      root1 = (-bb - rootDiscrim) / (2.0 * aa);
    }
    // sum of roots is -b/a product is c/a
    double root2 = cc / (aa * root1);
    if (root1 < root2)
    {
      twoBounds[0] = root1;
      twoBounds[1] = root2;
    }
    else
    {
      twoBounds[0] = root2;
      twoBounds[1] = root1;
    }
  }
  /** work out upper and lower bounds throughout in one fell swoop */
  public void getBounds(double[] allObs, double sigmage,
    double[] lower, double[] upper, double[] lowerD, double[] upperD)
  {
    double[] pair = new double[2];
    for (int i = 0; i < allObs.length; i++)
    {
      double sofar = 0.0;
      final double[] lf1 = linF[i];
      for (int j = 0; j < allObs.length; j++)
      {
        sofar += lf1[j] * allObs[j];
      }
      doBounds(sigmage, lf1, pair, sofar);
      lower[i] = pair[0];
      upper[i] = pair[1];
      final double[] lf2 = lindF[i];
      sofar = 0.0;
      for (int j = 0; j < allObs.length; j++)
      {
        sofar += lf2[j] * allObs[j];
      }
      doBounds(sigmage, lf2, pair, sofar);
      lowerD[i] = pair[0];
      upperD[i] = pair[1];
    }
  }
  public static void testFit2(Random r)
  {
    final double lineA = r.nextGaussian();
    final double lineB = r.nextGaussian();
    final int len = 3 + r.nextInt(10);
    final double[] points = new double[len];
    final double[] a = new double[len];
    final double[] b = new double[len];
    final double[] y = new double[len];
    for (int i = 0; i < len; i++)
    {
      final double here = r.nextGaussian();
      points[i] = here;
      a[i] = r.nextGaussian();
      final double bb = r.nextGaussian();
      b[i] = bb * bb;
      y[i] = (lineA * here + lineB) * a[i];
    }
    double width = 4.0 + r.nextGaussian();
    WeightLS wls = new WeightLS(points, a, b, width);
    double[] fitted = new double[len];
    wls.getFitted(y, fitted);
    double[] slope = new double[len];
    wls.getSlope(y, slope);
    for (int i = 0; i < len; i++)
    {
      double err = fitted[i] - (points[i] * lineA + lineB);
      double serr = slope[i] - lineA;
      if ((Math.abs(err) > 1.0E-10) || (Math.abs(serr) > 1.0E-10))
      {
	throw new IllegalStateException(
	  "Value error " + err + " slope error " + serr);
      }
    }
  }
  /** Usual main program */
  public static void main(String[] s) throws Exception
  {
    long seed = 42;
    double sigmage = 2.5758;
    int postFits = 10000;
    double width = -1.0;

    int s1 = s.length - 1;
    int testFits = 100;
    boolean trouble = false;
    String num = "";
    try
    {
      for (int i = 0; i < s.length; i++)
      {
	if("-postFits".equals(s[i]) && (i < s1))
	{
	  num = s[++i];
	  postFits = Integer.parseInt(num.trim());
	}
	else if("-seed".equals(s[i]) && (i < s1))
	{
	  num = s[++i];
	  seed = Long.parseLong(num.trim());
	}
	else if("-testFits".equals(s[i]) && (i < s1))
	{
	  num = s[++i];
	  testFits = Integer.parseInt(num.trim());
	}
	else if("-sig".equals(s[i]) && (i < s1))
	{
	  num = s[++i];
	  sigmage = Double.parseDouble(num.trim());
	}
	else if("-width".equals(s[i]) && (i < s1))
	{
	  num = s[++i];
	  width = Double.parseDouble(num.trim());
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
    if (width <= 0.0)
    {
      System.out.println("Must quote width > 0");
      trouble = true;
    }

    if (trouble)
    {
      System.err.println("Args are [-postFits #] [-seed #] [-sig #] " +
        "[-testFits #] [-width #]");
      return;
    }
    System.err.println("PostFits " + postFits + " seed " + seed +
      " sig " + sigmage + " testFits " + testFits + " width " + width);

    // self-test
    Random r = new Random(seed);
    for (int i = 0; i < testFits; i++)
    {
      // System.out.print('.');
      testFit2(r);
    }

    List<Double> numObs = new ArrayList<Double>();
    List<Double> xVals = new ArrayList<Double>();
    List<Double> yVals = new ArrayList<Double>();
    BufferedReader br = new BufferedReader(
      new InputStreamReader(System.in));
    String line = null;
    try
    {
      for (;;)
      { // Read line giving # observations, x value, total y values
	// where # can be floating, to be used as a weight
	line = br.readLine();
	if (line == null)
	{
	  break;
	}
	line = line.trim();
	if ((line.length() == 0) || line.startsWith("#"))
	{  // blank or comment
	  continue;
	}
	StringTokenizer st = new StringTokenizer(line, " \t\n\r,");
	numObs.add(new Double(st.nextToken().trim()));
	xVals.add(new Double(st.nextToken().trim()));
	yVals.add(new Double(st.nextToken().trim()));
      }
    }
    catch (NumberFormatException nfe)
    {
      System.err.println("Could not read <numObs> <xVal> <sumYVal> in " +
        line);
      return;
    }
    catch (NoSuchElementException nfe)
    {
      System.err.println("Could not find <numObs> <xVal> <sumYVal> in " +
        line);
      return;
    }
    double[] a = new double[numObs.size()];
    double[] points = new double[a.length];
    double[] obs = new double[a.length];
    for (int i = 0; i < a.length; i++)
    {
      a[i] = numObs.get(i);
      points[i] = xVals.get(i);
      obs[i] = yVals.get(i);
    }
    WeightLS wls = new WeightLS(points, a, a, width);
    double[] fitted = new double[points.length];
    wls.getFitted(obs, fitted);
    double[] derivs = new double[points.length];
    wls.getSlope(obs, derivs);
    double[] lower = new double[points.length];
    double[] upper = new double[points.length];
    double[] lowerD = new double[points.length];
    double[] upperD = new double[points.length];
    wls.getBounds(obs, sigmage, lower, upper, lowerD, upperD);
    for (int i = 0; i < fitted.length; i++)
    {
      System.out.println(a[i] + " " + points[i] + " " + obs[i] +
        " " + fitted[i] + " " + lower[i] + " " + upper[i] +
	" " + derivs[i] + " " + lowerD[i] + " " + upperD[i]);
    }
    if (postFits > 0)
    {
      MonteWLS msf = new MonteWLS(wls, a, a, obs, fitted,
        new Random(seed), sigmage);
      msf.runFits(postFits);
      msf.print();
    }
  }
}
