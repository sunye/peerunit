package uk.co.demon.mcdowella.algorithms;

/** 
 *  Abstract super class for edit distance classes
 */
public abstract class AbstractEditCost
{
  /** cost of a deletion */
  private double deleteCost = 1.0;
  /** check proposed cost is >= 0.0 */
  private void checkCost(double x)
  {
    if (x < 0.0)
    {
      throw new IllegalArgumentException("Cost " + x + " < 0.0");
    }
  }
  /** set the cost of a deletion */
  public void setDeleteCost(double x)
  {
    checkCost(x);
    deleteCost = x;
  }
  /** get the cost of a deletion */
  public final double getDeleteCost()
  {
    return deleteCost;
  }
  /** cost of an insertion */
  private double insertCost = 1.0;
  /** set the cost of an insertion */
  public void setInsertCost(double x)
  {
    checkCost(x);
    insertCost = x;
  }
  /** get the cost of an insertion */
  public final double getInsertCost()
  {
    return insertCost;
  }
  /** cost of a copy */
  private double copyCost = 0.0;
  /** set the cost of a copy */
  public void setCopyCost(double x)
  {
    checkCost(x);
    copyCost = x;
  }
  /** get the cost of a copy */
  public final double getCopyCost()
  {
    return copyCost;
  }
  /** cost of replace */
  private double replaceCost = 1.0;
  /** set the cost of a replacement */
  public void setReplaceCost(double x)
  {
    checkCost(x);
    replaceCost = x;
  }
  /** get the cost of a replacement */
  public final double getReplaceCost()
  {
    return replaceCost;
  }
  /** whether we want to trace back the answer */
  private boolean trace = true;
  /** set whether we want to trace back the answer. Default is true
   */
  public void setTrace(boolean x)
  {
    trace = true;
  }
  public final boolean getTrace()
  {
    return trace;
  }
  /** insert step */
  public static final int INSERT = 1;
  /** copy step */
  public static final int COPY = 2;
  /** delete step */
  public static final int DELETE = 3;
  /** replace step */
  public static final int REPLACE = 4;
  /** Compute the least const transform from one sequence of ints
   *  to another, returning the cost if possible, or Double.MAX_VALUE
   *  if not.
   */
  public abstract double computeCost(final int[] from, final int[] to);
  /** return the commands required to transform from to to. Each command
   *  is INSERT, COPY, or DELETE, with INSERT and REPLACE followed by
   *  the integer to insert or replace.
   */
  public abstract int[] getCommands();
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
  /** turn a string into an integer array */
  public static int[] toIntArray(String s)
  {
    int[] result = new int[s.length()];
    for (int i = 0; i < result.length; i++)
    {
      result[i] = s.charAt(i);
    }
    return result;
  }
  /** apply commands, throwing exception if input not exactly consumed 
   *  or rubbish commands
   */
  public static int[] apply(int[] commands, int[] input)
  {
    int len = 0;
    for (int i = 0; i < commands.length; i++)
    {
      switch (commands[i])
      {
        case INSERT:
	  len++;
	  i++;
	break;
	case DELETE:
	break;
	case COPY:
	  len++;
	break;
	case REPLACE:
	  len++;
	  i++;
	break;
	default:
	  throw new IllegalArgumentException("Invalid command " +
	    commands[i]);
      }
    }
    int[] result = new int[len];
    int wp = 0;
    int rp = 0;
    for (int i = 0; i < commands.length; i++)
    {
      switch (commands[i])
      {
        case INSERT:
	  result[wp++] = commands[++i];
	break;
	case DELETE:
	  rp++;
	break;
	case COPY:
	  if (rp >= input.length)
	  {
	    throw new IllegalArgumentException("Ran out of input");
	  }
	  result[wp++] = input[rp++];
	break;
	case REPLACE:
	  if (rp >= input.length)
	  {
	    throw new IllegalArgumentException("Ran out of input");
	  }
	  result[wp++] = commands[++i];
	  rp++;
	break;
	default:
	  throw new IllegalArgumentException("Invalid command " +
	    commands[i]);
      }
    }
    if (rp != input.length)
    {
      throw new IllegalArgumentException("Input not consumed");
    }
    return result;
  }
}
