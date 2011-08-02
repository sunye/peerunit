package uk.co.demon.mcdowella.stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.io.StreamTokenizer;
import java.util.StringTokenizer;

/** This class is given a rectangular array of data. It works out
 *  the distribution of the sum of a subset of those columns, under
 *  permutations of the rows.
 */
public class Columns
{
  /** index of sum with first non-zero probability */
  private int base;
  /** return index of sum with first non-zero probability */
  public int getBase()
  {
    return base;
  }
  /** probabilities of possible sums, starting at base */
  private double[] probs;
  /** return copy of array of probabilities of possible sums,
   *  starting at base
   */
  public double[] getProbs()
  {
    return (double[]) probs.clone();
  }
  /** Construct from the number of columns in the first group
   *  and the data, stored row, column. IllegalArgumentException
   *  for stupid parameters.
   */
  public Columns(int numFirst, int[][] data)
  {
    if (numFirst < 0)
    {
      throw new IllegalArgumentException("NumFirst = " + numFirst +
        " < 0");
    }
    /** establish base distribution */
    base = 0;
    probs = new double[] {1.0};
    if (data.length == 0)
    { // done if no data
      return;
    }
    int cols = data[0].length;
    if (numFirst > cols)
    {
      throw new IllegalArgumentException("numFirst = " + numFirst +
        " > " + cols);
    }
    int[] selected = new int[numFirst];

    // work out number of choices
    double choices = 1.0;
    for (int i = 0; i < numFirst; i++)
    {
      choices = (choices * (cols - i)) / (numFirst - i);
    }

    int numChoices = (int)Math.round(choices);
    if (Math.abs(choices - numChoices) > 0.01)
    {
      throw new IllegalStateException("Bad rounding computing choices");
    }
    int[] sums = new int[numChoices];
    double probSingle = 1.0 / numChoices;

    for (int i = 0; i < data.length; i++)
    { // for each row
      int[] ourRow = data[i];
      if (ourRow.length != cols)
      {
        throw new IllegalArgumentException("Have " + ourRow.length +
	  " of " + cols + " columns at row " + i);
      }

      // Work out the first set of sums. We will keep an explicit
      // stack of selected columns in selected
      int sofar = 0;
      for (int j = 0; j < numFirst; j++)
      {
        selected[j] = j;
	sofar += ourRow[j];
      }
      // where to write sum to
      int wp = 0;
      // position in stack of last column participating in current sum
      int pos = numFirst - 1;
      sumLoop: for (;;)
      {
	// Got a sum!
        sums[wp++] = sofar;

	// Now backtrack down stack to find another sum.
	for (;pos >= 0; pos--)
	{ // Here we know we have covered all combinations starting
	  // with the first pos + 1 columns selected here, so try
	  // incrementing the pos-th column

	  // back off last addition
	  // System.err.println("pos " + pos);
	  int selection = selected[pos];
	  // System.err.println("selection " + selection);
	  sofar -= ourRow[selection];

	  if ((selection + numFirst - pos) < cols)
	  { // can increment here
	    for (; pos < numFirst; pos++)
	    {
	      ++selection;
	      selected[pos] = selection;
	      sofar += ourRow[selection];
	    }
	    pos--;
	    continue sumLoop;
	  }
	}
	// Here with all combinations exhausted
	break;
      }
      if (wp != sums.length)
      { // program bug
        throw new IllegalStateException("Only " + wp + " of " +
	  sums.length + " possibilities covered");
      }
      // Sort sums in the hope that it has many copies of the
      // same values
      Arrays.sort(sums);
      // Need to know minimum sum to increment base
      // and max to work out length of new array
      int min = sums[0];
      int max = sums[sums.length - 1];
      base += min;
      double[] newProbs = new double[probs.length + max - min];
      for (int j = 0; j < sums.length;)
      {
	int sumHere = sums[j];
	int j1 = j + 1;
	for (;j1 < sums.length; j1++)
	{
	  if (sums[j1] != sumHere)
	  {
	    break;
	  }
	}
	int offset = sumHere - min;
	int count = j1 - j;
        for (int k = 0; k < probs.length; k++)
	{
	  newProbs[k + offset] += probs[k] * probSingle * count;
	}
        j = j1;
      }
      probs = newProbs;
      // Check probs sum to 1
      double sumProbs = 0.0;
      for (int j = 0; j < probs.length; j++)
      {
	sumProbs += probs[j];
      }
      if ((sumProbs < 0.999) || (sumProbs > 1.001))
      {
        throw new IllegalStateException("Probs sum to " + sumProbs);
      }
    }
  }

