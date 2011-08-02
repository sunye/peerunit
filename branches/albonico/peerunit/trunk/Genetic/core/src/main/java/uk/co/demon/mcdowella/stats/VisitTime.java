package uk.co.demon.mcdowella.stats;

import java.util.Arrays;

/** Work out how efficient it is to search the space of n-bit
    combinations by flipping individual bits at random. We
    keep track of the probability of being at each possible n-bit
    combination and see how this changes changes as we wander from
    the all-zeros combination. Print out the probability and location
    of the least probability point. This is not always the all-1s point,
    because at any given time all reachable points have either even
    or odd numbers of bits.
    */
public class VisitTime
{
  public static void main(String[] s)
  {
    int numBits = 10;
    int steps = 10000;
    double[] probs = new double[1 << numBits];
    probs[0] = 1.0;
    double[] tp = new double[probs.length];
    double pDim = 1.0 / numBits;
    for (int step = 0; step < steps; step++)
    {
      double minValue = -1.0;
      int accessible = 0;
      double totalProb = 0.0;
      for (int i = 0; i < probs.length; i++)
      {
	if (probs[i] == 0.0)
	{ // wrong parity for current step
	  continue;
	}
	totalProb += probs[i];
	accessible++;
        if ((minValue < 0.0) || (probs[i] < minValue))
	{
	  minValue = probs[i];
	}
      }
      System.out.println("Min value at step " + step + " is " + 
        minValue + " accessible " + accessible + " total " + totalProb);
      Arrays.fill(tp, 0.0);
      for (int i = 0; i < numBits; i++)
      {
	int change = 1 << i;
	for (int j = 0; j < probs.length; j++)
	{
	  tp[j ^ change] += probs[j] * pDim;
	}
      }
      tp[tp.length - 1] = 0.0;
      double[] t = probs;
      probs = tp;
      tp = t;
    }
  }
}
