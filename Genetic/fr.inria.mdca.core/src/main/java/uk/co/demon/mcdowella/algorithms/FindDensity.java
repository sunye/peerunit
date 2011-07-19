package uk.co.demon.mcdowella.algorithms;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** This was originally written to put a lower bound on the
    number of 10-long bit vectors, treated as rows, such that each
    triple of columns sees all 8 possible bit-patterns. This means
    that we need at least 120 occurrences of the 000 bit pattern
    in a row and so on, which puts constraints on the number of
    times each density appears as a bit vector. Find solutions
    for this by slow but easily written breadth first search 
*/
public class FindDensity
{
  /** Each bit pattern contains 000, 001, 011, and 111 bit-patterns
      in each triple of columns. Here is an array giving, for
      each density of bit pattern, the count of each type of
      bit-pattern. 10 chose 3 is 120, so all add up to this. */
  public static final int countsByDensity[][] = {
    {120, 0, 0,   0}, // density 0 is all 000s
    {84, 36, 0,   0},
    {56, 56, 8,   0},
    {35, 63, 21,  1},
    {20, 60, 36,  4},
    {10, 50, 50, 10}, // density 5 is symmettrical
    { 4, 36, 60, 20}, // and now repreat reversed
    { 1, 21, 63, 35},
    { 0,  8, 56, 56},
    { 0,  0, 36, 84},
    { 0,  0,  0, 120}
  };
  /** Need combination that sums up to at least this */
  public static final int target[] = {120, 360, 360, 120};
  /** info for combination */
  private static class Combination
  {
    /** one possible combination of offsets in countsByDensity
        that gives the final total, expressed as the number
	of times each density-vector is used
	*/
    private final int[] fromHere;
    /** current total */
    private final int[] total = {0, 0, 0, 0};
    /** whether two combinations are equal */
    @Override
    public boolean equals(Object o)
    {
      if (!(o instanceof Combination))
      {
        return false;
      }
      return Arrays.equals(total, ((Combination)o).total);
    }
    /** Hashcode compatible with equals */
    @Override
    public int hashCode()
    {
      int sofar = 0;
      for (int i = 0; i < total.length; i++)
      {
        sofar += total[i];
	sofar *= 501;
      }
      return sofar;
    }
    /** return whether satisifies constraint */
    public boolean satisifies(int[] constraint)
    {
      for (int i = 0; i < total.length; i++)
      {
        if (total[i] < constraint[i])
	{
	  return false;
	}
      }
      return true;
    }
    /** return default combination */
    public Combination()
    {
      fromHere = new int[11];
    }
    /** Return new combination produced by adding vector */
    public Combination(Combination old, int offset, int[] vector)
    {
      fromHere = old.fromHere.clone();
      fromHere[offset]++;
      for (int i = 0; i < total.length; i++)
      {
        total[i] = old.total[i] + vector[i];
      }
    }
  };
  public static void main(String[] s)
  {
    Set<Combination> sofar = new HashSet<Combination>();
    sofar.add(new Combination());
    for (;;)
    {
      System.out.println("Got " + sofar.size() + " combinations");
      Set<Combination> nextSteps = new HashSet<Combination>();
      for (Combination c: sofar)
      {
        for (int i = 0; i < countsByDensity.length; i++)
	{
	  Combination next = new Combination(c, i, countsByDensity[i]);
	  if (next.satisifies(target))
	  {
	    System.out.println("Satisified at " +
	      Arrays.toString(next.fromHere));
	    return;
	  }
	  nextSteps.add(next);
	}
      }
      sofar = nextSteps;
    }
  }
}
