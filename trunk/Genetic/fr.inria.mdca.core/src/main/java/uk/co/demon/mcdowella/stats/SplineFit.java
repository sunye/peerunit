package uk.co.demon.mcdowella.stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

/** This class allows you to build and fit splines to data, to
    produce the value of the spline at a point, and the derivative
    of the spline at that point. You can also produce confidence
    bounds, if you believe that the observation at each point
    has mean ap and variance bp(1-p), for point-dependent pre-specified
    a and b, where p is what is tracked by the spline.
  */
public class SplineFit
{
  /** A value at each point: expectation under model is ap, where p
      is the value assigned by the spline. a = 0 => no data */
  private final double[] aVal;
  /** B value at each point: variance under model is bp(1-p), where p
      is the value assigned by the spline */
  private final double[] bSqrt;
  /** spline basis. */
  private final double[][] splineTerm;
  /** derivative of spline basis */
  private final double[][] splineDeriv;
  /** vector giving the locations of the actual data points */
  private final int[] dataPoints;
  /** LU to be used to solve the least-squares fit */
  private LU lu;
  /** Get determinant of fit matrix for these observations */
  public double getFitDet()
  {
    return lu.getDeterminant();
  }
  /** Create given the position of the knots, the points (which
   must be enclosed by the knots), and a and b values for each point.
   There is an underlying value tracked by the spline which is p_i
   at point i, and the observation has mean a[i]p_i and variance
   b[i]p_i(1-p_i). Take a[i] = 0 to mean points at which we have no
   data.
   */
  public SplineFit(double[] knots, double[] points, double[] a,
    double[] b, int degree)
  {
    if (a.length != b.length)
    {
      throw new IllegalArgumentException("a length != b length");
    }
    if (points.length != a.length)
    {
      throw new IllegalArgumentException("points length != a length");
    }
    aVal = a.clone();
    bSqrt = new double[b.length];
    splineTerm = Spline.createBasis(knots, points, degree, false);
    splineDeriv = Spline.createBasis(knots, points, degree, true);
    /*
    System.out.println("Spline basis is " +
      Arrays.deepToString(splineTerm));
    */
    int numData = 0;
    for (int i = 0; i < b.length; i++)
    {
      if (aVal[i] == 0.0)
      {
        continue;
      }
      numData++;
    }
    // To fit y = Mx we need y = (M'M)^-1M'y
    double[] mm = new double[splineTerm.length * splineTerm.length];
    dataPoints = new int[numData];
    int wp = 0;
    double[] termHere = new double[splineTerm.length];
    for (int i = 0; i < aVal.length; i++)
    {
      double aHere = aVal[i];
      if (aHere == 0.0)
      {
        continue;
      }
      dataPoints[wp++] = i;
      // Take root of variance term and arrange the spline term so
      // that everything will have the same residual variance. The
      // variance of data is bp(1-p), so we want to divide it
      // by sqrt(b) before the fit. Therefore the linear vector must
      // predict data / sqrt(b), so we want it to be 
      // spline * a / sqrt(b)
      double root = Math.sqrt(b[i]);
      bSqrt[i] = root;
      for (int j = 0; j < splineTerm.length; j++)
      {
        termHere[j] = (splineTerm[j][i] * aHere) / root;
      }
      for (int j = 0; j < splineTerm.length; j++)
      {
        double m1 = termHere[j];
	mm[j + splineTerm.length * j] += m1 * m1;
	for (int k = 0; k < j; k++)
	{
	  double mf = m1 * termHere[k];
	  mm[j + splineTerm.length * k] += mf;
	  mm[k + splineTerm.length * j] += mf;
	}
      }
    }
    // System.out.println("MM is " + Arrays.toString(mm));
    lu = new LU(splineTerm.length, mm);
  }
  /** produce set of possible underlying values for a linear function
  of the contributions of each basis function,
  expressed as a series of intervals stored in the array returned.
  We have a linear function of the observations that gives an
  estimate of the underlying value we want. The variance of that
  estimate has a bound that is a quadratic function of the underlying
  value. We solve for this being sigmage away from the observed value
  of the linear function to work out the boundary of the our intervals.
  */
  public double[] singleBounds(double[] allObs, double sigmage,
    double[] basisContrib)
  {
    double d = lu.getDeterminant();
    if (d == 0.0)
    {
      throw new IllegalArgumentException(
        "Cannot get bounds as zero determinant");
    }
    // Need to work out linear function of spline value - evaluate
    // at every unit vector
    double[] fun = new double[dataPoints.length];
    double[] soln = new double[splineTerm.length];
    double[] my = new double[splineTerm.length];
    for (int i = 0; i < dataPoints.length; i++)
    {
      // Work out my using just this point, since all other points
      // are zero
      int index = dataPoints[i];
      // take out bSqrt to get residual variance down to constant
      double root = bSqrt[index];
      // probe with observation 1.0 here and 0.0 everywhere else
      // and divide by sqrt(var estimate) as usual to use same
      // equations as before
      double aHere = aVal[index];
      double obs = 1.0 / root; // and set this point to 1.0, while
      // dividing by root as before to put residuals on a common
      // variance
      for (int j = 0; j < splineTerm.length; j++)
      {
	// modify splineTerm as in computing M'M
        my[j] = (splineTerm[j][index] * aHere / root) * obs;
      }
      lu.solve(my, soln);
      // System.out.println("Soln " + Arrays.toString(soln));
      double val = 0.0;
      for (int j = 0; j < soln.length; j++)
      {
	val += soln[j] * basisContrib[j];
      }
      fun[i] = val;
    }
    double obs = 0;
    for (int i = 0; i < dataPoints.length; i++)
    {
      int index = dataPoints[i];
      obs += fun[i] * allObs[index];
    }
    // Work out a vector of Ai such that SUM_i AiPi(1-Pi) is the
    // variance of our estimated underlying spline value
    double[] a = new double[dataPoints.length];
    for (int i = 0; i < dataPoints.length; i++)
    {
      // Each contribution to our function is independent and starts
      // off with variance Pi(1-Pi). We need to take into account the
      // fact that it is multiplied by fun[i], and that we had a weight
      // provided for us, and we took its square root.
      double bp = fun[i];
      double sd = bSqrt[i] * bp;
      a[i] = sd * sd;
    }
    // System.out.println("Variance weights " + Arrays.toString(a));
    // fun currently holds a linear function taking a vector of
    // observations to an estimate for the underlying spline value
    // at a single point. Lets turn it into a vector giving the
    // expected spline value produced given the values for pi
    // at each point, by multiplying by Ai
    for (int i = 0; i < dataPoints.length; i++)
    {
      fun[i] *= aVal[dataPoints[i]];
    }
    // If we start off with a proposed value for
    // the spline at that point that puts a constraint on the possible
    // values of Pi, through SUM_i BiPi = x, where our function is
    // the Bi, and x is the proposed underlying spline value. We
    // want to work out the maximum possible variance of that linear
    // function, subject to this constraint. If we can write this
    // variance as SUM_i AiPi(1-Pi) we have a function that will solve
    // this for us, given the Ai and Pi, returning the result as a
    // quadratic function of x, the proposed underlying value.
    // abcCoeffs will be coeffs of ax^2 + bx + c where x
    // is the value of SUM_i BiPi and the result is the maximum
    // possible variance for Pi producing that x.
    double[] abcCoeffs = new double[3];
    QuadVar.quadVar(a, fun, abcCoeffs);
    // I think a coeff is always -ve as large values of x force
    // p(1-p) to be -ve
    // System.out.println("Coeffs " + Arrays.toString(abcCoeffs));
    // now need to solve for (obs - x)^2 = sigmage^2 * (ax^2 + bx + c)
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
      return new double[0];
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
      return new double[] {x, x};
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
      return new double[] {root1, root2};
    }
    else
    {
      return new double[] {root2, root1};
    }
  }
  /** work out the value at a given point from the observations passed
      in
   */
  private void getSoln(double[] allObs, double[] soln, 
    double[][] basisContrib)
  {
    double[] my = new double[splineTerm.length];
    for (int i = 0; i < dataPoints.length; i++)
    {
      int index = dataPoints[i];
      // take out bSqrt to get residual variance down to constant
      double root = bSqrt[index];
      double obs = allObs[index] / root;
      double aHere = aVal[index];
      for (int j = 0; j < splineTerm.length; j++)
      {
	// modify splineTerm as in computing M'M
        my[j] += (basisContrib[j][index] * aHere / root) * obs;
      }
    }
    lu.solve(my, soln);
    // System.out.println("Main Soln " + Arrays.toString(soln));
  }
  /** produce bounds for each point given obs and sigmage. If lowerD
    and upperD are both non-null then they will be filled in with
    bounds for the derivative */
  public void getBounds(double[] allObs, double sigmage,
    double[] lower, double[] upper, double[] lowerD, double[] upperD)
  {
    double[] basisContrib = new double[splineTerm.length];
    for (int i = 0; i < aVal.length; i++)
    {
      for (int j = 0; j < basisContrib.length; j++)
      {
        basisContrib[j] = splineTerm[j][i];
      }
      double[] bound = singleBounds(allObs, sigmage,
	basisContrib);
      lower[i] = bound[0];
      upper[i] = bound[1];
    }
    if ((upperD == null) || (lowerD == null))
    {
      return;
    }
    for (int i = 0; i < aVal.length; i++)
    {
      for (int j = 0; j < basisContrib.length; j++)
      {
        basisContrib[j] = splineDeriv[j][i];
      }
      double[] bound = singleBounds(allObs, sigmage,
	basisContrib);
      lowerD[i] = bound[0];
      upperD[i] = bound[1];
    }
  }
  /** evaluate at original points given observations for points
      with non-zero a values. Note that we return the value of
      the spline, so don't multiply back by the a term. Will throw
      IllegalArgument if getFitDet() would return 0.0
  */
  public void evaluate(double[] allObs, double[] allValues, 
    double[] derivs)
  {
    double d = lu.getDeterminant();
    if (d == 0.0)
    {
      throw new IllegalArgumentException("Cannot fit as zero determinant");
    }
    double[] soln = new double[splineTerm.length];
    getSoln(allObs, soln, splineTerm);
    // System.out.println("Solution is " + Arrays.toString(soln));
    for (int i = 0; i < aVal.length; i++)
    {
      double val = 0.0;
      for (int j = 0; j < splineTerm.length; j++)
      {
        val += soln[j] * splineTerm[j][i];
      }
      allValues[i] = val; // no a here as want spline, not prediction
    }
    if (derivs != null)
    {
      for (int i = 0; i < aVal.length; i++)
      {
	double val = 0.0;
	for (int j = 0; j < splineTerm.length; j++)
	{
	  val += soln[j] * splineDeriv[j][i];
	}
	derivs[i] = val; // no a here as want spline, not prediction
      }
    }
  }
  /** test routine to check with perfect fit */
  private static void test(int maxKnots, int maxPoints, Random r, int deg)
  {
    int addedPoints = r.nextInt(maxPoints);
    // System.out.println("Added " + addedPoints);
    double[] knots = new double[r.nextInt(maxKnots) + 2];
    for (int i = 0; i < knots.length; i++)
    {
      knots[i] = r.nextGaussian();
    }
    Arrays.sort(knots);
    // System.out.println("Knots " + Arrays.toString(knots));
    // Make sure determinant is OK by putting at least three points in
    // each section. Quadratic splines have gradient constraints enough
    // that less would do, but if we had just one section we wouldn't
    // necessarily get the right quadratic back because of end constraints
    double[] points = new double[(knots.length - 1) * 3 + addedPoints];
    int wp = 0;
    double[] aVec = new double[points.length];
    for (int i = 1; i < knots.length; i++)
    {
      double a = knots[i - 1];
      double b = knots[i];
      double[] po = new double[3];
      for (;;)
      { // generate 3 points not too close to each other
        for (int k = 0; k < po.length; k++)
	{
	  po[k] = r.nextDouble();
	}
	Arrays.sort(po);
	boolean ok = true;
	for (int k = 1; k < po.length; k++)
	{
	  double dist = po[k] - po[k - 1];
	  if (dist < 0.1)
	  {
	    ok = false;
	  }
	  break;
	}
	if (ok)
	{
	  break;
	}
      }
      for (int k = 0; k < po.length; k++)
      {
	double p = a + (b - a) * po[k];
	points[wp] = p;
	double am;
	for (;;)
	{
	  am = r.nextGaussian();
	  if (Math.abs(am) > 1.0e-3)
	  { // steer clear of numerical horrors
	    break;
	  }
	}
	aVec[wp] = am;
	wp++;
	// System.out.println("Obs point " + p);
      }
    }
    double aa = knots[0];
    double bb = knots[knots.length - 1];
    while (wp < points.length)
    {
      points[wp++] = aa + (bb - aa) * r.nextDouble();
    }
    double aTerm = r.nextGaussian();
    double bTerm = r.nextGaussian();
    double cTerm = r.nextGaussian();
    if (deg < 2)
    {
      aTerm = 0.0;
    }
    if (deg < 1)
    {
      bTerm = 0.0;
    }
    double[] bVec = new double[points.length];
    double[] obs = new double[points.length];
    double[] vals = new double[points.length];
    for (int i = 0; i < points.length; i++)
    {
      double p = points[i];
      double v = (aTerm * p + bTerm) * p + cTerm;
      double bv;
      for (;;)
      {
	// steer clear of numerical horrors
	bv = r.nextGaussian();
	if ((Math.abs(bv) < 2.0) && (Math.abs(bv) > 0.1))
	{
	  break;
	}
      }
      bv = bv * bv;
      bVec[i] = bv;
      vals[i] = v;
      obs[i] = aVec[i] * v;
    }
    SplineFit sf = new SplineFit(knots, points, aVec, bVec, deg);
    double[] backValues = new double[points.length];
    sf.evaluate(obs, backValues, null);
    double diff = 0.0;
    for (int i = 0; i < points.length; i++)
    {
      double d = vals[i] - backValues[i];
      diff += d * d;
    }
    if (diff > 1.0e-6)
    {
      System.out.println("Knots " + Arrays.toString(knots));
      System.out.println(Arrays.toString(vals));
      System.out.println(Arrays.toString(backValues));
      System.out.println("det " + sf.getFitDet());
    }
  }
  public static void main(String[] s) throws Exception
  {
    int degree = 2;
    Set<Double> knots = new TreeSet<Double>();
    int maxRandomKnots = 5;
    int maxAddedPoints = 10;
    int goes = 1000;
    long seed = 42;
    boolean testFit = false;
    double sigmage = 2.5758;
    int postFits = 10000;

    int s1 = s.length - 1;
    boolean trouble = false;
    String num = "";
    try
    {
      for (int i = 0; i < s.length; i++)
      {
	if ("-deg".equals(s[i]) && (i < s1))
	{
	  num = s[++i];
	  degree = Integer.parseInt(num.trim());
	}
	else if("-knot".equals(s[i]) && (i < s1))
	{
	  num = s[++i];
	  knots.add(new Double(num.trim()));
	}
	else if("-postFits".equals(s[i]) && (i < s1))
	{
	  num = s[++i];
	  postFits = Integer.parseInt(num.trim());
	}
	else if("-rg".equals(s[i]) && (i < s1))
	{
	  num = s[++i];
	  goes = Integer.parseInt(num.trim());
	}
	else if("-rk".equals(s[i]) && (i < s1))
	{
	  num = s[++i];
	  maxRandomKnots = Integer.parseInt(num.trim());
	}
	else if("-rp".equals(s[i]) && (i < s1))
	{
	  num = s[++i];
	  maxAddedPoints = Integer.parseInt(num.trim());
	}
	else if("-rs".equals(s[i]) && (i < s1))
	{
	  num = s[++i];
	  seed = Long.parseLong(num.trim());
	}
	else if("-sig".equals(s[i]) && (i < s1))
	{
	  num = s[++i];
	  sigmage = Double.parseDouble(num.trim());
	}
	else if ("-testFit".equals(s[i]))
	{
	  testFit = true;
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
    if (trouble)
    {
      System.err.println("Args are [-deg #] [-knot #] [-postFits #] [-rg #] [-rk #]" +
        "[-rp #] [-rs #] [-sig #] [-testFit]");
      return;
    }

    System.err.println("maxRandomKnots " + maxRandomKnots +
      " maxAddedPoints " + maxAddedPoints + " seed " + seed + " goes " +
      goes + " Degree " + degree + " testFit " + testFit + " postFits " + postFits);
    if (testFit)
    {
      Random r = new Random(seed);
      for (int i = 0; i < goes; i++)
      {
	test(maxRandomKnots, maxAddedPoints, r, degree);
      }
    }
    System.err.println("Degree " + degree + " knots " + knots +
      " sigmage " + sigmage);
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
    double[] ka = new double[knots.size()];
    int wp = 0;
    for (Double d: knots)
    {
      ka[wp++] = d;
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
    // both mean and variance are proportional to the number of
    // independent observations
    SplineFit sf = new SplineFit(ka, points, a, a, degree);
    if (sf.getFitDet() == 0.0)
    {
      System.err.println("Fit determinant is zero");
      return;
    }
    double[] fitted = new double[points.length];
    double[] derivs = new double[points.length];
    sf.evaluate(obs, fitted, derivs);
    double[] lower = new double[points.length];
    double[] upper = new double[points.length];
    double[] lowerD = new double[points.length];
    double[] upperD = new double[points.length];
    sf.getBounds(obs, sigmage, lower, upper, lowerD, upperD);
    for (int i = 0; i < fitted.length; i++)
    {
      System.out.println(a[i] + " " + points[i] + " " + obs[i] +
        " " + fitted[i] + " " + lower[i] + " " + upper[i] +
	" " + derivs[i] + " " + lowerD[i] + " " + upperD[i]);
    }
    if (postFits > 0)
    {
      MonteSplineFit msf = new MonteSplineFit(sf, a, a, obs, fitted,
        new Random(seed), sigmage);
      msf.runFits(postFits);
      msf.print();
    }
  }
}
