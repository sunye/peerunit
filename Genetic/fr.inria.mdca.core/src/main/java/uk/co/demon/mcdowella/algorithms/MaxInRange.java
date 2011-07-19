package uk.co.demon.mcdowella.algorithms;

import java.util.Arrays;

/** This class keeps track of a fixed-length array of values, of length
 *  n. You can update a value at any time, at cost log n. Given a
 *  range within [0..n-1], it will return the index of the largest
 *  value within that range, again at cost log n. The store required
 *  is linear in n. This works by imposing a binary tree structure on
 *  the array.
 */
public class MaxInRange
{
  /** maxIndexes[i][j] holds the index of the maximum value within
   *  all the values whose indexes, shifted right by (i+1) positions,
   *  are j. Note that this requires a total of only linear store,
   *  because 1 + 2 + 4 + ... 2^n = 2^(n+1)
   */
  private final int[][] maxIndexes;
  /** The values themselves, unmodified */
  private long[] theValues;
  /** return the number of values held */
  public int getLength()
  {
    return theValues.length;
  }
  /** Create array given its size. Initialise all values to
   *  Long.MIN_VALUE
   */
  public MaxInRange(int length)
  {
    int bits = 0;
    for (int needed = length;;)
    {
      if (needed == 1)
      {
        break;
      }
      needed = (needed + 1) >> 1;
      bits++;
    }
    maxIndexes = new int[bits][];
    bits = 0;
    for (int needed = length;;)
    {
      if (needed == 1)
      {
        break;
      }
      needed = (needed + 1) >> 1;
      int[] ar = new int[needed];
      maxIndexes[bits++] = ar;
      for (int j = 0; j < needed; j++)
      {
        ar[j] = j << bits;
      }
    }
    theValues = new long[length];
    Arrays.fill(theValues, Long.MIN_VALUE);
  }
  /** reset the state of the values held */
  public void reset()
  {
    Arrays.fill(theValues, Long.MIN_VALUE);
  }
  /** set a value */
  public void set(int index, long value)
  {
    // set the value
    theValues[index] = value;
    if (maxIndexes.length <= 0)
    {
      return;
    }
    // now update the maximum index tree
    int other = index ^ 1;
    int pos = index >> 1;
    if (other < theValues.length)
    {
      if (theValues[other] >= value)
      {
	if (maxIndexes[0][pos] == other)
	{ // no need to change anything
	  return;
	}
	maxIndexes[0][pos] = other;
	index = other;
      }
      else
      {
	maxIndexes[0][pos] = index;
      }
    }
    else
    { // don't actually need to set value here, as already
      // has this value, but we do need to follow it up the tree
    }

    // Propagate changes up tree. We need to track the current
    // subtree maximum, at index, up the tree
    for (int bits = 1; bits < maxIndexes.length; bits++)
    {
      int oldPos = pos;
      other = oldPos ^ 1;
      pos = oldPos >> 1;
      int[] indexArray = maxIndexes[bits - 1];
      if (other < indexArray.length)
      {
        int otherIndex = indexArray[other];
	if (theValues[otherIndex] >= theValues[index])
	{
	  if (maxIndexes[bits][pos] == otherIndex)
	  {
	    return;
	  }
	  maxIndexes[bits][pos] = otherIndex;
	  index = otherIndex;
	}
	else
	{
	  maxIndexes[bits][pos] = index;
	}
      }
      else
      { // need to set one of several possible values from lowere
        // level, even if no comparison with peers required at this
	// level
        maxIndexes[bits][pos] = index;
      }
    }
  }
  /** get a value */
  public long get(int offset)
  {
    return theValues[offset];
  }
  /** return the index of the largest value with 
   *  first &le; index &lt; past 
   */
  public int getMaxIndex(int first, int past)
  {
    if ((first < 0) || (past > theValues.length) || (past <= first))
    { // no possible valid return
      throw new ArrayIndexOutOfBoundsException("silly arguments");
    }
    if (maxIndexes.length <= 0)
    {
      return 0;
    }
    // Now search through range, taking as big leaps as possible
    // Holds maximum index in range searched so far
    int sofar = first++;
    // Increase the step size here, zeroing out low order bits in first,
    // until the step gets too big.
    int bits = -1;
    int stepSize = 1;
    while ((first + stepSize) <= past)
    { // at this point first is a multiple of stepSize
      // and we go through this loop at most maxIndexes.length times
      // System.err.println("First = " + first);
      if ((first & stepSize) != 0)
      { // take this to zero out a low order bit in first
	if (bits < 0)
	{
	  // System.err.println("Check " + first + " against " + sofar);
	  if (theValues[first] > theValues[sofar])
	  {
	    sofar = first;
	  }
	}
	else
	{
	  int tryHere = maxIndexes[bits][first >> (bits + 1)];
	  // System.err.println("Check 2 " + tryHere + " against " +
	  //   sofar);
	  if (theValues[tryHere] > theValues[sofar])
	  {
	    sofar = tryHere;
	  }
	}
	first += stepSize;
      }
      bits++;
      stepSize = stepSize << 1;
    }
    // Here with first a multiple of stepSize, and first+stepSize
    // > past, so all we need to do to get to past is to add on
    // low order bits.
    bits--;
    stepSize = stepSize >> 1;
    while(first < past)
    { // at this point we are within 2 * stepSize of past, and we
      // go through this loop at most maxIndexes.length times
      // System.err.println("First = " + first);
      if ((past & stepSize) != 0)
      { // take this to set a low order bit in first
	if (bits < 0)
	{
	  // System.err.println("Check 3 " + first + " against " +
	  //   sofar);
	  if (theValues[first] > theValues[sofar])
	  {
	    sofar = first;
	  }
	}
	else
	{
	  int tryHere = maxIndexes[bits][first >> (bits + 1)];
	  // System.err.println("Check 4 " + tryHere + " against " +
	  //   sofar);
	  if (theValues[tryHere] > theValues[sofar])
	  {
	    sofar = tryHere;
	  }
	}
	first += stepSize;
      }
      bits--;
      stepSize = stepSize >> 1;
    }
    return sofar;
  }
  void dump()
  {
    for (int i = 0; i < theValues.length; i++)
    {
      System.out.print(i + ":" + theValues[i] + " ");
    }
    System.out.println();
    for (int i = 0; i < maxIndexes.length; i++)
    {
      int[] index = maxIndexes[i];
      for (int j = 0; j < index.length; j++)
      {
	int pos = index[j];
        System.out.print(pos + ":" + theValues[pos] + " ");
      }
      System.out.println();
    }
  }
  /** Return a long such that if a &lt; b as doubles, then
   *  toOrderedLong(a) &lt; toOrderedLong(b). (That is, we
   *  preserve relative order).
   *  Otherwise, the value returned may not bear any relation to
   *  its input. Throws an IllegalArgumentException for NaNs
   *  and Infinites
   */
  public static long toOrderedLong(double d)
  {
    if (Double.isNaN(d) || Double.isInfinite(d))
    {
      throw new IllegalArgumentException("Value is NaN or Infinite");
    }
    long l = Double.doubleToLongBits(d);
    // Now have l's bits. Java uses IEEE 754, which formats its
    // numbers as <sign><biased exponent><mantissa>. The biased
    // exponent means that the most -ve possible exponent is 0
    // which means that relative order is preserved amongst +ve
    // numbers (sign 0). For -ve numbers, we need to flip them the
    // other way, because 0x800000000.. represents -ve zero, which
    // should be mapped to -1, not the most negative possible number
    if (l < 0)
    {
      // l = 0x7fffffffffffff... - l.
      l = Long.MAX_VALUE - l;
    }
    return l;
  }
  /** reverse the mapping produced by toOrderedLong(). This is therefore
   *  order-preserving, but not every long will map to a sensible 
   *  double, because some places are taken up by NaN and Infinity.
   *  In fact, we throw an exception if these are produced.
   */
  public static double fromOrderedLong(long l)
  {
    if (l < 0)
    {
      l = Long.MAX_VALUE - l;
    }
    double d = Double.longBitsToDouble(l);
    if (Double.isNaN(d) || Double.isInfinite(d))
    {
      throw new IllegalArgumentException("NaN or Infinity produced");
    }
    return d;
  }
}
