package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.io.PrintStream;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

/** This class finds the closest point or points to a query point.
 *  It keeps its collection of points in a balanced tree, and uses
 *  information stored in the nodes to narrow down the search. There
 *  isn't anything much you can say with certainty about its efficiency,
 *  but the main program here can be used to look at its behaviour in
 *  some example cases. StaticClosest or DynamicClosest are probably
 *  better. PatriciaClosest seems to be better than those.
 *  <br>
 *  The class Closest uses Nodes defined as abstract classes, so that
 *  code using it can provided different definitions for the distance
 *  function and a lower bound function. Each particular instance of
 *  a Closest should only use Nodes of one subclass of the Node class.
 *  To check this at compile time, Closest takes its Node type as
 *  a generic parameter, and requires that this extend its Node type.
 *  See the main program for examples of how to use it with
 *  example Node subclasses.
 *  <br>
 *  Not thread safe
 */
public class Closest<Y extends FindScattered.Boundable<Y>> implements Set<Y>
{
  /** The Node class is used to handle the tree structure.
   */
  private static class Node<X extends FindScattered.Boundable<X>>
  {
    /** point with distance information */
    private final X point;
    /** The left child */
    private Node<X> left;
    /** get the left point or null */
    X getLeftPoint()
    {
      if (left == null)
      {
        return null;
      }
      return left.point;
    }
    /** The right child */
    private Node<X> right;
    /** get the right point or null */
    X getRightPoint()
    {
      if (right == null)
      {
        return null;
      }
      return right.point;
    }
    /** The parent */
    private Node<X> parent;
    /** The balancing code is simple-minded. It requires a count
     *  of the maximum number of links to traverse along the tree to
     *  get from this node to any of its children.
     */
    private int maxDistanceToChild;
    /** Construct from original node */
    Node(X original)
    {
      point = original;
    }
    public String toString()
    {
      return "Node holding " + point.toString();
    }
  }
  /** Node based on Euclidean distance, but calculates bounds using
   *  distances to a few specified points and the triangle inequality.
   *  <br>
   *  Here this is really for testing only, but you might use such a
   *  strategy if you could compute a distance that obeyed the
   *  triangle inequality, but you couldn't get something close
   *  enough to a coordinate system that you could use a bounding
   *  box based estimate as in EuclidNode without excessive pain.
   *  <br>
   *  From d(a,c) &le; d(a,b) + d(b,c)
   *  <br>
   *  we get d(a,b) &ge; d(a,c) - d(b,c)
   *  <br>
   *  From d(b,c) &le; d(b,a) + d(a,c)
   *  <br>
   *  we get d(b,a) &ge; d(b,c) - d(a,c)
   *  <br>
   *  and this code assumes that distance is symmetric as well.
   */
  public static class TriangleNode implements 
    FindScattered.Boundable<TriangleNode>
  {
    /** Position in coordinates */
    private final double[] position;
    /** distance to selected points */
    private final double[] triangleDistance;
    /** minimum distance in this node, or any of its children, to
     *  the selected points
     */
    private final double[] minDistance;
    /** maximum coordinate in this node, or any of its children,
     *  to the selected points
     */
    private final double[] maxDistance;
    /** Create given coordinates of position */
    public TriangleNode(double[] forPos, double[] forDistance)
    {
      position = forPos.clone();
      triangleDistance = forDistance.clone();
      minDistance = triangleDistance.clone();
      maxDistance = triangleDistance.clone();
    }
    /** Need to return true distance here to get the triangle
     *  inequality to work.
     */
    public double distance(TriangleNode d, double maxInteresting)
    {
      double sum = 0.0;
      double m2 = maxInteresting * maxInteresting;
      for (int i = 0; (i < position.length) && (sum <= m2); i++)
      {
        double diff = position[i] - d.position[i];
	sum += diff * diff;
      }
      return Math.sqrt(sum);
    }
    public double lowerBound(TriangleNode d, double maxInteresting)
    {
      // We need a bound for the distance from the query point to
      // our point, or any descendant of it.
      // In d(a,b) >= d(a,c) - d(b,c) we have a the query point,
      // b our point or some descendant, and c each of the specified
      // points in turn. We don't use d(b,c) exactly, but our 
      // minDistance and maxDistance bounds on it.
      double bound = 0.0;
      for (int i = 0; (i < triangleDistance.length) &&
                      (bound <= maxInteresting); i++)
      {
        final double bound1 = minDistance[i] - d.triangleDistance[i];
	if (bound1 > bound)
	{
	  bound = bound1;
	}
        final double bound2 = d.triangleDistance[i] - maxDistance[i];
	if (bound2 > bound)
	{
	  bound = bound2;
	}
      }
      return bound;
    }
    public boolean updated(TriangleNode left, TriangleNode right)
    {
      if (left == null)
      { // left null => right null so no children at all
        boolean changed = false;
	for (int i = 0; i < triangleDistance.length; i++)
	{
	  double x = triangleDistance[i];
	  if (minDistance[i] != x)
	  {
	    changed = true;
	    minDistance[i] = x;
	  }
	  if (maxDistance[i] != x)
	  {
	    changed = true;
	    maxDistance[i] = x;
	  }
	}
        return changed;
      }
      boolean changed = false;
      for (int i = 0; i < triangleDistance.length; i++)
      {
	final double x = triangleDistance[i];
	double min = left.minDistance[i];
	double max = left.maxDistance[i];
	if (x < min)
	{
	  min = x;
	}
	if (x > max)
	{
	  max = x;
	}
	if (right != null)
	{
	  final double rmin = right.minDistance[i];
	  if (rmin < min)
	  {
	    min = rmin;
	  }
	  final double rmax = right.maxDistance[i];
	  if (rmax > max)
	  {
	    max = rmax;
	  }
	}
        double minBefore = minDistance[i];
        double maxBefore = maxDistance[i];
	if (min != minBefore)
	{
	  minDistance[i] = min;
	  changed = true;
	}
	if (max != maxBefore)
	{
	  maxDistance[i] = max;
	  changed = true;
	}
      }
      return changed;
    }
  }
  /** Node based on distance from points - approximates cloud 
   *  of points under a node by a circle. Note that for any pair of
   *  points, their bounding box is entirely within their bounding 
   *  circle, so this isn't a terribly good idea.
   *  This follows because the bounding circle has those two points on a
   *  diameter, with the tangent at these points perpendicular to the
   *  diameter, so that the edges of the bounding box must be within
   *  the circle at these points. It is not necessarily the case that
   *  a cluster of three or more points has a bounding box entirely within
   *  its bounding circle, but our code for calculating bounding circles
   *  works by merging two by two, and therefore would not necessarily
   *  calculate the optimal bounding circle for larger groups of points.
   */
  public static class CircleNode implements 
    FindScattered.Boundable<CircleNode>,
    StaticClosest.Positionable, StaticClosest.Visible<CircleNode>
  {
    public boolean isVisible()
    {
      return true;
    }
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
    /** position of centre of bounding region */
    private double[] centre;
    /** maximum distance of any point from the bounding region */
    private double radius; 
    /** Create given coordinates of position */
    public CircleNode(double[] forPos)
    {
      position = forPos.clone();
      centre = position.clone();
      radius = 0.0;
    }
    public String toString()
    {
      StringBuffer sb = new StringBuffer();
      sb.append("Pos ");
      sb.append(Arrays.toString(position));
      sb.append(" centre ");
      sb.append(Arrays.toString(centre));
      sb.append(" radius ");
      sb.append(radius);
      return sb.toString();
    }
    /** Need true distance for lower bound stuff to work, since one
     *  way of looking at is as yet another use of the triangle
     *  inequality.
     */
    public double distance(CircleNode d, double maxInteresting)
    {
      double sum = 0.0;
      double m2 = maxInteresting * maxInteresting;
      for (int i = 0; (i < position.length) && (sum <= m2); i++)
      {
	double diff = position[i] - d.position[i];
	sum += diff * diff;
      }
      return Math.sqrt(sum);
    }
    /**
     * returns lower bound on actual distance from any point in our
     * circle to the single query point specified by the centre 
     * position of the CircleNode provided.
     */
    public double lowerBound(CircleNode d, double maxInteresting)
    {
      // First work out distance from point to centre
      double sum = 0.0;
      for (int i = 0; (i < position.length); i++)
      {
	double diff = d.position[i] - centre[i];
	sum += diff * diff;
      }
      sum = Math.sqrt(sum);
      // We have d(point, withinCircle) >= d(point, centre) -
      //          d(withinCircle, centre)
      sum -= radius;
      if (sum < 0.0)
      {
        return 0.0;
      }
      return sum;
    }
    /** Need a method that works out the smallest circle enclosing
     *  two other circles. Can have newCircle the same array as
     *  either of the other two. This is the building block for our
     *  updated() method, although it does not necessarily produce
     *  the absolute best bounding circle when there are more than
     *  two points involved.
     */
    private static double enclosingRadius(double[] circleA,
      double radiusA, double[] circleB, double radiusB,
      double[] newCircle)
    {
      if (radiusA > radiusB)
      { // want a to be smallest
        double t = radiusA;
	radiusA = radiusB;
	radiusB = t;
	double[] tt = circleA;
	circleA = circleB;
	circleB = tt;
      }
      // Work out distance between centres
      double sum = 0.0;
      for (int i = 0; i < circleA.length; i++)
      {
        double diff = circleA[i] - circleB[i];
	sum += diff * diff;
      }
      sum = Math.sqrt(sum);
      if ((sum + radiusA) <= radiusB)
      { // A is entirely inside B, and in fact this follows
        // from the triangle inequality: any point in circle A
	// is within radiusA of its centre, so the distance from
	// the centre of B to it is at most radiusA + sum
	if (circleB != newCircle)
	{
	  System.arraycopy(circleB, 0, newCircle, 0, circleB.length);
	}
	return radiusB;
      }
      // Think of drawing a line between the centres of the two
      // circles. The points where that crosses the outside of the
      // two circles are on the smallest circle enclosing the two
      // so the centre of the new circle is halfway between them
      // This is [a + b + (a - b) (Ra - Rb) / |a -b|] / 2

      // This again follows from the triangle inequality. Our
      // centre point is distance (sum - radiusA + radiusB) / 2 from
      // a, and the radius of the circle is 
      // (radiusA + radiusB + sum) / 2
      // so any point outside the new circle is at least 
      // (sum + radiusA + radiusB)/2 - (sum - radiusA + radiusB)/2
      // from the centre of A, which is radiusA - so the new circle
      // does indeed enclose the old circle A and the calculation
      // for the new circle B is similar
      double scale = (radiusA - radiusB) / sum;
      for (int i = 0; i < newCircle.length; i++)
      {
	double a = circleA[i];
	double b = circleB[i];
        newCircle[i] = (a + b + (a - b) * scale) * 0.5;
      }
      // And the new radius is half the diameter, which is the
      // distance between the two extremities
      return (radiusA + radiusB + sum) * 0.5;
    }
    public boolean updated(CircleNode left, CircleNode right)
    {
      // System.out.println("Updated on " + this);
      if ((left == null) && (right == null))
      { // no children at all
	boolean changed = (radius != 0.0);
	radius = 0.0;
        for (int i = 0; i < position.length; i++)
	{
	  double p = position[i];
	  if (centre[i] != p)
	  {
	    centre[i] = p;
	    changed = true;
	  }
	}
	return changed;
      }
      // Work out circle enclosing left circle and current point
      double[] newCircle = new double[position.length];
      System.arraycopy(position, 0, newCircle, 0, position.length);
      double newRadius = 0.0;
      if (left != null)
      {
	newRadius = enclosingRadius(position, 0.0,
	  left.centre, left.radius, newCircle);
      }
      // Include right circle if it exists
      if (right != null)
      {
	newRadius = enclosingRadius(newCircle, newRadius,
	  right.centre, right.radius, newCircle);
      }
      boolean changed = false;
      if (newRadius != radius)
      {
        changed = true;
	radius = newRadius;
      }
      for (int i = 0; i < newCircle.length; i++)
      {
	double p = newCircle[i];
	if (centre[i] != p)
	{
	  centre[i] = p;
	  changed = true;
	}
      }
      return changed;
    }
  }
  /** generate a random Circle point with the given number of
   *  dimensions
   */
  private static CircleNode randomCircleNode(int dims, Random r)
  {
    double[] pos = new double[dims];
    for (int i = 0; i < pos.length; i++)
    {
      pos[i] = r.nextGaussian();
    }
    return new CircleNode(pos);
  }
  /** Iterator for test code */
  private static class CircleNodeSource implements Iterator<CircleNode>
  {
    private final int dim;
    private final Random r;
    CircleNodeSource(int forDim, Random forR)
    {
      dim = forDim;
      r = forR;
    }
    public void remove()
    {
      throw new UnsupportedOperationException();
    }
    public CircleNode next()
    {
      return randomCircleNode(dim, r);
    }
    public boolean hasNext()
    {
      return true;
    }
  }
  /** root node in tree */
  private Node<Y> root;
  /** Table mapping the original Y object to the Node<Y> holding it */
  private final Map<Y, Node<Y>> nodeByOriginal = new HashMap<Y, Node<Y>>();
  /** return the number of points stored */
  public int size()
  {
    return nodeByOriginal.size();
  }
  private static final boolean TRACE = false;
  public boolean isEmpty()
  {
    return root == null;
  }
  public void clear()
  {
    root = null;
    nodeByOriginal.clear();
  }
  public boolean contains(Object o)
  {
    return nodeByOriginal.containsKey(o);
  }
  public boolean containsAll(Collection<?>c)
  {
    return nodeByOriginal.keySet().containsAll(c);
  }
  public boolean equals(Object o)
  {
    if (!(o instanceof Closest))
    {
      return false;
    }
    Closest other = (Closest)o;
    return nodeByOriginal.equals(other.nodeByOriginal);
  }
  public int hashCode()
  {
    return nodeByOriginal.keySet().hashCode();
  }
  /** Find up to the given number of points closest to the query
      point and no more than the given distance away
   */
  public List<Y> findClosest(Y query, int maxPoints,
    double maxDistance)
  {
    return findClosestLeaf(new Node<Y>(query), maxPoints, maxDistance, false);
  }
  /** Find up to the given number of points closest to the query
      point and no more than the given distance away. If 
      closestLeaf is true, consider only leaves. (This is used
      internally during insertion). Return them as a list in
      order of distance with the smallest last.
   */
  private List<Y> findClosestLeaf(Node<Y> query, int maxPoints,
    double maxDistance, boolean closestLeaf)
  {
    if ((root == null) || (maxPoints <= 0))
    {
      List<Y> result = Collections.emptyList();
      return result;
    }
    PriorityQueue<StaticClosest.NodeWithDistance<Y>> sofar = 
      new PriorityQueue<StaticClosest.NodeWithDistance<Y>>();
    double bound = root.point.lowerBound(query.point, maxDistance);
    if (bound <= maxDistance)
    {
      double distance = Double.MAX_VALUE;
      if ((!closestLeaf) || (root.left == null) || (root.right == null))
      {
	distance = query.point.distance(root.point, maxDistance);
      }
      else if (TRACE)
      {
        System.out.println("Skip root node " + root);
      }
      // Inner accepts the point we have just calculated for, if
      // required, as well as continuing the search.
      inner(sofar, root, query, maxPoints, maxDistance, 
	distance, closestLeaf);
    }
    List<Y> result = new ArrayList<Y>();
    for (;;)
    {
      StaticClosest.NodeWithDistance<Y> nwd = sofar.poll();
      if (nwd == null)
      {
        break;
      }
      result.add(nwd.getNode());
    }
    return result;
  }
  /** Inner recursive routine to find nodes. OK to be recursive as
   *  the tree is balanced => can't be too deep. Returns current value
   *  of maxDistance.
   */
  private double inner(
    PriorityQueue<StaticClosest.NodeWithDistance<Y>> sofar,
    Node<Y> here, Node<Y> query, int maxPoints, double maxDistance,
    double thisDistance, boolean closestLeaf)
  {
    boolean full = sofar.size() >= maxPoints;
    if (full)
    {
      if (((!closestLeaf) || (here.left == null) || 
            (here.right == null)) &&
	  (thisDistance < maxDistance))
      {
	// ditch smallest
        sofar.poll();
	sofar.add(new StaticClosest.NodeWithDistance<Y>(thisDistance, here.point));
	maxDistance = sofar.peek().getDistance();
      }
    }
    else
    {
      if (((!closestLeaf) || (here.left == null) || 
            (here.right == null)) &&
	  (thisDistance <= maxDistance))
      {
        sofar.add(new StaticClosest.NodeWithDistance<Y>(thisDistance, here.point));
	if (sofar.size() >= maxPoints)
        {
	  maxDistance = sofar.peek().getDistance();
	}
      }
    }
    if (here.left == null)
    { // nothing else to look at in this subtree
      return maxDistance;
    }
    Node<Y> leftHand = here.left;
    // Note that lower bound is NOT symettric
    double leftBound = leftHand.point.lowerBound(query.point,
      maxDistance);
    double rightBound = 0.0;
    Node<Y> rightHand = here.right;
    if (rightHand != null)
    {
      // might swap left and right hand to visit least first
      rightBound = rightHand.point.lowerBound(query.point, maxDistance);
      if (rightBound < leftBound)
      {
	Node<Y> t = leftHand;
	leftHand = rightHand;
	rightHand = t;
	double tt = leftBound;
	leftBound = rightBound;
	rightBound = tt;
      }
    }
    if ((leftBound > maxDistance) || (full && (leftBound == maxDistance)))
    {
      if (TRACE)
      {
	System.out.println("Skip left " + leftHand + " bound " + leftBound);
	System.out.println("Skip left child " + leftHand.left);
	System.out.println("Skip right child " + leftHand.right);
      }
      return maxDistance;
    }
    double leftDistance = Double.MAX_VALUE;
    if ((!closestLeaf) || (leftHand.left == null) || 
	(leftHand.right == null))
    {
      leftDistance = query.point.distance(leftHand.point, maxDistance);
    }
    maxDistance = inner(sofar, leftHand, query, maxPoints, maxDistance,
      leftDistance, closestLeaf);
    if ((rightHand == null) || (rightBound > maxDistance) ||
        (full && (rightBound == maxDistance)))
    {
      if (TRACE)
      {
        System.out.println("Skip right " + rightHand);
      }
      return maxDistance;
    }
    double rightDistance = Double.MAX_VALUE;
    if ((!closestLeaf) || (rightHand.left == null) || 
	(rightHand.right == null))
    {
      rightDistance = query.point.distance(rightHand.point, maxDistance);
    }
    return inner(sofar, rightHand, query, maxPoints, maxDistance,
      rightDistance, closestLeaf);
  }
  /** Max imbalance to tolerate. Must be at least one, because
   *  we rebalance by a 'rotation' that increases the max distance
   *  at one child and decreases at another. So we can't rebalance
   *  to equality.
   */
  private static final int MAX_DIFFERENCE = 1;
  /** This method calls update and rebalances the tree, working
   *  from here (which has been modified so may not be in balance
   *  or updated), and travelling as far up as necessary. It returns
   *  whether or not it met checkAgainst and checked it during this
   *  journey.
   */
  private boolean chaseUp(Node<Y> here, Node<Y> checkAgainst)
  {
    boolean found = false;
    // In most cases, if we don't change the point we are working
    // on there is no need to propagate a change up to its parent.
    // But the point we are called on has certainly changed as
    // far as its parent is concerned just by being shoved in there,
    // so we will want to propagate regardless.
    boolean certainlyNew = true;
    for (boolean proceed = true; proceed && (here != null);
      here = here.parent)
    {
      if ((here.parent == null) && (here != root))
      {
        throw new IllegalArgumentException("Point not really in tree");
      }
      proceed = certainlyNew;
      certainlyNew = false;
      found |= (here == checkAgainst);
      if (here.left == null)
      { // no children
	proceed |= here.point.updated(here.getLeftPoint(),
	  here.getRightPoint());
	if (here.maxDistanceToChild != 0)
	{
	  proceed = true;
	  here.maxDistanceToChild = 0;
	}
	continue;
      }
      if (here.right == null)
      { // just one child
	int leftMax = here.left.maxDistanceToChild;
        if (leftMax <= MAX_DIFFERENCE)
	{ // difference is OK: check updated()
	  int maxHere = leftMax + 1;
	  if (maxHere != here.maxDistanceToChild)
	  {
	    proceed = true;
	    here.maxDistanceToChild = maxHere;
	  }
	  proceed |= here.point.updated(here.getLeftPoint(),
	    here.getRightPoint());
	  continue;
	}
        // Pull out our left child as our new right child
	here.right = here.left;
	here.left = here.right.left;
	here.right.left = here.right.right;
	here.right.right = null;
	here.left.parent = here;
	int maxHere = here.left.maxDistanceToChild;
	if (here.right.left == null)
	{
	  here.right.maxDistanceToChild = 0;
	}
	else
	{
	  int rightMax = 0;
	  if (here.right.left != null)
	  {
	    rightMax = here.right.left.maxDistanceToChild + 1;
	  }
	  here.right.maxDistanceToChild = rightMax;
	  if (rightMax > maxHere)
	  {
	    maxHere = rightMax;
	  }
	}
	here.right.point.updated(here.right.getLeftPoint(),
	  here.right.getRightPoint());
	if (++maxHere != here.maxDistanceToChild)
	{
	  proceed = true;
	  here.maxDistanceToChild = maxHere;
	}
	proceed |= here.point.updated(here.getLeftPoint(),
	  here.getRightPoint());
	continue;
      }
      // Two children.
      int difference = here.left.maxDistanceToChild -
        here.right.maxDistanceToChild;
      if (difference > MAX_DIFFERENCE)
      { // left is too heavy, so shift from left to right as before
	Node<Y> oldRight = here.right;
	here.right = here.left;
	here.left = here.right.left;
	here.right.left = here.right.right;
	here.right.right = oldRight;
	here.left.parent = here;
	here.right.right.parent = here.right;
	// For us to be unbalanced, here.left cannot be null
	int maxHere = here.left.maxDistanceToChild;
	if (here.right.left == null)
	{
	  // Must keep left non-null unless both are null.
	  // here.right.left will not be null after this, or
	  // here would only have had one child
	  here.right.left = here.right.right;
	  here.right.right = null;
	}
	int rightMax = here.right.left.maxDistanceToChild;
	if (here.right.right != null)
	{
	  int maybe = here.right.right.maxDistanceToChild;
	  if (maybe > rightMax)
	  {
	    rightMax = maybe;
	  }
	}
	here.right.maxDistanceToChild = ++rightMax;
	if (rightMax > maxHere)
	{
	  maxHere = rightMax;
	}
	if (++maxHere != here.maxDistanceToChild)
	{
	  proceed = true;
	  here.maxDistanceToChild = maxHere;
	}
	here.right.point.updated(here.right.left.point,
	  here.right.getRightPoint());
	proceed |= here.point.updated(here.left.point,
	  here.right.point);
	continue;
      }
      if (difference < -MAX_DIFFERENCE)
      { // right is too heavy, so shift from right to left
	Node<Y> oldLeft = here.left;
	here.left = here.right;
	here.right = here.left.right;
	here.left.right = here.left.left;
	here.left.left = oldLeft;
	// here.left.left cannot be null or we would have no children
	// at all
	here.left.left.parent = here.left;
	int maxHere = 0;
	if (here.right != null)
	{
	  here.right.parent = here;
	  maxHere = here.right.maxDistanceToChild;
	}
	int leftMax = here.left.left.maxDistanceToChild;
	if (here.left.right != null)
	{
	  int maybe = here.left.right.maxDistanceToChild;
	  if (maybe > leftMax)
	  {
	    leftMax = maybe;
	  }
	}
	here.left.maxDistanceToChild = ++leftMax;
	if (leftMax > maxHere)
	{
	  maxHere = leftMax;
	}
	if (++maxHere != here.maxDistanceToChild)
	{
	  proceed = true;
	  here.maxDistanceToChild = maxHere;
	}
	here.left.point.updated(here.left.left.point,
	  here.left.getRightPoint());
	proceed |= here.point.updated(here.left.point,
	  here.getRightPoint());
        continue;
      }
      // No need to rebalance, but need to update
      proceed |= here.point.updated(here.left.point,
        here.right.point);
      int leftMax = here.left.maxDistanceToChild;
      if (here.right.maxDistanceToChild > leftMax)
      {
        leftMax = here.right.maxDistanceToChild;
      }
      if (++leftMax != here.maxDistanceToChild)
      {
        here.maxDistanceToChild = leftMax;
	proceed = true;
      }
    }
    return found;
  }
  /** For testing only, after each insert, this holds the closest leaf
   *  to the inserted point.
   */
  private Node<Y> closestLeafSaved;
  /** insert a new point. This finds the closest leaf to the given
   *  one and inserts it there.
   */
  public boolean add(Y originalPoint)
  {
    if (nodeByOriginal.containsKey(originalPoint))
    { // there already
      return false;
    }
    Node<Y> point = new Node<Y>(originalPoint);
    nodeByOriginal.put(originalPoint, point);
    point.left = null;
    point.right = null;
    point.maxDistanceToChild = 0;
    if (root == null)
    {
      root = point;
      point.parent = null;
      point.point.updated(point.getLeftPoint(),
        point.getRightPoint());
      return true;
    }
    List<Y> l = findClosestLeaf(point, 1, Double.MAX_VALUE, true);
    Node<Y> closestLeaf = nodeByOriginal.get(l.iterator().next());
    closestLeafSaved = closestLeaf;
    point.parent = closestLeaf;
    if (closestLeaf.left == null)
    {
      closestLeaf.left = point;
    }
    else if (closestLeaf.right == null)
    {
      closestLeaf.right = point;
    }
    else
    {
      throw new IllegalStateException("Leaf had two children");
    }
    chaseUp(point, null);
    return true;
  }
  public boolean addAll(Collection<? extends Y> c)
  {
    boolean x = false;
    for (Y y: c)
    {
      x |= add(y);
    }
    return x;
  }
  /** Made this package-visibility to emphasise that you should
   *  probably use PatriciaClosest instead
   */
  /*public*/ Closest()
  {
  }
  /** Made this package-visibility to emphasise that you should
   *  probably use PatriciaClosest instead
   */
  /* public */ Closest(Collection<? extends Y> c)
  {
    addAll(c);
  }
  /** Remove the given point */
  public boolean remove(Object originalPoint)
  {
    Node<Y> point = nodeByOriginal.remove(originalPoint);
    if (point == null)
    { // don't have this
      return false;
    }
    if ((point.parent == null) && (point != root))
    { // should never happen
      throw new IllegalStateException("Point not in Closest");
    }
    Node<Y> child = point.right;
    if (child == null)
    { // easy case: can just edit this out
      Node<Y> parent = point.parent;
      point.parent = null;
      if (parent == null)
      {
        root = point.left;
	point.left = null;
	if (root != null)
	{
	  root.parent = null;
	}
	return true;
      }
      if (parent.left == point)
      {
        parent.left = point.left;
	if (parent.left == null)
	{
	  parent.left = parent.right;
	  parent.right = null;
	}
	else
	{
	  parent.left.parent = parent;
	}
      }
      else if (parent.right == point)
      {
        parent.right = point.left;
	if (parent.right != null)
	{
	  parent.right.parent = parent;
	}
      }
      else
      {
        throw new IllegalStateException("Inconsistent parent pointer");
      }
      point.left = null;
      chaseUp(parent, null);
      return true;
    }
    // Find a child leaf and swap it up
    while (child.right != null)
    {
      child = child.right;
    }
    Node<Y> childParent = child.parent;
    // Found child by moving to the right so the parent connection
    // must be on the right
    if (childParent.right == child)
    {
      childParent.right = child.left;
      if (childParent.right != null)
      {
        childParent.right.parent = childParent;
      }
    }
    else
    {
      throw new IllegalStateException("parent pointer mismatch");
    }
    Node<Y> parent = point.parent;
    child.parent = parent;
    if (parent == null)
    {
      root = child;
    }
    else if (parent.left == point)
    {
      parent.left = child;
    }
    else if (parent.right == point)
    {
      parent.right = child;
    }
    else
    {
      throw new IllegalStateException("Bad parent pointer");
    }
    child.left = point.left;
    child.right = point.right;
    child.left.parent = child;
    if (child.right != null)
    { // could be null if right child of point was our child
      // and that child had a null right link
      child.right.parent = child;
    }
    point.parent = null;
    point.left = null;
    point.right = null;
    boolean sawChild = false;
    if (childParent != point)
    {
      sawChild = chaseUp(childParent, child);
    }
    if (!sawChild)
    {
      chaseUp(child, null);
    }
    return true;
  }
  public boolean removeAll(Collection<?>c)
  {
    boolean all = false;
    for(Object o: c)
    {
      boolean result = remove(o);
      all |= result;
    }
    return all;
  }
  public boolean retainAll(Collection<?> c)
  {
    Set<Y> toRemove = new HashSet<Y>(nodeByOriginal.keySet());
    for (Object o: c)
    {
      toRemove.remove(o);
    }
    boolean changed = false;
    for (Y y: toRemove)
    {
      changed |= remove(y);
    }
    return changed;
  }
  public Object[] toArray()
  {
    return nodeByOriginal.keySet().toArray();
  }
  public <T> T[] toArray(T[] a)
  {
    return nodeByOriginal.keySet().toArray(a);
  }
  /** Test function to check invariants below given point */
  private void checkInvariants(Node<Y> point)
  {
    if (point.point.updated(point.getLeftPoint(),
      point.getRightPoint()))
    {
      throw new IllegalStateException("Updated required");
    }
    int max = 0;
    if (point.left != null)
    {
      if (point.left.parent != point)
      {
        throw new IllegalStateException("Parent pointer mismatch");
      }
      max = point.left.maxDistanceToChild + 1;
      checkInvariants(point.left);
    }
    if (point.right != null)
    {
      if (point.right.parent != point)
      {
        throw new IllegalStateException(
	  "Right Parent pointer mismatch");
      }
      if (point.left == null)
      {
        throw new IllegalStateException(
	  "Have right pointer but not left");
      }
      int maxHere = point.right.maxDistanceToChild + 1;
      if (maxHere > max)
      {
        max = maxHere;
      }
      checkInvariants(point.right);
    }
    if (max != point.maxDistanceToChild)
    {
      throw new IllegalStateException("Max distance mismatch");
    }
  }
  /** return iterator over collection returning base type. It
   *  would be faster to use nodeByOriginal, but this is
   *  here to test the tree structure.
   */
  private Iterator<Node<Y>> baseIterator()
  {
    final List<Node<Y>> stack = new ArrayList<Node<Y>>();
    if (root != null)
    {
      stack.add(root);
    }
    return new Iterator<Node<Y>>()
    {
      /** Stack of nodes heading subtrees not yet visited */
      private List<Node<Y>> pos = stack;
      public void remove()
      {
        throw new UnsupportedOperationException(
	  "Does not support remove via iterator");
      }
      public Node<Y> next()
      {
	int size = pos.size();
        if (size == 0)
	{
	  throw new NoSuchElementException();
	}
	Node<Y> result = pos.remove(--size);
	if (result.left != null)
	{
	  pos.add(result.left);
	}
	if (result.right != null)
	{
	  pos.add(result.right);
	}
	return result;
      }
      public boolean hasNext()
      {
        return !pos.isEmpty();
      }
    };
  }
  /** can provide an iterator just by returning iterator for
   * nodeByOriginal
   */
  public Iterator<Y> iterator()
  {
    return Collections.unmodifiableSet(nodeByOriginal.keySet()).
      iterator();
  }
  /** test function to check invariants */
  private void checkInvariants()
  {
    if (root == null)
    {
      return;
    }
    if (root.parent != null)
    {
      throw new IllegalStateException("Root with parent pointer");
    }
    checkInvariants(root);
  }
  /** for testing, insert a point and check everything */
  private void slowTestInsert(Y point)
  {
    if (TRACE)
    {
      System.out.println("Insert " + point);
    }
    Node<Y> closestLeafSoFar = null;
    double closestDistance = 0.0;
    int seen = 0;
    for(Iterator<Node<Y>> it = baseIterator(); it.hasNext();)
    {
      Node<Y> y = it.next();
      seen++;
      if ((y.left != null) && (y.right != null))
      {
        continue;
      }
      double distance = point.distance(y.point, Double.MAX_VALUE);
      if ((closestLeafSoFar == null) || (distance < closestDistance))
      {
        closestDistance = distance;
	closestLeafSoFar = y;
      }
    }
    if (seen != size())
    {
      throw new IllegalStateException("Size mismatch");
    }
    add(point);
    if (++seen != size())
    {
      throw new IllegalStateException("Size mismatch after");
    }
    if (isEmpty())
    {
      throw new IllegalStateException("Empty after insert");
    }
    checkInvariants();
    if ((closestLeafSaved != closestLeafSoFar) &&
        (closestDistance != point.distance(closestLeafSaved.point,
	  Double.MAX_VALUE)))
    {
      System.err.println("This point is " + point);
      System.err.println("Our closest " + closestDistance + " at " +
        closestLeafSoFar);
      System.err.println("Their closest " +
        point.distance(closestLeafSaved.point, Double.MAX_VALUE) + " at " +
	closestLeafSaved);
      throw new IllegalStateException("Closest leaf mismatch");
    }
  }
  /** generate a random Euclidean point with the given number of
   *  dimensions
   */
  static EuclidNode randomEuclidNode(int dims, Random r)
  {
    double[] pos = new double[dims];
    for (int i = 0; i < pos.length; i++)
    {
      pos[i] = r.nextGaussian();
    }
    return new EuclidNode(pos);
  }
  /** Iterator for test code */
  private static class EuclidNodeSource implements Iterator<EuclidNode>
  {
    private final int dim;
    private final Random r;
    EuclidNodeSource(int forDim, Random forR)
    {
      dim = forDim;
      r = forR;
    }
    public void remove()
    {
      throw new UnsupportedOperationException();
    }
    public EuclidNode next()
    {
      return randomEuclidNode(dim, r);
    }
    public boolean hasNext()
    {
      return true;
    }
  }
  /** Print out for debugging */
  public void print(PrintStream ps)
  {
    for (Y y: this)
    {
      ps.println(y);
    }
  }
  /** Test by doing inserts and removes of nodes */
  public static<X extends FindScattered.Boundable<X>> void insertTestIt(
    int num, Iterator<X> source, Random r)
  {
    Closest<X> euclid =
      new Closest<X>();
    if (!euclid.isEmpty())
    {
      throw new IllegalStateException("Not empty on construction");
    }
    List<X> contents =
      new ArrayList<X>();
    for (int i = 0; i < num; i++)
    {
      X node = source.next();
      euclid.slowTestInsert(node);
      contents.add(node);
      if (TRACE)
      {
	System.out.println("After insert: ");
	euclid.print(System.out);
      }
      node = source.next();
      euclid.slowTestInsert(node);
      contents.add(node);
      if (TRACE)
      {
	System.out.println("After insert: ");
	euclid.print(System.out);
      }
      int target = r.nextInt(contents.size());
      X toRemove = contents.get(target);
      if (!euclid.remove(toRemove))
      {
        throw new IllegalStateException("Remove failed");
      }
      euclid.checkInvariants();
      for (X en: euclid)
      {
        if (en == toRemove)
	{
	  throw new IllegalStateException("Still present after remove");
	}
      }
      int size = contents.size() - 1;
      if (target == size)
      {
        contents.remove(target);
      }
      else
      {
	contents.set(target, contents.remove(size));
      }
    }
    while (euclid.size() > 0)
    {
      int target = r.nextInt(contents.size());
      X toRemove = contents.get(target);
      if (!euclid.remove(toRemove))
      {
        throw new IllegalStateException("Remove failed 2");
      }
      euclid.checkInvariants();
      int size = contents.size() - 1;
      if (target == size)
      {
        contents.remove(target);
      }
      else
      {
	contents.set(target, contents.remove(size));
      }
    }
    if (!euclid.isEmpty())
    {
      throw new IllegalArgumentException("Empty not true at end");
    }
  }
  /** Create a random Triangle node and set up distances based
   *  on those given
   */
  private static TriangleNode randomTriangleNode(int dim, Random r,
    Collection<TriangleNode> toHere)
  {
    double[] pos = new double[dim];
    for (int i = 0; i < pos.length; i++)
    {
      pos[i] = r.nextGaussian();
    }
    TriangleNode forDist = new TriangleNode(pos, new double[0]);
    double[] dist = new double[toHere.size()];
    Iterator<TriangleNode> it = toHere.iterator();
    for (int i = 0; i < dist.length; i++)
    {
      dist[i] = forDist.distance(it.next(), Double.MAX_VALUE);
    }
    return new TriangleNode(pos, dist);
  }
  public static class TriangleNodeSource implements
    Iterator<TriangleNode>
  {
    private final int dim;
    private final Random r;
    private final Collection<TriangleNode> fromHere;
    public boolean hasNext()
    {
      return true;
    }
    public void remove()
    {
      throw new UnsupportedOperationException();
    }
    public TriangleNode next()
    {
      return randomTriangleNode(dim, r, fromHere);
    }
    public TriangleNodeSource(int forDim, Random forR,
      Collection<TriangleNode> forFrom)
    {
      dim = forDim;
      r = forR;
      fromHere = forFrom;
    }
  }
  private final static boolean TIME_EUCLID = true;
  private final static boolean TIME_CIRCLE = true;
  private final static boolean TIME_STATIC = true;
  private final static boolean TIME_TRIANGLE = true;
  private final static boolean TIME_SLOW = true;
  /** Speed test for Euclid Nodes */
  private static void euclidSpeed(int goes, int dim, int size,
    int queries, long seed)
  {
    System.out.println("Goes " + goes + " dim " + dim + " size " +
      size + " queries " + queries + " seed " + seed);
    Swatch treeTime = new Swatch();
    Swatch triangleTime = new Swatch();
    Swatch loadTime = new Swatch();
    Swatch arrayTime = new Swatch();
    Swatch circleTime = new Swatch();
    Swatch staticTime = new Swatch();
    EuclidNode[] array = new EuclidNode[size];
    TriangleNode[] tarray = new TriangleNode[size];
    CircleNode[] carray = new CircleNode[size];
    int numDistances = size;
    List<TriangleNode> bootstrapDistances = Collections.emptyList();
    double[] forPos = new double[dim];
    double[] forDist = new double[dim];
    for (int i = 0; i < goes; i++)
    {
      List<EuclidNode> sarray = new ArrayList<EuclidNode>();
      System.out.println("Speed go " + i);
      long cseed = i + seed;
      Random r = new Random(cseed);
      List<TriangleNode> forDistances = new ArrayList<TriangleNode>();
      for (int j = 0; j < numDistances; j++)
      {
	forDistances.add(randomTriangleNode(dim, r,
	  bootstrapDistances));
      }
      forDistances = FindScattered.findScattered(forDistances, dim);
      for (int j = 0; j < size; j++)
      {
	for (int k = 0; k < forPos.length; k++)
	{
	  forPos[k] = r.nextGaussian();
	}
        EuclidNode en = new EuclidNode(forPos);
        array[j] = en;
        sarray.add(new EuclidNode(forPos));
	TriangleNode tn = new TriangleNode(forPos, new double[0]);
	for (int k = 0; k < forDist.length; k++)
	{
	  forDist[k] = tn.distance(forDistances.get(k), Double.MAX_VALUE);
	}
	tarray[j] = new TriangleNode(forPos, forDist);
        CircleNode cn = new CircleNode(forPos);
        carray[j] = cn;
      }
      Closest<EuclidNode> euclid = new Closest<EuclidNode>();
      Closest<TriangleNode> tri = new Closest<TriangleNode>();
      Closest<CircleNode> circ = new Closest<CircleNode>();
      loadTime.start();
      for (int j = 0; j < size; j++)
      {
        euclid.add(array[j]);
	tri.add(tarray[j]);
	circ.add(carray[j]);
      }
      StaticClosest.Splitter<EuclidNode> splitter =
	new StaticClosest.PositionSplitter<EuclidNode>(sarray);
      StaticClosest<EuclidNode> sc =
	new StaticClosest<EuclidNode>(splitter);
      loadTime.stop();
      for (int j = 0; j < queries; j++)
      {
	int queryFor = r.nextInt(array.length);
	EuclidNode query = array[queryFor];
	TriangleNode tquery = tarray[queryFor];
	CircleNode cquery = carray[queryFor];
        treeTime.start();
	if (TIME_EUCLID)
	{
	  List<EuclidNode> cl = euclid.findClosest(query, 1,
	    Double.MAX_VALUE);
	  EuclidNode treeAnswer = cl.iterator().next();
	  if (query.distance(treeAnswer, Double.MAX_VALUE) != 0.0)
	  {
	    throw new IllegalArgumentException(
	      "Failed to find exact match at " + i + " total seed " +
	      cseed);
	  }
	}
	treeTime.stop();
	circleTime.start();
	if (TIME_CIRCLE)
	{
	  List<CircleNode> cl = circ.findClosest(cquery, 1,
	    Double.MAX_VALUE);
	  CircleNode treeAnswer = cl.iterator().next();
	  if (cquery.distance(treeAnswer, Double.MAX_VALUE) != 0.0)
	  {
	    throw new IllegalArgumentException(
	      "Failed to find exact match at " + i + " total seed " +
	      cseed);
	  }
	}
	circleTime.stop();
        triangleTime.start();
	if (TIME_TRIANGLE)
	{
	  List<TriangleNode> clt = tri.findClosest(tquery, 1,
	    Double.MAX_VALUE);
	  TriangleNode triAnswer = clt.iterator().next();
	  if (tquery.distance(triAnswer, Double.MAX_VALUE) != 0.0)
	  {
	    throw new IllegalArgumentException(
	      "Failed to find exact tmatch at " + i + " total seed " +
	      cseed);
	  }
	}
	triangleTime.stop();
	arrayTime.start();
	if (TIME_SLOW)
	{
	  EuclidNode arrayAnswer = null;
	  double arrayDistance = 0.0;
	  for (EuclidNode possible: array)
	  {
	    double distance = query.distance(possible, Double.MAX_VALUE);
	    if ((arrayAnswer == null) || (distance < arrayDistance))
	    {
	      arrayAnswer = possible;
	      arrayDistance = distance;
	    }
	  }
	}
	arrayTime.stop();
	staticTime.start();
	if (TIME_STATIC)
	{
	  List<EuclidNode> clt = sc.findClosest(query, 1,
	    Double.MAX_VALUE);
	  EuclidNode triAnswer = clt.iterator().next();
	  if (query.distance(triAnswer, Double.MAX_VALUE) != 0.0)
	  {
	    throw new IllegalArgumentException(
	      "Failed to find exact static match at " +
	        i + " total seed " + cseed);
	  }
	}
	staticTime.stop();
      }
    }
    System.out.println("Load time " + loadTime);
    System.out.println("Tree time " + treeTime);
    System.out.println("Triangle time " + triangleTime);
    System.out.println("Circle time " + circleTime);
    System.out.println("Array time " + arrayTime);
    System.out.println("Static time " + staticTime);
  }
  public static void main(String[] s)
  {
    int dim = 3;
    int goes = 10;
    int queries = 10000;
    long seed = 0;
    int size = 1000;
    int s1 = s.length - 1;
    boolean trouble = false;
    String num = null;
    boolean skipTest = false;
    try
    {
      for (int i = 0; i < s.length; i++)
      {
        if ("-dim".equals(s[i]) && (i < s1))
	{
	  num = s[++i].trim();
	  dim = Integer.parseInt(num);
	}
        else if ("-goes".equals(s[i]) && (i < s1))
	{
	  num = s[++i].trim();
	  goes = Integer.parseInt(num);
	}
        else if ("-queries".equals(s[i]) && (i < s1))
	{
	  num = s[++i].trim();
	  queries = Integer.parseInt(num);
	}
        else if ("-seed".equals(s[i]) && (i < s1))
	{
	  num = s[++i].trim();
	  seed = Long.parseLong(num);
	}
        else if ("-size".equals(s[i]) && (i < s1))
	{
	  num = s[++i].trim();
	  size = Integer.parseInt(num);
	}
	else if ("-skipTest".equals(s[i]))
	{
	  skipTest = true;
	}
	else
	{
	  System.err.println("Could not handle flag " + s[i]);
	  trouble = true;
	}
      }
    }
    catch (NumberFormatException nfe)
    {
      System.err.println("Could not read number in " + num);
      trouble = true;
    }
    if (trouble)
    {
      System.err.println("Args are [-dim #] [-goes #] [-queries #] " +
        "[-seed #] [-size #] [-skipTest]");
      return;
    }
    System.out.println("Dim " + dim + " goes " + goes + " queries " +
      queries + " seed " + seed + " size " + size);
    int next = 0;
    if (!skipTest)
    {
      for (int go = 0; go < goes; go++)
      {
	long cseed = seed + go;
	if (go > next)
	{
	  System.out.println("go " + go + " combined seed " + cseed);
	  next = next + 100;
	}
	FindScattered.testFindScattered(size, size / 2, cseed);
	// insertTest(dim, size, new Random(cseed));
	Random rr = new Random(cseed);
	insertTestIt(size, new EuclidNodeSource(dim, rr), rr);
	rr = new Random(cseed);
	insertTestIt(size, new CircleNodeSource(dim, rr), rr);
	// Work out nodes used for distance estimation
	rr = new Random(cseed);
	int numDistances = dim + rr.nextInt(size);
	List<TriangleNode> bootstrapDistances = Collections.emptyList();
	List<TriangleNode> forDistances = new ArrayList<TriangleNode>();
	for (int i = 0; i < numDistances; i++)
	{
	  forDistances.add(randomTriangleNode(dim, rr, bootstrapDistances));
	}
	forDistances = FindScattered.findScattered(forDistances, dim);
	insertTestIt(size, new TriangleNodeSource(dim, rr, 
	  forDistances), rr);
	// insertTriangleTest(dim, size, new Random(cseed));
      }
    }
    euclidSpeed(goes, dim, size, queries, seed);
  }
}
