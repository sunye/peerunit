package uk.co.demon.mcdowella.algorithms;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;

/** This class implements a fixed size updateable array of longs
 *  from which items can be retrieved by rank in sorted order as
 *  well as by position. It can also produce the rank of the associated
 *  value given as a position. Based on a Patricia tree, so operations
 *  shouldn't be unreasonably expensive.
 */
public class ByRank implements Ranker
{
  /** values in array */
  final PatKey[] values;
  /** Class for Patricia Key: long then sequence */
  private static class PatKey implements 
    PatriciaGeneric.KeyInterface<PatKey>
  {
    /** info from parent */
    private DInfo parentInfo;
    /** value currently held */
    private long val;
    /** value currently held changed so bit order is same as long 
      order */
    private long remappedVal = Long.MIN_VALUE;
    /** position in array */
    private final int pos;
    /** create given sequence number */
    PatKey(int p)
    {
      pos = p;
    }
    public int getBit(long offset)
    {
      if (offset < 64)
      { // get bit from long
        return (int)(1 & (remappedVal >> (63l - offset)));
      }
      offset -= 64;
      return 1 & (pos >> (31l - offset));
    }
    public long firstDifference(PatKey compareWith)
    {
      if (remappedVal != compareWith.remappedVal)
      {
        long diff = remappedVal ^ compareWith.remappedVal;
	return Long.numberOfLeadingZeros(diff);
      }
      if (pos != compareWith.pos)
      {
        return 64 + Integer.numberOfLeadingZeros(pos ^
	  compareWith.pos);
      }
      return -1;
    }
    @Override
    public boolean equals(Object o)
    {
      if (!(o instanceof PatKey))
      {
        return false;
      }
      PatKey po = (PatKey) o;
      if (po.val != val)
      {
        return false;
      }
      return (po.pos == pos);
    }
    @Override
    public int hashCode()
    {
      int c = (int)(val >> 32l);
      c = c * 131 + (int)val;
      c = c * 131 + (int)pos;
      return c;
    }
  }
  /** Child structure, keeping track of number of children below it */
  private static class DInfo implements
    PatriciaGeneric.ChildInfo<PatKey, PatKey, DInfo>
  {
    /** parent info */
    DInfo parentInfo;
    /** number of key children below this node */
    private int numChildren;
    /** largest child below this node */
    private long largestChild;
    public boolean leafUpdate(PatKey k1, PatKey v1,
      PatKey k2, PatKey v2)
    {
      k1.parentInfo = this;
      k2.parentInfo = this;
      long largest = k1.val;
      if (largest < k2.val)
      {
        largest = k2.val;
      }
      boolean changed = false;
      if (numChildren != 2)
      {
        changed = true;
	numChildren = 2;
      }
      if (largest != largestChild)
      {
	changed = true;
        largestChild = largest;
      }
      return !changed;
    }
    public boolean mixedUpdate(PatKey k1, PatKey v1, DInfo pc)
    {
      k1.parentInfo = this;
      pc.parentInfo = this;
      int newValue = pc.numChildren + 1;
      boolean changed = false;
      if (newValue != numChildren)
      {
        numChildren = newValue;
	changed = true;
      }
      long largestChildHere = k1.val;
      if (largestChildHere < pc.largestChild)
      {
        largestChildHere = pc.largestChild;
      }
      if (largestChildHere != largestChild)
      {
        largestChild = largestChildHere;
	changed = true;
      }
      return !changed;
    }
    public boolean internalUpdate(DInfo pc1, DInfo pc2)
    {
      pc1.parentInfo = this;
      pc2.parentInfo = this;
      int newValue = pc1.numChildren + pc2.numChildren;
      boolean changed = false;
      if (newValue != numChildren)
      {
	changed = true;
	numChildren = newValue;
      }
      long largestChildHere = pc1.largestChild;
      if (largestChildHere < pc2.largestChild)
      {
        largestChildHere = pc2.largestChild;
      }
      if (largestChildHere != largestChild)
      {
        largestChild = largestChildHere;
	changed = true;
      }
      return !changed;
    }
  }
  /** create children */
  public static class ChildFactory implements
    PatriciaGeneric.InfoFactory<DInfo>
  {
    public DInfo create()
    {
      return new DInfo();
    }
  }
  private static ChildFactory childFactory = new ChildFactory();
  /** Map from Key to itself */
  private final PatriciaGeneric<PatKey, PatKey, DInfo>
    keyByKey = new
      PatriciaGeneric<PatKey, PatKey, DInfo>(childFactory);
  /** Create an array of zeros */
  public ByRank(int len)
  {
    values = new PatKey[len];
    for (int i = 0; i < len; i++)
    {
      values[i] = new PatKey(i);
      keyByKey.put(values[i], values[i]);
    }
  }
  /** return the size of the array */
  public int getSize()
  {
    return values.length;
  }
  /** set the value at the specified position. Returns the
   *  previous value 
   */
  public long set(int pos, long newValue)
  {
    PatKey k = values[pos];
    long prev = k.val;
    keyByKey.remove(k);
    k.val = newValue;
    // Want bit string order to match long order, so flip
    // the top bit to put -ves below +ves
    newValue ^= Long.MIN_VALUE;
    k.remappedVal = newValue;
    keyByKey.put(k, k);
    return prev;
  }
  /** get the rank of the value at the specified slot. Values
   *  are numbered starting from 0, with the relative positions
   *  in the array used to break ties.
   */
  public int getRank(int pos)
  {
    int rankSoFar = 0;
    // Code pinched from search
    PatriciaGeneric.Node<PatKey, PatKey, DInfo> p =
      keyByKey.getHead();
    PatKey k = values[pos];
    for(PatriciaGeneric.Node<PatKey, PatKey, DInfo> q = p.next(k);
      q != null; q = p.next(k))
    {
      if (q == p.lastChild())
      {
	PatriciaGeneric.Node<PatKey, PatKey, DInfo> left =
	  p.firstChild();
	if (left != null)
	{
	  DInfo di = left.getDescendantInfo();
	  if (di == null)
	  { // key
	    rankSoFar++;
	  }
	  else
	  {
	    rankSoFar += di.numChildren;
	  }
	}
      }
      p = q;
    }
    return rankSoFar;
  }
  /** get the values starting at a given position */
  public void getValuesByPos(int startPos, int num, long[] writeHere)
  {
    for (int i = 0; i < num; i++)
    {
      writeHere[i] = values[startPos++].val;
    }
  }
  /** Get a value by rank */
  public long getValueByRank(int rank)
  {
    return values[getPosByRank(rank)].val;
  }
  /** Get position by rank */
  public int getPosByRank(int rank)
  {
    // Work down from root, skipping left children when they have
    // descendants <= rank
    PatriciaGeneric.Node<PatKey, PatKey, DInfo> n = keyByKey.getHead();
    for (;;)
    {
      DInfo di = n.getDescendantInfo();
      if (di == null)
      { // node is leaf node
        if (rank != 0)
	{
	  throw new ArrayIndexOutOfBoundsException(rank);
	}
	return n.getValue().pos;
      }
      PatriciaGeneric.Node<PatKey, PatKey, DInfo> fn = n.firstChild();
      DInfo dic = fn.getDescendantInfo();
      int childrenHere;
      if (dic == null)
      { // leaf node
        childrenHere = 1;
      }
      else
      {
        childrenHere = dic.numChildren;
      }
      if (childrenHere <= rank)
      {
        rank -= childrenHere;
	n = n.lastChild();
      }
      else
      {
        n = fn;
      }
    }
  }
  /** get position of smallest value >= value */
  public int getPosGe(long value)
  {
    // Work down from root, going left when we can and right when
    // we must
    PatriciaGeneric.Node<PatKey, PatKey, DInfo> n = keyByKey.getHead();
    for (;;)
    {
      DInfo di = n.getDescendantInfo();
      if (di == null)
      { // node is leaf node
	PatKey keyHere = n.getKey();
	if (keyHere.val >= value)
	{
	  return keyHere.pos;
	}
	// not found
	return values.length;
      }
      PatriciaGeneric.Node<PatKey, PatKey, DInfo> fn = n.firstChild();
      DInfo dic = fn.getDescendantInfo();
      if (dic == null)
      {
	if (fn.getKey().val >= value)
	{ // can go left to matching key
	  n = fn;
	  continue;
	}
      }
      else
      {
	if (dic.largestChild >= value)
	{ // can go left down into subtree
	  n = fn;
	  continue;
	}
      }
      PatriciaGeneric.Node<PatKey, PatKey, DInfo> other =
        n.lastChild();
      if (other != null)
      {
        n = other;
      }
      else
      {
        n = fn;
      }
    }
  }
  
