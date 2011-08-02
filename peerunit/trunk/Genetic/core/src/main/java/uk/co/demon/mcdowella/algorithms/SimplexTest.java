package uk.co.demon.mcdowella.algorithms;

import java.util.Arrays;
import uk.co.demon.mcdowella.stats.LU;
import java.util.Random;

/** This class tests the Simplex and SimplexSparse classes by 
    constructing linear
    programming problems to which it knows the answer and using
    Simplex to solve them. The main idea behind this is to construct
    cones with answer at the origin and then shift them somewhere
    else.
    */
public class SimplexTest
{
  /** This constructs a plane for an equality of
      the form a.x >= 0. Because the rhs is always zero, we
      do not return it. The normal to the plane will be axis +
      a random vector of up to half its length, so axis.normal >= 0.
      We are told the number of dimensions in a and x,
      dim, and the total number of slack variables, slack. Our slack
      variable is number ourSlack, counting the first slack variable
      as 0. We write to writeHere[wp] and the succeeding elements,
      writing the non-slack stuff first.
      */
  private static void constructPlane(int dim, int slack,
    int ourSlack, double[] axis, double axisLength, Random r,
    double[] writeHere, int wp)
  {
    for (;;)
    {
      double total = 0.0;
      double at = 0.0;
      // Chose a random a
      for (int i = 0; i < dim; i++)
      {
        double a = r.nextGaussian();
	writeHere[wp + i] = a;
	total += a * a;
      }
      if (total == 0.0)
      { // VERY unlikely - generated vector of zero length
        continue;
      }
      double totalLength = Math.sqrt(total);
      double varySize;
      for (;;)
      {
	varySize = r.nextDouble();
	if (varySize != 0.0)
	{
	  break;
	}
      }
      for (int i = 0; i < dim; i++)
      {
        writeHere[wp + i] = writeHere[wp + i] * axisLength * varySize/
	  (totalLength * 2.0);
      }
      break;
    }
    // Zap out slack variables
    for (int i = 0; i < slack; i++)
    {
      writeHere[wp + dim + i] = 0.0;
    }
    for (int i = 0; i < dim; i++)
    {
      writeHere[wp + i] += axis[i];
    }
    // We have a constraint a.x = 0. To get a.x >= 0 we need
    // to use our slack variable, which like all the other
    // variables else is forced >= 0. so a.x - slack = 0 will
    // do the job
    writeHere[wp + dim + ourSlack] = -1.0;
  }
  /** Suppose we have an equation forming part of a group
      intended to have their solution at the origin, and with
      RHS 0.0. Return an RHS which amounts to shifting the
      co-ordinates to the given point. Our equation has dim
      variables in it (not counting slacks) and starts at
      eqnStarts in eqns.
      */
  private static double shiftRhs(int dim, int eqnStarts,
    double[] eqns, double[] shiftToHere)
  {
    // We have a solution at x_0 = 0, x_1 = 0, x_2 = 0 and so on.
    // Define y_0 = x_0 + s0, y_1 = x_1 + s1 and so on. Then
    // substitute in to get an inequality in y which should be
    // part of a system with answer at s.
    double total = 0.0;
    for (int i = 0; i < dim; i++)
    {
      // y_0 = x_0 + s0 so x_0 = y_0 - s0 and
      // c_0 * x_0 = 0 becomes c_0 * y_0 = c_0 * s_0
      total += eqns[eqnStarts + i] * shiftToHere[i]; 
    }
    return total;
  }

