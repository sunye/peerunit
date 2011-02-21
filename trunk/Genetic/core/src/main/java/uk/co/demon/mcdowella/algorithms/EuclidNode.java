package uk.co.demon.mcdowella.algorithms;

import java.util.Arrays;
import java.util.Comparator;

/** This class is for use with the nearest neighbour classes
 *  such as Closest and StaticClosest. It provides a node whose
 *  lower bound calculations are based on bounding boxes. Its
 *  distance is the usual distance between two points expressed
 *  as coordinates: sqrt(x^2 + y^2 + ...).
 *  <br>
 *  We explicitly implement both Boundable and Visible,
 *  even though Visible in fact extends Boundable.
 */
public class EuclidNode implements FindScattered.Boundable<EuclidNode>,
  StaticClosest.Visible<EuclidNode>, StaticClosest.Positionable
{
  /** Position in coordinates */
  private final double[] position;
  /** for Positionable */
  public int getDim()
  {
    return position.length;
  }
  /** for Positionable */
  public double getCoordinate(int dim)
  {
    return position[dim];
  }
  /** minimum coordinate in this node, or any of its children */
  private final double[] minCoordinate;
  /** maximum coordinate in this node, or any of its children */
  private final double[] maxCoordinate;
  /** whether visible */
  public boolean isVisible()
  {
    return true;
  }
  /** Create given coordinates of position */
  public EuclidNode(double[] forPos)
  {
    position = forPos.clone();
    minCoordinate = position.clone();
    maxCoordinate = position.clone();
  }
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("Pos ");
    sb.append(Arrays.toString(position));
    sb.append(" min ");
    sb.append(Arrays.toString(minCoordinate));
    sb.append(" max ");
    sb.append(Arrays.toString(maxCoordinate));
    return sb.toString();
  }
  /** Might as well just return square of distance here because
   *  comparisons will return the same result.
   */
  public double distance(EuclidNode d, double maxInteresting)
  {
    double sum = 0.0;
    for (int i = 0; (i < position.length) && (sum <= maxInteresting); i++)
    {
      double diff = position[i] - d.position[i];
      sum += diff * diff;
    }
    return sum;
  }
  /**
   * returns lower bound on square of actual distance, for consistency
   * with distance()
   */
  public double lowerBound(EuclidNode d, double maxInteresting)
  {
    double sum = 0.0;
    for (int i = 0; (i < position.length) && (sum <= maxInteresting); i++)
    {
      double here = d.position[i];
      double low = here - minCoordinate[i];
      double high = here - maxCoordinate[i];
      if ((low >= 0.0) != (high >= 0.0))
      { // Point lies within the min and max, so could have
	// zero difference here
	continue;
      }
      // Point is outside range, so add on min distance to range
      low = Math.abs(low);
      high = Math.abs(high);
      if (low < high)
      {
	sum += low * low;
      }
      else
      {
	sum += high * high;
      }
    }
    return sum;
  }
  public boolean updated(EuclidNode left, EuclidNode right)
  {
    // System.out.println("Updated on " + this + " left " + left +
    //   " right " + right);
    boolean changed = false;
    for (int i = 0; i < position.length; i++)
    {
      final double x = position[i];
      double min = x;
      double max = x;
      if (left != null)
      {
	final double lmin = left.minCoordinate[i];
	if (lmin < min)
	{
	  min = lmin;
	}
	final double lmax = left.maxCoordinate[i];
	if (lmax > max)
	{
	  max = lmax;
	}
      }
      if (right != null)
      {
	final double rmin = right.minCoordinate[i];
	if (rmin < min)
	{
	  min = rmin;
	}
	final double rmax = right.maxCoordinate[i];
	if (rmax > max)
	{
	  max = rmax;
	}
      }
      double minBefore = minCoordinate[i];
      double maxBefore = maxCoordinate[i];
      if (min != minBefore)
      {
	// System.out.println("Change min " + i + " from " + minBefore +
	//   " to " + min);
	minCoordinate[i] = min;
	changed = true;
      }
      if (max != maxBefore)
      {
	// System.out.println("Change max " + i + " from " + maxBefore +
	//   " to " + max);
	maxCoordinate[i] = max;
	changed = true;
      }
    }
    return changed;
  }
}
