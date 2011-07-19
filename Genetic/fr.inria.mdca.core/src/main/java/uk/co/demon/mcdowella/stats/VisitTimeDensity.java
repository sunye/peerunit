package uk.co.demon.mcdowella.stats;

import java.util.Arrays;

/** Work out how efficient it is to search the space of n-bit
    combinations by flipping individual bits at random. It is easier
    to work with even n and consider flipping two bits at a time,
    because as we flip a single bit the density alternates between
    odd and even. Here we keep track only of density.
    */
public class VisitTimeDensity
{
  /** Do a single two-bit step. Prob contains the probability
      of being at density 0, 2, .. n given not density 1 yet.
      probBefore is prob density 1 before
      */
  public static double[] step(int n, double[] prob, double probBefore)
  {
    if ((n & 1) != 0)
    {
      throw new IllegalArgumentException("Only dealing with even n");
    }
    int numDensities = n / 2 + 1;
    double[] next = new double[numDensities];
    double n2 = n * n;
    // for each source density
    for (int i = 0; i < numDensities; i++)
    {
      // Prob 1 / n we flip the same bit twice
      next[i] += prob[i] / n;
      int num1s = i * 2;
      int num0s = n - num1s;
      if (num1s > 0)
      { 
        // We clear two set bits
	next[i - 1] += prob[i] * num1s * (num1s - 1.0) / n2;
      }
      if (num0s > 0)
      {
        // we set two cleared bits
	next[i + 1] += prob[i] * num0s * (num0s - 1.0) / n2;
      }
      // We clear a set bit and set a clear bit
      next[i] += prob[i] * num0s * num1s * 2.0 / n2;
    }
    double sum = probBefore;
    for (double x: next)
    {
      sum += x;
    }
    if (Math.abs(sum - 1.0) > 1.0e-6)
    {
      throw new IllegalStateException("Does not sum to one");
    }
    return next;
  }

  public static void main(String[] s)
  {
    for (int n = 2; n < 100; n += 2)
    {
      // start with density 0 probability 1
      int numDensities = n / 2 + 1;
      // This keeps track of the hit probs
      double[] p = new double[numDensities];
      p[0] = 1.0;
      // probability of hit at least once
      double probHit = 0.0;
      // This keeps track of the expected distance
      double[] q = new double[numDensities];
      q[0] = 1.0;
      int steps = 0;
      double meanD = 0.0;
      for (; steps < 250; steps++)
      {
	/*
	if (probHit >= 0.5)
	{
	  break;
	}
	*/
	p = step(n, p, probHit);
	probHit += p[p.length - 1];
	p[p.length - 1] = 0.0;
	q = step(n, q, 0.0);
	// Work out expected distance from 0 at the end
	double d = 0.0;
	for (int i = 0; i < q.length; i++)
	{
	  d += q[i] * i * 2.0;
	}
	meanD += d;
      }
      meanD = meanD / steps;
      // Work out number of random steps required to get
      // this high a probability of the all-1s state
      double logMissProbSteps = Math.log1p(-probHit);
      double logMissProbRand = Math.log1p(-Math.pow(0.5, n));
      double equivRandSteps = logMissProbSteps / logMissProbRand;
      // Ditto, but considering only even densities
      double numChoices = 0.0;
      for (int i = 0; i <= n; i += 2)
      {
        numChoices += Math.exp(LogFact.lF(n) - LogFact.lF(i) - LogFact.lF(n - i));
      }
      //System.out.println("Choices " + numChoices);
      double logMissProbHalfRand = Math.log1p(-1.0 / numChoices);
      double equivHalfRandSteps = logMissProbSteps / logMissProbHalfRand;
      if (Double.isInfinite(equivRandSteps))
      {
        // from floating point rounding to 1.0 of step probs
	equivRandSteps = 2.0 * steps;
      }
      if (Double.isInfinite(equivHalfRandSteps))
      {
        // from floating point rounding to 1.0 of step probs
	equivHalfRandSteps = steps;
      }
      // Print n, steps taken, prob hit, equiv steps rand, distance
      // percentage steps efficiency, percentage distance smoothness
      System.out.println(n + ", " + steps + ", " + probHit +
        ", " + equivRandSteps + ", " + meanD + ", " +
	equivRandSteps * 50.0 / steps + ", " + (meanD * 200.0 / n) + ", " +
	equivHalfRandSteps * 100.0 / steps);
    }
  }
}