  public static void runTest(int dim, Random r)
  {
    // Use two sets of equations, each of size dim * 2
    int numEqns = dim * 4;
    // Create random axis
    double[] axis = new double[dim];
    double axisLength;
    for (;;)
    {
      double total = 0.0;
      for (int i = 0; i < axis.length; i++)
      {
        double d = r.nextGaussian();
	if (d < 0.0)
	{
	  d = -d;
	}
	total += d * d;
	axis[i] = d;
      }
      if (total != 0.0)
      {
	axisLength = Math.sqrt(total);
        break;
      }
      // can we ever really get here?
    }
    // System.out.println("Axis is " + Arrays.toString(axis));
    // Need two slack variables for each equation. Use only
    // one at first
    int numSlack = numEqns * 2;
    int numVars = numSlack + dim;
    double[] eqns = new double[numEqns * numVars];
    for (int i = 0; i < numEqns; i++)
    {
      constructPlane(dim, numSlack, i, axis, axisLength, r, 
        eqns, i * numVars);
    }
    // We will shift the equations in two groups, both along
    // the axis. The right answer will correspond to the furthest
    // shifted
    double[] rhs = new double[numEqns];
    double s1;
    for (;;)
    {
      s1 = r.nextGaussian();
      if (s1 == 0.0)
      {
        continue;
      }
      if (s1 < 0)
      {
	s1 = - s1;
      }
      break;
    }
    // s1 = 0.0;
    double[] shiftToHere = new double[axis.length];
    for (int j = 0; j < shiftToHere.length; j++)
    {
      shiftToHere[j] = axis[j] * s1;
    }
    for (int i = 0; i < dim; i ++)
    {
	rhs[i] = shiftRhs(dim, i * numVars,
	  eqns, shiftToHere);
    }
    double s2;
    for (;;)
    {
      s2 = r.nextGaussian();
      if (s2 < 0)
      {
	s2 = - s2;
      }
      if (s1 <= s2)
      {
	continue;
      }
      break;
    }
    for (int i = dim; i < numEqns; i ++)
    {
      /*
      (fixed)
      // If we do this outside the loop then we fall into infinite
      // loops in simplex. There seems to be a lot of cases where
      // things should exactly equal each other but don't. I think
      // floating point rounding error is killing us
      // Fixes:
      // (1) check sign of divisor when working out how far we
      // can move a newly introduced variable, so not considering
      // sign of multiplicand at that point
      // (2) make most tests against 0 tests against +/- small
      // (3) when grouping stuff use +/-small again to keep very
      // similar stuff in same random group.
      double s2;
      for (;;)
      {
	s2 = r.nextGaussian();
	if (s2 < 0)
	{
	  s2 = - s2;
	}
	if (s1 <= s2)
	{
	  continue;
	}
	break;
      }
      */
      for (int j = 0; j < shiftToHere.length; j++)
      {
	shiftToHere[j] = axis[j] * s2;
      }
      rhs[i] = shiftRhs(dim, i * numVars,
	eqns, shiftToHere);
    }
    // Print out room at our solution, as a check
    for (int i = 0; i < numEqns; i++)
    {
      double total = 0.0;
      for (int j = 0; j < dim; j++)
      {
        total += axis[j] * s1 * eqns[numVars * i + j];
      }
      total -= rhs[i];
      // System.out.println("Slack " + total);
    }
    // We have a perfectly good problem, but there is only
    // one proper feasible point, at the right answer. So
    // use our second slack variable per equation to allow
    // deviations from the objective function, but penalise them.
    // So turn a.x - s_0 = 0 into a.x - s_0 + t_0 = 0
    for (int i = 0; i < numEqns; i++)
    {
      eqns[(i + 1) * numVars - numEqns + i] = 1.0;
    }
    // New objective is axis but with penalties - 1000 should do
    // given everything else is gaussian
    double[] objective = new double[numVars];
    System.arraycopy(axis, 0, objective, 0, dim);
    for (int i = 0; i < numEqns; i++)
    {
      objective[numVars - numEqns + i] = 1000.0;
    }
    if (false)
    {
      System.out.println("Eqns");
      LU.print(numVars, eqns);
      System.out.println("Rhs " + Arrays.toString(rhs));
      System.out.println("Objective " + Arrays.toString(objective));
    }
    // Create problem
    Simplex sx = new Simplex(objective, eqns, rhs, 42, 1.0e-6);
    SimplexSparse sxs = new SimplexSparse(objective, eqns, rhs, 
      42, 1.0e-6);
    // with numEqns equations we need that many basis variables.
    // The equations are of the form a.x - s_0 + t_0 = rhs
    // and the axis based construction means that rhs is always
    // >= 0 so the penalty stuff should do
    int[] nonZero = new int[numEqns];
    for (int i = 0; i < numEqns; i++)
    {
      nonZero[i] = numVars - i - 1;
    }
    // testing
    /*
    for (int i = 0; i < dim; i++)
    {
      nonZero[i] = i;
    }
    for (int i = dim; i < numEqns; i++)
    {
      nonZero[i] = i + dim;
    }
    */
    double[] xValues = new double[numVars];
    double[] discrepancy = new double[1];
    double[] xsValues = new double[numVars];
    double[] sdiscrepancy = new double[1];
    int[] nonZeros = nonZero.clone();
    // System.out.println("NonZero is " + Arrays.toString(nonZero));
    double minCost = sx.driver(nonZero, xValues, discrepancy, System.out);
    double minCosts = sxs.driver(nonZeros, xsValues, sdiscrepancy, System.out);
    // System.out.println("Mincost " + minCost + " and " + minCosts);
    if (discrepancy[0] > 1.0e-6)
    {
      throw new IllegalStateException("Discrepancy " + discrepancy[0]);
    }
    if (sdiscrepancy[0] > 1.0e-6)
    {
      throw new IllegalStateException("Discrepancy " + sdiscrepancy[0]);
    }
    double[] ourAnswer = new double[dim];
    for (int i = 0; i < dim; i++)
    {
      ourAnswer[i] = axis[i] * s1;
    }
    double[][] aa = new double[][] {xValues, xsValues};
    double[] cq = new double[] {minCost, minCosts};
    for (int q = 0; q < aa.length; q++)
    {
      double[] xd = aa[q];
      for (int i = 0; i < dim; i++)
      {
	double distance = ourAnswer[i] - xd[i];
	if (Math.abs(distance) > 1.0e-6)
	{
	  double expectedCost = 0.0;
	  for (int j = 0; j < ourAnswer.length; j++)
	  {
	    expectedCost += ourAnswer[j] * objective[j];
	  }
	  if (cq[q] > expectedCost)
	  {
	    throw new IllegalStateException("Not at expected location " +
	      Arrays.toString(ourAnswer) + " but " + Arrays.toString(xd) +
	      " expected cost " + expectedCost + " cost found " + cq[q]);
	  }
	}
      }
    }
  }
  public static void main(String[] s)
  {
    // runTest(1, r);
    for (int i = 1; i < 40; i++)
    {
      System.out.println("Test dim " + i);
      for (int j = 0; j < 100; j++)
      {
	Random r = new Random(i * 1000000 + j * 17);
	runTest(i, r);
      }
    }
  }
}
