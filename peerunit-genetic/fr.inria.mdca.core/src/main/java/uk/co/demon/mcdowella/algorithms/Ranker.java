package uk.co.demon.mcdowella.algorithms;

/** This interface defines an array of fixed size that makes it
 *  easy to address the contents by rank. Ties are broken by
 *  considering the relative positions of the values in the
 *  array.
 */
public interface Ranker
{
  /** return number of slots in array */
  int getSize();
  /** set value in array */
  long set(int pos, long newValue);
  /** get rank given slot in array */
  int getRank(int pos);
  /** read values from array by position */
  void getValuesByPos(int startPos, int num, long[] writeHere);
  /** get value by rank */
  long getValueByRank(int rank);
  /** get position by rank */
  int getPosByRank(int rank);
  /** get position of smallest value >= value */
  int getPosGe(long value);
}
