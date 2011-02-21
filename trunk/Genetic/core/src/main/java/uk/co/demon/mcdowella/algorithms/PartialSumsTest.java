package uk.co.demon.mcdowella.algorithms;

import java.util.Random;

/** This class provides a test harness for the PartialSums class,
 *  largely by duplicating its functionality in linear time, rather
 *  than logarithmic time.
 */
public class PartialSumsTest
{
  private double[] vals;
  PartialSumsTest(int len)
  {
    vals = new double[len];
  }
  public double get(int index)
  {
    return vals[index];
  }
  public void put(int index, double value)
  {
    vals[index] = value;
  }
  public double getSumBefore(int past)
  {
    double sofar = 0.0;
    if (past > vals.length)
    {
      past = vals.length;
    }
    for (int i = 0; i < past; i++)
    {
      sofar += vals[i];
    }
    return sofar;
  }
  public double sumInterval(int first, int past)
  {
    double sofar = 0.0;
    for (int i = first; i < past; i++)
    {
      sofar += vals[i];
    }
    return sofar;
  }
  public static void main(String[] s)
  {
    long seed = 42;
    int goes = 100;
    int maxSize = 100;

    int argp = 0;
    boolean trouble = false;
    int s1 = s.length - 1;
    try
    {
      for (;argp < s.length; argp++)
      {
        if ("-goes".equals(s[argp]) && (argp < s1))
	{
	  goes = Integer.parseInt(s[++argp]);
	}
        else if ("-seed".equals(s[argp]) && (argp < s1))
	{
	  seed = Long.parseLong(s[++argp]);
	}
        else if ("-size".equals(s[argp]) && (argp < s1))
	{
	  maxSize = Integer.parseInt(s[++argp]);
	}
	else
	{
	  System.err.println("Cannot handle flag " + s[argp]);
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
      System.err.println("Args are [-goes #] [-seed #] [-size #]");
      return;
    }

    System.err.println("Goes " + goes + " seed " + seed +
      " size " + maxSize);

    Random r = new Random(seed);
    int lastReported = 0;
    for (int go = 0; go < goes; go++)
    {
      if (lastReported * 4 < go * 3)
      {
	System.err.println("Go " + go + " of " + goes);
	lastReported = go;
      }
      int len = r.nextInt(maxSize + 1);
      PartialSumsTest slow = new PartialSumsTest(len);
      PartialSums fast = new PartialSums(len);
      if (fast.getLength() != len)
      {
        throw new IllegalStateException("Length mismatch");
      }
      int inner = len * 3;
      for (int i = 0; i < inner; i++)
      {
        int readp = r.nextInt(len);
	// All inputs are integers, so answers should be exact
	if (fast.get(readp) != slow.get(readp))
	{
	  throw new IllegalStateException("read mismatch");
	}
	int writep = r.nextInt(len);
	double val = r.nextInt();
	fast.put(writep, val);
	slow.put(writep, val);
	int pos = r.nextInt(len + 4) - 2;
	if (fast.getSumBefore(pos) != slow.getSumBefore(pos))
	{
	  throw new IllegalStateException("Sum before mismatch");
	}
	int first = r.nextInt(len);
	int past = r.nextInt(len);
	if (fast.sumInterval(first, past) !=
	  slow.sumInterval(first, past))
	{
	  throw new IllegalStateException("Sum interval mismatch");
	}
      }
    }
  }
}
