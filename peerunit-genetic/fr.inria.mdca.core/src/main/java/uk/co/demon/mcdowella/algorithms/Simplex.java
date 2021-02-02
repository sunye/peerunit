package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import uk.co.demon.mcdowella.stats.LU;
import java.io.PrintStream;
import java.util.StringTokenizer;
import java.util.Random;

/** This class implements a basic version of the revised simplex
method, sometimes following the textual description in section 17-3
of "Introduction to Operations Research Techniques", by Daellenbach,
George, and McNickle.
<br>
We minimise z = c.x subject to Ax = b and x >= 0
for vector c, x, b, matrix A, and scalar z. We assume that we have
a feasible solution to start with. If not, first solve the problem
of finding the smallest w such that x_i + w >= 0 for all i and
Ax = b, without other constraint on any x. There is a feasible solution
of the main system iff this has a solution with w = 0. To turn that
into the form we accept we can replace each x as x = p - m with
p >= 0 and m >= 0. We then need to minimise max(m_i). To do this
introduce s where s >= 0. We then have m_i = w - s_i. So this gives us
minimise w s.t.
<pre>
A(p_i - m_i) = b
m_i + s_i - w = 0
p, m, s >= 0
</pre>
Testing shows that there are all sorts of horrors lying around here which I think
are due to floating point rounding in near-degenerate situations. Whether this is
good enough for your particular job depends on how tractable your particular problem
instances are.
*/

