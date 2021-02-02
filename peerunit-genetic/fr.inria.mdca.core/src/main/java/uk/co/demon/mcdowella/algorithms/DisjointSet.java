package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/** Class for disjoint set problem aka equivalence problem. Uses
 *  union by rank and path compression.
 */
public class DisjointSet
{
  /** up pointer or -1 if at top of tree for set */
  private final int[] up;
  /** rank if at top of tree. This is an upper bound on the length
   *  of the longest path from any leaf to the root
   */
  private final int[] rank;
  /** create Disjoint set structure for n items, numbered 0..n */
  public DisjointSet(int n)
  {
    up = new int[n];
    Arrays.fill(up, -1);
    rank = new int[n];
  }
  /** Find number of set containing k */
  public int getSetNumber(int k)
  {
    int result = k;
    // find
    for (;;)
    {
      int x = up[result];
      if (x < 0)
      {
        break;
      }
      result = x;
    }
    // compress path
    int here = k;
    for (;;)
    {
      int x = up[here];
      if (x < 0)
      {
        break;
      }
      up[here] = result;
      here = x;
    }
    return result;
  }
  /** Merge sets containing x and y */
  public void merge(int x, int y)
  {
    int rx = getSetNumber(x);
    int ry = getSetNumber(y);
    if (rx == ry)
    {
      return;
    }
    int kx = rank[rx];
    int ky = rank[ry];
    if (kx < ky)
    { // rank[x] < rank[y] so making x a child of y does not
      // increase the rank of y
      up[rx] = ry;
      return;
    }
    if (kx > ky)
    {
      up[ry] = rx;
      return;
    }
    // might increase rank. Joining two trees with the same
    // rank so we preserve the invariant that the size of
    // the tree is at least 2^rank.
    up[rx] = ry;
    // identifier returned by search is now ry
    rank[ry]++;
  }
  /** Test code - slow */
  private static class SlowDj
  {
    private final int[] code;
    private SlowDj(int n)
    {
      code = new int[n];
      for (int i = 0; i < n; i++)
      {
	code[i] = i;
      }
    }
    private int getSetNumber(int k)
    {
      return code[k];
    }
    private void merge(int x, int y)
    {
      int newValue = code[x];
      int oldValue = code[y];
      if (newValue == oldValue)
      {
        return;
      }
      for (int i = 0; i < code.length; i++)
      {
        if (code[i] == oldValue)
	{
	  code[i] = newValue;
	}
      }
    }
  }
  public static void main(String[] s)
  {
    int size = 100;
    int goes = 100000;
    long seed = 42;
    int[] space = new int[size];
    int[] slowSpace = new int[size];
    for (int i = 0; i < goes; i++)
    {
      Random r = new Random(seed + i);
      // make next choice more random
      r.nextLong();
      int sizeHere = r.nextInt(size);
      System.out.println("Go " + i + " size " + sizeHere);
      DisjointSet ds = new DisjointSet(sizeHere);
      SlowDj sd = new SlowDj(sizeHere);
      for (int j = 0; j < sizeHere; j++)
      {
        if (ds.getSetNumber(j) != j)
	{
	  throw new IllegalStateException("bad initial state");
	}
      }
      if (sizeHere <= 0)
      {
        continue;
      }
      int combs = r.nextInt(sizeHere);
      for (int j = 0; j < combs; j++)
      {
	int a = r.nextInt(sizeHere);
	int b = r.nextInt(sizeHere);
	// System.out.println("Merge " + a + " and " + b);
        ds.merge(a, b);
	sd.merge(a, b);
      }
      for (int j = 0; j < sizeHere; j++)
      {
        space[j] = ds.getSetNumber(j);
	// System.out.println("id of " + j + " is " + space[j]);
        slowSpace[j] = sd.getSetNumber(j);
	// System.out.println("slow id of " + j + " is " + slowSpace[j]);
      }
      for (int j = 0; j < sizeHere; j++)
      {
        for (int k = 0; k < j; k++)
	{
	  if ((space[k] == space[j]) !=
	      (slowSpace[k] == slowSpace[j]))
	  {
	    throw new IllegalStateException("comparison mismatch");
	  }
	}
      }
    }
  }
}
