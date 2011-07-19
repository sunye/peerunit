package uk.co.demon.mcdowella.algorithms;

import java.util.Arrays;

/** This class is used by set coverage. It keeps track of which
    of a set of ints given to it during construction are currently
    in or out of the set, and provides an iterator over the in
    elements. The ints should be small numbers >= 0.
    */
public class InOutSet
{
  /** array of ints showing whether any item is in or out */
  private final boolean isIna[];
  /** array of ints giving next in doubly linked list */
  private final int next[];
  /** array of ints giving prev in doubly linked list */
  private final int prev[];
  /** number in */
  private int numIn;
  /** return number in */
  public int getNumIn()
  {
    return numIn;
  }
  /** construct given array of ints to keep track of. After
    construction everything is selected */
  public InOutSet(int[] members)
  {
    int max = 0;
    for (int x: members)
    {
      if (x > max)
      {
        max = x;
      }
    }
    isIna = new boolean[max + 1];
    next = new int[max + 1];
    prev = new int[max + 1];
    try
    {
      for (int x: members)
      {
	putIn(x);
      }
    }
    catch (IllegalArgumentException ia)
    { // here if set has repeats
      throw new IllegalArgumentException("Possible repeats in " +
        Arrays.toString(members), ia);
    }
  }
  public boolean isIn(int x)
  {
    return isIna[x];
  }
  /** put in */
  public void putIn(int num)
  {
    if (isIna[num])
    {
      // Assume this is an error for now
      throw new IllegalArgumentException("Already in");
      // return;
    }
    numIn++;
    isIna[num] = true;
    next[num] = head;
    prev[num] = -1;
    if (head >= 0)
    {
      prev[head] = num;
    }
    head = num;
  }
  /** take out */
  public void takeOut(int num)
  {
    if (!isIna[num])
    {
      // Assume this is an error for now
      throw new IllegalArgumentException("Already out");
      // return;
    }
    isIna[num] = false;
    numIn--;
    int p = prev[num];
    if (p >= 0)
    {
      next[p] = next[num];
    }
    else
    {
      head = next[num];
    }
    int n = next[num];
    if (n >= 0)
    {
      prev[n] = prev[num];
    }
  }
  /** Head of list of in links */
  private int head = -1;
  /** so we can provide callbacks showing which numbers are in */
  public interface PerMember
  {
    void callback(int here);
  }
  /** ask for callbacks showing what is active. Can cope if
      the callee deletes itself from the list. */
  public void callMe(PerMember pm)
  {
    int current = head;
    for (;;)
    {
      if (current < 0)
      {
        return;
      }
      int n = next[current];
      pm.callback(current);
      current = n;
    }
  }
}
