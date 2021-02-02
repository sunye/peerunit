package uk.co.demon.mcdowella.algorithms;

/** This class works out the least cost way to transform one
 *  sequence of integer to another, with the allowable editing
 *  steps being the deletion, insertion, copying, and replacement
 *  of single integers
 */
public class EditCost extends AbstractEditCost
{
  /** set whether we want to trace back the answer. Default is true
   */
  public void setTrace(boolean x)
  {
    super.setTrace(x);
    if (!getTrace())
    {
      step = null;
      oldTo = null;
    }
  }
  /** holds info for trace: least cost of transforming first
   *  m of from to first n of to is in step[m][n-1]
   *  for m in range 0..from.length and n in range 1..to.length
   */
  private int[][] step;
  /** holds old target for trace */
  private int[] oldTo;
  /** Compute the least const transform from one sequence of ints
   *  to another, returning the cost. We treat this as a dynamic
   *  programming problem. To compute the least cost way of transforming
   *  the subsequence consisting of the first m elements of one
   *  sequence to the first n elements of another, we use the least
   *  costs of transforming (m-1, n-1), (m-1, n), and (m, n-1) lengths.
   *  This version of computeCost should always succeed.
   */
  public double computeCost(final int[] from, final int[] to)
  {
    oldTo = (int[]) to.clone();
    final int[] oldFrom = (int[])from.clone();
    if (getTrace())
    {
      step = new int[oldFrom.length + 1][oldTo.length];
    }
    else
    {
      step = null;
    }
    // This holds the min cost required to transform the first
    // 0, 1, 2, ... from.length elements of from to the first
    // <iteration> elements of the other
    double[] oldCost = new double[oldFrom.length + 1];
    // Work out the initial costs, which must come from deletions
    // as the target length is 0
    double sofar = 0.0;
    for (int i = 0; i < oldCost.length; i++)
    {
      oldCost[i] = sofar;
      sofar += getDeleteCost();
    }
    for (int i = 0; i < oldTo.length; i++)
    { // work out the lowest cost way of transforming to the
      // first i+1 elements of the target

      // holds previous value of cost most recently modified
      // for j > 0
      double prevCost = Double.MAX_VALUE;
      for (int j = 0; j < oldCost.length; j++)
      {
        // Here to find the cost of transforming the first j elements
	// of from to the first i+1 elements of to

	// cost of inserting an element to extend the previous match
	double minSofar = oldCost[j] + getInsertCost();
	double was = oldCost[j];
	// transform first j to i and then insert an element
	int decision = INSERT;
	if (j > 0)
	{
	  // cost of transforming the first j-1 elements of from to
	  // the first i+1 elements of to and then deleting the jth
	  // element of from.
	  double perhaps = oldCost[j - 1] + getDeleteCost();
	  if (perhaps < minSofar)
	  {
	    minSofar = perhaps;
	    decision = DELETE;
	  }
	  if (oldFrom[j - 1] == oldTo[i])
	  {
	    // cost of transforming the first j-1 elements of from to
	    // the first i elements of to and then copying in an element
	    perhaps = prevCost + getCopyCost();
	    if (perhaps < minSofar)
	    {
	      minSofar = perhaps;
	      decision = COPY;
	    }
	  }
	  // cost of transforming the first j-1 elements of from to
	  // the first i elements of to and then replacing the jth
	  // element
	  perhaps = prevCost + getReplaceCost();
	  if (perhaps < minSofar)
	  {
	    minSofar = perhaps;
	    decision = REPLACE;
	  }
	}
	if (step != null)
	{
	  step[j][i] = decision;
	}
	prevCost = was;
	oldCost[j] = minSofar;
      }
    }
    if (!getTrace())
    {
      oldTo = null;
    }
    return oldCost[oldFrom.length];
  }
  /** return the commands required to transform from to to. Each command
   *  is INSERT, COPY, or DELETE, with INSERT and REPLACE followed by
   *  the integer to insert or replace. Most recent computeCost most
   *  have been with trace on, and trace must not have been switched
   *  off since then.
   */
  public int[] getCommands()
  {
    if (step == null)
    {
      throw new IllegalStateException("No trace info");
    }
    // First work out the length of array we need to hold the result
    int len = 0;
    int fromPos = step.length - 1;
    int toPos;
    for (toPos = step[0].length - 1; toPos >= 0;)
    {
      // Here with a best route which involves transforming
      // the first pos elements of from to the first toPos+1 elements of
      // to
      int code = step[fromPos][toPos];
      len++;
      if ((code == INSERT) || (code == REPLACE))
      {
        len++;
      }
      if (code != INSERT)
      { // step consumes an element of the source
        fromPos--;
      }
      if (code != DELETE)
      { // step produces an element of the target
        toPos--;
      }
    }
    // need this many deletes in the begining
    len += fromPos;
    int[] result = new int[len];
    int wp = result.length;
    fromPos = step.length - 1;
    for (toPos = step[0].length - 1; toPos >= 0;)
    {
      int code = step[fromPos][toPos];
      if ((code == INSERT) || (code == REPLACE))
      {
        result[--wp] = oldTo[toPos];
      }
      if (code != INSERT)
      {
        fromPos--;
      }
      if (code != DELETE)
      {
        toPos--;
      }
      result[--wp] = code;
    }
    while (wp > 0)
    {
      result[--wp] = DELETE;
    }
    return result;
  }
  /** translate commands */
  public static String translate(int[] commands)
  {
    StringBuffer sb = new StringBuffer();
    String sep = "";
    for (int i = 0; i < commands.length; i++)
    {
      sb.append(sep);
      int code = commands[i];
      switch (code)
      {
        case INSERT:
	  sb.append("insert ");
	  sb.append((char)commands[++i]);
	break;
	case DELETE:
	  sb.append("delete");
	break;
	case COPY:
	  sb.append("copy");
	break;
	case REPLACE:
	  sb.append("replace ");
	  sb.append((char)commands[++i]);
	break;
	default:
	  throw new IllegalArgumentException("Bad command");
      }
      sep = " ";
    }
    return sb.toString();
  }
  public static void main(String[] s)
  {
    EditCost ec = new EditCost();
    final int[] from = toIntArray(s[0]);
    final int[] to = toIntArray(s[1]);
    double cost = ec.computeCost(from, to);
    System.err.println("Cost from " + s[0] + " to " + s[1] + " is " +
      cost);
    int[] commands = ec.getCommands();
    System.err.println(EditCost.translate(commands));
    int[] check = apply(commands, from);
    if (check.length != to.length)
    {
      throw new IllegalArgumentException("Lengths differ");
    }
    for (int i = 0; i < check.length; i++)
    {
      if (check[i] != to[i])
      {
        throw new IllegalArgumentException("Commands do not work");
      }
    }
  }
}
