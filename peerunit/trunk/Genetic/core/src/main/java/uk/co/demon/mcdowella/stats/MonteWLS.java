package uk.co.demon.mcdowella.stats;

import java.util.Random;

/** Monte-Carlo follow-on test for WeightLS. We have y values for
    observations and associated x values. The y values have mean
    a_i.p_i and variance b_i.p_i(1-p_i), where p_i depends on the
    x values. We use WeightLS to estimate the underlying p_i values
    p_i values, using the b values to affect the weights.
    This class supports
    Monte Carlo examination of this procedure, by creating random
    data according to either an estimate of the p_i or the smoothed
    p_i and then recalculating an estimate of the p_i. We are
    interested in the error in the recalculated q_i at each point,
    and in how often the recalculated bounds do not include the
    underlying value.
  */
public class MonteWLS
{
  /** Results values when using fitted p */
  private final Fitter fitted;
  /** Results using estimated p */
  private final Fitter estimated;
  /** sigmage to use with bounds */
  private final double sigmage;
  /** Create given WeightLS, a and b values, observations, and
      fitted values
  */
  public MonteWLS(WeightLS wls, double[] a,
    double[] b, double[] obs, double[] fittedVal, Random ran, 
    double sig)
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
    fitted = new Fitter(a, b, fittedVal, ran, wls);
    estimated = new Fitter(a, b, estimatedVal, ran, wls);
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
    final MonteSplineFit.PointGen[] pgen;
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
    /** WeightLS used to run fits */
    final WeightLS wls;
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
    Fitter(double[] a, double[] b, double[] p, Random r, 
      WeightLS forWls)
    {
      pgen = new MonteSplineFit.PointGen[a.length];
      fitErrors = new Deviant[a.length];
      derivedErrors = new Deviant[a.length];
      for (int i = 0; i < a.length; i++)
      {
        pgen[i] = new MonteSplineFit.PointGen(a[i], b[i], p[i], r);
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
      wls = forWls;
      // To work out right answer, feed in true underlying mean
      for (int i = 0; i < ourObs.length; i++)
      {
        ourObs[i] = p[i] * a[i];
      }
      wls.getFitted(ourObs, actualFitted);
      wls.getSlope(ourObs, actualDerivs);
    }
    void fit(double sigmage)
    {
      for (int i = 0; i < ourObs.length; i++)
      {
        ourObs[i] = pgen[i].generate();
      }
      wls.getFitted(ourObs, fitted);
      wls.getSlope(ourObs, derivs);
      wls.getBounds(ourObs, sigmage, lower, upper, lowerD, upperD);
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
}
