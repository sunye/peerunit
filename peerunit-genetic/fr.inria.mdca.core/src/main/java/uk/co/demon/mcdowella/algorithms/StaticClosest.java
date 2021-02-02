package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

/** This is a data structure for the nearest neighbour problem
 *  that is constructed from an iterator over points and answers only
 *  queries: it does not permit updates or deletes, although you
 *  can reversibly mark points as invisible, in which case they will
 *  not appear as answers to nearest neighbour queries.
 */
public class StaticClosest<Y extends StaticClosest.Visible<Y>>
{
  /** Interface for a node that holds a position held as a coordinate.
   *  We need this sort of information from nodes for the default
   *  implementation of Splitter, which splits up nodes to build
   *  the tree used by StaticClosest.
   */
  public static interface Positionable
  {
    /** Return the dimension of the space the position is in */
    int getDim();
    /** return the value of one of the coordinates */
    double getCoordinate(int dim);
  }
  /** Class so we can keep nodes found in a priority queue with the
   *  smallest one (the one we might remove from the queue and throw
   *  away) being the one with the largest distance.
   */
  static class NodeWithDistance<Z>
    implements Comparable<NodeWithDistance<Z>>
  {
    /** distance associated with this node */
    private final double distance;
    /** get the distance */
    public double getDistance()
    {
      return distance;
    }
    /** the node */
    private final Z node;
    /** return the node */
    public Z getNode()
    {
      return node;
    }
    /** Want smallest one to be the one with the largest distance */
    public int compareTo(NodeWithDistance other)
    {
      if (distance > other.distance)
      {
        return -1;
      }
      if (distance < other.distance)
      {
        return 1;
      }
      return 0;
    }
    NodeWithDistance(double forDistance, Z forNode)
    {
      distance = forDistance;
      node = forNode;
    }
  }
  /** root of tree of nodes */
  private Node<Y> root;
  /** get the point at the root, or null */
  public Y getRootPoint()
  {
    if (root == null)
    {
      return null;
    }
    return root.point;
  }
  /** interface used to allow nodes to declare themselves invisible
   *  or visible. We can mimic deletions by making nodes invisible
   */
  public interface Visible<X> extends FindScattered.Boundable<X>
  {
    /** whether the node is visible */
    boolean isVisible();
  }
  /** Used to build tree from input data */
  static class Node<X extends Visible<X>>
  {
    /** point held */
    private X point;
    /** left child */
    private Node<X> left;
    /** return left point, if any, or null */
    X getLeftPoint()
    {
      if (left == null)
      {
        return null;
      }
      return left.point;
    }
    /** right child */
    private Node<X> right;
    /** return right point, if any, or null */
    X getRightPoint()
    {
      if (right == null)
      {
        return null;
      }
      return right.point;
    }
    /** parent */
    private Node<X> parent;
  }
  /** Find up to the given number of points closest to the query
      point and no more than the given distance away. Return as
      a list in order with furthest away first.
   */
  public List<Y> findClosest(Y query, int maxPoints,
    double maxDistance)
  {
    // System.out.println("Static findClosest");
    if ((root == null) || (maxPoints <= 0))
    {
      return Collections.emptyList();
    }
    PriorityQueue<NodeWithDistance<Y>> sofar = 
      new PriorityQueue<NodeWithDistance<Y>>();
    double bound = root.point.lowerBound(query, maxDistance);
    if (bound <= maxDistance)
    {
      inner(sofar, root, query, maxPoints, maxDistance);
    }
    List<Y> result = new ArrayList<Y>();
    for (;;)
    {
      NodeWithDistance<Y> nwd = sofar.poll();
      if (nwd == null)
      {
        break;
      }
      result.add(nwd.getNode());
    }
    return result;
  }
  /** routine to dump out nodes by adding them to a
   *  collection
   */
  public void addTo(Collection<Y> toThis)
  {
    if (root == null)
    {
      return;
    }
    toThis.add(root.point);
    innerAddTo(toThis, root.left);
    innerAddTo(toThis, root.right);
  }
  /** recursive routine for dumping out nodes */
  private void innerAddTo(Collection<Y> toThis, Node<Y> from)
  {
    if (from == null)
    {
      return;
    }
    toThis.add(from.point);
    innerAddTo(toThis, from.left);
    innerAddTo(toThis, from.right);
  }
  /** Inner recursive routine to find nodes. OK to be recursive as
   *  the tree is balanced => can't be too deep. Returns current value
   *  of maxDistance.
   */
  private double inner(PriorityQueue<NodeWithDistance<Y>> sofar,
    Node<Y> here, Y query, int maxPoints, double maxDistance)
  {
    if (here.point.isVisible())
    {
      // System.out.println("Check visible point ");
      double distance = query.distance(here.point, maxDistance);
      if (sofar.size() >= maxPoints)
      {
	if (distance < maxDistance)
	{
	  // ditch smallest (largest distance)
	  sofar.poll();
	  sofar.add(new NodeWithDistance<Y>(distance,
	    here.point));
	  maxDistance = sofar.peek().getDistance();
	}
      }
      else
      {
	if (distance <= maxDistance)
	{
	  sofar.add(new NodeWithDistance<Y>(distance,
	    here.point));
	  if (sofar.size() >= maxPoints)
	  {
	    maxDistance = sofar.peek().getDistance();
	  }
	}
      }
    }
    double leftBound = Double.MAX_VALUE;
    Node<Y> leftHand = null;
    if (here.left != null)
    {
      leftHand = here.left;
      // Note that lower bound is NOT symettric
      leftBound = leftHand.point.lowerBound(query,
	maxDistance);
      // System.out.println("Left bound " + leftBound + " from " +
      //   leftHand.point);
    }
    double rightBound = Double.MAX_VALUE;
    Node<Y> rightHand = null;
    if (here.right != null)
    {
      rightHand = here.right;
      rightBound = rightHand.point.lowerBound(query, maxDistance);
      // System.out.println("Right bound " + rightBound + " from " +
      //  rightHand.point);
    }
    if ((rightHand != null) && (leftHand != null) &&
        (rightBound < leftBound))
    {
      // System.out.println("Swap");
      // swap left and right hand to visit least first
      Node<Y> t = leftHand;
      leftHand = rightHand;
      rightHand = t;
      double tt = leftBound;
      leftBound = rightBound;
      rightBound = tt;
    }
    boolean full = sofar.size() >= maxPoints;
    if ((leftHand != null) && 
        ((leftBound < maxDistance) ||
	  ((sofar.size() < maxPoints) && (leftBound == maxDistance))))
    {
      maxDistance = inner(sofar, leftHand, query, maxPoints,
        maxDistance);
      full = sofar.size() >= maxPoints;
    }
    else
    {
      // System.out.println("Skip left");
    }
    if ((rightHand != null) && 
        ((rightBound < maxDistance) ||
	  ((!full) && (rightBound == maxDistance))))
    {
      maxDistance = inner(sofar, rightHand, query, maxPoints,
        maxDistance);
    }
    else
    {
      // System.out.println("Skip right");
    }
    return maxDistance;
  }
  /** Interface for an object that manages a collection of nodes
   *  to be made into a tree. It allows access to them by index,
   *  and reorders them as necessary to split them up.
   *  The split shouldn't be too uneven, or the tree will be too tall 
   *  and recursive routines on it will blow the stack.
   */
  public interface Splitter<X>
  {
    /** Called with offsets of nodes split: the first node in
        the section of nodes to consider, and the length of that
	section. This method should return the offset in the main
	array of the centre of the split, rearranging the nodes
	as necessary so we have three groups of nodes at these offsets:
	<br>
	firstNode..returnValue - 1, returnValue, 
	returnValue+1..firstNode + len-1
	<br>
	We will call this only to set up finer and finer splits,
	with each split made on exactly the left or right portion
	of an earlier split.
	*/
    int split(int firstNode, int len);
    /** get the node at a given offset */
    X getNode(int offset);
    /** return the number of nodes held. Nodes are indexed from
     *  0 to size() - 1
     */
    int size();
  }
  /** Create a StaticClosest holding the data array given, using
      the splitter given
    */
  public StaticClosest(Splitter<Y> splitter)
  {
    root = createNodes(0, splitter.size(), splitter, null);
    if (root != null)
    {
      if (root.parent != null)
      {
	throw new IllegalArgumentException("Root parent mismatch");
      }
      // Can leave this in because the work done to check is small
      // compared with the work done to create
      checkNode(root);
    }
  }
  /** print out spaces */
  private static void spaces(int offset)
  {
    for (int i = 0; i < offset; i++)
    {
      System.out.print(' ');
    }
  }
  /** Print out the contents of a tree */
  private static<X extends Visible<X>> void
    printNode(Node<X> node, int offset)
  {
    spaces(offset);
    System.out.println(node);
    spaces(offset);
    System.out.println(node.point);
    if (node.left != null)
    {
      printNode(node.left, offset + 2);
    }
    if (node.right != null)
    {
      printNode(node.right, offset + 2);
    }
  }
  /** recursive routine called by constructor */
  private Node<Y> createNodes(int first, int len,
    Splitter<Y> splitter, Node<Y> parent)
  {
    // System.out.println("Create " + first + " " + len);
    if (len <= 0)
    {
      return null;
    }
    int split = splitter.split(first, len);
    Node<Y> result = new Node<Y>();
    result.point = splitter.getNode(split);
    result.parent = parent;
    result.left = createNodes(first, split - first, splitter,
      result);
    result.right = createNodes(split + 1, first + len - split - 1,
      splitter, result);
    result.point.updated(result.getLeftPoint(),
      result.getRightPoint());
    /*
    System.out.println("Updated " + result);
    System.out.println("Left " + result.left + " right "
      + result.right);
    System.out.println("Updated point " + result.point);
    */
    return result;
  }
  /** debug/test routine to check invariants */
  private void checkNode(Node<Y> node)
  {
    if (node.left != null)
    {
      if (node.left.parent != node)
      {
        throw new IllegalStateException("left parent mismatch");
      }
    }
    if (node.right != null)
    {
      if (node.right.parent != node)
      {
        throw new IllegalStateException("right parent mismatch");
      }
    }
    if (node.point.updated(node.getLeftPoint(), node.getRightPoint()))
    {
      throw new IllegalStateException("Update fail at " + node);
    }
  }
  /** Call updated() on a node and its ancestors, stopping when
   *  one calls false. Returns a reference to the root node, even if
   *  update is not called on it. Might be called after the visibility
   *  of a node has changed. This allows a caller that has made a
   *  Y visible or invisible to track from that Y up to its root,
   *  even if they haven't noted down its containing StaticClosest.
   *  They might then work out which StaticClosest it belongs too by
   *  examining that root.
   */
  public static<X extends Visible<X>> X update(Node<X> here,
   boolean call)
  {
    for (;;)
    {
      // Note that && will not evaluate its right side if its left
      // is not true.
      call = call && here.point.updated(
        here.getLeftPoint(), here.getRightPoint());
      if (here.parent == null)
      {
        return here.point;
      }
      here = here.parent;
    }
  }
  public static void main(String[] s)
  {
    long seed = 51;
    int size = 1000;
    int queries = 1000;
    int dims = 50;
    int goes = 2;
    int times = 10;
    System.out.println("Seed " + seed + " size " + size +
      " queries " + queries + " dims " + dims + " goes " +
      goes + " times " + times);
    final boolean TIME_EUCLID = true;
    final boolean TIME_CIRCLE = true;
    final boolean TIME_STATIC = true;
    final boolean TIME_SLOW = false;
    final boolean SHOW_TREES = false;
    for (int go = 0; go < goes; go++)
    {
      long cseed = seed + go;
      System.out.println("Seed " + cseed);
      Random r = new Random(cseed);
      List<EuclidNode> en = new ArrayList<EuclidNode>();
      for (int i = 0; i <size; i++)
      {
	en.add(Closest.randomEuclidNode(dims, r));
      }
      Splitter<EuclidNode> splitter =
	new PositionSplitter<EuclidNode>(en);
      StaticClosest<EuclidNode> sc =
	new StaticClosest<EuclidNode>(splitter);
      for (int i = 0; i < size; i++)
      {
	EuclidNode query = en.get(i);
        List<EuclidNode> closest = sc.findClosest(
	  query, 1, Double.MAX_VALUE);
        if (closest.size() != 1)
	{
	  throw new IllegalStateException("Closest missed completely");
	}
	EuclidNode result = closest.iterator().next();
	double resultDistance = query.distance(
	  result, Double.MAX_VALUE);
	if (resultDistance != 0.0)
	{
	  throw new IllegalStateException("Closest returned " +
	    result + " distance " + resultDistance + " not " + query);
	}
      }
    }
    Swatch loadEuclid = new Swatch();
    Swatch loadStatic = new Swatch();
    Swatch loadCircle = new Swatch();
    EuclidNode[] array = new EuclidNode[size];
    Swatch treeTime = new Swatch();
    Swatch staticTime = new Swatch();
    Swatch arrayTime = new Swatch();
    Swatch circleTime = new Swatch();
    for (int i = 0; i < times; i++)
    {
      List<EuclidNode> slist = new ArrayList<EuclidNode>();
      List<Closest.CircleNode> clist =
	new ArrayList<Closest.CircleNode>();
      long cseed = seed + i;
      System.out.println("Time Seed is " + cseed);
      Random r = new Random(cseed);
      for (int j = 0; j < size; j++)
      {
	double[] forPos = new double[dims];
	for (int k = 0; k < forPos.length; k++)
	{
	  forPos[k] = r.nextGaussian();
	}
	EuclidNode en = new EuclidNode(forPos);
	array[j] = en;
	en = new EuclidNode(forPos);
	slist.add(en);
	clist.add(new Closest.CircleNode(forPos));
      }
      Closest<EuclidNode> euclid = new Closest<EuclidNode>();
      loadEuclid.start();
      for (int j = 0; j < size; j++)
      {
	euclid.add(array[j]);
      }
      loadEuclid.stop();
      loadStatic.start();
      Splitter<EuclidNode> splitter =
	new PositionSplitter<EuclidNode>(slist);
      StaticClosest<EuclidNode> seuclid =
        new StaticClosest<EuclidNode>(splitter);
      if (SHOW_TREES)
      {
	printNode(seuclid.root, 0);
      }
      splitter = null;
      loadStatic.stop();
      loadCircle.start();
      Splitter<Closest.CircleNode> csplitter =
	new PositionSplitter<Closest.CircleNode>(clist);
      StaticClosest<Closest.CircleNode> staticCircle =
        new StaticClosest<Closest.CircleNode>(csplitter);
      if (SHOW_TREES)
      {
	printNode(staticCircle.root, 0);
      }
      csplitter = null;
      loadCircle.stop();
      for (int j = 0; j < queries; j++)
      {
	int queryFor = r.nextInt(array.length);
	EuclidNode query = array[queryFor];
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
	staticTime.start();
	if (TIME_STATIC)
	{
	  List<EuclidNode> cl = seuclid.findClosest(query, 1,
	    Double.MAX_VALUE);
	  EuclidNode treeAnswer = cl.iterator().next();
	  if (query.distance(treeAnswer, Double.MAX_VALUE) != 0.0)
	  {
	    throw new IllegalArgumentException(
	      "Failed to find static exact match at " + i + " total seed " +
	      cseed);
	  }
	}
	staticTime.stop();
	Closest.CircleNode cquery = clist.get(queryFor);
	circleTime.start();
	if (TIME_CIRCLE)
	{
	  List<Closest.CircleNode> cl =
	    staticCircle.findClosest(cquery, 1, Double.MAX_VALUE);
	  Closest.CircleNode treeAnswer = cl.iterator().next();
	  if (cquery.distance(treeAnswer, Double.MAX_VALUE) != 0.0)
	  {
	    throw new IllegalArgumentException(
	      "Failed to find static circle exact match at " +
	        i + " total seed " +
	      cseed);
	  }
	}
	circleTime.stop();
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
      }
    }
    System.out.println("Tree Load time " + loadEuclid);
    System.out.println("Static Load time " + loadStatic);
    System.out.println("Static Circle Load time " + loadCircle);
    System.out.println("Tree time " + treeTime);
    System.out.println("Static time " + staticTime);
    System.out.println("Circle time " + circleTime);
    System.out.println("Array time " + arrayTime);
  }
  /** class to hold a node and its offset for PositionSplitter */
  private static class NodeWithOffset<X>
  {
    private final X node;
    private final int offset;
    NodeWithOffset(X n, int o)
    {
      node = n;
      offset = o;
    }
  }
  /** Comparator for NodeWithOffset based on a coordinate */
  private static class Comp<X extends Positionable>
    implements Comparator<NodeWithOffset<X>>
  {
    int coord;
    public int compare(NodeWithOffset<X> a, NodeWithOffset<X> b)
    {
      double ac = a.node.getCoordinate(coord);
      double bc = b.node.getCoordinate(coord);
      if (ac < bc)
      {
        return -1;
      }
      if (ac > bc)
      {
        return 1;
      }
      return 0;
    }
  }
  /** Splitter for Positionables. */
  public static class PositionSplitter<X extends Positionable>
    implements StaticClosest.Splitter<X>
  {
    /** number of coordinates in nodes */
    final int numCoords;
    /** Array of X created */
    private final List<X> nodes;
    /** For each coordinate, we maintain an array of offsets. Within
     *  each section so far created, the offsets show how to pull
     *  out the nodes in order of that coordinate. So at the start
     *  nodes[offsets[0][0]] and nodes[offsets[0][size()-1]] are the
     *  nodes with min and max values of the 0th coordinate. After a split,
     *  for indexes x within a split we still have nodes[offsets[i][x]]
     *  in sorted order for each i.
     */
    private final int[][] offsets;
    /** array used during split to record where each
     *  node was moved to
     */
    private final int[] movedTo;
    /** ArrayList used during split as a temporary. This would be
     *  an array, except that we can't create generic Arrays so
     *  this preserves type checking
     */
    private final ArrayList<X> temp;
    /** create from array of X, which it both modifies
     *  (reorders) and requires that nothing else modify.
     */
    public PositionSplitter(List<X> forNodes)
    {
      nodes = forNodes;
      final int len = forNodes.size();
      if (len == 0)
      {
        offsets = new int[0][];
	movedTo = new int[len];
	temp = new ArrayList<X>(len);
	numCoords = 0;
	return;
      }
      numCoords = nodes.get(0).getDim();
      for (int i = 0; i < len; i++)
      {
        if (nodes.get(i).getDim() != numCoords)
	{
	  throw new IllegalArgumentException("Dimension mismatch");
	}
      }
      // Sort and create offsets
      List<NodeWithOffset<X>>nwo = 
        new ArrayList<NodeWithOffset<X>>(len);
      for (int i = 0; i < len; i++)
      {
        nwo.add(new NodeWithOffset<X>(nodes.get(i), i));
      }
      offsets = new int[numCoords][];
      Comp<X> c = new Comp<X>();
      for (int i = 0; i < numCoords; i++)
      {
        c.coord = i;
	Collections.sort(nwo, c);
	int[] row = new int[nwo.size()];
	for (int j = 0; j < row.length; j++)
	{
	  row[j] = nwo.get(j).offset;
	}
	offsets[i] = row;
      }
      nwo = null;
      movedTo = new int[len];
      temp = new ArrayList<X>(len);
      for (int i = 0; i < len; i++)
      {
	temp.add(null);
      }
    }
    public int split(int firstNode, int len)
    {
      // System.out.println("First " + firstNode + " len " + len);
      if (len <= 2)
      {
        return firstNode;
      }
      if (numCoords <= 0)
      {
        return firstNode + len / 2;
      }
      // Work out which coordinate has the largest range within
      // the split
      int last = firstNode + len - 1;
      double maxRange = nodes.get(offsets[0][last]).getCoordinate(0) -
        nodes.get(offsets[0][firstNode]).getCoordinate(0);
      int maxCoord = 0;
      for (int i = 1; i < numCoords; i++)
      {
	double range = nodes.get(offsets[i][last]).getCoordinate(i) -
	  nodes.get(offsets[i][firstNode]).getCoordinate(i);
        if (range > maxRange)
	{
	  maxRange = range;
	  maxCoord = i;
	}
      }
      // When we split at a node, we start with a range a-e and
      // end up with a point c and ranges a-b and d-e. The reduction
      // in the range is the gap d-b
      // Search the middle third of the nodes for the largest gap
      int numPossible = len / 3;
      int firstPossible = firstNode + numPossible;
      int pastPossible = firstPossible + numPossible;
      int splitAt = firstNode + len / 2;
      int[] row = offsets[maxCoord];
      double maxSplit = nodes.get(row[splitAt + 1]).getCoordinate(
        maxCoord) - nodes.get(row[splitAt - 1]).getCoordinate(maxCoord);
      for (int i = firstPossible; i < pastPossible; i++)
      {
	double split = nodes.get(row[i + 1]).getCoordinate(maxCoord) -
	  nodes.get(row[i - 1]).getCoordinate(maxCoord);
        if (split > maxSplit)
	{
	  maxSplit = split;
	  splitAt = i;
	}
      }
      // Now rearrange the arrays to reflect the split. Start off 
      // by moving the nodes into sorted order of the coordinate we
      // split on, and recording
      // where each node went to, given its old index
      for (int i = firstNode; i <= last; i++)
      {
        int oldOffset = row[i];
	movedTo[oldOffset] = i;
	temp.set(i, nodes.get(oldOffset));
	// we could set row[i] = i here but we do that later so we
	// can use row as temporary storage for a bit
      }
      for (int i = firstNode; i <= last; i++)
      {
        nodes.set(i, temp.get(i));
      }
      // Adjust the other coordinates, so that they put the sections
      // we have created into order
      for (int i = 0; i < numCoords; i++)
      {
        if (i == maxCoord)
	{
	  continue;
	}
	int[] rowHere = offsets[i];
	// We will use row as temporary storage, writing values starting
	// at these two pointers
	int leftWrite = firstNode;
	int rightWrite = splitAt + 1;
	for (int j = firstNode; j <= last; j++)
	{
	  // These offsets were sorted on this coordinate before
	  // the moved. Now we adjust them so they are sorted
	  // after the move, picking them up in sorted order
	  int valueForJ = movedTo[rowHere[j]];
	  // This is all very well, but these offsets sort the section
	  // we were given as a whole, mixing together offsets to
	  // both left and right halves. We need to disentangle this.
	  if (valueForJ < splitAt)
	  { // value points at left hand side of split
	    row[leftWrite++] = valueForJ;
	  }
	  else if (valueForJ > splitAt)
	  { // value points at right hand side of split
	    row[rightWrite++] = valueForJ;
	  }
	  else
	  { // offset of centre point
	    row[splitAt] = valueForJ;
	  }
	}
	System.arraycopy(row, firstNode, rowHere, firstNode, len);
      }
      // Fix up the row we used as temporary storage
      for (int i = firstNode; i <= last; i++)
      {
        row[i] = i;
      }
      // Whether to check that everything really is in sorted order
      // within its segments
      final boolean CHECK_ORDER = true;
      if (CHECK_ORDER)
      {
        for (int i = 0; i < numCoords; i++)
	{
	  int[] checkRow = offsets[i];
	  for (int j = firstNode; j < splitAt; j++)
	  {
	    int p = checkRow[j];
	    if ((p < firstNode) || (p >= splitAt))
	    {
	      throw new IllegalStateException("Out of range on left");
	    }
	  }
	  for (int j = firstNode + 1; j < splitAt; j++)
	  {
	    if (nodes.get(checkRow[j - 1]).getCoordinate(i) >
	      nodes.get(checkRow[j]).getCoordinate(i))
	    {
	      throw new IllegalStateException("Out of order on left");
	    }
	  }
	  if (checkRow[splitAt] != splitAt)
	  {
	    throw new IllegalStateException("Centre disordered");
	  }
	  for (int j = splitAt + 1; j <= last; j++)
	  {
	    int p = checkRow[j];
	    if ((p <= splitAt) || (p > last))
	    {
	      throw new IllegalStateException("Out of range on right");
	    }
	  }
	  for (int j = splitAt + 1; j < last; j++)
	  {
	    if (nodes.get(checkRow[j]).getCoordinate(i) >
	      nodes.get(checkRow[j + 1]).getCoordinate(i))
	    {
	      throw new IllegalStateException("Out of order on right");
	    }
	  }
	}
        int[] maxRow = offsets[maxCoord];
	if (nodes.get(maxRow[splitAt - 1]).getCoordinate(maxCoord) >
	  nodes.get(maxRow[splitAt]).getCoordinate(maxCoord))
	{
	  throw new IllegalStateException("Out of order before split");
	}
	if (nodes.get(maxRow[splitAt]).getCoordinate(maxCoord) >
	  nodes.get(maxRow[splitAt + 1]).getCoordinate(maxCoord))
	{
	  throw new IllegalStateException("Out of order before split");
	}
      }
      return splitAt;
    }
    public X getNode(int offset)
    {
      return nodes.get(offset);
    }
    public int size()
    {
      return nodes.size();
    }
  }
}