  /** Create with all-zero contents and use to test what we can */
  private static void testCreated(int size, Ranker br)
  {
    if (br.getSize() != size)
    {
      throw new IllegalStateException("Size mismatch");
    }
    if (size == 0)
    {
      return;
    }
    long[] v = new long[1];
    for (int i = 0; i < size; i++)
    {
      if (br.getRank(i) != i)
      {
        throw new IllegalStateException("Getrank mismatch size " + size + " i " + i);
      }
      br.getValuesByPos(i, 1, v);
      if (v[0] != 0)
      {
        throw new IllegalStateException("getValueByPos mismatch size " + size +
	  " i " + i);
      }
      long vi = br.getValueByRank(i);
      if (vi != 0)
      {
        throw new IllegalStateException("getValueByRank mismatch size " + size +
	  " i " + i + " returned " + vi);
      }
    }
  }
  /** dump out info */
  private static void dump(Ranker r1)
  {
    long[] info = new long[r1.getSize()];
    r1.getValuesByPos(0, info.length, info);
    for (int i = 0; i < info.length; i++)
    {
      int rank = r1.getRank(i);
      System.out.println("pos " + i + " value " + info[i] + " rank " +
        rank);
    }
    if (r1 instanceof ByRank)
    {
      ByRank br = (ByRank)r1;
      for (Map.Entry<PatKey, PatKey> me: br.keyByKey)
      {
        PatKey pk = me.getKey();
	System.out.println("Value " + pk.val + " pos " + pk.pos +
	  " ranker " + pk.remappedVal);
      }
    }
  }
  private static final Swatch firstTime = new Swatch();
  private static final Swatch secondTime = new Swatch();
  /** test two implementations against each other */
  private static void testAgainst(Ranker r1, Ranker r2, int passes,
    long seed)
  {
    int size = r1.getSize();
    if (r2.getSize() != size)
    {
      throw new IllegalStateException("Size mismatch");
    }
    if (size <= 0)
    {
      return;
    }
    Random r = new Random(seed);
    long[] values1 = new long[10];
    long[] values2 = new long[10];
    for (int i = 0; i < passes; i++)
    {
      for (int j = 0; j < 10; j++)
      {
	int p = r.nextInt(size);
	long v;
	if (r.nextInt(2) == 0)
	{
	  v = r.nextInt(size);
	}
	else
	{
	  v = r.nextLong();
	}
	firstTime.start();
        long l1 = r1.set(p, v);
	firstTime.stop();
	secondTime.start();
	long l2 = r2.set(p, v);
	secondTime.stop();
	if (l1 != l2)
	{
	  throw new IllegalStateException("Mismatch in set");
	}
      }
      /*
      System.out.println("R1");
      dump(r1);
      System.out.println("R2");
      dump(r2);
      */
      for (int j = 0; j < 10; j++)
      {
        int p = r.nextInt(size);
	firstTime.start();
	int rank = r1.getRank(p);
	firstTime.stop();
	secondTime.start();
	int secondRank = r2.getRank(p);
	secondTime.stop();
	if (rank != secondRank)
	{
	  throw new IllegalStateException("MisMatch in getRank");
	}
	firstTime.start();
	int posBack = r1.getPosByRank(rank);
	firstTime.stop();
	if (posBack != p)
	{
	  throw new IllegalStateException("MisMatch in backPos");
	}
	secondTime.start();
	posBack = r2.getPosByRank(rank);
	secondTime.stop();
	if (posBack != p)
	{
	  throw new IllegalStateException("MisMatch in backPos");
	}
	int room = size - p;
	if (room > values1.length)
	{
	  room = values1.length;
	}
	firstTime.start();
	r1.getValuesByPos(p, room, values1);
	firstTime.stop();
	secondTime.start();
	r2.getValuesByPos(p, room, values2);
	secondTime.stop();
	if (!Arrays.equals(values1, values2))
	{
	  throw new IllegalStateException("MisMatch in getValuesByPos");
	}
	firstTime.start();
	long r1v = r1.getValueByRank(rank);
	firstTime.stop();
	if (r1v != values1[0])
	{
	  throw new IllegalStateException(
	    "MisMatch in getValueByRank1");
	}
	secondTime.start();
	long r2v = r2.getValueByRank(rank);
	secondTime.stop();
	if (r2v != values1[0])
	{
	  throw new IllegalStateException(
	    "MisMatch in getValueByRank2");
	}
	long ammended = r1v ^ r.nextInt(4);
	firstTime.start();
	int p1 = r1.getPosGe(ammended);
	firstTime.stop();
	if (p1 != size)
	{
	  // don't time this as don't bother on other side
	  r1.getValuesByPos(p1, 1, values1);
	  if (values1[0] < ammended)
	  {
	    dump(r1);
	    System.out.println("Pos for " + ammended + " was " + p1);
	    throw new IllegalStateException(
	      "Mismatch in getPosGe back check");
	  }
	}
	secondTime.start();
	int p2 = r2.getPosGe(ammended);
	secondTime.stop();
	if (p1 != p2)
	{
	  throw new IllegalStateException("Ge mismatch");
	}
      }
    }
  }
  public static void main(String[] s)
  {
    long seed = 42;
    for (int i = 0; i < 1000; i++)
    {
      System.out.println("i = " + i);
      ByRank br = new ByRank(i);
      testCreated(i, br);
      SlowRanker sr = new SlowRanker(i);
      testCreated(i, sr);
      testAgainst(br, sr, 100, seed + i);
    }
    System.out.println("First Time " + firstTime);
    System.out.println("Second Time " + secondTime);
  }
  /** Class that provides the same functionality, but more slowly,
   *  for testing 
   */
  private static class SlowRanker implements Ranker
  {
    /** Class to keep track of values and slots */
    private static class ValuePos implements Comparable<ValuePos>
    {
      /** position in main array */
      private final int pos;
      /** value */
      private long val;
      /** rank in sorted array */
      private int rank;
      ValuePos(int p)
      {
        pos = p;
      }
      public int compareTo(ValuePos vp)
      {
        if (val < vp.val)
	{
	  return -1;
	}
	if (val > vp.val)
	{
	  return 1;
	}
	if (pos < vp.pos)
	{
	  return -1;
	}
	if (pos > vp.pos)
	{
	  return 1;
	}
	return 0;
      }
    }
    /** values */
    private final ValuePos[] values;
    /** values in sortedOrder */
    private final ValuePos[] sortedValues;
    /** whether sorted */
    private boolean isSorted = true;
    SlowRanker(int size)
    {
      values = new ValuePos[size];
      sortedValues = new ValuePos[size];
      for (int i = 0; i < size; i++)
      {
	ValuePos vp = new ValuePos(i);
	vp.rank = i;
        values[i] = vp;
	sortedValues[i] = vp;
      }
    }
    /** return number of slots in array */
    public int getSize()
    {
      return values.length;
    }
    /** sort if required */
    private void sort()
    {
      if (isSorted)
      {
        return;
      }
      Arrays.sort(sortedValues);
      for (int i = 0; i < sortedValues.length; i++)
      {
        sortedValues[i].rank = i;
      }
      isSorted = true;
    }
    /** set value in array */
    public long set(int pos, long newValue)
    {
      long ret = values[pos].val;
      if (ret == newValue)
      {
        return ret;
      }
      isSorted = false;
      values[pos].val = newValue;
      return ret;
    }
    /** get rank given slot in array */
    public int getRank(int pos)
    {
      sort();
      return values[pos].rank;
    }
    /** read values from array by position */
    public void getValuesByPos(int startPos, int num, long[] writeHere)
    {
      for (int i = 0; i < num; i++)
      {
        writeHere[i] = values[startPos++].val;
      }
    }
    /** get value by rank */
    public long getValueByRank(int rank)
    {
      sort();
      return sortedValues[rank].val;
    }
    /** get position by rank */
    public int getPosByRank(int rank)
    {
      sort();
      return sortedValues[rank].pos;
    }
    public int getPosGe(long value)
    {
      sort();
      int first = 0;
      int past = values.length;
      int returnSoFar = past;
      for (;;)
      { // Here to look for match in [first, past)
        // or just return returnSoFar
	if (first >= past)
	{ // nothing in range
	  return returnSoFar;
	}
	if (first == (past + 1))
	{ // just one option in range
	  if (sortedValues[first].val >= value)
	  {
	    return sortedValues[first].pos;
	  }
	  return returnSoFar;
	}
	// past is > first + 1 so probe is neither
	// first nor past
	int probe = (first + past) / 2;
	ValuePos probed = sortedValues[probe];
	if (probed.val >= value)
	{ // can always go back and answer from return probe
	  // will shorten area searched
	  past = probe;
	  returnSoFar = probed.pos;
	  continue;
	}
	// will shortened area searched
	first = probe + 1;
      }
    }
  }
}
