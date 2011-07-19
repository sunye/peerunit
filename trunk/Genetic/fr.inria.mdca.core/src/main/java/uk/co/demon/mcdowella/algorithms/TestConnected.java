package uk.co.demon.mcdowella.algorithms;

import java.util.Arrays;
import java.util.Random;

/** Test harness for Connected Components class */
public class TestConnected
{
  /** shuffle an int[] */
  static void shuffle(int[] ai, Random r)
  {
    if (ai.length <= 1)
    {
      return;
    }
    for (int i = ai.length - 1; i > 0; i--)
    {
      // Pick random element for ai[i]
      int from = r.nextInt(i + 1);
      // and swap in
      int t = ai[from];
      ai[from] = ai[i];
      ai[i] = t;
    }
  }
  /** Generate a single connected component at random, making
   *  connectins within this component and to components below
   *  it.
   */
  static void fillIn(int[][] requires, int first, int past,
    Random r)
  {
    int available = past - first;
    if (available <= 0)
    {
      return;
    }
    if (available == 1)
    {
      if (r.nextDouble() < 0.5)
      {
        requires[first] = null;
      }
      else
      {
        requires[first] = new int[] {first};
      }
      return;
    }
    int p1 = past - 1;
    for (int i = first; i < past; i++)
    {
      int[] here = new int[1 + r.nextInt(available)];
      // first item is cyclic, ensuring connected
      if (i >= p1)
      {
        here[0] = first;
      }
      else
      {
	here[0] = i + 1;
      }
      for (int j = 1; j < here.length; j++)
      {
        // here[j] = first + r.nextInt(available);
        here[j] = r.nextInt(past);
      }
      shuffle(here, r);
      requires[i] = here;
    }
  }
  public static void main(String[] s)
  {
    long seed = 42;
    int goes = 100;
    int maxGroupLen = 20;
    int maxGroups = 10;
    int s1 = s.length - 1;
    boolean trouble = false;
    boolean verbose = false;
    int argp = 0;

    try
    {
      for (;argp < s.length; argp++)
      {
        if ((argp < s1) && "-goes".equals(s[argp]))
	{
	  goes = Integer.parseInt(s[++argp].trim());
	}
        else if ((argp < s1) && "-groups".equals(s[argp]))
	{
	  maxGroups = Integer.parseInt(s[++argp].trim());
	}
        else if ((argp < s1) && "-len".equals(s[argp]))
	{
	  maxGroupLen = Integer.parseInt(s[++argp].trim());
	}
        else if ((argp < s1) && "-seed".equals(s[argp]))
	{
	  seed = Long.parseLong(s[++argp].trim());
	}
	else if ("-verbose".equals(s[argp]))
	{
	  verbose = true;
	}
	else
	{
	  System.err.println("Could not handle flag " + s[argp]);
	  trouble = true;
	}
      }
    }
    catch (NumberFormatException nfe)
    {
      System.err.println("Could not read number in " + s[argp]);
      trouble = true;
    }

    if (trouble)
    {
      System.err.println("Args are [-goes #] [-groups #] [-len #] " +
        "[-seed #]");
      return;
    }

    System.out.println("Goes " + goes + " groups " + maxGroups +
      " len " + maxGroupLen + " seed " + seed);


    for (int go = 0; go < goes; go++)
    {
      Random r = new Random(go + seed);
      int[] groupCount = new int[r.nextInt(maxGroups)];
      int total = 0;
      for (int i = 0; i < groupCount.length; i++)
      {
        groupCount[i] = 1 + r.nextInt(maxGroupLen);
	total += groupCount[i];
      }
      int[][] base = new int[total][];
      int[] groupNumber = new int[total];
      total = 0;
      for (int i = 0; i < groupCount.length; i++)
      {
	int next = total + groupCount[i];
        fillIn(base, total, next, r);
	Arrays.fill(groupNumber, total, next, i);
	total = next;
      }
      if (verbose)
      {
	for (int i = 0; i < groupCount.length; i++)
	{
	  System.out.println("Group count " + groupCount[i]);
	}
	for (int i = 0; i < total; i++)
	{
	  int[] here = base[i];
	  if (here == null)
	  {
	    here = new int[0];
	  }
	  System.out.print(i + ":");
	  for (int j = 0; j < here.length; j++)
	  {
	    System.out.print(' ');
	    System.out.print(here[j]);
	  }
	  System.out.println();
	}
      }
      int[] toGarbled = new int[total];
      for (int i = 0; i < toGarbled.length; i++)
      {
        toGarbled[i] = i;
      }
      shuffle(toGarbled, r);
      int[] fromGarbled = new int[toGarbled.length];
      for (int i = 0; i < toGarbled.length; i++)
      {
        fromGarbled[toGarbled[i]] = i;
      }
      int[][] garbled = new int[base.length][];
      for (int i = 0; i < base.length; i++)
      {
	int[] here = base[i];
	if (here == null)
	{
	  here = new int[0];
	}
        int[] translated = new int[here.length];
	for (int j = 0; j < here.length; j++)
	{
	  translated[j] = toGarbled[here[j]];
	}
	garbled[toGarbled[i]] = translated;
      }
      if (verbose)
      {
	System.out.println("Garbled");
	for (int i = 0; i < total; i++)
	{
	  int[] here = garbled[i];
	  if (here == null)
	  {
	    here = new int[0];
	  }
	  System.out.print(i + ":");
	  for (int j = 0; j < here.length; j++)
	  {
	    System.out.print(' ');
	    System.out.print(here[j]);
	  }
	  System.out.println();
	}
      }
      int[][] connected = Connected.connected(garbled);
      if (verbose)
      {
        for (int i = 0; i < connected.length; i++)
	{
	  int[] here = connected[i];
	  for (int j = 0; j < here.length; j++)
	  {
	    System.out.print(here[j]);
	    System.out.print(' ');
	  }
	  System.out.println();
	}
      }
      if (connected.length != groupCount.length)
      {
        throw new IllegalArgumentException(
	  "Wrong number of connected components at go " + go +
	  " claim " + connected.length + " answer " +
	  groupCount.length);
      }
      boolean[] seen = new boolean[groupNumber.length];
      for (int i = 0; i < connected.length; i++)
      {
        int[] here = connected[i];
	if (here.length < 1)
	{
	  throw new IllegalArgumentException("zero length component");
	}
	int h = fromGarbled[here[0]];
	int group = groupNumber[h];
	if (groupCount[group] != here.length)
	{
	  throw new IllegalArgumentException("Group length wrong");
	}
	if (seen[h])
	{
	  throw new IllegalArgumentException("Node seen twice");
	}
	seen[h] = true;
	for (int j = 1; j < here.length; j++)
	{
	  h = fromGarbled[here[j]];
	  if (group != groupNumber[h])
	  {
	    throw new IllegalArgumentException("Group mismatch");
	  }
	  if (seen[h])
	  {
	    throw new IllegalArgumentException("Node seen twice 2");
	  }
	  seen[h] = true;
	}
      }
      for (int i = 0; i < seen.length; i++)
      {
        if (!seen[i])
	{
	  throw new IllegalArgumentException("Group member missed");
	}
      }
    }
  }
}
