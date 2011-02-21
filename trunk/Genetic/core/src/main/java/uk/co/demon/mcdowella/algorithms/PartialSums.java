package uk.co.demon.mcdowella.algorithms;

/** The following class allows you to hold an array of some fixed
 *  number of doubles, and efficently query it for partial sums.
 *  The operations of altering an array element, and of working out
 *  any partial sum, both take time logarithmic in the number of
 *  elements in the array. This is achieved by keeping track of
 *  the sums of pairs, quadruples, 8-somes, 16-somes, and so on.
 *  The initial contents of the array are zero. We store doubles,
 *  but note that Java doubles will represent integers of up to
 *  2^48 or so exactly, since we only ever use addition and
 *  subtraction (and if you restrict yourself to get, put
 *  and getSumBefore(), we don't even use subtraction).
 */
public class PartialSums
{
  /** These arrays keep track of the partial sums, and also the
   *  array elements themselves. sum[i][j] keeps the sum of the
   *  2^i elements starting at j*2^i.
   */
  private double[][] sums;
  PartialSums(int len)
  {
    if (len == 0)
    {
      sums = new double[0][];
      return;
    }
    if (len < 0)
    {
      throw new IllegalArgumentException("Len must be >= 0");
    }
    int numArrays = 1;
    int maxSize = 1;
    while (maxSize < len)
    {
      maxSize += maxSize;
      numArrays++;
    }
    sums = new double[numArrays][];
    for (int i = 0; i < sums.length; i++)
    {
      sums[i] = new double[len];
      len = (len + 1) >> 1;
    }
  }
  public int getLength()
  {
    if (sums.length == 0)
    {
      return 0;
    }
    return sums[0].length;
  }
  /**
   * Retrieve the value at the given index. Java will thrown an
   * exception here if the index is out of range
   */
  public double get(int index)
  {
    return sums[0][index];
  }
  /**
   * Set the value at the given index. Java will throw an exception
   * here if the index is out of range
   */
  public void put(int index, double value)
  {
    int pos = 0;
    sums[pos][index] = value;
    for (;;)
    {
      int newPos = pos + 1;
      if (newPos >= sums.length)
      {
        break;
      }
      int newIndex = index >> 1;
      int other = index ^ 1;
      if (other < sums[pos].length)
      {
        value += sums[pos][other];
      }
      sums[newPos][newIndex] = value;
      pos = newPos;
      index = newIndex;
    }
  }
  /** Return the sum of elements from element 0 up to but not
   * including the value given by past, which is the same thing as 
   * saying that it returns the sum of the first past elements.
   * Should not throw an exception for
   * any value of past.
   */
  public double getSumBefore(int past)
  {
    if (past <= 0)
    {
      return 0.0;
    }
    if (sums.length == 0)
    {
      return 0.0;
    }
    int line = sums.length - 1;
    if (past >= sums[0].length)
    {
      return sums[line][0];
    }
    // Here => we want the sum of some chunk that is not as big
    // as the entire array but contains at least 1 element
    line--;
    int pastCovered = 0;
    int pos = 0;
    double sofar = 0.0;
    int chunkSize = 1 << line;
    for (;;)
    {
      int newCovered = pastCovered + chunkSize;
      if (newCovered <= past)
      {
        sofar += sums[line][pos];
	if (newCovered == past)
	{
	  return sofar;
	}
	pastCovered = newCovered;
	pos++;
      }
      pos += pos;
      line--;
      chunkSize = chunkSize >> 1;
    }
  }
  /** Convenience routine to get the sum of elements in any
   *  contiguous interval, starting with first and ending just
   *  before past
   */
  public double sumInterval(int first, int past)
  {
    if (first >= past)
    {
      return 0.0;
    }
    return getSumBefore(past) - getSumBefore(first);
  }
}
