package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;

/** This is a map built using PatriciaGeneric that can be used
 *  to find the N key-value pairs closest to a specified key. The
 *  distance is euclidean. Keys are arrays of doubles, and all keys
 *  in a map must have the same number of doubles, although this is
 *  not checked until run time.
 */
public class PatriciaClosest<V>
{
  /** At each interior point we keep track of a bounding box for
   *  all descendants. Because nodes are stored in a Patricia tree,
   *  the left descendants of a node all start with the same prefix of
   *  bits, which is not shared with the right descendants of that node.
   *  Because our Patricia key is a bit-interleaved version of the
   *  original coordinates, this means that the bounding boxes of
   *  the two descendants of a node are distinct.
   */
  private static class CloseDescendant<V> implements
    PatriciaGeneric.ChildInfo<PatriciaGeneric.DoubleArrayKey,
      V, CloseDescendant<V>>
  {
    /** min, max over all descendants for each dimension:
        min1, max1, min2, max2, min3, max3...
	*/
    private double[] bounds;
    public boolean leafUpdate(PatriciaGeneric.DoubleArrayKey k1,
      V v1, PatriciaGeneric.DoubleArrayKey k2, V v2)
    {
      double[] d1 = k1.toDouble();
      double[] d2 = k2.toDouble();
      if (d1.length != d2.length)
      {
        throw new IllegalArgumentException(
	  "double array length mismatch");
      }
      boolean result = true;
      if (bounds == null)
      {
        bounds = new double[d1.length * 2];
	result = false;
      }
      int off = 0;
      for (int i = 0; i < d1.length; i++)
      {
	final double x1 = d1[i];
	final double x2 = d2[i];
        final double min = Math.min(x1, x2);
	if (min != bounds[off])
	{
	  bounds[off] = min;
	  result = false;
	}
	off++;
        final double max = Math.max(x1, x2);
	if (max != bounds[off])
	{
	  bounds[off] = max;
	  result = false;
	}
	off++;
      }
      return result;
    }
    public boolean mixedUpdate(PatriciaGeneric.DoubleArrayKey k, V v,
      CloseDescendant<V> di)
    {
      double[] dk = k.toDouble();
      if (di.bounds.length != (dk.length * 2))
      {
        throw new IllegalArgumentException("Dimension mismatch");
      }
      boolean result = true;
      if (bounds == null)
      {
        result = false;
	bounds = new double[di.bounds.length];
      }
      int off = 0;
      for (int i = 0; i < dk.length; i++)
      {
	final double x = dk[i];
        final double min = Math.min(x, di.bounds[off]);
	if (min != bounds[off])
	{
	  bounds[off] = min;
	  result = false;
	}
	off++;
	final double max = Math.max(x, di.bounds[off]);
	if (max != bounds[off])
	{
	  bounds[off] = max;
	  result = false;
	}
	off++;
      }
      return result;
    }
    public boolean internalUpdate(CloseDescendant<V> di1,
      CloseDescendant<V> di2)
    {
      if (di1.bounds.length != di2.bounds.length)
      {
        throw new IllegalArgumentException("Bounds mismatch");
      }
      boolean result = true;
      if (bounds == null)
      {
        result = false;
	bounds = new double[di1.bounds.length];
      }
      for (int i = 0; i < di1.bounds.length;)
      {
        final double min = Math.min(di1.bounds[i], di2.bounds[i]);
	if (min != bounds[i])
	{
	  bounds[i] = min;
	  result = false;
	}
	i++;
	final double max = Math.max(di1.bounds[i], di2.bounds[i]);
	if (max != bounds[i])
	{
	  bounds[i] = max;
	  result = false;
	}
	i++;
      }
      return result;
    }
    /** work out minimum possible distance to given point */
    double minDistance(double[] point)
    {
      double sqMin = 0.0;
      if ((point.length * 2) != bounds.length)
      {
        throw new IllegalArgumentException("Point bounds mismatch");
      }
      for (int i = 0; i < point.length; i++)
      {
        double x = point[i];
	final double bLow = bounds[2 * i];
	if (x < bLow)
	{
	  double d = x - bLow;
	  sqMin += d * d;
	}
	final double bHigh = bounds[2 * i + 1];
	if (x > bHigh)
	{
	  double d = bHigh - x;
	  sqMin += d * d;
	}
      }
      return Math.sqrt(sqMin);
    }
  }
  private static class DescendantFactory<V> implements
    PatriciaGeneric.InfoFactory<CloseDescendant<V>>
  {
    public CloseDescendant<V> create()
    {
      return new CloseDescendant<V>();
    }
  }
  /** hold almost all info in this Patricia Map */
  private final PatriciaGeneric<PatriciaGeneric.DoubleArrayKey, V,
    CloseDescendant<V>> map;
  /** construct empty version */
  PatriciaClosest()
  {
    map = new
      PatriciaGeneric<PatriciaGeneric.DoubleArrayKey, V,
	CloseDescendant<V>>(new DescendantFactory<V>());
  }
  /** construct copy of another PatriciaClosest */
  PatriciaClosest(PatriciaClosest<V> p)
  {
    map = new
      PatriciaGeneric<PatriciaGeneric.DoubleArrayKey, V,
	CloseDescendant<V>>(p.map, new DescendantFactory<V>());
  }
  /** clear all info */
  public void clear()
  {
    map.clear();
  }
  /** return whether empty */
  public boolean isEmpty()
  {
    return map.isEmpty();
  }
  /** put all the mappings in another PatriciaClosest into this one */
  public void putAll(PatriciaClosest<V> p)
  {
    map.putAll(p.map);
  }
  /** Retrieve a value with key matching that quoted,
   * or null.
   * @param k a key to look for in the map
   */
  public V get(PatriciaGeneric.DoubleArrayKey k)
  {
    return map.get(k);
  }
  /** Insert a key-value pair, overwriting any existing exact
   * match.
   * @param key the key
   * @param value its value
   */
  public V put(PatriciaGeneric.DoubleArrayKey key, V value)
  {
    return map.put(key, value);
  }
  /** Delete an exactly matching key.
   * @param s a string exactly matching the key of a
   * key-value pair to be deleted.
   * @return the previously associated value, or null
   */
  public V remove(PatriciaGeneric.DoubleArrayKey k)
  {
    return map.remove(k);
  }
  /** Number of items in the map
  */
  public int size()
  {
    return map.size();
  }
  /** return iterator over entries */
  public Iterator<Map.Entry<PatriciaGeneric.DoubleArrayKey,
    V>> iterator()
  {
    return map.iterator();
  }
  /** class to keep track of key and value with associated distance
   */
  public static class WithDistance<V> implements 
    Comparable<WithDistance<V>>
  {
    /** key */
    private final PatriciaGeneric.DoubleArrayKey k;
    /** - the distance so the largest distance is at the head of a 
     *    priority queue
     */
    private final double minusDistance;
    /** value */
    private final V v;
    /** construct from info */
    WithDistance(PatriciaGeneric.DoubleArrayKey key,
      double distance, V value)
    {
      k = key;
      minusDistance = -distance;
      v = value;
    }
    public int compareTo(WithDistance<V> other)
    {
      if (minusDistance < other.minusDistance)
      {
        return -1;
      }
      if (minusDistance > other.minusDistance)
      {
        return 1;
      }
      return 0;
    }
    /** return distance */
    public double getDistance()
    {
      return -minusDistance;
    }
    /** return value */
    public V getValue()
    {
      return v;
    }
    /** return key */
    public PatriciaGeneric.DoubleArrayKey getKey()
    {
      return k;
    }
  }
  /** Append to the given list the closest N points within the
   *  given distance of the key point, closest first. Return the
   *  number appended, which will be less if not that many such
   *  points exist.
   */
  public int appendClosest(PatriciaGeneric.DoubleArrayKey k,
    double maxDistance, int n,
      List<WithDistance<V>> appendHere)
  {
    final double[] kPos = k.toDouble();
    final double sqMax;
    if (maxDistance < Double.MAX_VALUE)
    {
      sqMax = maxDistance * maxDistance;
    }
    else
    {
      sqMax = Double.MAX_VALUE;
    }
    // priority queue ordered with largest at the head, so we can
    // get rid of it
    final PriorityQueue<WithDistance<V>> q =
      new PriorityQueue<WithDistance<V>>();
    // start with top node in our hand
    PatriciaGeneric.Node<PatriciaGeneric.DoubleArrayKey, V,
      CloseDescendant<V>> node = map.getHead();
    // stack of nodes not visited yet
    Stack<PatriciaGeneric.Node<PatriciaGeneric.DoubleArrayKey, V,
      CloseDescendant<V>>> al = new Stack<PatriciaGeneric.Node<
        PatriciaGeneric.DoubleArrayKey, V, CloseDescendant<V>>>();
    for (;;)
    {
      if (node == null)
      { // Nothing in our hand. See if anything left to visit
	if (al.empty())
	{
	  break;
	}
	node = al.pop();
      }
      CloseDescendant<V> info = node.getDescendantInfo();
      if (info == null)
      { // reached leaf
	acceptLeaf(node, q, sqMax, kPos, n);
	node = null;
        continue;
      }
      // Here with internal node
      PatriciaGeneric.Node<PatriciaGeneric.DoubleArrayKey, V,
	CloseDescendant<V>> left = node.firstChild();
      final CloseDescendant<V> leftInfo = left.getDescendantInfo();
      if (leftInfo == null)
      { // got leaf on left
	acceptLeaf(left, q, sqMax, kPos, n);
	node = node.lastChild();
	continue;
      }
      PatriciaGeneric.Node<PatriciaGeneric.DoubleArrayKey, V,
	CloseDescendant<V>> right = node.lastChild();
      final CloseDescendant<V> rightInfo = right.getDescendantInfo();
      if (rightInfo == null)
      { // got leaf on right
	acceptLeaf(right, q, sqMax, kPos, n);
	node = left;
	continue;
      }
      // Here when both left and right are internal nodes.
      double leftDistance = leftInfo.minDistance(kPos);
      double rightDistance = rightInfo.minDistance(kPos);
      if (leftDistance > rightDistance)
      {
        double t = leftDistance;
	leftDistance = rightDistance;
	rightDistance = t;
	PatriciaGeneric.Node<PatriciaGeneric.DoubleArrayKey, V,
	  CloseDescendant<V>> tt = left;
	left = right;
	right = tt;
      }
      // here with right distance largest. Either eliminate it or
      // stack it.
      if (q.size() >= n)
      {
	// this must be at least as small as the current threshold or it
	// wouldn't be on the queue
        maxDistance = q.peek().getDistance();
      }
      if (rightDistance <= maxDistance)
      { // will have to work on both, since left < right
        al.push(right);
	node = left;
	continue;
      }
      if (leftDistance <= maxDistance)
      { // keep going down left side
        node = left;
      }
      else
      { // don't need either node
        node = null;
      }
    }
    // Retrieve the answers, largest distance first, by pulling them
    // off the queue one by one
    ArrayList<WithDistance<V>> results =
      new ArrayList<WithDistance<V>>();
    for (;;)
    {
      WithDistance<V> item = q.poll();
      if (item == null)
      {
        break;
      }
      results.add(item);
    }
    // Append to destination queue, smallest first
    for (int i = results.size() - 1; i >= 0; i--)
    {
      appendHere.add(results.get(i));
    }
    return 0;
  }
  /** utility method to add info from leaf to queue if necessary */
  private static<V> void acceptLeaf(PatriciaGeneric.Node<
    PatriciaGeneric.DoubleArrayKey, V, CloseDescendant<V>> node,
    PriorityQueue<WithDistance<V>> q, double sqMax, double[] kPos,
    int n)
  {
    PatriciaGeneric.DoubleArrayKey dk = node.getKey();
    double[] pos = dk.toDouble();
    double sqDist = 0.0;
    for (int i = 0; i < kPos.length; i++)
    {
      final double diff = kPos[i] - pos[i];
      sqDist += diff * diff;
    }
    if (sqDist > sqMax)
    { // beyond bound
      return;
    }
    double distance = Math.sqrt(sqDist);
    if (q.size() >= n)
    {
      WithDistance<V> wd = q.peek();
      if (distance >= wd.getDistance())
      { // outside top n
	return;
      }
      q.poll();
    }
    // got new entry for queue
    q.add(new WithDistance<V>(dk, distance, node.getValue()));
  }
  /** check invariants of base map (slow) */
  public void checkInvariants()
  {
    map.checkInvariants();
  }
}
