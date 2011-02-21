package uk.co.demon.mcdowella.stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.StringTokenizer;

/** This class works out the exact significance of the sum
     of counts down the left side of a 2xn table under the
     null hypothesis that each row of the table is binomially
     distributed with a given probability. Alternatively, score
     square of differences between cells.
     */
public class MultiBin
{
  /** no need for constructor */
  private MultiBin()
  {
  }
  /** return significance given data. Table is given with the
      rows contiguous in the array
      */
  public static SigProb computeMultiBin(double probLeft, int[] table,
    boolean useSquare)
  {
    if ((probLeft < 0.0) || (probLeft > 1.0))
    {
      throw new IllegalArgumentException("Nonsensical probability");
    }
    if ((table.length % 2) != 0)
    {
      throw new IllegalArgumentException("Table length is odd");
    }
    int sumLeft = 0;
    for (int i = 0; i < table.length; i += 2)
    {
      int x = table[i];
      if (x < 0)
      {
        throw new IllegalArgumentException("-ve entry on left");
      }
      int y = table[i + 1];
      if (y < 0)
      {
        throw new IllegalArgumentException("-ve entry on right");
      }
      if (useSquare)
      {
        x -= y;
	sumLeft += (x * x);
      }
      else
      {
	sumLeft += x;
      }
    }
    // Could reduce cpu by being working out only what we need
    // to get probability of < result, = result, and > result but
    // don't bother too much here - for correctness and clarity -
    // although the saving could be signficant for tables with lots
    // of rows with small sums

    // Want an array giving the probability of every sum up to the
    // sum achieved and one more
    double[] probHere = new double[sumLeft + 2];
    // At the moment the distribution is all at sum=0.0
    probHere[0] = 1.0;
    double[] probNext = new double[probHere.length];
    int maxSeen = 0;
    for (int i = 0; i < table.length; i += 2)
    {
      int x = table[i];
      int y = table[i + 1];
      int sum = x + y;
      BinomialProb bp = new BinomialProb(sum, probLeft);
      for (int j = 0; j <= sum; j++)
      { // possible result for this row
	double probNow = bp.getProb(j); // and its prob
	int contrib;
	if (useSquare)
	{
	  contrib = sum - 2 * j;
	  contrib = contrib * contrib;
	}
	else
	{
	  contrib = j;
	}
	for (int k = 0; k <= maxSeen; k++)
	{ // so update probNext with consequences
	  int result = contrib + k;
	  if (result > sumLeft)
	  {
	    result = sumLeft + 1;
	  }
	  probNext[result] += probNow * probHere[k];
	}
      }
      // System.out.println("Here " + Arrays.toString(probHere));
      // System.out.println("Next " + Arrays.toString(probNext));
      for (int j = 0; j <= maxSeen; j++)
      { // clean for next use
        probHere[j] = 0.0;
      }
      double[] t = probHere;
      probHere = probNext;
      probNext = t;
      if (useSquare)
      {
	maxSeen += sum * sum;
      }
      else
      {
	maxSeen += sum;
      }
      if (maxSeen >= probHere.length)
      {
        maxSeen = probHere.length - 1;
      }
    }
    double probLt = 0.0;
    for (int i = 0; i < sumLeft; i++)
    {
      probLt += probHere[i];
    }
    double checkSum = Math.abs(1.0 - probHere[sumLeft] - 
      probHere[sumLeft + 1] - probLt);
    if (checkSum > 1.0e-6)
    {
      throw new IllegalArgumentException("Internal sum check failed");
    }
    return new SigProb(probLt, probHere[sumLeft], probHere[sumLeft + 1]);
  }
  public static void main(String[] s) throws Exception
  {
    double probLeft = 0.5;
    boolean useSquare = false;
    int s1 = s.length - 1;
    boolean trouble = false;
    for (int i = 0; i < s.length; i++)
    {
      if (("-probLeft".equals(s[i])) && (i < s1))
      {
        probLeft = Double.parseDouble(s[++i].trim());
      }
      else if ("-sqr".equals(s[i]))
      {
        useSquare = true;
      }
      else
      {
        System.err.println("Cannot handle flag " + s[i]);
	trouble = true;
      }
    }
    if (trouble)
    {
      System.err.println("Args are [-probLeft #] [-sqr]");
      return;
    }
    List<Integer> nums = new ArrayList<Integer>();
    BufferedReader br = new BufferedReader(new InputStreamReader(
      System.in));
    for (;;)
    {
      String line = br.readLine();
      if (line == null)
      {
        break;
      }
      StringTokenizer st = new StringTokenizer(line);
      if (!st.hasMoreElements())
      {
        continue;
      }
      String first = st.nextToken().trim();
      if (first.equals("#"))
      {
        continue;
      }
      if (!st.hasMoreElements())
      {
        throw new IllegalArgumentException("Need two numbers per line");
      }
      String second = st.nextToken().trim();
      if (st.hasMoreElements())
      {
        throw new IllegalArgumentException(
	  "Want just two numbers per line");
      }
      nums.add(Integer.parseInt(first));
      nums.add(Integer.parseInt(second));
    }
    int[] data = new int[nums.size()];
    for (int i = 0; i < data.length; i++)
    {
      data[i] = nums.get(i);
    }
    System.out.println("ProbLeft = " + probLeft + " useSquare " +
      useSquare);
    SigProb sp = computeMultiBin(probLeft, data, useSquare);
    System.out.println("Statistic " + sp);
    System.out.println("Prob >= " + (sp.getGt() + sp.getEq()));
    System.out.println("Prob <= " + (sp.getLt() + sp.getEq()));
  }
}