public class Simplex
{
  private final double small;
  /** set up the problem. c is the objective vector. A is the
    matrix, stored with rows contiguous. b is the constraint, so
    we must have A.length = c.length * b.length != 0. Takes copies
    of arguments.
    The seed is used when we need to choose a pivot at random.
    Due to floating point rounding we need to know a small value, where
    we treat values in [-small, small] as zero. This is most important when we
    look at what happens as we change a variable from 0.0 to 1.0. We want to
    know if doing that, and then changing the other non-zero variables to keep
    to the constraints, increases or decreases the cost, and, if we then make
    that variable non-zero and increase it, when one of the currently non-zero
    variables first has to be decreased to zero in compensation. So we compare
    the cost change with small to see if it is, after accounting rounding error,
    > 0, and we look to see if the impact on other variables is to increase or
    decrease them.
    */
  public Simplex(double[] c, double[] A, double[] b, long seed, double forSmall)
  {
    small = forSmall;
    if (A.length != (c.length * b.length))
    {
      throw new IllegalArgumentException(
        "A.length != c.length * b.length");
    }
    if (A.length == 0)
    {
      throw new IllegalArgumentException("No info or no constraints");
    }
    objective = c.clone();
    amat = A.clone();
    bvec = b.clone();
    r = new Random(seed);
  }
  /** objective vector. Each element in x corresponds to 
    an element in objective */
  private final double[] objective;
  /** matrix A. Each element in x corresponds to a column in A */
  private final double[] amat;
  /** vector b  - one element per constraint. Each element in b 
    corresponds to a row in A */
  private final double[] bvec;
  /** to generate random pivot points */
  private final Random r;
  /** turns on expensive debugging checks. Cheap ones we
      keep on all the time and claim they are to detect problems
      due to floating point rounding.
      */
  private final boolean EXPENSIVE_CHECKS = false;
  /** Do up to the specified number of passes starting with the
      position at which the b.length numbered x values are non-zero.
      Do at most maxPasses. Return the value of the best solution
      found. Set xValues to the value of all x here. Set foundBest
      to true if found best solution, and numPassesDone to the
      number of passes done. May return early if floating point
      rounding errors mess us up. Discrepancy returned in
      descrepancy[0]
      */
  double searchFrom(int[] nonZeroX, int maxPasses, double[] xValues,
    boolean[] foundBest, int[] numPassesDone, double[] discrepancy)
  {
    if (nonZeroX.length != bvec.length)
    {
      throw new IllegalArgumentException("nonZeroX.length != b.length");
    }
    if (xValues.length != objective.length)
    {
      throw new IllegalArgumentException("XValues.length != c.length");
    }
    discrepancy[0] = 0.0;
    // First of all work out the inverse of the matrix formed by
    // choosing the A-columns corresponding to the non-zero x values
    // chosen
    double[] bmat = new double[nonZeroX.length * nonZeroX.length];
    boolean[] inBmat = new boolean[objective.length];
    int wp = 0;
    for (int i = 0; i < nonZeroX.length; i++)
    {
      int chosen = nonZeroX[i];
      // System.out.println("Chosen " + chosen);
      if (inBmat[chosen])
      {
        throw new IllegalArgumentException("Variable chosen twice");
      }
      inBmat[chosen] = true;
      for (int j = 0; j < nonZeroX.length; j++)
      {
        bmat[j * nonZeroX.length + i] = 
	  amat[j * objective.length + chosen];
      }
    }
    // System.out.println("BMat is");
    // LU.print(nonZeroX.length, bmat);
    LU lu = new LU(nonZeroX.length, bmat);
    double det = lu.getDeterminant();
    // System.out.println("Determinant " + det);
    if (det == 0.0)
    {
      throw new IllegalArgumentException("starting matrix is singular");
    }
    // work out values implied for non-zero x
    double[] nzx = new double[nonZeroX.length];
    lu.solve(bvec, nzx);
    // Will be kept at cost of current solution
    double cost = 0.0;
    double[] costs = new double[nonZeroX.length];
    for (int i = 0; i < nzx.length; i++)
    {
      double here = nzx[i];
      if (here < 0.0)
      {
	if (here < -small)
	{
	  throw new IllegalArgumentException(
	    "-ve value at supposed feasible point: " + here + " var " + nonZeroX[i]);
	}
	else
	{ // assume floating point rounding
	  // System.out.println("Shove " + here + " up to zero");
	  nzx[i] = 0.0;
        }	
      }
      costs[i] = objective[nonZeroX[i]];
      // cost += here * objective[nonZeroX[i]];
    }
    cost = MatWrapper.dot(nzx.length, 0, 1, nzx, 0, 1, costs);
    // System.out.println("Cost at start is " + cost);
    // Need to keep the inverse of our B matrix. We have
    // B * B^-1 = I, so each column of B^-1 is the solution of
    // Bx = J where J has one element set to 1 and everything
    // else zero.
    double[] binv = new double[bmat.length];
    double[] t = new double[nonZeroX.length];
    double[] ti = new double[nonZeroX.length];
    for (int i = 0; i < t.length; i++)
    {
      t[i] = 1.0;
      lu.solve(t, ti);
      t[i] = 0.0;
      for (int j = 0; j < nonZeroX.length; j++)
      {
        binv[j * nonZeroX.length + i] = ti[j];
      }
    }
    if (false)
    {
      System.out.println("Initial B is ");
      LU.print(nzx.length, bmat);
      System.out.println("Initial B^-1 is ");
      LU.print(nzx.length, binv);
    }
    // Temporary used within loop
    double[] cb = new double[nonZeroX.length];
    for (int pass = 0; pass < maxPasses; pass++)
    {
      // System.out.println("NonZero " + Arrays.toString(nonZeroX));
      // At the moment all of the variables except those corresponding 
      // to columns in the B matrix are zero. If we were to set one of 
      // those to 1, we could use B^-1 to work out how the variables 
      // in the B matrix would have to change to maintain Ax = b. If the
      // column in the A matrix corresponding to our new variable was a,
      // the change in the other variables would be -B^-1a, and the
      // impact of this on the total cost would be -cB^-1a. We will work
      // out cB^-1 now. (These conventions are different from the book).
      // The book uses updates
      // to work out c incrementally, but I just work it out anew from 
      // B^-1
      for (int i = 0; i < nonZeroX.length; i++)
      {
        costs[i] = objective[nonZeroX[i]];
      }
      for (int i = 0; i < nonZeroX.length; i++)
      {
	/*
	double total = 0.0;
	for (int j = 0; j < nonZeroX.length; j++)
	{
	  total += objective[nonZeroX[j]] * 
	    binv[j * nonZeroX.length + i];
	}
	cb[i] = total;
	*/
	cb[i] = MatWrapper.dot(nonZeroX.length, 0, 1, costs,
	  i, nonZeroX.length, binv);
      }
      // Search for a variable currently set to zero that will 
      // decrease the cost if we put it in the B matrix. Try and pick 
      // the best possibility, breaking
      // ties at random. This random choice probably isn't necessary,
      // but it can do no harm.
      int numSameCost = 0;
      double leastCost = Double.MAX_VALUE;
      int bestAt = -1;
      // System.out.println("cb " + Arrays.toString(cb));
      for (int i = 0; i < objective.length; i++)
      {
        if (inBmat[i])
	{ // already chosen
	  continue;
	}
	// Work out what happens to the objective via the other
	// variables if we increase the current one to 1.0. We have
	// to change them so that the net change on Ax = b is zero, so
	// if we set a new x to 1, we need to change the others by
	// -B^-1a, where a is the column for the new variable
	// in the A matrix. The cost of this is -cB^-1a. The book uses
	// some sort of update scheme to keep track of cB^-1, but I just
	// rederive it above.
	/*
	double changeFromOtherVars = 0.0;
	for (int j = 0; j < cb.length; j++)
	{
	  changeFromOtherVars -= cb[j] * amat[j * objective.length + i];
	}
	*/
	double changeFromOtherVars = -MatWrapper.dot(cb.length, 0, 1, cb,
	  i, objective.length, amat);
	// System.out.println("Change from others " + changeFromOtherVars);
	// Now introduce the contribution from the objective function 
	// for i
	changeFromOtherVars += objective[i];
	// System.out.println("i = " + i + " change " + changeFromOtherVars);
	if (changeFromOtherVars >= -small)
	{ 
	  // Does not decrease cost - no good to us.
	  // This looks reckless, but if we allow floating point 
	  // rounding to make 0 profit changes look worth pursuing
	  // we will chase our tails for ever
	  continue;
	}
	if (changeFromOtherVars > (leastCost + small))
	{ // more expensive than best so far
	  continue;
	}
	if (changeFromOtherVars < (leastCost - small))
	{ // better than best so far
	  leastCost = changeFromOtherVars;
	  numSameCost = 1;
	  bestAt = i;
	  continue;
	}
	// about the same
	numSameCost++;
	if (r.nextInt(numSameCost) == 0)
	{
	  // Now a total of numSameScore contendors. This one wins 
	  // with probability 1 in numSameScore, which gets everybody
	  // the same chance.
	  bestAt = i;
	  leastCost = changeFromOtherVars;
	}
      }
      if ((bestAt < 0) || (leastCost >= 0.0))
      {
        // we have found the answer, because we are at a local
	// minimum. The problem is linear, therefore convex, so
	// this is also a global minimum.
	foundBest[0] = true;
	numPassesDone[0] = pass;
	break;
      }
      // We are now going to put bestAt in the basis, which we do by 
      // increasing its value from zero. As we do this, the other
      // variables in the basis have to decrease to compensate. The one
      // to leave is the first one to drop down to zero. Their current
      // values are held in nzx. We need the value of
      // B^-1 a to work out how these new values decrease
      Arrays.fill(t, 0.0);
      for (int i = 0; i < nzx.length; i++)
      {
	/*
	final int row = i * nzx.length;
        double total = 0.0;
	for (int j = 0; j < nzx.length; j++)
	{
	  total += binv[row + j] * amat[j * objective.length + bestAt];
	}
	t[i] = total;
	*/
	t[i] = MatWrapper.dot(nzx.length, i * nzx.length, 1, binv,
	  bestAt, objective.length, amat);
      }
      // The amount of the new variable we put in is the minimum of
      // the amounts allowed by the various variables already in, and
      // the variable with the least is the one that leaves the basis
      int leaveAt = - 1;
      int numSameAmount = 0;
      double amountNewVar = Double.MAX_VALUE;
      for (int i = 0; i < nzx.length; i++)
      { // can increase new variable from 0 to amountHere as far as this
        // variable is concerned.
	if (t[i] < small)
	{ // ignore this as can increase indefinitely and variable
	  // we are worrying about either increases or stays the same
	  // System.out.println("treat " + t[i] + " as -ve");
	  continue;
	}
        double amountHere = nzx[i] / t[i];
	// System.out.println("Amount for " + i + " at " + nonZeroX[i] + " is " + amountHere);

	// Don't fuzz these two comparisons because we can quite
	// reasonable get two amounts very close together here
	// and if we pick the wrong one we end up with -ve variables
	if (amountHere > (amountNewVar /* + small*/))
	{
	  continue;
	}
	if (amountHere < (amountNewVar /* - small */))
	{
	  leaveAt = i;
	  numSameAmount = 1;
	  amountNewVar = amountHere;
	  continue;
	}

	if (amountHere < amountNewVar)
	{
	  amountHere = amountNewVar;
	}
	numSameAmount++;
	if (r.nextInt(numSameAmount) == 0)
	{
	  leaveAt = i;
	}
	// The random choice above does have a purpose. There is the
	// possibility that changing the basis vectors will not change
	// the solution, because amountNewVar = 0. In particular this
	// means that we will not decrease the cost, which means that
	// there is no obvious progress towards an optimum. This can
	// actually happen, especially with contorted problems in which
	// we have forced something into our canonical form. Making the
	// choice here random means that we should eventually wander out
	// to a solution in these cases, and not simply swap variable A
	// for variable B indefinitely.
      }
      if (leaveAt < 0)
      {
	System.out.println("Apparently unbounded solution");
        discrepancy[0] = leaveAt;
	break;
      }
      // Now know that we are going to introduce bestAt and
      // remove nonZeroX[leaveAt]. Need to update B^-1.
      // Multiplication between a matrix and a column vector Bv
      // amounts to producing a weighted sum of columns of B. Think 
      // of BB^-1 considering the columns of B^-1 one by one: B^-1
      // is a collection of columns that tell us how to get the
      // columns (1, 0, 0, ..), (0, 1, 0, ...) by adding together
      // columns of B. We are going to change one of the columns of B.
      // Before we do this, work out what the new columns is as a
      // combination of the old columns. Then we can use this to work 
      // out B^-1 in the new regime. We can use B^-1 to work out
      // what the new column is in terms of the old: B B^-1 r = r
      // so (B^-1 r) is a column that tells us now to write r as
      // a combination of the column of b, and we already have this
      // in vector t

      double expectedChange = amountNewVar * leastCost;
      if (false)
      {
	System.out.println("Expect cost change " +
	  expectedChange + " to " + (cost + expectedChange));
        System.out.println("B is ");
	LU.print(nzx.length, bmat);
        System.out.println("B^-1 is ");
	LU.print(nzx.length, binv);
	System.out.print("Replace " + leaveAt + " by");
	for (int i = 0; i < nzx.length; i++)
	{
	  System.out.print(" " + amat[i * objective.length + bestAt]);
	}
	System.out.println();
      }

      if (t[leaveAt] == 0.0)
      { // Our new column is a combination of the other old columns,
        // and does not contain any part of the column it is replacing. 
	// This means that our new bmat would be singular, and also 
	// that the objective function shouldn't have improved, 
	//  because if the point we are moving to is solvable at all, 
	// it should have the same solution as before. Let's hope this 
	// never happens!
	foundBest[0] = false;
	numPassesDone[0] = pass;
	discrepancy[0] = 1.0;
	break;
      }
      for (int i = 0; i < nzx.length; i++)
      { // iterate over columns of binv
	// Now use t to update binv. We need what amounts to as much
	// of the old outgoing column as before. We know each new 
	// column amounts to t[i] of the old column, so we need this
	// much of the new column to replace it.
	double amountNewColumn = binv[leaveAt * nzx.length + i] /
	  t[leaveAt];
	// System.out.println("leaveat " + t[leaveAt]);
	binv[leaveAt * nzx.length + i] = amountNewColumn;
	// This has brought in stuff that used to come from other 
	// columns, so we need to subtract that back out
	for (int j = 0; j < nzx.length; j++)
	{
	  if (j == leaveAt)
	  {
	    continue;
	  }
	  binv[j * nzx.length + i] -= amountNewColumn * t[j];
	  /*
	  System.out.println("Row " + j + " column " + i +
	    " " + amountNewColumn + " * " + t[j] + " becomes " +
	    binv[j * nzx.length + i]);
	  */
	}
      }
      // System.out.println("New binv is");
      // LU.print(nzx.length, binv);
      // update bmat
      for (int i = 0; i < nzx.length; i++)
      {
        bmat[i * nzx.length + leaveAt] = 
	  amat[i * objective.length + bestAt];
      }
      if (EXPENSIVE_CHECKS)
      {
        // Check B^-1 B = I
	for (int i = 0; i < nzx.length; i++)
	{
	  for (int j = 0; j < nzx.length; j++)
	  {
	    /*
	    double total = 0.0;
	    for (int k = 0; k < nzx.length; k++)
	    {
	      total += bmat[i * nzx.length + k] *
	        binv[k * nzx.length + j];
	    }
	    */
	    double total = MatWrapper.dot(nzx.length, i * nzx.length, 1, bmat,
	      j, nzx.length, binv);
	    if (i == j)
	    {
	      total -= 1.0;
	    }
	    if (Math.abs(total) > small)
	    {
	      throw new IllegalStateException("B * B^-1 != I: " + total);
	    }
	  }
	}
      }
      // Update choice of columns
      inBmat[nonZeroX[leaveAt]] = false;
      nonZeroX[leaveAt] = bestAt;
      inBmat[bestAt] = true;
      // and work out what our solution turns out to be
      for (int i = 0; i < nonZeroX.length; i++)
      {
	/*
        double total = 0.0;
	for (int j = 0; j < nonZeroX.length; j++)
	{
	  total += binv[i * nonZeroX.length + j] * bvec[j];
	}
	*/
	double total = MatWrapper.dot(nonZeroX.length, i * nonZeroX.length, 1,
	  binv, 0, 1, bvec);
	if ((total < 0) && (total > -small))
	{
	  // System.out.println("Forced " + total + " to zero");
	  total = 0.0;
	}
	nzx[i] = total;
      }
      // double newCost = 0.0;
      for (int i = 0; i < nzx.length; i++)
      {
	double here = nzx[i];
	if (here < 0.0)
	{ // trouble!
	    discrepancy[0] = here;
	}
	costs[i] = objective[nonZeroX[i]];
	// newCost += here * objective[nonZeroX[i]];
      }
      double newCost = MatWrapper.dot(nzx.length, 0, 1, nzx,
        0, 1, costs);
      /*
      System.out.println("New cost " + newCost + " at " +
	Arrays.toString(nzx) + " nz " + Arrays.toString(nonZeroX));
      Can get differences more than this if B matrix is nearly singular
      if (Math.abs(cost + expectedChange - newCost) > small)
      {
	// If here, we have miscalculated effects => bug
        throw new IllegalStateException("Predictions not fulfilled old cost " +
	  cost + " expected change " + expectedChange + " new cost " +
	  newCost + " missed by " + (cost + expectedChange - newCost));
      }
      */
      if (discrepancy[0] != 0.0)
      {
        cost = newCost;
	break;
      }
      if (newCost > cost)
      {
        // probably an error but report it back anyway
	if (discrepancy[0] == 0)
	{
	  discrepancy[0] = newCost - cost;
	}
	cost = newCost;
	foundBest[0] = false;
	numPassesDone[0] = pass;
	break;
      }
      cost = newCost;
    }
    // Now fill in xvalues from nzx
    Arrays.fill(xValues, 0.0);
    for (int i = 0; i < nzx.length; i++)
    {
      xValues[nonZeroX[i]] = nzx[i];
    }
    return cost;
  }
  /** read a row of numbers from a single line of System.in,
      using # as a comment */
  public static double[] readRow(BufferedReader br) throws IOException
  {
    for (;;)
    {
      String s = br.readLine();
      if (s == null)
      {
        return null;
      }
      String orig = s;
      int index = s.indexOf('#');
      if (index >= 0)
      { // trim comments
        s = s.substring(0, index);
      }
      s = s.trim();
      if (s.equals(""))
      { // all comment, or all blank
        continue;
      }
      StringTokenizer st = new StringTokenizer(s);
      double[] result = new double[st.countTokens()];
      for (int i = 0; st.hasMoreTokens(); i++)
      {
        String num = st.nextToken().trim();
	try
	{
	  result[i] = Double.parseDouble(num);
	}
	catch (NumberFormatException nf)
	{
	  throw new IOException("Cannot read number " + num +
	    " from input line " + orig);
	}
      }
      return result;
    }
  }
  /** Test/utility routine to read input and solve.
   *  First row must be objective
   *  Second row must be 0/1 with 1s marking choice
   *  for initial feasible point
   *  Subsequent rows must be rows of A matrix, with additional
   *  number at the end giving constraint
   */
  public static void main(String[] s) throws IOException
  {
    BufferedReader br = new BufferedReader(new InputStreamReader(
      System.in));
    double[] objective = readRow(br);
    if (objective == null)
    {
      throw new IllegalArgumentException("Objective vector not read");
    }
    double[] inFirst = readRow(br);
    if (inFirst == null)
    {
      throw new IllegalArgumentException("Choice row not read");
    }
    if (inFirst.length != objective.length)
    {
      throw new IllegalArgumentException(
        "Length of objective does not match length of choice vector");
    }
    List<double[]> conRows = new ArrayList<double[]>();
    for (;;)
    {
      double[] newRow = readRow(br);
      if (newRow == null)
      {
        break;
      }
      if (newRow.length != (objective.length + 1))
      {
        throw new IllegalArgumentException("Constraint row " +
	  Arrays.toString(newRow) + " wrong length");
      }
      conRows.add(newRow);
    }
    double[] a = new double[conRows.size() * objective.length];
    double[] b = new double[conRows.size()];
    int wp = 0;
    for (double[] r: conRows)
    {
      b[wp] = r[objective.length];
      System.arraycopy(r, 0, a, wp * objective.length, 
        objective.length);
      wp++;
    }
    Simplex sx = new Simplex(objective, a, b, 42, 1.0e-6);
    int[] nonZero = new int[b.length];
    wp = 0;
    for (int i = 0; i < inFirst.length; i++)
    {
      double d = inFirst[i];
      if (d == 0.0)
      {
        continue;
      }
      if (d != 1.0)
      {
        throw new IllegalArgumentException("Number " + d +
	  " in choice row " + Arrays.toString(inFirst) +
	  " is neither 0 nor 1");
      }
      if (wp >= nonZero.length)
      {
        throw new IllegalArgumentException("Choice row " +
	  Arrays.toString(inFirst) + " selects too many variables");
      }
      nonZero[wp++] = i;
    }
    if (wp != nonZero.length)
    {
        throw new IllegalArgumentException("Choice row " +
	  Arrays.toString(inFirst) + " selects too few variables");
    }
    double[] xValues = new double[objective.length];
    double[] discrepancy = new double[1];
    double cost = sx.driver(nonZero, xValues, discrepancy, System.out);
    System.out.println("Cost " + cost + " at " +
      Arrays.toString(xValues) + " chosen " +
      Arrays.toString(nonZero));
  }
  /** Driver routine with comments to stream provided. Requires array of
      indexes to form first basis. Returns descrepancy and solution point
      to arrays given */
  public double driver(int[] nonZero, double[] xValues, double[] discrepancy,
    PrintStream writeHere)
  {
    boolean[] foundBest = new boolean[1];
    int[] numPassesDone = new int[1];
    for (;;)
    {
      double cost = searchFrom(nonZero, 50, xValues,
	foundBest, numPassesDone, discrepancy);
      // System.out.println("Cost " + cost);
      /*
      System.out.println("Cost " + cost + " at " +
        Arrays.toString(xValues) + " chosen " +
	Arrays.toString(nonZero) + " after " + numPassesDone[0] +
	" discrepancy " + discrepancy[0] + " complete " +
	foundBest[0]);
	*/
      if (foundBest[0])
      {
        return cost;
      }
    }
  }
}
