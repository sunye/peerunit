package uk.co.demon.mcdowella.algorithms;

import java.util.Arrays;
import java.util.Random;

public class MaxInRangeTest
{
  private static void test(int len, int goes, Random r)
  {
    double[] da = new double[len];
    long[] la = new long[len];
    for (int i = 0; i < len; i++)
    {
      double d = r.nextDouble();
      da[i] = d;
      la[i] = MaxInRange.toOrderedLong(d);
    }
    Arrays.sort(da);
    Arrays.sort(la);
    for (int i = 0; i < len; i++)
    {
      if (da[i] != MaxInRange.fromOrderedLong(la[i]))
      {
        throw new IllegalStateException("Ordering problem");
      }
    }
    MaxInRange mir = new MaxInRange(len);
    long[] info = new long[len];
    Arrays.fill(info, Long.MIN_VALUE);
    for (int i = 0; i < goes; i++)
    {
      int index = r.nextInt(info.length);
      long l = r.nextLong();
      info[index] = l;
      mir.set(index, l);
      int a = r.nextInt(info.length);
      int b = r.nextInt(info.length);
      if (a > b)
      {
        int t = a;
	a = b;
	b = t;
      }
      int first = a;
      int past = b + 1;
      /*
      System.out.println("first " + first + " past " + past + 
        " len " + len);
      */
      int maxAt = mir.getMaxIndex(first, past);
      long v = info[maxAt];
      for (int p = first; p < past; p++)
      {
        if (info[p] > v)
	{
	  System.out.println("[" + first + ", " + past + ")");
	  System.out.println("Claimed " + maxAt);
	  mir.dump();
	  throw new IllegalStateException("Not max");
	}
      }
    }
    for (int i = 0; i < len; i++)
    {
      if (info[i] != mir.get(i))
      {
        throw new IllegalStateException("Mismatch");
      }
    }
  }
  public static void main(String[] s)
  {
    int goes = 1000;
    int maxLength = 1000;
    int passes = 100;
    long seed = 42;

    boolean trouble = false;
    int argp = 0;
    int s1 = s.length - 1;

    try
    {
      for (;argp < s.length; argp++)
      {
        if ((argp < s1) && "-goes".equals(s[argp]))
	{
	  goes = Integer.parseInt(s[++argp].trim());
	}
        else if ((argp < s1) && "-len".equals(s[argp]))
	{
	  maxLength = Integer.parseInt(s[++argp].trim());
	}
        else if ((argp < s1) && "-passes".equals(s[argp]))
	{
	  passes = Integer.parseInt(s[++argp].trim());
	}
        else if ((argp < s1) && "-seed".equals(s[argp]))
	{
	  seed = Long.parseLong(s[++argp].trim());
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
      System.err.println("Could not read number in " + s[argp]);
      trouble = true;
    }

    if (trouble)
    {
      System.err.println("Args are [-goes #] [-len #] [-passes #] " +
        "[-seed #]");
      return;
    }

    System.out.println("Goes " + goes + " len " + maxLength +
      " passes " + passes + " seed " + seed);

    for (int i = 0; i < passes; i++)
    {
      System.out.println("Pass " + i + " of " + passes);
      Random r = new Random(seed + i);
      int len = r.nextInt(maxLength) + 1;
      System.out.println("Len " + len);
      test(len, goes, r);
    }
  }
}
