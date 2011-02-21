package uk.co.demon.mcdowella.stats;

import java.util.Arrays;

/** Class to calculate the basis for a spline approximation. For our
 *  purposes, a spline is a linear combination of more basic functions,
 *  of the form max(0, x-a)^n, where n is the order of the spline and
 *  a is a knot position. For n=0 we just have piecewise constant
 *  functions. For n=1 the result is continuous because max(0, x-a) = 0
 *  at x = a, and for n=2 the result has continuous derivatives because
 *  (max(0, x-a)^2)' = 0 at x=a. We chose linear combinations of
 *  functions such that the resulting basis vectors are zero except on
 *  a finite interval of points. Again, for n=0 each basis function is
 *  a constant for some interval and 0 outside it. For n=1 each basis
 *  function is a triangle which rises from zero to a peak and then
 *  drops back to zero. For n=2 each basis function
 */
public class Spline
{
  /** Never need to construct one */
  private Spline()
  {
  }
  /** return a vector of vectors, where each vector is the result
   *  of evaluation a basis vector at all the points. Knots mark
   *  where the pieces of the splines start and end, and must
   *  be destinct. The knots must strictly enclose the points, and
   *  there must
   *  be at least two of them. Degree is the degree of the splines,
   *  which at the moment must be 0, 1, or 2.
   *  can optionally return vector of derivatives of basis instead
   */
  public static double[][] createBasis(double[] knots, double[] points,
    int degree, boolean derivativeInstead)
  {
    if (degree < 0)
    {
      throw new IllegalArgumentException("-ve degree");
    }
    if (knots.length < 2)
    {
      throw new IllegalArgumentException("Only " + knots.length +
        " knots");
    }
    double[] sortedKnots = knots.clone();
    Arrays.sort(sortedKnots);
    for (int i = 1; i < sortedKnots.length; i++)
    {
      if (sortedKnots[i] <= sortedKnots[i - 1])
      {
        throw new IllegalArgumentException("Identical knots");
      }
    }
    double min = sortedKnots[0];
    double max = sortedKnots[sortedKnots.length - 1];
    for (int i = 0; i < points.length; i++)
    {
      if ((points[i] <= min) || (points[i] >= max))
      {
        throw new IllegalArgumentException("Point not inside knots");
      }
    }
    switch (degree)
    {
      case 0:
        return constantBasis(sortedKnots, points, derivativeInstead);
      case 1:
        return linearBasis(sortedKnots, points, derivativeInstead);
      case 2:
        return quadraticBasis(sortedKnots, points, derivativeInstead);
      default:
        throw new IllegalArgumentException("Cannot handle degree " +
	  degree);
    }
  }
  static double[][] constantBasis(double[] sortedKnots, double[] points,
    boolean derivativeInstead)
  {
    double[][] basis = new double[sortedKnots.length - 1][];
    for (int i = 0; i < basis.length; i++)
    {
      basis[i] = new double[points.length];
    }
    if (derivativeInstead)
    {
      return basis;
    }
    for (int i = 0; i < points.length; i++)
    {
      double p = points[i];
      int pos = Arrays.binarySearch(sortedKnots, p);
      if (pos >= 0)
      { // exact match: point it at that knot
        basis[pos][i] = 1.0;
      }
      else
      {
        int ip = -pos - 1;
	// ip points to first element > key, or off end of list if
	// greater but we know that can't happen because we checked for
	// it. Use the previous element
	basis[ip - 1][i] = 1.0;
      }
    }
    return basis;
  }
  static double[][] linearBasis(double[] sortedKnots, double[] points,
    boolean derivativeInstead)
  {
    double[][] basis = new double[sortedKnots.length][];
    for (int i = 0; i < basis.length; i++)
    {
      basis[i] = new double[points.length];
    }
    for (int i = 0; i < points.length; i++)
    {
      double p = points[i];
      int pos = Arrays.binarySearch(sortedKnots, p);
      int ip;
      if (pos >= 0)
      { // exact match: leave as is
        ip = pos;
      }
      else
      {
        ip = -pos - 1;
	// ip points to first element > key, or off end of list if
	// greater but we know that can't happen because we checked for
	// it. Use the previous element
	ip--;
      }
      // Our basis functions are triangles rising up to a single point,
      // taking the value of 1.0 there, and zero at all other points.
      // Each interval between knots is under the right side of one
      // triangle and the left side of another.
      if (derivativeInstead)
      {
	double relativePosition = 1.0 /
	  (sortedKnots[ip + 1] - sortedKnots[ip]);
	basis[ip][i] += -relativePosition;
	basis[ip + 1][i] += relativePosition;
      }
      else
      {
	double relativePosition = (p - sortedKnots[ip]) /
	  (sortedKnots[ip + 1] - sortedKnots[ip]);
	basis[ip][i] += 1.0 - relativePosition;
	basis[ip + 1][i] += relativePosition;
      }
    }
    return basis;
  }
  static double[][] quadraticBasis(double[] sortedKnots, double[] points,
    boolean derivativeInstead)
  {
    double[][] basis = new double[sortedKnots.length + 1][];
    for (int i = 0; i < basis.length; i++)
    {
      basis[i] = new double[points.length];
    }
    for (int i = 0; i < points.length; i++)
    {
      double p = points[i];
      int pos = Arrays.binarySearch(sortedKnots, p);
      int ip;
      if (pos >= 0)
      { // exact match: leave as is
        ip = pos;
      }
      else
      {
        ip = -pos - 1;
	// ip points to first element > key, or off end of list if
	// greater but we know that can't happen because we checked for
	// it. Use the previous element
	ip--;
      }
      // a and b are set to true or imagined position of knots
      // before matching sortedKnots[ip] and sortedKnots[ip + 1]
      double a;
      double b;
      switch (ip)
      {
	case 0:
	{
	  double gap = sortedKnots[ip + 1] - sortedKnots[ip];
	  b = sortedKnots[ip] - gap;
	  a = b - gap;
	}
	break;
	case 1:
	{
	  a = 2.0 * sortedKnots[0] - sortedKnots[1];
	  b = sortedKnots[0];
	}
	break;
	default:
	{
	  a = sortedKnots[ip - 2];
	  b = sortedKnots[ip - 1];
	}
      }
      // c and d are set to true or imagined position of knots
      // after sortedKnots[ip] and sortedKnots[ip + 1]
      double c;
      double d;
      switch (sortedKnots.length - ip)
      {
	case 2:
	{
	  double gap = sortedKnots[ip + 1] - sortedKnots[ip];
	  // System.out.println("Gap " + gap);
	  c = sortedKnots[ip + 1] + gap;
	  d = c + gap;
	}
	break;
	case 3:
	{
	  /*
	  System.out.println("3Gap " + sortedKnots[ip + 1] + " " +
	    sortedKnots[ip + 2]);
	  */
	  c = sortedKnots[ip + 2];
	  d = sortedKnots[ip + 2] * 2.0 - sortedKnots[ip + 1];
	}
	break;
	default:
	{
	  c = sortedKnots[ip + 2];
	  d = sortedKnots[ip + 3];
	}
      }
      /*
      System.out.println("--");
      System.out.println(" p " + p + " a " + a + " b " + b +
        " c " + c + " d " + d + " ip " + sortedKnots[ip] +
	" ip1 " + sortedKnots[ip + 1]);
      */
      // earliest spline is furthest along its progression
      setQuadratic(a, b, sortedKnots[ip], sortedKnots[ip + 1],
	basis[ip], i, 2, p, derivativeInstead);
      setQuadratic(b, sortedKnots[ip], sortedKnots[ip + 1], c,
	basis[ip + 1], i, 1, p, derivativeInstead);
      setQuadratic(sortedKnots[ip], sortedKnots[ip + 1], c, d,
	basis[ip + 2], i, 0, p, derivativeInstead);
    }
    return basis;
  }
  /** fill in the basis vector for a quadratic with points at
   *  a, b, c, and d. Use the target offset into the basis vector
   *  given that the point (position given) is in the given segment,
   *  counting the segment from a to b as segment 0
   */
  private static void setQuadratic(double a, double b, double c,
    double d, double[] basis, int target, int segment, double pos,
    boolean derivativeInstead)
  {
    // We have a three-segment quadratic spline formed by subtracting
    // one two-segment spline from another. Both splines end with
    // derivative zero and we scale the second spline to ensure that
    // the result is zero at the final point.

    // The first spline ends as (x-a)^2 - (x-b)^2(c-a)/(c-b)
    double ca = c - a;
    double cb = c - b;
    double finalFirst = ca * ca - cb * ca;
    // the second spline ends as (x-b)^2 - (x-c)^2(d-b)/(d-c)
    double db = d - b;
    double dc = d - c;
    double finalSecond = db * db - dc * db;
    double secondScale = finalFirst / finalSecond;
    double value;
    switch (segment)
    {
      case 0:
      { // only first spline
	if (derivativeInstead)
	{
	  value = 2.0 * pos - 2.0 * a;
	}
	else
	{
	  value = (pos - a) * (pos - a);
	}
      }
      break;
      case 2:
      { // final value of first spline - second spline
	if (derivativeInstead)
	{
	  double secondValue = 2.0 * pos - 2.0 * b -
	    (2.0 * pos - 2.0 * c) * db / dc;
	  value = - secondValue * secondScale;
	}
	else
	{
	  double secondValue = (pos - b) * (pos - b) -
	    (pos - c) * (pos - c) * db / dc;
	  value = finalFirst - secondValue * secondScale;
	}
      }
      break;
      case 1:
      {
	if (derivativeInstead)
	{
	  double firstContrib = 2.0 * pos - 2.0 * a -
	    (2.0 * pos - 2.0 * b) * ca / cb;
	  double secondContrib = 2.0 * pos - 2.0 * b;
	  value = firstContrib - secondScale * secondContrib;
	}
	else
	{
	  double firstContrib = (pos - a) * (pos - a) -
	    (pos - b) * (pos - b) * ca / cb;
	  double secondContrib = (pos - b) * (pos - b);
	  value = firstContrib - secondScale * secondContrib;
	}
      }
      break;
      default:
        throw new IllegalArgumentException("Segment out of range");
    }
    basis[target] = value;
  }
}
