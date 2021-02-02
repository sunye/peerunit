package uk.co.demon.mcdowella.algorithms;

import java.util.Arrays;

/** This class looks for sets of bit-vectors such that in every
    triple of columns every 3-bit pattern occurs at least once.
    It does this by reducing it to set coverage.
    */
public class TripleCoverage
{
  /** number of bits in bit-vectors */
  private final int numBits;
  /** This vector of vectors shows which triples are covered
      by which vectors. We can turn every solution into one in
      which the all-zeros vector is chosen by flipping columns,
      so we assume that the all-zeros vector is chosen and do
      our search for the other vectors. So every triple has
      seven possibilities we care about. We number the possibilities
      we care about with the seven alternatives contiguous, starting
      with the possibilities for the columns 0,1,2.
      */
  private final int[][] coverages;
  /** bit patterns associated with selections, so we can read out the answer */
  private final int[] selections;
  /** costs */
  private final double[] cost;
  /** whether to use the linear programming bound in set cover */
  private final boolean useLpBound;
  /** Create given number of bits in each bit-vector. If balance is set
      then every bit-vector that does not have exactly half its bits set
      is paired with its inverse. If useLpBound is set, have the
      set coverage stuff use the linear programming bound in its branch
      and bound
      */
  TripleCoverage(int bits, boolean balance, boolean forUseLpBound)
  {
    useLpBound = forUseLpBound;
    numBits = bits;
    // Number of different bit-patterns, except for the all-zero
    // vector we assume is in. If balanced, assume all-1s vector
    // is in.
    int numTribits;
    if (balance)
    {
      numTribits = 6;
    }
    else
    {
      numTribits = 7;
    }
    int numCovers = (1 << bits) - 1;
    int[][] ourCoverages = new int[numCovers][];
    int numTriples = (numBits * (numBits - 1) * (numBits - 2)) / 6;
    int numCoversWritten = 0;
    int[] ourSelections = new int[numCovers];
    double[] ourCosts = new double[numCovers];
    for (int i = 1; i <= numCovers; i++)
    {  // i counts bit patterns
      // build up triples covered in here, neglecting all-0 triples
      int twiceBitsHere = Integer.bitCount(i) * 2;
      int[] coverHere;
      if (balance)
      {
	if (balance && (twiceBitsHere > numBits))
	{ // covered by inverse of another bit pattern
	  continue;
	}
	coverHere = new int[numTriples * 2];
      }
      else
      {
	coverHere = new int[numTriples];
      }
      int wp = 0;
      int tripleCount = 0;
      for (int b1 = 0; b1 < numBits; b1++)
      {
        for (int b2 = b1 + 1; b2 < numBits; b2++)
	{
	  for (int b3 = b2 + 1; b3 < numBits; b3++)
	  {
	    int valueHere = ((i >> b1) & 1) |
	                    ((i >> (b2 - 1)) & 2) |
			    ((i >> (b3 - 2)) & 4);
	    if (balance)
	    {
	      if ((valueHere != 0) && (valueHere != 7))
	      {
		coverHere[wp++] = tripleCount * numTribits + (valueHere - 1);
		if (twiceBitsHere != numBits)
		{
		  coverHere[wp++] = tripleCount * numTribits + ((7 & ~valueHere) - 1);
		}
	      }
	    }
	    else if (valueHere != 0)
	    {
	      coverHere[wp++] = tripleCount * numTribits + (valueHere - 1);
	    }
	    tripleCount++;
	  }
	}
      }
      ourSelections[numCoversWritten] = i;
      ourCoverages[numCoversWritten] = new int[wp];
      for (int j = 0; j < wp; j++)
      {
        ourCoverages[numCoversWritten][j] = coverHere[j];
      }
      if (balance && (twiceBitsHere != numBits))
      {
        ourCosts[numCoversWritten] = 2.0;
      }
      else
      {
        ourCosts[numCoversWritten] = 1.0;
      }
      numCoversWritten++;
    }
    coverages = new int[numCoversWritten][];
    System.arraycopy(ourCoverages, 0, coverages, 0, numCoversWritten);
    selections = new int[numCoversWritten];
    cost = new double[numCoversWritten];
    for (int i = 0; i < selections.length; i++)
    {
      selections[i] = ourSelections[i];
      cost[i] = ourCosts[i];
    }
  }
  /** Call to run backtracking search for solution */
  public void backtrack(double tooCostly)
  {
    final SetCover.SolutionReceiver sr = new SetCover.SolutionReceiver()
    {
      public void gotSolution(int[] answer, double cost)
      {
        System.err.println("Answer cost " + cost);
	for (int i = 0; i < answer.length; i++)
	{
	  String sep = "";
	  int here = selections[answer[i]];
	  for (int j = 0; j < numBits; j++)
	  {
	    System.out.print(sep);
	    sep = " ";
	    System.out.print((here >> j) & 1);
	  }
	  System.out.println();
	}
      }
    };
    if (tooCostly <= 0.0)
    {
      tooCostly = 2.8 * coverages.length + 1.0;
    }
    SetCover sc = new SetCover(coverages, cost, tooCostly, sr);
    sc.setUseLpBound(useLpBound);
    sc.backtrack();
  }
  public static void main(String[] s)
  {
    boolean trouble = false;
    int len = 0;
    double tooCostly = -1.0;
    int s1 = s.length - 1;
    String number = "";
    boolean balance = false;
    boolean useLpBound = false;
    try
    {
      for (int i = 0; i < s.length; i++)
      {
	if ("-balance".equals(s[i]))
	{
	  balance = true;
	}
	else if ("-len".equals(s[i]) && (i < s1))
	{
	  number = s[++i];
	  len = Integer.parseInt(number.trim());
	}
	else if ("-lp".equals(s[i]))
	{
	  useLpBound = true;
	}
	else if ("-tooCostly".equals(s[i]) && (i < s1))
	{
	  number = s[++i];
	  tooCostly = Double.parseDouble(number);
	}
	else
	{
	  System.err.println("Cannot handle flag " + s[i]);
	  trouble = true;
	}
      }
    }
    catch (NumberFormatException nf)
    {
      System.err.println("Cannot read number in " + number);
      trouble = true;
    }
    if (len <= 0)
    {
      System.err.println("Must state len > 0");
      trouble = true;
    }
    if (trouble)
    {
      System.err.println("Args are -len # [-tooCostly #] [-lp] [-balance]");
      return;
    }
    Swatch sw = new Swatch();
    sw.start();
    TripleCoverage tc = new TripleCoverage(len, balance, useLpBound);
    tc.backtrack(tooCostly);
    sw.stop();
    System.out.println("Took " + sw);
  }
}
