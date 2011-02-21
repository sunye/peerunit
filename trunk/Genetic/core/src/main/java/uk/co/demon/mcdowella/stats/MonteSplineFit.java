package uk.co.demon.mcdowella.stats;

import java.util.Random;

/** Monte-Carlo follow-on test for SplineFit. We have y values for
    observations and associated x values. The y values have mean
    a_i.p_i and variance b_i.p_i(1-p_i), where p_i depends on the
    x values. We are interested in a spline drawn on the underlying
    p_i values, using the b values as weights. This class supports
    Monte Carlo examination of this procedure, by creating random
    data according to either an estimate of the p_i or the smoothed
    spline p_i and then recalculating an estimate of the p_i. We are
    interested in the error in the recalculated q_i at each point,
    and in how often the recalculated bounds do not include the
    underlying value.
  */
public class MonteSplineFit
{
  /**
    Class to generate random values for a point, given its a and
    b values and an underlying probability. Make this a binomial
    if a and b values suit that, otherwise scale it.
  */
  static class PointGen
  {
    /** multiply random integer by this */
    final double r;
    /** add as final component of random generation */
    final double s;
    /** used to generate binomial component */
    final RoughRandom rr;
    /** construct from a and b weights and underlying probability */
    PointGen(double a, double b, double p, Random ran)
    {
      if (b < 0.0)
      {
        throw new IllegalArgumentException("-ve variance weight");
      }
      // Make base binomial to get as close as we can to the variance
      int n = (int)Math.round(b);
      if (n == 0)
      {
        n = 1;
      }
      // To fit mean and variance the number we generate is
      // actually rx + s, where x is the number from the binomial.
      // Set r to get variance of bp(1-p) instead of np(1-p)
      r = Math.sqrt(b / n);
      // Now set s to get the mean of ap instead of nrp
      s = p * (a - r * n);
      // Work out binomial probabilities
      double[] probs = new double[n + 1];
      BinomialProb bp = new BinomialProb(n, p);
      double total = 0.0;
      for (int i = 0; i < probs.length; i++)
      {
        probs[i] = bp.getProb(i);
      }
      rr = new RoughRandom(probs, ran);
    }
    /** generate a random value */
    public double generate()
    {
      return r * rr.next() + s;
    }
  }
  /** Results values when using fitted p */
  private final Fitter fitted;
  /** Results using estimated p */
  private final Fitter estimated;
  /** sigmage to use with bounds */
  private final double sigmage;
  /** Create given SplineFit, a and b values, observations, and
      fitted values for the spline and its derivatives.
  */
  public MonteSplineFit(SplineFit sf, double[] a,
    double[] b, double[] obs, double[] fittedVal, Random ran, double sig)
  {
    sigmage = sig;
    double[] estimatedVal = new double[a.length];
    for (int i = 0; i < a.length; i++)
    {
      // flattened estimate if obs are successes and b is total tries
      double estimate = (obs[i] + 1.0) / (b[i] + 2.0);
      if (estimate < 0.0)
      {
        estimate = 0.0;
      }
      if (estimate > 1.0)
      {
        estimate = 1.0;
      }
      estimatedVal[i] = estimate;
    }
    fitted = new Fitter(a, b, fittedVal, ran, sf);
    estimated = new Fitter(a, b, estimatedVal, ran, sf);
  }
  /** run specified number of random fits */
  public void runFits(int numFits)
  {
    for (int i = 0; i < numFits; i++)
    {
      fitted.fit(sigmage);
      estimated.fit(sigmage);
    }
  }
  /** print out results */
  public void print()
  {
    // System.out.println("Fitted values");
    fitted.print();
    // System.out.println("Estimated values");
    estimated.print();
  }
  /** Class to perform and record fit */
  private static class Fitter
  {
    /** PointGens used to generate actual data */
    final PointGen[] pgen;
    /** scratch space for observations */
    final double[] ourObs;
    /** scratch space for fitted values */
    final double[] fitted;
    /** scratch space for derivative of fitted values */
    final double[] derivs;
    /** actual right answer fitted values */
    final double[] actualFitted;
    /** actual right answer derivative values */
    final double[] actualDerivs;
    /** records errors in fits divided by half distance between 1-sigma bounds */
    final Deviant[] fitErrors;
    /** pooled fit errors */
    final Deviant pooledFitErrors = new Deviant();
    /** records errors in derivs divided by half distance between 1-sigma bounds */
    final Deviant[] derivedErrors;
    /** pooled deriv errors */
    final Deviant pooledDerivedErrors = new Deviant();
    /** spline used to run fits */
    final SplineFit spline;
    /** scratch upper bounds */
    final double[] upper;
    /** scratch lower bounds */
    final double[] lower;
    /** scratch upper bounds for derivatives */
    final double[] upperD;
    /** scratch lower bounds for derivatives */
    final double[] lowerD;
    /** tries */
    int numTries;
    /** below lower bound */
    int belowLower;
    /** below lower in derivative */
    int belowLowerD;
    /** above upper bound */
    int aboveUpper;
    /** above upper in derivative */
    int aboveUpperD;
    Fitter(double[] a, double[] b, double[] p, Random r, SplineFit sf)
    {
      pgen = new PointGen[a.length];
      fitErrors = new Deviant[a.length];
      derivedErrors = new Deviant[a.length];
      for (int i = 0; i < a.length; i++)
      {
        pgen[i] = new PointGen(a[i], b[i], p[i], r);
	fitErrors[i] = new Deviant();
	derivedErrors[i] = new Deviant();
      }
      ourObs = new double[pgen.length];
      fitted = new double[pgen.length];
      derivs = new double[pgen.length];
      upper = new double[pgen.length];
      lower = new double[pgen.length];
      upperD = new double[pgen.length];
      lowerD = new double[pgen.length];
      actualFitted = new double[pgen.length];
      actualDerivs = new double[pgen.length];
      spline = sf;
      // To work out right answer, feed in true underlying mean
      for (int i = 0; i < ourObs.length; i++)
      {
        ourObs[i] = p[i] * a[i];
      }
      sf.evaluate(ourObs, actualFitted, actualDerivs);
    }
    void fit(double sigmage)
    {
      for (int i = 0; i < ourObs.length; i++)
      {
        ourObs[i] = pgen[i].generate();
      }
      spline.evaluate(ourObs, fitted, derivs);
      spline.getBounds(ourObs, sigmage, lower, upper, lowerD, upperD);
      for (int i = 0; i < ourObs.length; i++)
      {
	numTries++;
	final double range = (upper[i] - lower[i]) / (2.0 * sigmage);
        double error = (fitted[i] - actualFitted[i]) / range;
        fitErrors[i].sample(error);
	pooledFitErrors.sample(error);
	if (actualFitted[i] < lower[i])
	{
	  belowLower++;
	}
	if (actualFitted[i] > upper[i])
	{
	  aboveUpper++;
	}
	final double rangeD = (upperD[i] - lowerD[i]) / (2.0 * sigmage);
        error = (derivs[i] - actualDerivs[i]) / rangeD;
        derivedErrors[i].sample(error);
	pooledDerivedErrors.sample(error);
	if (actualDerivs[i] < lowerD[i])
	{
	  belowLowerD++;
	}
	if (actualDerivs[i] > upperD[i])
	{
	  aboveUpperD++;
	}
      }
    }
    /** print out final results */
    public void print()
    {
      if (false)
      {
	for (int i = 0; i < fitErrors.length; i++)
	{
	  System.out.println("Point " + i);
	  System.out.println("Fit errors " + fitErrors[i]);
	  System.out.println("Derived errors " + derivedErrors[i]);
	}
	System.out.println("tries " + numTries);
	System.out.println("underlying below lower " + belowLower);
	System.out.println("underlying above upper " + aboveUpper);
	System.out.println("underlying deriv below lower " + belowLowerD);
	System.out.println("underlying deriv above upper " + aboveUpperD);
      }
      System.err.println("Pooled fit errors " + pooledFitErrors);
      System.err.println("tries " + numTries);
      System.err.println("underlying below lower " + belowLower);
      System.err.println("underlying above upper " + aboveUpper);
      System.err.println("Pooled deriv errors " + pooledDerivedErrors);
      System.err.println("underlying deriv below lower " + belowLowerD);
      System.err.println("underlying deriv above upper " + aboveUpperD);
    }
  }
  /** check PointGen */
  public static void main(String[] s)
  {
    Random r = new Random(42);
    double p1a = 10.0;
    double p1b = 10.0;
    double p1p = 0.5;
    PointGen p1 = new PointGen(p1a, p1b, p1p, r);
    Deviant dp1 = new Deviant();
    double p2a = 5.5;
    double p2b = 20.7;
    double p2p = 0.3;
    PointGen p2 = new PointGen(p2a, p2b, p2p, r);
    Deviant dp2 = new Deviant();
    int goes = 1000000;
    for (int i = 0; i < goes; i++)
    {
      dp1.sample(p1.generate());
      dp2.sample(p2.generate());
    }
    System.out.println("Expect mean " + p1a * p1p + " var " +
      p1b * p1p * (1.0 - p1p));
    System.out.println(dp1);
    System.out.println("Expect mean " + p2a * p2p + " var " +
      p2b * p2p * (1.0 - p2p));
    System.out.println(dp2);
  }
}
