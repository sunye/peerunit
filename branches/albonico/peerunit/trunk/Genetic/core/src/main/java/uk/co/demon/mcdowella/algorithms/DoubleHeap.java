package uk.co.demon.mcdowella.algorithms;

import java.util.Comparator;
import java.util.NoSuchElementException;

/** This class provides a generic double-ended heap: that is, a heap
 *  that allows access to both the highest and lowest entries in the
 *  heap at constant cost. See Knuth Vol III exercises 5.2.3 number
 *  31, and the supplied answer.
 *  <pre>
 *  The invariants are:
 *  upperHeap[i] >= upperHeap[2 * i + 1]
 *  upperHeap[i] >= upperHeap[2 * i + 2]
 *  lowerHeap[i] <= lowerHeap[2 * i + 1]
 *  lowerHeap[i] <= lowerHeap[2 * i + 2]
 *  (For leaves only: where 2 * i + 1 is outside the heap)
 *  lowerHeap[i] <= upperHeap[i]
 *  </pre>
 */
public class DoubleHeap<T>
{
  /**
   * used to create DoubleHeap of Comparable<S>, using default 
   * comparator
   */
  private static class DefaultComparator<S extends Comparable>
    implements Comparator<S>
  {
    public int compare(S a, S b)
    {
      return a.compareTo(b);
    }
  }
  /** return the number of elements stored */
  public int size()
  {
    return present;
  }
  /** return whether empty */
  public boolean isEmpty()
  {
    return present <= 0;
  }
  /** Create a heap capable of storing size items given a Comparator */
  // This suppression does not work with Java 1.5.0 because
  // suppression is not implemented there
  @SuppressWarnings("unchecked")
  public DoubleHeap(int size, Comparator<T> vc)
  {
    comp = vc;
    givenSize = size;
    upperHeap = (T[])new Object[size / 2];
    lowerHeap = (T[])new Object[upperHeap.length];
    present = 0;
  }
  /** Create a heap capable of storing size comparable items, using
   * the obvious default comparator
   */
  public static class DefaultDoubleHeap<X extends Comparable>
    extends DoubleHeap<X>
  {
    DefaultDoubleHeap(int size)
    {
      super(size, new DefaultComparator<X>());
    }
  }
  /** insert something into the heap
   * @exception IllegalStateException if heap full
   */
  public void add(T toInsert)
  {
    if (present >= givenSize)
    {
      throw new IllegalStateException("Too many items");
    }
    present++;
    if ((present & 1) == 1)
    {
      odd = toInsert;
      return;
    }
    int target = (present >> 1) - 1;
    if (comp.compare(odd, toInsert) < 0)
    { // toInsert > odd, so put it in upper heap
      upperHeap[target] = toInsert;
      lowerHeap[target] = odd;
    }
    else
    {
      upperHeap[target] = odd;
      lowerHeap[target] = toInsert;
    }
    // zap out odd to keep garbage collector happy
    odd = null;
    // restore invariant within upper, possibly reducing
    // value of upperHeap[target]
    heapUpperFromLeaf(target);
    // restore invariant within lower, possibly increasing
    // value of lowerHeap[target]
    heapLowerFromLeaf(target);
    balance(target);
  }
  /** Here with invariants OK except that if you look as the heaps
   *  as two binary trees placed back to back the path from root
   *  to root through leafChanged may be messed up with the two
   *  leaf elements correctly ordered wrt each other but possibly
   *  not wrt the rest of the path.
   */
  private void balance(int leafChanged)
  {
    heapUpperFromLeaf(leafChanged);
    heapLowerFromLeaf(leafChanged);
    // Now have both paths OK. If the two messed up nodes were each
    // in the right heap all is OK. If not, since they were OK wrt
    // each other one of them must be in the right heap and the other
    // is at the boundary, so we need to swap it through and reheap
    if (comp.compare(upperHeap[leafChanged],
      lowerHeap[leafChanged]) < 0)
    {
      T t = upperHeap[leafChanged];
      upperHeap[leafChanged] = lowerHeap[leafChanged];
      lowerHeap[leafChanged] = t;
      heapUpperFromLeaf(leafChanged);
      heapLowerFromLeaf(leafChanged);
    }
  }
  /** restore the heap invariant to the upper heap, starting from
   *  the leaf marked target, which is the only element out of order
   *  in that heap
   */
  private void heapUpperFromLeaf(int target)
  {
    while (target > 0)
    {
      int next = (target - 1) >> 1;
      if (comp.compare(upperHeap[next], upperHeap[target]) >= 0)
      {
        return;
      }
      T t = upperHeap[next];
      upperHeap[next] = upperHeap[target];
      upperHeap[target] = t;
      target = next;
    }
  }
  /** restore the heap invariant to the lower heap, starting from
   *  the leaf marked target, which is the only element out of order
   *  in that heap
   */
  private void heapLowerFromLeaf(int target)
  {
    while (target > 0)
    {
      int next = (target - 1) >> 1;
      if (comp.compare(lowerHeap[next], lowerHeap[target]) <= 0)
      {
        return;
      }
      T t = lowerHeap[next];
      lowerHeap[next] = lowerHeap[target];
      lowerHeap[target] = t;
      target = next;
    }
  }
  /** return the highest element in the heap 
   *  @exception NoSuchElementException if heap empty
   */
  public T last()
  {
    if (present <= 0)
    {
      throw new NoSuchElementException("Heap empty");
    }
    if (present == 1)
    {
      return odd;
    }
    T contendor = upperHeap[0];
    if ((present & 1) != 0)
    {
      if (comp.compare(odd, contendor) > 0)
      {
        return odd;
      }
    }
    return contendor;
  }
  /** return the lowest element in the heap
   *  @exception NoSuchElementException if heap empty
   */
  public T first()
  {
    if (present <= 0)
    {
      throw new NoSuchElementException("Heap empty");
    }
    if (present == 1)
    {
      return odd;
    }
    T contendor = lowerHeap[0];
    if ((present & 1) != 0)
    {
      if (comp.compare(odd, contendor) < 0)
      {
        return odd;
      }
    }
    return contendor;
  }
  /** remove top value from heap, returning it
   *  @exception NoSuchElementException if heap empty
   */
  public T removeLast()
  {
    if (present <= 0)
    {
      throw new NoSuchElementException("Heap empty");
    }
    if (present == 1)
    {
      T result = odd;
      odd = null;
      present = 0;
      return result;
    }
    T contendor = upperHeap[0];
    present--;
    if ((present & 1) == 0)
    { // odd is in use
      if (comp.compare(odd, contendor) >= 0)
      { // heaps themselves are unchanged
	T result = odd;
	// keep garbage-collector happy
	odd = null;
        return result;
      }
      upperHeap[0] = odd;
      // keep garbage-collector happy
      odd = null;
    }
    else
    { // we will bring odd into use
      int prevLast = present >> 1;
      upperHeap[0] = upperHeap[prevLast];
      odd = lowerHeap[prevLast];
      // keep garbage-collector happy
      upperHeap[prevLast] = null;
      lowerHeap[prevLast] = null;
    }
    if (present > 1)
    {
      int leafChanged = heapUpperFromRoot();
      if (leafChanged >= 0)
      {
	balance(leafChanged);
      }
    }
    return contendor;
  }
  /** remove lowest item from heap and return it
   *  @exception NoSuchElementException if heap empty
   */
  public T removeFirst()
  {
    if (present <= 0)
    {
      throw new NoSuchElementException("Heap empty");
    }
    if (present == 1)
    {
      T result = odd;
      odd = null;
      present = 0;
      return result;
    }
    T contendor = lowerHeap[0];
    present--;
    if ((present & 1) == 0)
    { // odd is in use
      if (comp.compare(odd, contendor) <= 0)
      { // heaps themselves are unchanged
	T result = odd;
	odd = null;
        return result;
      }
      lowerHeap[0] = odd;
      odd = null;
    }
    else
    { // we will bring odd into use
      int prevLast = present >> 1;
      lowerHeap[0] = lowerHeap[prevLast];
      odd = upperHeap[prevLast];
      lowerHeap[prevLast] = null;
      upperHeap[prevLast] = null;
    }
    if (present > 1)
    {
      int leafChanged = heapLowerFromRoot();
      if (leafChanged >= 0)
      {
	balance(leafChanged);
      }
    }
    return contendor;
  }
  /** Fix upper heap with possibly duff root element, returning
   *  index of changed leaf, if any. This changes only a single
   *  path down from the root
   */
  private int heapUpperFromRoot()
  {
    int past = present >> 1;
    for (int here = 0;;)
    {
      int next = here + here + 1;
      if (next >= past)
      { // reached leaf
        return here;
      }
      int next1 = next + 1;
      if (next1 < past)
      {
        if (comp.compare(upperHeap[next], upperHeap[next1]) < 0)
	{
	  next = next1;
	}
      }
      if (comp.compare(upperHeap[here], upperHeap[next]) >= 0)
      { // patched up without disturbing leaf
        return -1;
      }
      T t = upperHeap[here];
      upperHeap[here] = upperHeap[next];
      upperHeap[next] = t;
      here = next;
    }
  }
  /** Fix lower heap with possibly duff root element, returning
   *  index of changed leaf, if any. This changes only a single
   *  path down from the root
   */
  private int heapLowerFromRoot()
  {
    int past = present >> 1;
    for (int here = 0;;)
    {
      int next = here + here + 1;
      if (next >= past)
      { // reached leaf
        return here;
      }
      int next1 = next + 1;
      if (next1 < past)
      {
        if (comp.compare(lowerHeap[next], lowerHeap[next1]) > 0)
	{
	  next = next1;
	}
      }
      if (comp.compare(lowerHeap[here], lowerHeap[next]) <= 0)
      { // patched up without disturbing leaf
        return -1;
      }
      T t = lowerHeap[here];
      lowerHeap[here] = lowerHeap[next];
      lowerHeap[next] = t;
      here = next;
    }
  }
  /** size requested on creation */
  private int givenSize;
  /** number of items present */
  private int present;
  /** upperHeap[0] or odd is the largest item present */
  private T[] upperHeap;
  /** lowerHeap[0] or odd is the smallest item present */
  private T[] lowerHeap;
  /** holds extra item if odd number of elements present */
  private T odd;
  private Comparator<T> comp;
}
