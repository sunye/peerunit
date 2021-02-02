package uk.co.demon.mcdowella.algorithms;

import java.util.Random;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Test harness for DoubleHeap
 */
public class TestDoubleHeap
{
  public void pass(int size, Random r)
  {
    DoubleHeap<Integer> ih = new
      DoubleHeap.DefaultDoubleHeap<Integer>(size);
    TreeSet<Integer> is = new TreeSet<Integer>();
    for (int i = 0; i < size; i++)
    {
      Integer ni;
      for (;;)
      {
        ni = new Integer(r.nextInt());
	if (!is.contains(ni))
	{ // Set doesn't keep equal items
	  break;
	}
      }
      ih.add(ni);
      is.add(ni);
      if (!(ih.size() == (i + 1)))
      {
        throw new IllegalStateException("size mismatch: " + ih.size()
	  + " at " + i);
      }
      if (!ih.first().equals(is.first()))
      {
        throw new IllegalStateException("First not the same");
      }
      if (!ih.last().equals(is.last()))
      {
        throw new IllegalStateException("Last not the same");
      }
    }
    for (int i = 0; i < size; i++)
    {
      int choice = r.nextInt(2);
      if (choice > 0)
      {
        Integer fromSet = is.first();
	is.remove(fromSet);
	Integer removeFirst = ih.removeFirst();
	if (!fromSet.equals(removeFirst))
	{
	  throw new IllegalStateException("remove first mismatch");
	}
      }
      else
      {
        Integer fromSet = is.last();
	is.remove(fromSet);
	if (!fromSet.equals(ih.removeLast()))
	{
	  throw new IllegalStateException("remove last mismatch");
	}
      }
      if ((ih.size() != is.size()) ||
          (ih.isEmpty() != is.isEmpty()))
      {
        throw new IllegalStateException("Size mismatch: " +
	  ih.size() + " vs " + is.size());
      }
      if (!ih.isEmpty())
      {
	if (!ih.first().equals(is.first()))
	{
	  throw new IllegalStateException(
	    "First not the same after remove");
	}
	if (!ih.last().equals(is.last()))
	{
	  throw new IllegalStateException(
	    "Last not the same after remove");
	}
      }
    }
  }
  public void walk(int len, int maxSize, Random r)
  {
    int size = 0;
    SortedMap<Double, int[]> m = new TreeMap<Double, int[]>();
    DoubleHeap<Double> dh =
      new DoubleHeap.DefaultDoubleHeap<Double>(maxSize);
    for (int i = 0; i < len; i++)
    {
      boolean add;
      if (size <= 0)
      {
        add = true;
      }
      else if (size >= maxSize)
      {
        add = false;
      }
      else
      {
        if (r.nextInt(2) == 0)
	{
	  add = true;
	}
	else
	{
	  add = false;
	}
      }
      if (add)
      {
        size++;
	Double d = new Double(r.nextGaussian());
	dh.add(d);
	int[] val = m.get(d);
	if (val == null)
	{
	  val = new int[] {0};
	  m.put(d, val);
	}
	val[0]++;
      }
      else
      {
        size--;
	Double d;
	if (r.nextInt(2) == 0)
	{
	  d = dh.removeFirst();
	  if (!d.equals(m.firstKey()))
	  {
	    throw new IllegalStateException("first key mismatch");
	  }
	}
	else
	{
	  d = dh.removeLast();
	  if (!d.equals(m.lastKey()))
	  {
	    throw new IllegalStateException("first key mismatch");
	  }
	}
        int[] val = m.get(d);
	if (--val[0] <= 0)
	{
	  m.remove(d);
	}
      }
      if (size != dh.size())
      {
        throw new IllegalStateException("Walk size mismatch");
      }
      if (dh.isEmpty() != m.isEmpty())
      {
        throw new IllegalStateException("Empty walk mismatch");
      }
      if (!dh.isEmpty())
      {
	if (!dh.first().equals(m.firstKey()))
	{
	  throw new IllegalStateException("First key walk mismatch");
	}
	if (!dh.last().equals(m.lastKey()))
	{
	  throw new IllegalStateException("Last key walk mismatch");
	}
      }
    }
  }
  public static void main(String[] s)
  {
    long seed = 42;
    int maxSize = 10000;
    int passes = 1000;
    int walkLen = 100000;

    boolean trouble = false;
    int argp = 0;
    int s1 = s.length - 1;
    try
    {
      for (;argp < s.length; argp++)
      {
        if ((argp < s1) && ("-len".equals(s[argp])))
	{
	  walkLen = Integer.parseInt(s[++argp]);
	}
        else if ((argp < s1) && ("-passes".equals(s[argp])))
	{
	  passes = Integer.parseInt(s[++argp]);
	}
        else if ((argp < s1) && ("-seed".equals(s[argp])))
	{
	  seed = Long.parseLong(s[++argp]);
	}
        else if ((argp < s1) && ("-size".equals(s[argp])))
	{
	  maxSize = Integer.parseInt(s[++argp]);
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
      System.err.println("Cannot read number in " + s[argp]);
      trouble = true;
    }

    if (trouble)
    {
      System.err.println("Args are [-len #] [-passes #] [-seed #]" +
	" [-size #]");
      return;
    }

    System.out.println("Len " + walkLen + " passes " + passes +
      " seed " + seed  + " maxSize " + maxSize);

    TestDoubleHeap tdh = new TestDoubleHeap();
    Random r = new Random(seed);
    for (int go = 0; go < passes; go++)
    {
      System.out.println("Go " + go + " of " + passes);
      int size = r.nextInt(maxSize);
      tdh.pass(size, r);
      tdh.walk(walkLen, maxSize, r);
    }
  }
}
