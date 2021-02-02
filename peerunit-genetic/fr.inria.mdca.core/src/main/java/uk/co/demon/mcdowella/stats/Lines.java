package uk.co.demon.mcdowella.stats;

import java.util.Arrays;
import java.util.Random;

/** Given a series of points and directions from that point,
 *  find a single point that minimises an error term intended
 *  to represent the total errors in specifying direction to the
 *  point
 */
public class Lines
{
  /** compute error given points from which observations were taken,
   *  corresponding vectors in direction of unknown point, and a
   *  minimum distance: we ignore any observations closer than that
   *  distance as get very stupid directions and if it really was
   *  that close we would know where it was
   */
  public static double error(double[] point, double[][] points,
    double[][] directions, double minDistance)
  {
    double total = 0.0;
    double sqMin = minDistance * minDistance;
    int dim = point.length;
    if (directions.length != points.length)
    {
      throw new IllegalArgumentException(
	"Points and directions do not match");
    }
    double[] diff = new double[point.length];
    for (int i = 0; i < points.length; i++)
    {
      double[] here = points[i];
      double[] dir = directions[i];
      if (here.length != point.length)
      {
        throw new IllegalArgumentException("dim mismatch in point");
      }
      if (dir.length != point.length)
      {
        throw new IllegalArgumentException("dim mismatch in dir");
      }
      // Work out dot product with direction
      double dot = 0.0;
      double distance = 0.0;
      // don't assume direction is a unit vector
      double scale = 0.0;
      for (int j = 0; j < diff.length; j++)
      {
        double d = point[j] - here[j];
        diff[j] = d;
	// dot product of distance from point with direction vector
	dot += d * dir[j];
	distance += d * d;
	scale += dir[j] * dir[j];
      }
      if (scale == 0.0)
      { // zero direction
        continue;
      }
      if (distance < sqMin)
      { // don't count stuff that is too close
        continue;
      }
      // subtract off direction to find perpendicular distance
      double perp = 0.0;
      for (int j = 0; j < diff.length; j++)
      {
        double pd = point[j] - (here[j] + dot * dir[j] / scale);
        perp += pd * pd;
      }
      // Error is perpendicular distance / distance
      total += perp / distance;
    }
    return total;
  }
  /** compute revised solution given previous solution or null */
  public static double[] revise(double[] point, double[][] points,
    double[][] directions, double minDistance)
  {
    // Each component of error can be written as
    // [|a-x|^2 - |(a-x).v*v|^2] / |a-x|^2
    // which is 1 - ((a-x).v)^2 / |a-x|^2
    // assuming v is a unit vector.
    // The error as a whole is SUM fi/gi, and has derivatives
    // fi'/gi - figi'/gi^2
    // let Gi be a constant equal to gi at the current point,
    // and Fi be a constant equal to fi at the current point. Then
    // SUM fi/Gi - Figi/Gi^2 has the same derivatives at the current
    // point as SUM fi/gi, and is a quadratic. Work out its terms
    // so we can optimise it in the hope of improving on the current
    // point, except that if we don't have a current point we
    // set Gi = 1.0 and Fi = 0.0 which amounts to working out the
    // point that minimises the sum of perpendicular distances

    // Another way of looking at this is that we choose to ignore
    // 3 out of 5 terms from the calculation of the second partial
    // derivatives of f/g - oops! - but it seems to work
    if (points.length == 0)
    {
      throw new IllegalArgumentException("No data");
    }
    double sqMin = minDistance * minDistance;
    // don't care about constant terms as they don't affect the
    // position of the optimum, just its value
    // linear terms of fi/Gi - Figi/Gi^2
    double[] linearCoeff = new double[points[0].length];
    // xixj terms of fi/Gi - Figi/Gi^2
    double[] squareCoeff = new double[linearCoeff.length *
      linearCoeff.length];
    // We will express our quadratic as x'Sx + l.x
    for (int i = 0; i < points.length; i++)
    {
      double[] here = points[i];
      double[] dir = directions[i];
      double gi = 1.0;
      double fi = 0.0;
      if (point != null)
      {
	// Work out dot product with direction
	double dot = 0.0;
	double distance = 0.0;
	// don't assume direction is a unit vector
	double scale = 0.0;
	double[] diff = new double[point.length];
	for (int j = 0; j < diff.length; j++)
	{
	  double d = point[j] - here[j];
	  diff[j] = d;
	  // dot product of distance from point with direction vector
	  dot += d * dir[j];
	  distance += d * d;
	  scale += dir[j] * dir[j];
	}
	if (scale == 0.0)
	{ // zero direction
	  continue;
	}
	if (distance < sqMin)
	{ // sitting right on the point so can't trust its
	  // direction
	  continue;
	}
	// subtract off direction to find perpendicular distance
	double perp = 0.0;
	for (int j = 0; j < diff.length; j++)
	{
	  double pd = point[j] - (here[j] + dot * dir[j] / scale);
	  perp += pd * pd;
	}
	gi = distance;
	fi = perp;
      }
      // System.out.println("gi = " + gi);
      // System.out.println("fi = " + fi);
      // gi = |ai-x|^2, so we get a square term of 1 and a linear
      // term of -2ai and no cross product terms
      double gscale = -fi / (gi * gi);
      double fscale = 1.0 / gi;
      // because fi/gi is actually of form (a-b)/a all the
      // gscale terms actually appear as fscale terms as well
      gscale += fscale;
      // For possible future optimisation, note that the square and
      // linear components calculated here are almost fixed, with
      // a dependence on the current point entering only via gscale
      // and fscale, which act to produce a linear combination of
      // two fixed contributions.
      // System.out.println("Adjust gscale to " + gscale);
      double scale = 0.0;
      for (int j = 0; j < linearCoeff.length; j++)
      {
	linearCoeff[j] -= 2.0 * here[j] * gscale;
	squareCoeff[j + j * linearCoeff.length] += gscale;
	scale += dir[j] * dir[j];
      }
      // Now work out -|(a-x).v|^2
      for (int j = 0; j < linearCoeff.length; j++)
      {
        for (int k = 0; k < linearCoeff.length; k++)
	{
	  double commonTerm = dir[j] * dir[k] * fscale / scale;
	  squareCoeff[j + k * linearCoeff.length] -=
	    commonTerm;
	  linearCoeff[j] += here[k] * commonTerm;
	  linearCoeff[k] += here[j] * commonTerm;
	}
      }
      // System.out.println("Linear terms now " + Arrays.toString(
      //   linearCoeff));
    }
    // Now we have x'Ax + b.x, so solution is -A^-1b/2
    LU lu = new LU(linearCoeff.length, squareCoeff);
    while (lu.getDeterminant() == 0.0)
    {
      System.out.println("Degenerate matrix");
      // no single solution that minimises x'Ax + bx. Add in
      // x'x term.
      double maxSeen = 0.0;
      for (int i = 0; i < squareCoeff.length; i++)
      {
        double m = Math.abs(squareCoeff[i]);
	if (m > maxSeen)
	{
	  maxSeen = m;
	}
      }
      if (maxSeen == 0.0)
      {
        maxSeen = 1.0;
      }
      else
      {
        maxSeen = maxSeen * 0.001;
      }
      for (int i = 0; i < linearCoeff.length; i++)
      {
        squareCoeff[i + i * linearCoeff.length] += maxSeen;
      }
      lu = new LU(linearCoeff.length, squareCoeff);
    }
    double[] solution = new double[linearCoeff.length];
    for (int i = 0; i < linearCoeff.length; i++)
    {
      linearCoeff[i] *= -0.5;
    }
    lu.solve(linearCoeff, solution);
    return solution;
  }
  /** compute revised solution given previous solution or null */
  public static double[] reviseFull2(double[] point, double[][] points,
    double[][] directions, double minDistance)
  {
    // Each component of error can be written as
    // [|a-x|^2 - |(a-x).v*v|^2] / |a-x|^2
    // which is 1 - ((a-x).v)^2 / |a-x|^2
    // assuming v is a unit vector.
    // The error as a whole is SUM fi/gi, and has derivatives
    // fi'/gi - figi'/gi^2

    // The partial derivatives are
    // 1/g f" - 1/g^2f'g' - 1/g^2f'g' + 2f/g^3 g'g' - f/g^2 g"
    // where f" and g" are d/dxidxj
    // and f' is d/dxi or d/dxj alternately
    if (points.length == 0)
    {
      throw new IllegalArgumentException("No data");
    }
    double sqMin = minDistance * minDistance;
    // don't care about constant terms as they don't affect the
    // position of the optimum, just its value
    // D/dxi
    double[] firstDeriv = new double[points[0].length];
    // d/dxidxj
    double[] secondDeriv = new double[firstDeriv.length *
      firstDeriv.length];
    boolean all = true;
    if (point == null)
    {
      point = new double[firstDeriv.length];
      // if no starting point treat g as a constant and optimise
      // only f to get perpendicular distance
      all = false;
    }
    double[] fd = new double[firstDeriv.length];
    double[] gd = new double[firstDeriv.length];
    for (int i = 0; i < points.length; i++)
    {
      double[] here = points[i];
      double[] dir = directions[i];
      // Work out dot product with direction
      double dot = 0.0;
      double distance = 0.0;
      // don't assume direction is a unit vector
      double scale = 0.0;
      double[] diff = new double[point.length];
      for (int j = 0; j < diff.length; j++)
      {
	double d = point[j] - here[j];
	diff[j] = d;
	// dot product of distance from point with direction vector
	dot += d * dir[j];
	distance += d * d;
	scale += dir[j] * dir[j];
      }
      if (scale == 0.0)
      { // zero direction
	continue;
      }
      if (distance < sqMin)
      { // sitting right on the point so can't trust its
	// direction
	continue;
      }
      // subtract off direction to find perpendicular distance
      double perp = 0.0;
      for (int j = 0; j < diff.length; j++)
      {
	double pd = point[j] - (here[j] + dot * dir[j] / scale);
	perp += pd * pd;
      }
      double gi = distance;
      double fi = perp;
      // System.out.println("Gi " + gi + " fi " + fi);
      // g" is simply 2I
      for (int j = 0; j < firstDeriv.length; j++)
      {
	if (all)
	{
	  secondDeriv[j + j * firstDeriv.length] -= 2.0 * fi / (gi * gi);
	}
	// account for first part of f"
	secondDeriv[j + j * firstDeriv.length] += 2.0 / gi;
      }
      // Now do second part of f"
      for (int j = 0; j < firstDeriv.length; j++)
      {
        for (int k = 0; k < firstDeriv.length; k++)
	{
	  secondDeriv[j + k * firstDeriv.length] -=
	    2.0 * dir[j] * dir[k] / (gi * scale);
	}
      }
      // Compute f' and g'
      for (int j = 0; j < firstDeriv.length; j++)
      {
	gd[j] = 2.0 * (point[j] - here[j]);
	fd[j] = gd[j] - 2.0 * dir[j] * dot / scale;
	if (!all)
	{
	  gd[j] = 0.0;
	}
	firstDeriv[j] += fd[j] / gi - (fi * gd[j]) / (gi * gi);
      }
      // Intermediate terms can be generated from gd and fd
      for (int j = 0; j < firstDeriv.length; j++)
      {
        for (int k = 0; k < firstDeriv.length; k++)
	{
	  double terms = -(fd[j] * gd[k] + fd[k] * gd[k]) / (gi * gi) +
	    2.0 * fi * gd[j] * gd[k] / (gi * gi * gi);
	  secondDeriv[j + k * firstDeriv.length] += terms;
	}
      }
    }
    // Now we have the first and second partial derivatives of
    // our objective function at the current point. To set the
    // first partial derivative to zero, we want to move x by
    // -D2^-1 d
    for (int i = 0; i < firstDeriv.length; i++)
    {
      firstDeriv[i] *= -1.0;
    }
    double startError = error(point, points, directions,
      minDistance);
    double factor = 1.0;
    for (int i = 0; i < 10; i++)
    {
      LU lu = new LU(firstDeriv.length, secondDeriv);
      if (lu.getDeterminant() != 0)
      {
	double[] solution = new double[firstDeriv.length];
	lu.solve(firstDeriv, solution);
	for (int j = 0; j < firstDeriv.length; j++)
	{
	  solution[j] += point[j];
	}
	double errorHere = error(solution, points, directions,
	  minDistance);
        if (errorHere < startError)
	{
	  return solution;
	}
      }
      System.out.println("Retry solve");
      // no single solution that minimises x'Ax + bx. Add in
      // x'x term, to reduce step length and make matrix more
      // manageable
      double maxSeen = 0.0;
      for (int j = 0; j < secondDeriv.length; j++)
      {
        double m = Math.abs(secondDeriv[j]);
	if (m > maxSeen)
	{
	  maxSeen = m;
	}
      }
      if (maxSeen == 0.0)
      {
        maxSeen = factor;
      }
      else
      {
        maxSeen = maxSeen * factor;
      }
      for (int j = 0; j < firstDeriv.length; j++)
      {
        secondDeriv[j + j * firstDeriv.length] += maxSeen;
      }
      factor = factor * 2.0;
    }
    return point;
  }
  /** method to converge to a solution from scratch */
  public static double[] solve(double[][] points,
    double[][] directions, double minDistance, int maxPasses)
  {
    double[] current = reviseFull2(null, points, directions, minDistance);
    double[] sofar = null;
    double bestSoFar = Double.MAX_VALUE;
    for (int i = 0; i < maxPasses; i++)
    {
      double errorHere = error(current, points, directions,
        minDistance);
      System.out.println("Error " + errorHere + " at pass " + i +
        " for " + Arrays.toString(current));
      if (errorHere >= bestSoFar)
      {
	if (true)
	{
	  return sofar;
	}
	else
	{
	  double[] mean = new double[sofar.length];
	  for (int j = 0; j < mean.length; j++)
	  {
	    mean[j] = (sofar[j] * 7.0 + current[j]) / 8.0;
	  }
	  errorHere = error(mean, points, directions,
	    minDistance);
	  System.out.println("Mean Error " + errorHere + " at pass " +
	    i + " for " + Arrays.toString(mean));
	  if (errorHere >= bestSoFar)
	  {
	    return sofar;
	  }
	  current = mean;
	}
      }
      sofar = current;
      bestSoFar = errorHere;
      current = reviseFull2(current, points, directions, minDistance);
    }
    return sofar;
  }
  /** make up a solution */
  private static void pose(double[][] points, double[][] dir,
    double[] answer, Random r)
  {
    for (int i = 0; i < 3; i++)
    {
      answer[i] = r.nextGaussian(); 
      // (1.0 - 2 * r.nextInt(2)) * r.nextInt(10);
    }
    double errorSize = 0.1;
    for (int i = 0; i < points.length; i++)
    {
      points[i] = new double[3];
      dir[i] = new double[3];
      for (int j = 0; j < 3; j++)
      {
	points[i][j] = r.nextGaussian();
	// points[i][j] = (1.0 - 2 * r.nextInt(2)) * r.nextInt(10);
	dir[i][j] = answer[j] - points[i][j] +
	  r.nextGaussian() * errorSize;
      }
    }
  }
  public static void main(String[] s)
  {
    double minDistance = 1.0E-6;
    // Trivial example
    double[][] points = new double[][]
    { 
      new double[] {1.0, 0.0, 0.0},
      new double[] {0.0, 1.0, 0.0},
      new double[] {0.0, 0.0, 1.0}
    };
    double[][] dirs = new double[][]
    {
      new double[] {-1.0, 0.0, 0.0},
      new double[] {0.0, -1.0, 0.0},
      new double[] {0.0, 0.0, -1.0}
    };
    int maxPasses = 20;
    double[] answer = solve(points, dirs, minDistance, maxPasses);
    System.out.println("Answer is " + Arrays.toString(answer));
    System.out.println("Error at zero is " +
      error(new double[3], points, dirs, minDistance));
    /** add error */
    double[][] edirs = new double[][]
    {
      new double[] {-1.0, 0.01, 0.01},
      new double[] {0.01, -1.0, 0.01},
      new double[] {0.01, 0.01, -1.0}
    };
    answer = solve(points, edirs, minDistance, maxPasses);
    System.out.println("Answer is " + Arrays.toString(answer));
    System.out.println("Error at zero is " +
      error(new double[3], points, edirs, minDistance));
    // shift points up
    double[][] pointsUp = new double[][]
    { 
      new double[] {2.0, 1.0, 1.0},
      new double[] {1.0, 2.0, 1.0},
      new double[] {1.0, 1.0, 2.0}
    };
    answer = solve(pointsUp, dirs, minDistance, maxPasses);
    System.out.println("Answer is " + Arrays.toString(answer));
    System.out.println("Error at zero is " +
      error(new double[3], pointsUp, dirs, minDistance));
    // Exercise cross-product terms
    double[][] xpoints = new double[][]
    { 
      new double[] {1.0, 1.0, 0.0},
      new double[] {0.0, 1.0, 1.0},
      new double[] {0.0, 0.0, 1.0}
    };
    double[][] xdirs = new double[][]
    {
      new double[] {-1.0, -1.0, 0.0},
      new double[] {0.0, -1.0, -1.0},
      new double[] {0.0, 0.0, -1.0}
    };
    answer = solve(xpoints, xdirs, minDistance, maxPasses);
    System.out.println("Answer is " + Arrays.toString(answer));
    System.out.println("Error at zero is " +
      error(new double[3], xpoints, xdirs, minDistance));
    int goes = 10000;
    long seed = 42;
    int maxNumPoints = 10;
    Random r = new Random(seed);
    double maxError = 0.0;
    double maxErrorAbove = 0.0;
    int above = 0;
    for (int i = 0; i < goes; i++)
    {
      int numPoints = r.nextInt(maxNumPoints) + 1;
      double[][] pHere = new double[numPoints][];
      double[][] dirHere = new double[numPoints][];
      double[] constructedAnswer = new double[3];
      pose(pHere, dirHere, constructedAnswer, r);
      for (int j = 0; j < pHere.length; j++)
      {
        System.out.println("Point " + Arrays.toString(pHere[j]));
        System.out.println("Dir " + Arrays.toString(dirHere[j]));
      }
      System.out.println("Constructed answer is " +
        Arrays.toString(constructedAnswer));
      double[] ourAnswer = solve(pHere, dirHere, minDistance, maxPasses);
      double errorHere = error(ourAnswer, pHere, dirHere, minDistance);
      if (errorHere > maxError)
      {
        maxError = errorHere;
      }
      System.out.println("Answer is " + Arrays.toString(ourAnswer));
      double cError = error(constructedAnswer, pHere, dirHere,
        minDistance);
      if (errorHere > cError)
      {
        System.out.println("Above constructed error");
	above++;
      }
      if ((errorHere > cError) && (errorHere > maxErrorAbove))
      {
        maxErrorAbove = errorHere;
      }
      System.out.println("Error at constructed answer is " + cError);
      System.out.println();
    }
    System.out.println("Max error is " + maxError);
    System.out.println("Max error above constructed error is " +
      maxErrorAbove);
    System.out.println("Above constructed " + above + " of " + goes);
  }
}