  public static void main(String[] s) throws IOException
  {
    int cols = -1;
    Set<Integer> mainSet = new LinkedHashSet<Integer>();
    Set<Integer> all = new LinkedHashSet<Integer>();
    double increment = 0.0;
    boolean trouble = false;
    double grain = 1.0;
    int s2 = s.length - 1;
    String num = "";
    long seed = 42;
    int mc = 0;
    try
    {
      for (int i = 0; i < s.length; i++)
      {
	if ((i < s2) && "-cols".equals(s[i]))
	{
	  num = s[++i];
	  cols = Integer.parseInt(num.trim());
	}
	else if ((i < s2) && "-add".equals(s[i]))
	{
	  num = s[++i];
	  increment = Double.parseDouble(num.trim());
	}
	else if ((i < s2) && "-grain".equals(s[i]))
	{
	  num = s[++i];
	  grain = Double.parseDouble(num.trim());
	}
	else if ((i < s2) && "-main".equals(s[i]))
	{
	  StringTokenizer st = new StringTokenizer(s[++i], " ,");
	  while (st.hasMoreElements())
	  {
	    num = st.nextToken();
	    Integer col = new Integer(num.trim());
	    if (!mainSet.add(col))
	    {
	      System.err.println("Already mentioned column " + col);
	      trouble = true;
	    }
	    if (!all.add(col))
	    {
	      System.err.println("Already mentioned column " + col);
	      trouble = true;
	    }
	  }
	}
	else if ((i < s2) && "-seed".equals(s[i]))
	{
	  num = s[++i];
	  seed = Long.parseLong(num);
	}
	else if ((i < s2) && "-mc".equals(s[i]))
	{
	  num = s[++i];
	  mc = Integer.parseInt(num);
	}
	else if ((i < s2) && "-compare".equals(s[i]))
	{
	  StringTokenizer st = new StringTokenizer(s[++i], " ,");
	  while (st.hasMoreElements())
	  {
	    num = st.nextToken();
	    Integer col = new Integer(num.trim());
	    if (!all.add(col))
	    {
	      System.err.println("Already mentioned column " + col);
	      trouble = true;
	    }
	  }
	}
	else
	{
	  System.err.println("Could not handle flag " + s[i]);
	  trouble = true;
	}
      }
    }
    catch (NumberFormatException nfe)
    {
      System.err.println("Could not read number in " + num);
      trouble = true;
    }
    if (cols <= 0)
    {
      System.err.println("Must quote cols > 0");
      trouble = true;
    }
    for (Iterator i = all.iterator(); i.hasNext();)
    {
      int x = ((Integer)i.next()).intValue();
      if ((x < 0) || (x >= cols))
      {
        System.err.println("Cannot have columns " + x);
	trouble = true;
      }
    }
    int mainCols = mainSet.size();
    int allCols = all.size();
    if (allCols <= mainCols)
    {
      System.err.println("Nothing but main columns specified");
      trouble = true;
    }
    if (trouble)
    {
      System.err.println("Args are -cols # [-add #] -main #,#... " +
        "-compare #,#,... [-mc #] [-grain #] [-seed #]");
      System.err.println("Columns are counted starting from 0");
      return;
    }
    StreamTokenizer st = new StreamTokenizer(
      new InputStreamReader(System.in));
    st.resetSyntax();
    st.commentChar('#');
    // Have to read numbers as words because StreamTokenizers don't
    // handle exponentials
    st.wordChars('0', '9');
    st.wordChars('-', '-');
    st.wordChars('+', '+');
    st.wordChars('.', '.');
    st.wordChars('e', 'e');
    st.wordChars('E', 'E');
    st.slashSlashComments(true);
    st.slashStarComments(true);
    List<double[]> rows = new ArrayList<double[]>();
    readLoop: for (;;)
    {
      double[] row = new double[allCols];
      int mainp = 0;
      int comparep = mainCols;
      for (int i = 0; i < cols; i++)
      {
        int tok = st.nextToken();
	if (tok == StreamTokenizer.TT_EOF)
	{
	  if (i != 0)
	  {
	    System.err.println("Data ended part way through a row");
	    return;
	  }
	  break readLoop;
	}
	if (tok == StreamTokenizer.TT_WORD)
	{
	  double numRead;
	  try
	  {
	    numRead = Double.parseDouble(st.sval.trim());
	  }
	  catch (NumberFormatException nfe)
	  {
	    System.err.println("Could not read number in " + st.sval);
	    return;
	  }
	  Integer col = new Integer(i);
	  if (mainSet.contains(col))
	  {
	    row[mainp++] = numRead + increment;
	  }
	  else if (all.contains(col))
	  {
	    row[comparep++] = numRead;
	  }
	}
	else if (tok == StreamTokenizer.TT_NUMBER)
	{
	  System.err.println("Internal error - got raw number");
	  return;
	}
	else
	{
	  i--;
	}
      }
      rows.add(row);
    }
    double[][] data = new double[rows.size()][];
    data = (double[][])rows.toArray(data);
    System.out.println("Got " + data.length + " rows");
    System.out.println("Adding " + increment + " to main cells");
    System.out.print("main columns are");
    String sep = " ";
    for (Iterator i = mainSet.iterator(); i.hasNext();)
    {
      System.out.print(sep);
      sep = ", ";
      System.out.print(i.next());
    }
    System.out.println();
    System.out.print("Comparing with cols");
    sep = " ";
    for (Iterator i = all.iterator(); i.hasNext();)
    {
      Object val = i.next();
      if (mainSet.contains(val))
      {
        continue;
      }
      System.out.print(sep);
      sep = ", ";
      System.out.print(val);
    }
    System.out.println();
    double mainSum = 0.0;
    double compareSum = 0.0;
    for (int i = 0; i < data.length; i++)
    {
      double[] here = data[i];
      for (int j = 0; j < mainCols; j++)
      {
        mainSum += here[j];
      }
      for (int j = mainCols; j < allCols; j++)
      {
        compareSum += here[j];
      }
    }
    System.out.println("Average main cell after increment is " +
      mainSum / (mainCols * data.length));
    System.out.println("Average compare cell after increment is " +
      compareSum / ((allCols - mainCols) * data.length));
    System.out.println("Grain is " + grain);
    int[][] asInt = new int[data.length][];
    double constant = 0.0;
    for (int i = 0; i < data.length; i++)
    {
      double[] rowHere = data[i];
      int[] here = new int[allCols];
      asInt[i] = here;
      double conHere = rowHere[0];
      constant += conHere;
      for (int j = 0; j < allCols; j++)
      {
        here[j] = (int)Math.round((rowHere[j] - conHere) / grain);
      }
    }
    int mainSumInt = 0;
    int compareSumInt = 0;
    for (int i = 0; i < asInt.length; i++)
    {
      int[] here = asInt[i];
      for (int j = 0; j < mainCols; j++)
      {
        mainSumInt += here[j];
      }
      for (int j = mainCols; j < allCols; j++)
      {
        compareSumInt += here[j];
      }
    }
    System.out.println("Average grained main cell after increment is " +
      ((mainSumInt * grain + constant * mainCols) /
       (mainCols * (double)asInt.length)));
    System.out.println("Average compare cell after increment is " +
      ((compareSumInt * grain + constant * (allCols - mainCols)) / 
       ((allCols - mainCols) * (double)asInt.length)));
    Columns cs = new Columns(mainCols, asInt);
    double probLow = 0.0;
    double probHigh = 0.0;
    int seenOffset = mainSumInt - cs.getBase();
    double[] probs = cs.getProbs();
    for (int i = 0; i < seenOffset; i++)
    {
      probLow += probs[i];
    }
    for (int i = seenOffset + 1; i < probs.length; i++)
    {
      probHigh += probs[i];
    }
    System.out.println("Prob < observed value is " + probLow);
    System.out.println("Prob = observed value is " + probs[seenOffset]);
    System.out.println("Prob > observed value is " + probHigh);
    System.out.println("Prob >= observed value is " + (probHigh +
      probs[seenOffset]));
    // Work out two tailed probability by adding together extreme
    // probabilities so as to keep the two tails grown as equal as
    // possible until the grown area reaches the area occupied by the
    // real data, which must happen before either area comes to grief
    // This is a real tail probability, because under the null
    // hypothesis the probability of getting a value of p or smaller
    // is at most p (induction in the order in which probabilities
    // are selected and added up). It does look a bit odd though if
    // there are two equal probabilities at the extreme ends.
    // Whichever one is selected first (which depends only on the
    // probability distribution, not the observed point within it)
    // gets a smaller tail probability than the other, so this
    // is not symmetric.
    int bottomPointer = 0;
    double bottomSum = 0.0;
    int topPointer = probs.length - 1;
    double topSum = 0.0;
    double twoTailProb;
    for (;;)
    {
      double extendBottom = bottomSum + probs[bottomPointer];
      double extendTop = topSum + probs[topPointer];
      int added;
      if (extendBottom < extendTop)
      {
        bottomSum = extendBottom;
	added = bottomPointer;
	bottomPointer++;
      }
      else
      {
        topSum = extendTop;
	added = topPointer;
	topPointer--;
      }
      if (added == seenOffset)
      {
        twoTailProb = topSum + bottomSum;
	break;
      }
    }
    System.out.println("Two-tailed prob is " + twoTailProb);
    if (mc > 0)
    { // provide Monte-Carlo version as check
      Random r = new Random(seed);
      int[] allColNums = new int[all.size()];
      int wp = 0;
      for (int i = 0; i < allColNums.length; i++)
      {
	allColNums[i] = i;
      }
      int lower = 0;
      int equal = 0;
      int greater = 0;
      for (int pass = 0; pass < mc; pass++)
      {
        double sum = 0;
	for (int i = 0; i < data.length; i++)
	{
	  double[] row = data[i];
	  for (int j = 0; j < mainCols; j++)
	  {
	    // Pick column at random to include in sum
	    // from amongst those not used in this row yet
	    int offset = j + r.nextInt(allCols - j);
	    int col = allColNums[offset];
	    sum += row[col];
	    // Now put column number out of the way till we are
	    // finished on this row
	    allColNums[offset] = allColNums[j];
	    allColNums[j] = col;
	  }
	}
	if (sum < mainSum)
	{
	  lower++;
	}
	else if (sum == mainSum)
	{
	  equal++;
	}
	else
	{
	  greater++;
	}
      }
      System.out.println("Of " + mc + " runs with seed " + seed);
      System.out.println(lower + " < observed " + equal +
        " = observed " + greater + " > observed");
    }
  }
}
