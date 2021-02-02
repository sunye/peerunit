package uk.co.demon.mcdowella.algorithms;

import java.util.Arrays;
import java.util.Random;

/** This class solves the Assignment Problem. It is based on the account
 *  in "The Stanford Graphbase" by D.E.Knuth, but is far from being
 *  a literal transliteration of that - so almost certainly
 *  inferior, but at least I will understand it
 */
public class Assignment
{
  /** Instead of changing the matrix, we modify the rowAdditive and
   *  the column additive, and pretend the matrix values have changed
   *  to matrix[i][j] + rowAdditive[j] + columnAdditive[j]. The
   *  solution can be recognised by the fact that all the modified
   *  cells are >= 0, and the cells picked out to form the solution
   *  are all zero. This shows that the solution solves the modified
   *  matrix. The modified matrix is derived from the original matrix
   *  by adding on a constant to all cells in a row or all cells in 
   *  a column. Since each row and each column in the sum is used
   *  exactly once a minimum in one matrix must also be a minimum
   *  in the other matrix.
   */
  private final long[] rowAdditive;
  /** column additive - see rowAdditive */
  private final long[] columnAdditive;
  /** One part of the process is to find the maximum number of zeros
   *  in the modified matrix such that no two are on the same row and
   *  column. We consider each such chosen zero as a match between its
   *  row and its column. We keep track of the row and column indexes
   *  of these matched zeros. Here are the row indexes. -1 marks an unused
   *  cell. So rowMatching[i] is the index of the row matching column i
   */
  private final int[] rowMatching;
  /** These are column indexes of matched zeros. See rowMatching. */
  private final int[] columnMatching;
  /** we find matching rows and columns by building a forest of trees. 
   *  This holds the parent indexes of columns. Columns are linked to 
   *  rows and vice versa so the parent indexes of columns are rows. 
   *  We don't need to keep track of the parent indexes of rows 
   *  because they are always the column of the independent zero for 
   *  that row. This is a systematic way of working out which zeros
   *  are reachable via paths of a type described below.
   */
  private final int[] parentRow;
  /** node yet to be visited in forest we explore to find all cells
   *  reachable by paths described later.
   */
  private final int[] nodesToDo;
  /** minimum unchosen value in each column. This is later used to
   *  decrement some rows and increment some columns to create more
   *  zeros.
   */
  private final long[] slack;
  /** In which row we can find the minimum unchosen value */
  private final int[] slackRow;
  /** check solution */
  public void checkSolution()
  {
    for (int i = 0; i < matrix.length; i++)
    {
      long rowValue = rowAdditive[i];
      for (int j = 0; j < matrix.length; j++)
      {
	// work out value here in modified matrix
        long x = matrix[i][j] + rowValue + columnAdditive[j];
	if (x < 0)
	{ // no x should be -ve
	  throw new IllegalStateException("-ve cell value");
	}
	if (j != columnMatching[i])
	{
	  continue;
	}
	if (x != 0)
	{
	  throw new IllegalStateException("solution value not zero");
	}
      }
    }
  }
  /** used to turn on debug info */
  private final static boolean SHOW_WORKING = false;
  /** copy of matrix passed in */
  private final long[][] matrix;
  /** Construct a solution from the given matrix, which is
   *  copied. Various properties of this solution are then available
   *  as methods on the constructed object. The input matrix must
   *  have at least as many columns as rows.
   *  A solution consists of a set of cells in the matrix,
   *  using each row column exactly once and each row exactly once,
   *  which minimises the sum of the selected cells. This expects a
   *  long matrix, not a double, because this code makes comparisons 
   *  against 0 which in practice are supposed to be exact, and also 
   *  does a variety of additions and subtractions, and I am worried 
   *  about the effect of cumulative rounding here.
   */
  public Assignment(long[][] forMatrix)
  {
    for (int i = 0; i < forMatrix.length; i++)
    {
      if (forMatrix[i].length != forMatrix[0].length)
      { // matrix is not of constant width
        throw new IllegalArgumentException(
	  "Dimension mismatch at row " + i);
      }
    }
    int cols = 0;
    if (forMatrix.length > 0)
    {
      cols = forMatrix[0].length;
      if (cols < forMatrix.length)
      {
        throw new IllegalArgumentException(
	  "More rows than columns");
      }
    }
    matrix = new long[forMatrix.length][];
    for (int i = 0; i < matrix.length; i++)
    {
      matrix[i] = forMatrix[i].clone();
    }
    if (SHOW_WORKING)
    {
      System.out.println("Matrix is " + Arrays.deepToString(matrix));
    }
    // Create additives
    rowAdditive = new long[matrix.length];
    columnAdditive = new long[cols];
    parentRow = new int[cols];
    // book-keeping arrays
    rowMatching = new int[cols];
    columnMatching = new int[matrix.length];
    nodesToDo = new int[matrix.length];
    slackRow = new int[cols];
    slack = new long[cols];
    if (matrix.length <= 0)
    {
      return;
    }

    // Initialise the forest
    Arrays.fill(rowMatching, -1);
    Arrays.fill(columnMatching, -1);
    // Set row additive up to zero out at least one entry
    // in every row
    for (int i = 0; i < matrix.length; i++)
    {
      final long[] rowHere = matrix[i];
      long least = rowHere[0];
      for (int j = 1; j < cols; j++)
      {
        long x = rowHere[j];
	if (x < least)
	{
	  least = x;
	}
      }
      rowAdditive[i] = -least;
    }
    if (cols == matrix.length)
    {
      // Set column additive up to see if we can introduce a few
      // more zeros. Can only do this when as many rows as column
      // because we can't modify the implicit rows
      for (int i = 0; i < cols; i++)
      {
	long least = matrix[0][i] + rowAdditive[0];
	for (int j = 1; j < matrix.length; j++)
	{
	  long x = matrix[j][i] + rowAdditive[j];
	  if (x < least)
	  {
	    least = x;
	  }
	}
	columnAdditive[i] = -least;
      }
    }
    // Look for zeros
    for (int i = 0; i < matrix.length; i++)
    {
      long[] rowHere = matrix[i];
      long rowAdditiveHere = rowAdditive[i];
      for (int j = 0; j < cols; j++)
      {
        long valueHere = rowHere[j] + rowAdditiveHere + columnAdditive[j];
	if (valueHere > 0)
	{
	  continue;
	}
	if (rowMatching[j] < 0)
	{ // can match this zero, as no row so far uses this column
	  rowMatching[j] = i;
	  columnMatching[i] = j;
	  break;
	}
      }
    }
    // Iterate here until completion
    for (;;)
    {
      // Note down rows not yet chosen
      int numNodesToDo = 0;
      for (int i = 0; i < columnMatching.length; i++)
      {
        if (columnMatching[i] < 0)
	{
          if (SHOW_WORKING)
          {
            System.out.println("Row " + i + " is not chosen");
          }
	  nodesToDo[numNodesToDo++] = i;
	}
	else
	{ // This shouldn't be necessary because we start off with unmatched
	  // rows to work on and zero out the columns corresponding to matched
	  // rows before we add them to the todo list
	  // System.out.println("Zero out slack for column " + columnMatching[i]);
	  // slack[columnMatching[i]] = 0;
	}
      }
      if (numNodesToDo == 0)
      { // all chosen!
	checkSolution();
        return;
      }
      // System.out.println(numNodesToDo + " nodes to do of " +
      //   rowMatching.length);
      Arrays.fill(slack, Long.MAX_VALUE);
      Arrays.fill(parentRow, -1);
      extendMatching(numNodesToDo);
    }
  }
  /** extend the current matching of rows and columns */
  private void extendMatching(int numNodesToDo)
  {
    if (SHOW_WORKING)
    {
      System.out.println("Extend matching with " + numNodesToDo +
        " unmatched nodes");
      System.err.println("Row matching is " + Arrays.toString(rowMatching));
      System.err.println("Column matching is " + Arrays.toString(columnMatching));
      System.out.println("Todo is " + Arrays.toString(nodesToDo) + " len " +
        numNodesToDo);
    }
    // This works by finding the maximum number of independent zeros
    // of the matrix (the maximum number of cells with zero values such
    // that no two lie on the same row or column). It also builds up
    // a collection of rows and columns of minimum size which cover all
    // the zeros in the matrix. To start off with our independent zeros are
    // the zeros where each matched row intersects its matching column

    // It does this by considering paths of the form R0-C1=R1-C1-..Cq=Rq
    // which is why we build a forest. We link R-C if matrix(R,C) is a
    // zero not in our collection of independent zeros. We link R=C if
    // matrix(R,C) is one of our independent zeros. We chose columns Cq
    // reachable via paths of the form given, with R0 not matched, and present,
    // so the shortest valid path is R0-C1=R1. We chose
    // Rows Rx linked by = to a column not chosen, so we have one line for
    // each independent zero, and working out which zeros are
    // reachable and which are not is not too hard.

    // If we have c=r then we chose either c or r so that zero is covered
    // by one of our lines. If we have c-r and neither are chosen then
    // we can increase our matching immediately by adding r,c to our stock
    // of zeros.
    // If we have r-c=r' then c is reachable by a path of the required form
    // so we have chosen c and covered the zero.
    // If we have c'=r-c and r has been chosen we have covered the zero.
    // if we have ...c'=r-c and c' has been chosen then either c has been chosen
    // in some path and the zero is covered or we can choose it here by 
    // lengthening the matching, turning all = signs to - signs and vice versa.

    // The above assumes that the matrix is square. This code also works
    // for matrices with more columns than rows. We can look at what it
    // does as doing the first rows of a larger square matrix, with the
    // implicit rows filled in with zeros. Because we can always extend
    // the partial solution in the explicit rows by picking any non-
    // conflicting zeros in the implicit rows, we have a solution.

    for (int i = 0; i < numNodesToDo;)
    {
      // Work through list from left to right, adding to it when we can
      for (; i < numNodesToDo; i++)
      { // Note that we may increment numNodesToDo as we find more stuff reachable
        // from the current node
        int targetRow = nodesToDo[i]; // R0 in possible path and link out is
        if (SHOW_WORKING)
        {
          System.out.println("Considering R0 = row " + targetRow);
        }
        // - because R0 is not chosen
        long rowAdd = rowAdditive[targetRow];
        long[] thisRow = matrix[targetRow];
        for (int j = 0; j < rowAdditive.length; j++)
        {
          long slackHere = slack[j];
          if (slackHere <= 0)
          { // marks a column that is already chosen, or at least
	    // already dealt with.
            if (SHOW_WORKING)
            {
              System.out.println("Slack 0 at " + j);
            }
            continue;
          }
          else
          {
            if (SHOW_WORKING)
            {
              System.out.println("non-zero slack " + slackHere + " at " + j);
            }
          }
          long valueHere = thisRow[j] + rowAdd + columnAdditive[j];
          if (valueHere < slackHere)
          {
            if (valueHere == 0)
            { // we are on a zero
              int rowMatchingHere = rowMatching[j];
              if (rowMatchingHere < 0)
              { // we can extend the matching immediately
                if (SHOW_WORKING)
                {
                  System.out.println("Extend immediate set " + j + " to " +
                    targetRow);
                }
                increaseMatching(j, targetRow);
                return;
              }
              // R0-rowMatching[j] which we have found a zero on so we can
              // chose this column.
              slack[j] = 0;
              // Put path so far in forest of trees we are growing
              parentRow[j] = targetRow;
              if (SHOW_WORKING)
              {
                System.out.println("Parent of " + j + " is " + targetRow);
              }
              // Chosen this column, so everything connected to it becomes
              // reachable and needs exploring. We add each row only once
              // because we started off with only the completely unmatched
              // rows and here we add a row that matchines a column. We don't
              // revisit that column within this call because is slack is
              // now zero.
              if (SHOW_WORKING)
              {
                System.out.println("Add new reachable row " + rowMatchingHere + 
                  " as matched " + j);
              }
              nodesToDo[numNodesToDo++] = rowMatchingHere;
            }
            else
            { // just update calculation of minimum slack
              slack[j] = valueHere;
              slackRow[j] = targetRow;
            }
          }
        }
      }
      // Here with no increase in the matching yet. 
      // Find the smallest slack
      boolean notFound = true;
      long smallestSlack = 0;
      for (int j = 0; j < slack.length; j++)
      {
        long slackHere = slack[j];
        if (slackHere <= 0)
        { // chosen column don't count
          continue;
        }
        if ((slackHere < smallestSlack) || notFound)
        {
          smallestSlack = slackHere;
          notFound = false;
        }
      }
      if (SHOW_WORKING)
      {
        System.out.println("Smallest slack is " + smallestSlack);
      }
      // Subtract the slack from all the entries in each unchosen row
      for (int j = 0; j < numNodesToDo; j++)
      {
        rowAdditive[nodesToDo[j]] -= smallestSlack;
      }
      boolean foundNewIndependentZero = false;
      // Adjust the values of slack. This amounts to taking a row not chosen
      // with the minimum value of slack and subtracting the least
      // value from it, while adding that value to all the chosen columns.
      // It maintains all the independent zeros, because they are in exactly one
      // row and one column: if they are not in a chosen
      // row then they are added to and subtracted from. If they are in a chosen
      // row then they are neither added to nor subtracted from. If we get this
      // far, there are no zeros in an unchosen row without being in a chosen
      // column, because we could have used them to increase the matching.
      int rowFound = 0;
      for (int j = 0; j < slack.length; j++)
      {
        long slackHere = slack[j];
        if (slackHere <= 0)
        { // Chosen column, so add the minimum slack to it
          columnAdditive[j] += smallestSlack;
          continue;
        }
        long newSlack = slackHere - smallestSlack;
        slack[j] = newSlack;
        if (newSlack > 0)
        {
          continue;
        }
        // We have a new zero
        rowFound = slackRow[j];
        if (SHOW_WORKING)
        {
          System.out.println("Slack row is " + rowFound + " for column " + i);
        }
        if (foundNewIndependentZero)
        { // Only continuing to update additives and slack
          continue;
        }
        int rowMatchingHere = rowMatching[j];
        if (rowMatchingHere < 0)
        { // our new zero is independent
          if (SHOW_WORKING)
          {
            System.out.println("New zero set " + j + " to " + rowFound);
          }
          foundNewIndependentZero = true;
          increaseMatching(j, rowFound);
        }
        else
        {
          // Add this zero to the forest
          parentRow[j] = rowFound;
          nodesToDo[numNodesToDo++] = rowMatchingHere;
          if (SHOW_WORKING)
          {
            System.out.println("Add path node " + rowMatchingHere);
          }
        }
      }
      if (foundNewIndependentZero)
      {
        return;
      }
    }
    // Out before here if we found an extra zero. If not we managed to
    // cover the existing zeros with < matrix size lines, which means that
    // when we looked at slack we could reduce non-chosen rows to add a
    // zero that required a new line, while preserving the existing
    // independent zeros
    throw new IllegalStateException("I don't think we can be here");
    // increaseMatching(colFound, rowFound);
  }
  /** Call this to increase the number of matched cells */
  private void increaseMatching(int unmatchedColumn, int forestRow)
  {
    if (SHOW_WORKING)
    {
      System.out.println("Increase " + unmatchedColumn + ", row " + forestRow);
    }
    // We have something of the form R0-C1=R1-C2 where the final C
    // is unmatched. Flip - for = to increase the number of matches.
    // This works because the initial row was not chosen so we
    // don't introduce a row or column conflict. After this step
    // we rebuild the forest from scratch so we don't have to
    // worry about introducing inconsistency there.
    for (;;)
    {
      int nextColumn = columnMatching[forestRow];
      columnMatching[forestRow] = unmatchedColumn;
      rowMatching[unmatchedColumn] = forestRow;
      if (nextColumn < 0)
      {
        return;
      }
      forestRow = parentRow[nextColumn];
      // System.out.println("Parent of " + unmatchedColumn + " is " + forestRow);
      unmatchedColumn = nextColumn;
    }
  }
  /** return selected columns for each row */
  public int[] getSelectedColumnsPerRow()
  {
    return columnMatching.clone();
  }
  /** create and solve a test instance. Note that checkSolution()
   *  is called routinely from within the Assignment constructor so
   *  we can let it check itself.
   */
  private static void createAndSolve(Random r, int maxSize, int maxValue)
  {
    int rows = r.nextInt(maxSize + 1);
    int cols = r.nextInt(maxSize + 1);
    if (rows > cols)
    {
      int t = rows;
      rows = cols;
      cols = t;
    }
    System.out.println("Rows " + rows + " cols " + cols);
    long[][] matrix = new long[rows][];
    for (int i = 0; i < matrix.length; i++)
    {
      long[] row = new long[cols];
      for (int j = 0; j < row.length; j++)
      {
        row[j] = r.nextInt(maxValue + 1);
      }
      matrix[i] = row;
    }
    new Assignment(matrix);
  }
  /** main routine is for testing */
  public static void main(String[] s) throws Exception
  {
    long seed = 42;
    int maxSize = 10;
    int maxValue = 10000000;
    int goes = 10;
    int s1 = s.length - 1;
    boolean trouble = false;
    for (int i = 0; i < s.length; i++)
    {
      if ("-goes".equals(s[i]) && (i < s1))
      {
        goes = Integer.parseInt(s[++i].trim());
      }
      else if ("-maxSize".equals(s[i]) && (i < s1))
      {
        maxSize = Integer.parseInt(s[++i].trim());
      }
      else if ("-maxValue".equals(s[i]) && (i < s1))
      {
        maxValue = Integer.parseInt(s[++i].trim());
      }
      else if ("-seed".equals(s[i]) && (i < s1))
      {
        seed = Long.parseLong(s[++i].trim());
      }
      else
      {
        System.err.println("Cannot handle flag " + s[i]);
        trouble = true;
      }
    }
    if (trouble)
    {
      System.err.println("Args are [-goes #] [-maxSize #] [-maxValue #] " +
        "[-seed #]");
      return;
    }
    System.out.println("Goes " + goes + " max size " + maxSize + " max value " +
      maxValue + " seed " + seed);
    for (int i = 0; i < goes; i++)
    {
      System.out.println("i = " + i);
      Random r = new Random(seed + i);
      createAndSolve(r, maxSize, maxValue);
    }
  }
}
