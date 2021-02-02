package uk.co.demon.mcdowella.stats;
/**
 * This class contains a single static method which is used to find
 * an element in an array with a particular rank.
 */
public class GetRank
{
  /**
   * Re-order the array from v[first] to v[past-1] so that the item at
   * v[first+rank] has that rank.
   * rank must be between 0 and past-first-1.
   * After this run, all items in the first..past-1 section above the
   * returned index
   * are at least as large as the value at the index location returned.
   * All items in the first..past-1 section below it are not. 
   * Uses quicksort-like fat pivot algorithm
   * We expect to halve the range of permissible values each time, at a
   * cost linear the range of permissible values. The total cost is a
   * geometric series which ends up summing to a value linear in the size
   * of the array.
   * @param first the index of the first item in the array to be ranked
   * @param past the index of an item in the array just past the last
   * item to be ranked
   * @param rank the rank required, relative to the start of the array
   * @param v the array containing the stretch of items to be ranked
   */
  public static void forceRank(int first, int past, int rank, double v[])
  {
    if (rank < 0 || rank >= past-first)
      throw new ArrayIndexOutOfBoundsException("Bad rank to getrank");
    rank+=first; // make absolute
    for(;;)
    {
      int probe=(first+past)/2;  // Must be in range first..past-1. Probably better to chose
                                 // a random value within that range, although partially
                                 // sorted values would be all to the good here
      double val=v[probe];
      int nextlow=first; // first..nextlow-1 is area below probe value
      int nexthigh=past-1; // nexthigh+1..past-1 is area above probe value
                         // nextlow..i-1 is equal to probe value
                         // i..nexthigh is unknown
      // System.out.println("value is "+val);
      for(int i=first;i<=nexthigh;)
      {
        // System.out.println("Check value "+resid[i]);
        if(v[i]==val)
        { // This guarantees us at least some reduction!
          // otherwise trouble with non-standard floating point values
          i++;
        }
        else if(v[i]<val)
        {
          if(i>nextlow)
          {
            double t=v[nextlow];
            v[nextlow]=v[i];
            v[i]=t;
          }
          // if i was greater than nextlow, have now moved up a point which was
          // equal to the pivot. If not, i now points at a point < pivot value 
          nextlow++;
          i++;    
        }
        else
        {
          double t=v[nexthigh];
          v[nexthigh]=v[i];
          v[i]=t;
          nexthigh--;    
        }
      }
      // now we have first..nextlow-1, nextlow..nexthigh, nexthigh+1..past-1
      // System.out.println("Target "+rank+" first "+first+" past "+past+
      //                   " nextlow "+nextlow+" nexthigh "+nexthigh);
      if(rank<nextlow)
      {
        // result somewhere in first..nextlow-1
        past=nextlow;
      }
      else if(rank>nexthigh)
      { // result somewhere in nexthigh+1..last
        first=nexthigh+1;
      }
      else
      { // jackpot!
        break;
      }
    }
  }
}