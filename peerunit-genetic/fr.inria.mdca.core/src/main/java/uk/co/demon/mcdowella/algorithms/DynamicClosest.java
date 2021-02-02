package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

/** This class uses the class StaticClosest to answer nearest
 *  neighbour queries, but in a manner that means that the amortised
 *  cost of inserts and deletes is relatively small. It is based
 *  far too loosely on the sort of doubling tricks done properly in
 *  chapter 7 of Data Structures and Efficient Algorithms, by Mehlhorn,
 *  (available online at 
 *  http://www.mpi-sb.mpg.de/~mehlhorn/DatAlgbooks.html).
 *  <br>
 *  You should probably use PatriciaClosest instead.
*/
public class DynamicClosest<X extends FindScattered.Boundable<X> &
  StaticClosest.Positionable> implements Set<X>
{
  /** We create our own DynamicNode structure to keep track
   *  of the visibility or otherwise of nodes, or else we would
   *  have to require that clients maintain isVisible even after
   *  removing points
   */
  private static class DynamicNode<Y extends FindScattered.Boundable<Y> &
    StaticClosest.Positionable>
    implements StaticClosest.Visible<DynamicNode<Y>>,
    StaticClosest.Positionable
  {
    /** point provided by user */
    private final Y point;
    /** create from user's point */
    DynamicNode(Y y)
    {
      point = y;
      visible = true;
    }
    /** whether visible */
    private boolean visible;
    public boolean isVisible()
    {
      return visible;
    }
    public boolean updated(DynamicNode<Y> left, DynamicNode<Y> right)
    {
      Y l = null;
      if (left != null)
      {
        l = left.point;
      }
      Y r = null;
      if (right != null)
      {
        r = right.point;
      }
      return point.updated(l, r);
    }
    public double lowerBound(DynamicNode<Y> other, double d)
    {
      return point.lowerBound(other.point, d);
    }
    public double distance(DynamicNode<Y> other, double d)
    {
      return point.distance(other.point, d);
    }
    public double getCoordinate(int d)
    {
      return point.getCoordinate(d);
    }
    public int getDim()
    {
      return point.getDim();
    }
  }
  /** We keep a number of StaticClosest structures to represent
   *  the main bulk of data stored. They are created at their
   *  maximum size, but deleted nodes may be set to 'invisible'.
   *  This gives the minimum created size of the smallest node during
   *  inserts (it may be smaller after a large number of deletes).
   */
  private final int minCreateSize;
  /** create given pool size, which regulates the size of
   *  a pool of completely unordered instances. A dozen or so should
   *  be fine: the Algorithm would work with a pool size of 1, but
   *  I thought allowing larger pool sizes might help improve the
   *  constant factor. Note that because the actual size of the
   *  pool fluctuates between 0 and the maximum pool size, observing
   *  the effect of changes to the pool size is tricky.
   */
  public DynamicClosest(int poolSize)
  {
    minCreateSize = poolSize;
  }
  /** default maximum pool size */
  private static final int DEFAULT_MIN = 12;
  /** default constructor.
  Made package visibility to emphasise that you should probably
  use PatriciaClosest instead */
  /* public */ DynamicClosest()
  {
    minCreateSize = DEFAULT_MIN;
  }
  /** construct from collection
  Made package visibility to emphasise that you should probably
  use PatriciaClosest instead */
  /* public */ DynamicClosest(Collection<? extends X>col)
  {
    minCreateSize = DEFAULT_MIN;
    addAll(col);
  }
  /**
   * A structure will be completely destroyed when the fraction
   * of remaining visible nodes in it drops below 1:this number
   */
  private final int minVisibleFraction = 4;
  /** This holds a small number of nodes not contained in any
   *  StaticClosest
   */
  private final Set<X> pool = new HashSet<X>();
  /**
   *  This holds the staticClosests, with each one at least twice
   *  the size of its predecessor, but no more than four times that
   *  size.
   */
  private final List<StaticClosest<DynamicNode<X>>> scList =
    new ArrayList<StaticClosest<DynamicNode<X>>>();
  /** remove all contents */
  public void clear()
  {
    pool.clear();
    scList.clear();
    dynamicByPoint.clear();
    numVisible = 0;
  }
  /** remove all points in given collection and return whether
   *  collection changed
   */
  public boolean removeAll(Collection<?> col)
  {
    boolean changed = false;
    for (Object o: col)
    {
      changed |= innerRemove(o);
    }
    checkVisible();
    return changed;
  }
  /** remove all saved points in given collection and return
   *  whether collection changed
   */
  public boolean retainAll(Collection<?> col)
  {
    Set<X> sofar = new HashSet<X>(pool);
    sofar.addAll(dynamicByPoint.keySet());
    if (!sofar.retainAll(col))
    {
      return false;
    }
    clear();
    addAll(sofar);
    return true;
  }
  /** add all specified points */
  public boolean addAll(Collection<? extends X> col)
  {
    boolean changed = false;
    for (X x: col)
    {
      changed |= innerAdd(x);
    }
    checkPool();
    return changed;
  }
  /** return whether we contain all of the specified collection */
  public boolean containsAll(Collection<?> col)
  {
    for (Object o: col)
    {
      if (!(dynamicByPoint.containsKey(o) || pool.contains(o)))
      {
        return false;
      }
    }
    return true;
  }
  /** return whether empty */
  public boolean isEmpty()
  {
    return pool.isEmpty() && (numVisible == 0);
  }
  /** return number of objects contained */
  public int size()
  {
    return pool.size() + numVisible;
  }
  /** return whether we contain the specified object */
  public boolean contains(Object o)
  {
    return dynamicByPoint.containsKey(o) || pool.contains(o);
  }
  /** return contents as array of specified type */
  public <T> T[] toArray(T[] a)
  {
    List<X> l = new ArrayList<X>(dynamicByPoint.keySet());
    l.addAll(pool);
    return l.toArray(a);
  }
  /** return contents as array */
  public Object[] toArray()
  {
    List<X> l = new ArrayList<X>(dynamicByPoint.keySet());
    l.addAll(pool);
    return l.toArray();
  }
  /** Iterator over nodes held */
  private static class NodeIt<X> implements Iterator<X>
  {
    private final Iterator<X> poolIt;
    private final Iterator<X> mainIt;
    NodeIt(Iterator<X> forPool, Iterator<X> main)
    {
      poolIt = forPool;
      mainIt = main;
    }
    public boolean hasNext()
    {
      return mainIt.hasNext() || poolIt.hasNext();
    }
    public X next()
    {
      if (poolIt.hasNext())
      {
        return poolIt.next();
      }
      return mainIt.next();
    }
    public void remove()
    {
      throw new UnsupportedOperationException();
    }
  }
  public Iterator<X> iterator()
  {
    return new NodeIt<X>(pool.iterator(),
      dynamicByPoint.keySet().iterator());
  }
  /** number of visible nodes in trees */
  private int numVisible = 0;
  /** map from nodes to their dynamic nodes */
  private final Map<X, DynamicNode<X>> dynamicByPoint = new
    HashMap<X, DynamicNode<X>>();
  /** slow routine to check the invariants */
  private void checkInvariants()
  {
    if (pool.size() >= minCreateSize)
    {
      throw new IllegalStateException("Pool too big");
    }
    for (X x: pool)
    {
      if (dynamicByPoint.containsKey(x))
      {
        throw new IllegalStateException("Pool point in map");
      }
    }
    Set<DynamicNode<X>> all = new HashSet<DynamicNode<X>>();
    Set<X> allPoints = new HashSet<X>();
    int seen = 0;
    int past = scList.size();
    int minSize = minCreateSize;
    for (int i = 0; i < past; i++)
    {
      // System.err.println("I = " + i + " minSize = " + minSize);
      StaticClosest<DynamicNode<X>> sc = scList.get(i);
      int nextMin = minSize + minSize;
      if (sc == null)
      {
	minSize = nextMin;
        continue;
      }
      List<DynamicNode<X>> fromHere =
	new ArrayList<DynamicNode<X>>();
      sc.addTo(fromHere);
      int sizeHere = fromHere.size();
      if (sizeHere >= nextMin)
      {
        throw new IllegalStateException("SC too big size " + sizeHere +
	  " expected < " + nextMin + " at " + i);
      }
      if ((i > 0) && (sizeHere < minSize))
      { // can be small at i=0 after a delete
        throw new IllegalStateException("SC too small");
      }
      minSize = nextMin;
      seen += sizeHere;
      all.addAll(fromHere);
      for (DynamicNode<X> dx: fromHere)
      {
	X x = dx.point;
	if (pool.contains(dx))
	{
	  throw new IllegalStateException("Point in pool");
	}
        allPoints.add(x);
      }
    }
    if (seen != all.size())
    {
      for (int j = 0; j < past; j++)
      {
	StaticClosest<DynamicNode<X>> sc = scList.get(j);
	if (sc == null)
	{
	  continue;
	}
	List<DynamicNode<X>> l = new ArrayList<DynamicNode<X>>();
	sc.addTo(l);
        System.out.println("At " + j + ": " + l);
      }
      throw new IllegalStateException(
        "DynamicNode in multiple staticClosests");
    }
    if (allPoints.size() != all.size())
    {
      throw new IllegalStateException(
        "Point in multiple static points");
    }
    int visible = 0;
    for (DynamicNode<X> x: all)
    {
      if (x.isVisible())
      {
        visible++;
      }
    }
    if (visible != numVisible)
    {
      throw new IllegalStateException("Visible count mismatch");
    }
  }
  /** insert a new DynamicNode. */
  public boolean add(X x)
  {
    boolean result = innerAdd(x);
    checkPool();
    return result;
  }
  /** utility routine to add a single object to the pool
   *  and provide return value for add()
   */
  private boolean innerAdd(X x)
  {
    if (pool.contains(x))
    { // already present
      return false;
    }
    DynamicNode<X> d = dynamicByPoint.get(x);
    if (d != null)
    { // A node corresponding to this is already in the tree
      if (d.isVisible())
      { // was already present and visible
        return false;
      }
      // was present but not visible
      d.visible = true;
      numVisible++;
      // System.err.println("True from visible");
      return true;
    }
    // System.err.println("True as previously unknown");
    pool.add(x);
    return true;
  }
  /** Utility routine to check the size of the pool and
   *  flush it if necessary
   */
  private void checkPool()
  {
    if (pool.size() < minCreateSize)
    { // Pool still small enough to accept new info
      return;
    }
    // System.err.println("Will flush pool");
    // Work from left to right until we can account for all the
    // nodes we have to store in a single new StaticClosest
    int nextSize = minCreateSize;
    int numSc = scList.size();
    List<DynamicNode<X>> pooled = new ArrayList<DynamicNode<X>>();
    for (X p: pool)
    {
      DynamicNode<X> dd = new DynamicNode<X>(p);
      dynamicByPoint.put(p, dd);
      pooled.add(dd);
      numVisible++;
    }
    pool.clear();
    int i = 0;
    for (;;i++)
    {
      if (i < numSc) 
      {
        StaticClosest<DynamicNode<X>> h = scList.get(i);
	if (h != null)
	{
	  List<DynamicNode<X>> fromHere =
	    new ArrayList<DynamicNode<X>>();
	  h.addTo(fromHere);
	  for (DynamicNode<X> dd: fromHere)
	  {
	    if (dd.isVisible())
	    {
	      pooled.add(dd);
	    }
	    else
	    {
	      dynamicByPoint.remove(dd.point);
	    }
	  }
	  scList.set(i, null);
	}
      }
      else
      {
        scList.add(null);
      }
      int doubleSize = nextSize + nextSize;
      int here = pooled.size();
      // On entry we have pooled.size >= minCreateSize = nextSize
      // at i = 0, so we have nextSize <= here. If doubleSize <= here
      // then nextSize <= here next time round, so eventually, if only
      // when we run off the end of the current scList, both parts
      // of the if will be true.
      if ((doubleSize > here) && (nextSize <= here))
      {
        break;
      }
      nextSize = doubleSize;
    }
    // Here to create a single StaticClosest holding all the data
    // seen in our left-right sweep in a slot dedicated to its size.
    recreateFrom(i, pooled);
  }
  /** Create a new StaticClosest from the info passed in to at i */
  private void recreateFrom(int i, List<DynamicNode<X>> pooled)
  {
    // Here to create StaticClosest structures from i downwards.
    // If only inserts are done, the StaticClosest objects we
    // see are always of their full size, and the largest
    // StaticClosest constructed is therefore new (or it would
    // not mop up the overflow that produces it). Because the sizes
    // go up as powers of two, it is big enough to contain all the
    // data from the previous objects, which means that nodes
    // move to the right every time they are involved in a creation,
    // so each object is involved in at most lg N creations. The cost
    // of a creation grows as n lg n, as the number n of objects
    // involved increases, because creations involve sorts, but consumes
    // the inserts from n objects. So the worst case for n insertions
    // can be no greater than creating an object holding n nodes lg n
    // times, or n (lg n)^2.
    StaticClosest.PositionSplitter<DynamicNode<X>> ps =
      new StaticClosest.PositionSplitter<DynamicNode<X>>(pooled);
    StaticClosest<DynamicNode<X>> newSc =
      new StaticClosest<DynamicNode<X>>(ps);
    scList.set(i, newSc);
    pool.clear();
  }
  /** Delete a dynamicNode. Most of the time a deletion just marks
   *  a node as deleted, but when there are too many deleted nodes
   *  in the trees, we rebuild the whole thing, dropping the
   *  deleted nodes. To calculate the amortised costs of a sequence
   *  of insertions and deletions, divide the work into chunks,
   *  with each chunk starting just after a deletion has triggered
   *  a rebuild, and ending with the work of the next rebuild. The
   *  cost of the work triggered by insertions is spread over all
   *  the insertions. The cost of the work triggered by deletions
   *  is spread over all the deletions. Because deletions are
   *  triggered when the fraction of deleted nodes in the tree
   *  becomes too high, we know there is at least a minimum of
   *  non-deleted nodes in the trees at all times. The cost of
   *  insertions is almost the same as for the same number of straight
   *  insertions without intervening deletions. This is larger than
   *  if intervening deletions removed nodes from the tree, but because
   *  of the bound on the fraction of deleted nodes, not too much
   *  higher. There is also an extra cost due to the extra nodes
   *  left over from the previous phase, but they are in a single
   *  StaticClosest kept high enough up the list that we don't encounter
   *  them unless this phase contains at least as many insertions as
   *  the number of nodes left over from the previous phase.
   *  Most deletions have only constant cost, to mark a node
   *  as deleted. We can amortise the n lg n cost of our rebuild over
   *  all kn objects deleted, were k is the ratio of present to deleted
   *  objects that triggers a rebuild. Again the costs are reasonable.
   */
  public boolean remove(Object onode)
  {
    boolean result = innerRemove(onode);
    checkVisible();
    return result;
  }
  private boolean innerRemove(Object onode)
  {
    if (pool.remove(onode))
    {
      return true;
    }
    DynamicNode<X> d = dynamicByPoint.get(onode);
    if (d == null)
    { // not held
      // System.err.println("Not held");
      return false;
    }
    if (!d.isVisible())
    { // not visible
      // System.err.println("Not visible");
      return false;
    }
    d.visible = false;
    numVisible--;
    return true;
  }
  private void checkVisible()
  {
    if ((numVisible * minVisibleFraction) >= dynamicByPoint.size())
    {
      return;
    }
    // Accumulate a list of all the visible nodes and rebuild
    List<DynamicNode<X>> pooled = new ArrayList<DynamicNode<X>>();
    for (X x: pool)
    {
      DynamicNode<X> dd = new DynamicNode<X>(x);
      dynamicByPoint.put(x, dd);
      pooled.add(dd);
    }
    int numSc = scList.size();
    for (int i = 0; i < numSc; i++)
    {
      StaticClosest<DynamicNode<X>> sc = scList.get(i);
      if (sc == null)
      {
        continue;
      }
      scList.set(i, null);
      List<DynamicNode<X>> fromHere = new ArrayList<DynamicNode<X>>();
      for (DynamicNode<X> dh: fromHere)
      {
        if (!dh.isVisible())
	{
	  dynamicByPoint.remove(dh);
	}
	else
	{
	  pooled.add(dh);
	}
      }
    }
    numVisible = pooled.size();
    int canStore = minCreateSize;
    for (int i = 0;; i++)
    {
      int doubleStore = canStore + canStore;
      if (numVisible < doubleStore)
      {
	recreateFrom(i, pooled);
	return;
      }
      canStore = doubleStore;
    }
  }
  /** Find up to the given number of points closest to the query
      point and no more than the given distance away. Resulting list
      is sorted in order of distance with closest point first.
   */
  public List<X> findClosest(X query, int maxPoints,
    double maxDistance)
  {
    if (maxPoints <= 0)
    {
      return Collections.emptyList();
    }
    Queue<StaticClosest.NodeWithDistance<X>> q = new
      PriorityQueue<StaticClosest.NodeWithDistance<X>>();
    for (X x: pool)
    {
      double distance = query.distance(x, maxDistance);
      if (distance > maxDistance)
      {
	continue;
      }
      if (q.size() >= maxPoints)
      {
        if (distance == maxDistance)
	{
	  continue;
	}
	// remove current worst entry
	q.poll();
      }
      q.add(new StaticClosest.NodeWithDistance<X>(distance, x));
      if (q.size() >= maxPoints)
      {
	maxDistance = q.peek().getDistance();
      }
    }
    int past = scList.size();
    DynamicNode<X> dq = new DynamicNode<X>(query);
    for (int i = 0; i < past; i++)
    {
      StaticClosest<DynamicNode<X>> here = scList.get(i);
      if (here == null)
      {
        continue;
      }
      List<DynamicNode<X>> possible = here.findClosest(dq,
        maxPoints, maxDistance);
      for (DynamicNode<X> x: possible)
      {
        double distance = query.distance(x.point, maxDistance);
	if (distance > maxDistance)
	{
	  continue;
	}
	if (q.size() >= maxPoints)
	{
	  if (distance == maxDistance)
	  {
	    continue;
	  }
	  // remove current smallest entry
	  q.poll();
	}
	q.add(new StaticClosest.NodeWithDistance<X>(distance, x.point));
	if (q.size() >= maxPoints)
	{
	  maxDistance = q.peek().getDistance();
	}
      }
    }
    // create result in wrong order
    ArrayList<X> backwards = new ArrayList<X>();
    for (;;)
    {
      // pull out furthest remaining
      StaticClosest.NodeWithDistance<X> nwd = q.poll();
      if (nwd == null)
      {
        break;
      }
      backwards.add(nwd.getNode());
    }
    // reverse to get right order
    ArrayList<X> result = new ArrayList<X>();
    for (int i = backwards.size() - 1; i >= 0; i--)
    {
      result.add(backwards.get(i));
    }
    return result;
  }
  public static void main(String[] s)
  {
    long seed = 42000;
    int goes = 10;
    int size = 30000;
    int inner = 10000;
    int dim = 3;
    int poolSize = 20;
    int numToFind = 1;
    final boolean TEST_RANDOM = false;
    final boolean TEST_INSDEL = false;
    final boolean TIME_INSDEL = true;
    boolean trouble = false;
    int s1 = s.length - 1;
    String num = null;
    try
    {
      for (int i = 0; i < s1; i++)
      {
        if ("-dim".equals(s[i]))
	{
	  num = s[++i].trim();
	  dim = Integer.parseInt(num);
	}
        else if ("-find".equals(s[i]))
	{
	  num = s[++i].trim();
	  numToFind = Integer.parseInt(num);
	}
        else if ("-goes".equals(s[i]))
	{
	  num = s[++i].trim();
	  goes = Integer.parseInt(num);
	}
        else if ("-inner".equals(s[i]))
	{
	  num = s[++i].trim();
	  inner = Integer.parseInt(num);
	}
        else if ("-pool".equals(s[i]))
	{
	  num = s[++i].trim();
	  poolSize = Integer.parseInt(num);
	}
        else if ("-seed".equals(s[i]))
	{
	  num = s[++i].trim();
	  seed = Long.parseLong(num);
	}
        else if ("-size".equals(s[i]))
	{
	  num = s[++i].trim();
	  size = Integer.parseInt(num);
	}
	else
	{
	  System.err.println("Cannot handle flag " + s[i]);
	  trouble = true;
	}
      }
      if ((s.length & 1) != 0)
      {
        System.err.println("Args are all of form -flag #");
	trouble = true;
      }
    }
    catch (NumberFormatException nfe)
    {
      System.err.println("cannot read number in " + num);
      trouble = true;
    }
    if (trouble)
    {
      System.err.println("Args are [-dim #] [-find #] [-goes #] " +
        "[-inner #] [-pool #] [-seed #] [-size #]");
      return;
    }
    System.out.println("Seed " + seed + " goes " + goes + " size " +
      size + " inner " + inner + " dim " + dim + " pool " + poolSize +
      " to find " + numToFind);
    Swatch cLoad = new Swatch();
    Swatch dLoad = new Swatch();
    Swatch cSearch = new Swatch();
    Swatch dSearch = new Swatch();
    for (int go = 0; go < goes; go++)
    {
      long cseed = seed + go;
      System.out.println("Seed " + cseed);
      Random r = new Random(cseed);
      EuclidNode[] ar = new EuclidNode[size];
      for (int i = 0; i < ar.length; i++)
      {
        ar[i] = Closest.randomEuclidNode(dim, r);
      }
      if (TEST_RANDOM)
      {
	DynamicClosest<EuclidNode> dc =
	  new DynamicClosest<EuclidNode>(poolSize);
	Set<EuclidNode> cc = new HashSet<EuclidNode>();
	for (int i = 0; i < inner; i++)
	{
	  EuclidNode e = ar[r.nextInt(ar.length)];
	  // System.err.println("Add " + e);
	  boolean a = dc.add(e);
	  boolean b = cc.add(e);
	  if (a != b)
	  {
	    throw new IllegalStateException("Difference in add: " +
	      a + " vs " + b);
	  }
	  dc.checkInvariants();
	  e = ar[r.nextInt(ar.length)];
	  // System.err.println("Remove " + e);
	  a = dc.remove(e);
	  b = cc.remove(e);
	  if (a != b)
	  {
	    throw new IllegalStateException("Difference in remove: " +
	      a + " vs " + b);
	  }
	  dc.checkInvariants();
	}
      }
      if (TEST_INSDEL)
      {
	DynamicClosest<EuclidNode> dc =
	  new DynamicClosest<EuclidNode>(poolSize);
	int todo = r.nextInt(ar.length);
        for (int i = 0; i < todo; i++)
	{
	  if ((i % 100) == 0)
	  {
	    System.err.println("Insert " + i);
	  }
	  EuclidNode e = ar[i];
	  if (!dc.add(e))
	  {
	    throw new IllegalStateException("Add said not added");
	  }
	  dc.checkInvariants();
	}
        for (int i = 0; i < todo; i++)
	{
	  if ((i % 100) == 0)
	  {
	    System.err.println("Remove " + i);
	  }
	  EuclidNode e = ar[i];
	  if (!dc.remove(e))
	  {
	    throw new IllegalStateException("Remove said not removed");
	  }
	  dc.checkInvariants();
	}
      }
      if (TIME_INSDEL)
      {
	r = new Random(cseed);
	EuclidNode[] forC = new EuclidNode[size];
	EuclidNode[] forD = new EuclidNode[size];
	double[] pos = new double[dim];
	for (int i = 0; i < size; i++)
	{
	  for (int j = 0; j < pos.length; j++)
	  {
	    pos[j] = r.nextGaussian();
	  }
	  forC[i] = new EuclidNode(pos);
	  forD[i] = new EuclidNode(pos);
	}
	int[] remove = new int[forC.length / 4];
	for (int i = 0; i < remove.length; i++)
	{
	  remove[i] = r.nextInt(forC.length);
	}
	boolean[] removed = new boolean[remove.length];
	cLoad.start();
	Closest<EuclidNode> close = new Closest<EuclidNode>();
	for (int i = 0; i < size; i++)
	{
	  close.add(forC[i]);
	}
	for (int i = 0; i < remove.length; i++)
	{
	  removed[i] = close.remove(forC[remove[i]]);
	}
	cLoad.stop();
	dLoad.start();
	DynamicClosest<EuclidNode> dyn =
	  new DynamicClosest<EuclidNode>(poolSize);
	for (int i = 0; i < size; i++)
	{
	  dyn.add(forD[i]);
	}
	for (int i = 0; i < remove.length; i++)
	{
	  boolean wasRemoved = dyn.remove(forD[remove[i]]);
	  if (wasRemoved != removed[i])
	  {
	    throw new IllegalStateException("removed mismatch closest " +
	      removed[i] + " dyn " + wasRemoved);
	  }
	}
	dLoad.stop();
	for (int i = 0; i < inner; i++)
	{
	  EuclidNode query = forC[r.nextInt(size)];
	  cSearch.start();
	  List<EuclidNode> cList = close.findClosest(query, numToFind,
	    Double.MAX_VALUE);
	  cSearch.stop();
	  dSearch.start();
	  List<EuclidNode> dList = dyn.findClosest(query, numToFind,
	    Double.MAX_VALUE);
	  dSearch.stop();
	  int cSize = cList.size();
	  if (cSize != dList.size())
	  {
	    throw new IllegalStateException("List size mismatch: " +
	      cSize + ", " + dList.size());
	  }
	  for (int j = 0; j < cSize; j++)
	  {
	    EuclidNode cn = cList.get(j);
	    EuclidNode dn = dList.get(j);
	    for (int k = 0; k < dim; k++)
	    {
	      if (cn.getCoordinate(k) != dn.getCoordinate(k))
	      {
		throw new IllegalStateException("Position mismatch");
	      }
	    }
	  }
	}
      }
    }
    System.out.println("Closest load " + cLoad + " search " + cSearch);
    System.out.println("Dynamic load " + dLoad + " search " + dSearch);
  }
}
