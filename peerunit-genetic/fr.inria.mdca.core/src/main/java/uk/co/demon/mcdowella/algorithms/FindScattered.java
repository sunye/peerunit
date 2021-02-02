package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** This class contains an interface for objects which are separated
 *  by computable distances, and a static method to cluster such
 *  objects. There is some other stuff related to methods of finding 
 *  nearest points that seem, in practice, to be less efficient than 
 *  PatriciaClosest.
 */
public class FindScattered
{
  /** This interface defines an object that can compute the distance to
   *  objects of some type. It is sufficient for findScattered, but not
   *  Closest, or for the other stuff in this class. For findScattered,
   *  it suffices that the distance is symmetric. The second parameter 
   *  is an indication of the maximum distance that is interesting. All
   *  values above that will produce the same behaviour, so e.g. if 
   *  you calculate the distance by adding up a large number of 
   *  positive values, once you are above that value you might as well 
   *  stop.
   */
  public interface Rangeable<X>
  {
    double distance(X other, double maxInteresting);
  }
  private static boolean TRACE = false;
  /** This routine goes through a list of points and picks out a
   *  subset that may be widely scattered. If passed an array of
   *  points grouped into clusters where the distance between
   *  clusters is much larger than the distance between points
   *  in the same cluster, it should return points in as many different
   *  clusters as possible, but it does not guarantee to return an
   *  optimum solution.
   */
  public static<W extends Rangeable> List<W> findScattered(
    Iterable<W> input, int numPoints)
  {
    // Fill up with numPoints first
    List<W> result = new ArrayList<W>();
    Iterator<W> it = input.iterator();
    for (int i = 0; i < numPoints; i++)
    {
      if (!it.hasNext())
      {
        break;
      }
      result.add(it.next());
    }
    if ((result.size() < numPoints) || (numPoints <= 1))
    { // must have run out of points, or result so far will do
      // as well as any
      return result;
    }
    // We will deal with each remaining point in turn, keeping track
    // of the minimum distance between the points we hold. If the
    // minimum distance from the new point to any of the points we
    // hold is larger than that minimum distance, we replace the
    // closest of the two points linked by the min distance by the
    // new point.

    // If the input points are split into clusters with distances
    // between clusters large in comparison to distances within
    // clusters then we should accept points from clusters we have
    // never seen before as long as we have two points from the
    // same cluster. 

    // We use MaxInRange so that the cost of doing all these
    // comparisons doesn't grow too fast. Because we are using
    // MaxInRange it is easier to think about targeting maximum
    // values (which are really -distance)

    // This records, for each point, the distance to all other points,
    // and is used to track the minimum such distance.
    MaxInRange[] perPoint = new MaxInRange[numPoints];
    // This will be - the minimum distance between any two different
    // points
    long maxInMatrix = Long.MIN_VALUE;
    // These will be the two ends of the minimum distance
    int minFrom = -1;
    int minTo = -1;
    for (int i = 0; i < numPoints; i++)
    {
      perPoint[i] = new MaxInRange(numPoints);
      // Null out distance to self
      perPoint[i].set(i, Long.MIN_VALUE);
      for (int j = 0; j < i; j++)
      {
        double d = result.get(i).distance(result.get(j),
	  Double.MAX_VALUE);
	// We will care about minimums, so flip sign
	long converted = MaxInRange.toOrderedLong(-d);
	if (TRACE)
	{
	  System.out.println("Distance from " + i + " to " + j +
	    " is " + d);
	}
	perPoint[i].set(j, converted);
	perPoint[j].set(i, converted);
	if (converted > maxInMatrix)
	{
	  maxInMatrix = converted;
	  minFrom = i;
	  minTo = j;
	}
      }
    }
    // Keep track of the distances from the existing point to a new
    // point under consideration
    double[] newDistanceAsDouble = new double[numPoints];
    while (it.hasNext())
    {
      W point = it.next();
      double minDistance = Double.MAX_VALUE;
      long converted = Long.MAX_VALUE;
      // anything above the current minimum distance is just cream on 
      // top
      double maxInteresting = -MaxInRange.fromOrderedLong(maxInMatrix);
      for (int i = 0; i < numPoints; i++)
      {
        double d = point.distance(result.get(i), maxInteresting);
	newDistanceAsDouble[i] = d;
	if (TRACE)
	{
	  System.out.println("New distance to " + i + " is " + d);
	}
	if (d <= minDistance)
	{ // Allow equality through here to guarantee that we accept
	  // first time round
	  minDistance = d;
	  converted = MaxInRange.toOrderedLong(-minDistance);
	  if (converted >= maxInMatrix)
	  { // too close already
	    break;
	  }
	}
      }
      if (TRACE)
      {
	System.out.println("Compare with " +
	  -MaxInRange.fromOrderedLong(maxInMatrix));
      }
      if (converted >= maxInMatrix)
      { // distance to point we hold is <= min distance between
        // points
	if (TRACE)
	{
	  System.out.println("Reject");
	}
        continue;
      }
      // Here to accept a new point. The cost of this step must grow
      // at least with the number of points held, because we have that
      // many distances to compute. Because we use MaxInRange, it costs
      // no more than an additional log(points held) factor times this.
      int atPoint;
      // Swap with whichever of the two existing min distance points
      // is closest, so remaining distance is longer
      if (newDistanceAsDouble[minFrom] < newDistanceAsDouble[minTo])
      {
        atPoint = minFrom;
      }
      else
      {
        atPoint = minTo;
      }
      if (TRACE)
      {
	System.out.println("Accept at " + atPoint);
      }
      // Replace point and update bookkeeping
      result.set(atPoint, point);
      for (int i = 0; i < numPoints; i++)
      {
	// Recompute here as we might have cut corners earlier on
        double d = point.distance(result.get(i), Double.MAX_VALUE);
	long convertedHere = MaxInRange.toOrderedLong(-d);
        perPoint[i].set(atPoint, convertedHere);
        perPoint[atPoint].set(i, convertedHere);
      }
      perPoint[atPoint].set(atPoint, Long.MIN_VALUE);
      maxInMatrix = Long.MIN_VALUE;
      for (int i = 0; i < numPoints; i++)
      {
	int maxAt = perPoint[i].getMaxIndex(0, numPoints);
        long maxHere = perPoint[i].get(maxAt);
	if (maxHere >= maxInMatrix)
	{
	  maxInMatrix = maxHere;
	  minFrom = i;
	  minTo = maxAt;
	}
      }
    }
    return result;
  }
  /** Class for testFindScattered bearing a cluster number
   *  as well as a position and so on.
   */
  private static class ClusteredPoint extends EuclidNode
  {
    /** cluster number */
    private final int clusterNumber;
    /** work out a position within the specified cluster */
    private static double[] makePos(int clusterNum, int dim, Random r)
    {
      // Each cluster as its centre at a point 0,0,0,...,0,1,0,...
      // where the position of the zero depends on the cluster number
      double[] result = new double[dim];
      // The centres of the clusters are sqrt(2) apart, so if we
      // make their radii sqrt(2)/4 then the distance between any
      // two points in different clusters is at least sqrt(2)/2.
      // Shove in a fudge factor in case of floating point error
      final double radius = Math.sqrt(2.0) * 0.9999 / 4.0;
      double radHere = 0.0;
      for (int i = 0; i < result.length; i++)
      {
        result[i] = r.nextDouble() - 0.5;
	radHere += result[i] * result[i];
      }
      radHere = Math.sqrt(radHere);
      if (radHere > 0.0)
      {
        for (int i = 0; i < result.length; i++)
	{
	  result[i] = result[i] * radius / radHere;
	}
      }
      result[clusterNum] = result[clusterNum] + 1.0;
      return result;
    }
    ClusteredPoint(int clusterNum, int dim, Random r)
    {
      super(makePos(clusterNum, dim, r));
      clusterNumber = clusterNum;
    }
  }
  /** Test routine for findScattered. Create data with clusters
   *  where the distance between any two points in different
   *  clusters is greater than the distance between any two
   *  points in the same cluster and check that we retrieve
   *  as many points as possible.
   */
  static void testFindScattered(int numPoints,
    int numClusters, long seed)
  {
    if (numClusters > numPoints)
    {
      numClusters = numPoints;
    }
    // Create one point from each cluster
    List<ClusteredPoint> cpl = new ArrayList<ClusteredPoint>();
    Random r = new Random(seed);
    for (int i = 0; i < numClusters; i++)
    {
      cpl.add(new ClusteredPoint(i, numClusters, r));
    }
    // Now fill up with random points
    for (int i = numClusters; i < numPoints; i++)
    {
      cpl.add(new ClusteredPoint(r.nextInt(numClusters),
        numClusters, r));
    }
    // shuffle to derange our initial collection of points
    Collections.shuffle(cpl, r);
    if (TRACE)
    {
      for (ClusteredPoint cp: cpl)
      {
        System.out.println("Cluster " + cp.clusterNumber + " is " + cp);
      }
    }

    List<ClusteredPoint> selected = findScattered(cpl, numClusters);
    if (selected.size() > numClusters)
    {
      throw new IllegalStateException("Cluster cheat! too many points");
    }
    boolean[] seen = new boolean[numClusters];
    for (ClusteredPoint cp: selected)
    {
      seen[cp.clusterNumber] = true;
    }
    for (boolean wasSeen: seen)
    {
      if (!wasSeen)
      {
        throw new IllegalStateException("Missed cluster");
      }
    }
  }
  /** This interface represents objects living in a tree that can 
   *  compute a lower bound for the distance between a specified point 
   *  and any point in the tree below them, or themselves. To allow 
   *  them to keep track of summary information to do this, they are 
   *  provided with calls when their children change.
   */
  public interface Boundable<X> extends FindScattered.Rangeable<X>
  {
    /** Work out a lower bound on the minimum distance between the 
     *  given Node and this node, or any other node below it in
     *  the tree. Implementations of this will probably keep summary
     *  info in each Node, updating it when updated() is called. This
     *  should be consistent with Rangeable.distance() but does not
     *  otherwise have to have any particular properties. In particular,
     *  if any monotonic increasing function is applied to both, the
     *  properties of the code here should be unchanged. As with 
     *  distance, maxInteresting is provided as an (optional) 
     *  opportunity for saving time by quitting early once it is 
     *  obvious the answer will be > maxInteresting.
     */
    double lowerBound(X other, double maxInteresting);
    /** This is called after the tree has been modified. Each
     *  Node above a point of modification can get called, with
     *  children called before their parents. If you don't require
     *  a call to propagate your information up, return false from
     *  this method (although you may get a call anyway).
     *  <br>
     *  It is an opportunity for
     *  subclasses of this class to recalculate from their children
     *  any state that they keep to answer lowerBound() queries.
     *  Return whether this change alone requires a change in the
     *  parent.
     *  <br>
     *  The correctness of the main code here does not change if
     *  this function always returns true. But some self-checking code
     *  relies on this to return false when called on a node that
     *  is already up to date w.r.t. its children. So it is a very
     *  good idea for this to be the case.
     */
    boolean updated(X leftChild, X rightChild);
  }
}
