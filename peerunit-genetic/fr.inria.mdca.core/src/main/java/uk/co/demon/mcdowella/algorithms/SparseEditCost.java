package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** This class computes the least cost way to turn one sequence into
 *  another, if that cost is &le; a pre-selected value. It makes use
 *  of this threshold to discard expensive sequences, and uses this -
 *  and a sparse array representation - to save both memory and cpu.
 *  This justification of this is that it uses the same dynamic
 *  programming recursion as EditCost - see that for more info.
 *  In practice this does not pay off except for very small numbers
 *  of changes.
 */
public class SparseEditCost extends AbstractEditCost
{
  /** interface to sparse array classes */
  private interface Sparse
  {
    /** get number of values currently stored */
    int getNumValues();
    /** get value at some arbitrary index of values running from 0
     *  to getNumValues() - 1
     */
    public double getValueAt(int x);
    /** get index at some arbitrary index of values running from 0
     *  to getNumValues() - 1
     */
    public int getIndexAt(int x);
    /** set */
    public void set(int index, double value);
    /** get */
    public double get(int index);
  }
  /** Cross-checking version */
  private static class CrossCheck implements Sparse
  {
    CrossCheck(double def)
    {
      v1 = new SparseArray(def);
      v2 = new ContigSparse(def);
    }
    private Sparse v1;
    private Sparse v2;
    /** get number of values currently stored */
    public int getNumValues()
    { // OK to store different number of values
      return v1.getNumValues();
    }
    /** get value at some arbitrary index of values running from 0
     *  to getNumValues() - 1
     */
    public double getValueAt(int x)
    {
      double result = v1.getValueAt(x);
      int index = v1.getIndexAt(x);
      if (result != v1.get(index))
      {
        throw new IllegalStateException("Self get mismatch at " +
	  index);
      }
      if (result != v2.get(index))
      {
        throw new IllegalStateException("GetValueAt mismatch at " +
	  index);
      }
      return result;
    }
    public int getIndexAt(int x)
    {
      return v1.getIndexAt(x);
    }
    public void set(int index, double value)
    {
      v1.set(index, value);
      v2.set(index, value);
    }
    /** get */
    public double get(int index)
    {
      double result = v1.get(index);
      if (result != v2.get(index))
      {
        throw new IllegalStateException("Get mismatch at " + index);
      }
      return result;
    }
  }
  private class CheckFactory implements SparseFactory
  {
    private final double defaultValue;
    CheckFactory(double def)
    {
      defaultValue = def;
    }
    public Sparse create()
    {
      return new CrossCheck(defaultValue);
    }
  }
  /** factory for Sparse */
  private interface SparseFactory
  {
    Sparse create();
  }
  /** Class to hold sparse array based on the hope that we only
   *  need to hold a contiguous section of values
   */
  private static class ContigSparse implements Sparse
  {
    /** default value - respond with this if no set yet */
    private final double defaultValue;
    /** create, given default value */
    ContigSparse(double def)
    {
      defaultValue = def;
    }
    /** space for store of values */
    double[] values = new double[5];
    /** minimum index of any stored value */
    int minStored = 0;
    /** maximum index of any stored value. Min and Max are set up
     *  at first to represent 0 stored values
     */
    int maxStored = -1;
    /** Add this to index to work out array offset to fetch or modify */
    int addToIndex = 0;
    public int getNumValues()
    {
      return maxStored - minStored + 1;
    }
    public int getIndexAt(int x)
    {
      return minStored + x;
    }
    public double getValueAt(int x)
    {
      return values[x + minStored + addToIndex];
    }
    public double get(int index)
    {
      if ((index < minStored) || (index > maxStored))
      {
        return defaultValue;
      }
      return values[index + addToIndex];
    }
    /** set may have to expand store to one side or another. The idea
     *  here is at an expansion of one side never reduces the space
     *  free on the other, and an expansion that involves a copy
     *  is always at least a doubling, so as to reduce the total
     *  copy overhead
     */
    public void set(int index, double value)
    {
      if (minStored > maxStored)
      { // nothing stored yet - centre on this value
        int target = values.length >> 1;
	values[target] = value;
	minStored = index;
	maxStored = index;
	addToIndex = target - index;
	return;
      }
      if ((index >= minStored) && (index <= maxStored))
      { // within stored range
        values[index + addToIndex] = value;
	return;
      }
      // Here if outside stored range
      if (value == defaultValue)
      { // don't need to expand for defaultValue
        return;
      }
      if (index > maxStored)
      { // expand to right
        int target = index + addToIndex;
	if (target >= values.length)
	{
	  int len = target + 1;
	  int len2 = values.length + values.length;
	  if (len < len2)
	  {
	    len = len2;
	  }
	  /*
	  System.err.println("Increase to " + len);
	  System.err.println("Len now " + len + " min " +
	    minStored + " max " + maxStored);
	  */
	  double[] newValues = new double[len];
	  int first = minStored + addToIndex;
	  System.arraycopy(values, first,
	    newValues, first, maxStored - minStored + 1);
	  values = newValues;
	}
	if (index > (maxStored + 1))
	{
	  /*
	  System.err.println("Target " + target + " len " +
	    values.length);
	  */
	  Arrays.fill(values, maxStored + addToIndex + 1, target,
	    defaultValue);
	}
	values[target] = value;
	maxStored = index;
	return;
      }
      // x < minStored: expand to left
      int target = index + addToIndex;
      if (target < 0)
      {
	int increase = -target;
	if (increase < values.length)
	{
	  increase = values.length;
	}
        int len = values.length + increase;
	// System.err.println("left len now " + len);
	double[] newValues = new double[len];
	int oldFirst = minStored + addToIndex;
	int newFirst = oldFirst + increase;
	System.arraycopy(values, oldFirst, newValues, newFirst,
	  maxStored - minStored + 1);
	addToIndex += increase;
	values = newValues;
	target = index + addToIndex;
      }
      if (index < (minStored - 1))
      {
	Arrays.fill(values, target + 1, minStored + addToIndex,
	  defaultValue);
      }
      values[target] = value;
      minStored = index;
    }
  }
  private static class ContigFactory implements SparseFactory
  {
    private final double defaultValue;
    ContigFactory(double def)
    {
      defaultValue = def;
    }
    public Sparse create()
    {
      return new ContigSparse(defaultValue);
    }
  }
  /** Need a class to hold a sparse array of doubles. Here we
   *  make use of the fact that we typically work through the
   *  array from left to right.
   */
  private static class SparseArray implements Sparse
  {
    /** Number of valid entries */
    private int numValid;
    /** indexes for entries, in ascending order */
    private int[] indexes = new int[5];
    /** values, parallel with indexes */
    private double[] values = new double[indexes.length];
    /** offset of location last accessed */
    private int lastOffset;
    /** default value */
    private final double defaultValue;
    SparseArray(double def)
    {
      defaultValue = def;
    }
    public int getNumValues()
    {
      return numValid;
    }
    public double getValueAt(int x)
    {
      return values[x];
    }
    public int getIndexAt(int x)
    {
      return indexes[x];
    }
    /** set */
    public void set(int index, double value)
    {
      // System.err.println("set " + value + " at " + index + " in " + this);
      int target = 0;
      if (lastOffset < numValid)
      { // see if we can find the index
        while ((indexes[lastOffset] > index) && (lastOffset > 0))
	{
	  lastOffset--;
	}
	// lastOffset is at or before the correct place
	int n1 = numValid - 1;
	while ((indexes[lastOffset] < index) &&
	  (lastOffset < n1))
	{
	  lastOffset++;
	}
	// lastOffset is at or after the correct place, or at end
	if (indexes[lastOffset] == index)
	{
	  // System.err.println("Set at " + lastOffset);
	  values[lastOffset] = value;
	  return;
	}
	target = lastOffset;
      }
      if ((target < numValid) && (indexes[target] < index))
      {
        target++;
      }
      int toMove = numValid - target;
      numValid++;
      lastOffset = target;
      // System.err.println("set at " + target);
      if (numValid > indexes.length)
      {
        int[] newIndexes = new int[indexes.length * 2 + 1];
	// System.err.println("Expand to len " + indexes.length);
	double[] newValues = new double[newIndexes.length];
	if (target > 0)
	{
	  System.arraycopy(indexes, 0, newIndexes, 0, target);
	  System.arraycopy(values, 0, newValues, 0, target);
	}
	newIndexes[target] = index;
	newValues[target] = value;
	if (toMove > 0)
	{
	  System.arraycopy(indexes, target, newIndexes, target + 1,
	    toMove);
	  System.arraycopy(values, target, newValues, target + 1,
	    toMove);
	}
	indexes = newIndexes;
	values = newValues;
	return;
      }
      if (toMove > 0)
      {
	/*
	for (int i = numValid - 1; i > target; i--)
	{
	  System.err.println("Set from " + indexes[i - 1]);
	  indexes[i] = indexes[i - 1];
	  values[i] = values[i - 1];
	}
	*/
	System.arraycopy(indexes, target, indexes, target + 1,
	  toMove);
	System.arraycopy(values, target, values, target + 1,
	  toMove);
      }
      if ((index == 0) && (target != 0))
      {
        throw new IllegalStateException("target set out of sync");
      }
      indexes[target] = index;
      values[target] = value;
      for (int i = 1; i < numValid; i++)
      {
        if (indexes[i] == 0)
	{
	  throw new IllegalStateException("0 index at " + i + 
	    " after target " + target + " numValid " + numValid +
	    " toMove " + toMove);
	}
      }
    }
    public double get(int index)
    {
      // System.err.println("lastOffset " + lastOffset);
      if (lastOffset < numValid)
      { // see if we can find the index
        while ((indexes[lastOffset] > index) && (lastOffset > 0))
	{
	  lastOffset--;
	}
	// lastOffset is at or before the correct place
	int n1 = numValid - 1;
	while ((indexes[lastOffset] < index) &&
	  (lastOffset < n1))
	{
	  lastOffset++;
	}
	// lastOffset is at or after the correct place
	if (indexes[lastOffset] == index)
	{
	  if ((index == 0) && (lastOffset != 0))
	  {
	    throw new IllegalStateException("Get 0 fail");
	  }
	  // System.err.println("Return " + values[lastOffset] + " at " +
	  //   index + " from " + this);
	  return values[lastOffset];
	}
      }
      return defaultValue;
    }
  }
  private static class SparseArrayFactory implements SparseFactory
  {
    private final double defaultValue;
    SparseArrayFactory(double def)
    {
      defaultValue = def;
    }
    public Sparse create()
    {
      return new SparseArray(defaultValue);
    }
  }
  /** factory for sparse array stuff */
  private final SparseFactory factory;
  /** create. Either use contiguous or sparse store for chunks
   *  of values held
   */
  public SparseEditCost(boolean useContigStore)
  {
    if (false)
    { // can put this in to check sparse stuff
      factory = new CheckFactory(Double.MAX_VALUE);
    }
    else if (useContigStore)
    {
      factory = new ContigFactory(Double.MAX_VALUE);
    }
    else
    {
      factory = new SparseArrayFactory(Double.MAX_VALUE);
    }
  }
  /** holds info for trace: least cost of transforming first
   *  m of from to first n of to is in step[n].getCost(m)
   *  for m in range 0..from.length and n in range 1..to.length
   */
  Sparse[] step;
  /** holds old target for trace */
  private int[] oldTo;
  /** holds old source for trace */
  private int[] oldFrom;
  public void setTrace(boolean x)
  {
    super.setTrace(x);
    if (!getTrace())
    {
      step = null;
      oldTo = null;
      oldFrom = null;
    }
  }
  /** compute cost using threshold doubling each time */
  public double computeCost(int[] from, int[] to)
  {
    // Work out minimum possible cost
    double minCopy = getCopyCost();
    double proposed = getReplaceCost();
    if (proposed < minCopy)
    {
      minCopy = proposed;
    }
    proposed = getDeleteCost() + getInsertCost();
    if (proposed < minCopy)
    {
      minCopy = proposed;
    }
    // minimum possible cost increment
    double delta = getReplaceCost() - minCopy;
    proposed = getInsertCost() - minCopy;
    if ((proposed > 0.0) && ((proposed < delta) || (delta <= 0.0)))
    {
      delta = proposed;
    }
    proposed = getDeleteCost() - minCopy;
    if ((proposed > 0.0) && ((proposed < delta) || (delta <= 0.0)))
    {
      delta = proposed;
    }
    double minCost;
    if (from.length > to.length)
    {
      minCost = minCopy * to.length +
        getDeleteCost() * (from.length - to.length);
    }
    else
    {
      minCost = minCopy * from.length +
        getInsertCost() * (to.length - from.length);
    }
    proposed = minCost;
    boolean changedDelta = false;
    for (;;)
    {
      double result = computeCost(from, to, proposed);
      if (result <= proposed)
      {
	if (changedDelta && false)
	{
	  /*
	  System.err.println("Result " + result + " min " +
	    minCost);
	  */
	}
	return result;
      }
      if (delta <= 0.0)
      {
	// delta <= 0.0 => we should have a solution at min cost
	// but might just have rounding error
	delta = proposed * 0.1;
	changedDelta = true;
      }
      proposed += delta;
      delta *= 2;
    }
  }
  public double computeCost(int[] from, int[] to, double maxCost)
  {
    // System.err.println("Maxcost is " + maxCost);
    oldTo = (int[]) to.clone();
    oldFrom = (int[]) from.clone();
    if (getTrace())
    {
      step = new Sparse[oldTo.length + 1];
    }
    else
    {
      step = null;
    }
    // This holds the min cost required to transform the first
    // 0, 1, 2, ... from.length elements of from to the first
    // <iteration> elements of the other
    Sparse oldCost = factory.create();
    // Work out the initial costs, which must come from deletions
    // as the target length is 0
    double sofar = 0.0;
    // System.err.println("MaxCost is " + maxCost);
    for (int i = 0; i <= oldFrom.length; i++)
    {
      if (sofar > maxCost)
      {
        break;
      }
      oldCost.set(i, sofar);
      // System.err.println("Set " + i + " to " + sofar);
      sofar += getDeleteCost();
    }
    /*
    System.err.print("First costs");
    for (int i = 0; i <= oldFrom.length; i++)
    {
      System.err.print(' ');
      System.err.print(oldCost.get(i));
    }
    System.err.println();
    */
    step[0] = oldCost;
    // System.err.println("Initial 0 is " + step[0].get(0));
    for (int i = 0; i < oldTo.length; i++)
    { // work out the lowest cost way of transforming to the
      // first i+1 elements of the target

      Sparse newCost = factory.create();
      final int num = oldCost.getNumValues();
      if (num <= 0)
      { // all solutions too expensive
        return Double.MAX_VALUE;
      }
      for (int j = 0; j < num; j++)
      {
	// cost of transforming first numFrom elements of from
	// to first i elements of the target: take a solution that
	// produces the first i-1 elements and extend it
        double costHere = oldCost.getValueAt(j);
	if (costHere >= Double.MAX_VALUE)
	{ // forget about these
	  continue;
	}
	int numFrom = oldCost.getIndexAt(j);
	// System.err.println("Old cost " + costHere + " at " + numFrom);
	if (numFrom < oldFrom.length)
	{
	  int numFrom1 = numFrom + 1;
	  if (oldFrom[numFrom] == oldTo[i])
	  { // could try copy
	    double proposed = costHere + getCopyCost();
	    if ((proposed < newCost.get(numFrom1)) &&
	        (proposed <= maxCost))
	    {
	      /*
	      System.err.println("Set copy " + proposed + " at " +
	        numFrom1);
	      */
	      newCost.set(numFrom1, proposed);
	    }
	  }
	  // try replace
	  double proposed = costHere + getReplaceCost();
	  if ((proposed < newCost.get(numFrom1)) &&
	      (proposed <= maxCost))
	  {
	    /*
	    System.err.println("Set replace " + proposed + " at " +
	      numFrom1);
	    */
	    newCost.set(numFrom1, proposed);
	  }
	}
	// try insert
	double proposed = costHere + getInsertCost();
	if ((proposed < newCost.get(numFrom)) &&
	    (proposed <= maxCost))
	{
	  /*
	  System.err.println("Set insert " + proposed + " at " +
	    numFrom);
	  */
	  newCost.set(numFrom, proposed);
	  // System.err.println("Value back is " + newCost.get(numFrom));
	}
      }
      // use the values that produce i+1 from j to produce
      // i+1 from j+1, j+2, ... via delete
      // Note that the array can grow as we work, and we have to
      // keep up with that
      for (int j = 0; j < newCost.getNumValues(); j++)
      {
        double costHere = newCost.getValueAt(j);
	if (costHere >= Double.MAX_VALUE)
	{
	  continue;
	}
	int target = newCost.getIndexAt(j) + 1;
	if (target > oldFrom.length)
	{ // don't care about any source longer than original
	  break;
	}
	// System.err.println("Start from targeting " + target);
	for (int numFrom = target; numFrom <= oldFrom.length; numFrom++)
	{
	  costHere += getDeleteCost();
	  if (costHere > maxCost)
	  { // cost must be too high
	    break;
	  }
	  if (costHere < newCost.get(numFrom))
	  {
	    /*
	    System.err.println("Set delete at " + numFrom + " cost " +
	      costHere);
	    */
	    newCost.set(numFrom, costHere);
	  }
	  else
	  { // the value we have just failed to replace is
	    // a better starting point
	    /*
	    System.err.println("No better at " + numFrom + " was " +
	      newCost.get(numFrom));
	    */
	    break;
	  }
	}
      }
      oldCost = newCost;
      step[i + 1] = newCost;
      /*
      System.err.print((i + 1) + " costs");
      for (int j = 0; j <= oldFrom.length; j++)
      {
	System.err.print(' ');
	System.err.print(newCost.get(j));
      }
      System.err.println();
      */
    }
    return oldCost.get(oldFrom.length);
  }
  /** Trace back to find sequence of commands attaining the minimum
   *  cost. This should be able to find such a sequence despite floating
   *  point error, because it computes the same partial sums in the
   *  same order as computeCost(). That might not be true in some
   *  circumstances if some intermediates are ever calculated to 
   *  different, precision, in which case this probably becomes an
   *  integer-only algorithm
   */
  public int[] getCommands()
  {
    if (step == null)
    {
      throw new IllegalStateException("No trace info");
    }
    if (step[step.length - 1].getNumValues() <= 0)
    {
      throw new IllegalStateException("All solutions too costly");
    }
    List reverseCommands = new ArrayList();
    int numFrom = oldFrom.length;
    int numTo = oldTo.length;
    while ((numTo > 0) || (numFrom > 0))
    {
      double cost = step[numTo].get(numFrom);
      /*
      System.err.println("Attempt cost " + cost + " with to " +
        numTo + " from " + numFrom);
      */
      if (numFrom > 0)
      {
	// perhaps command before this was a delete
	double proposedCost = step[numTo].get(numFrom - 1) +
	  getDeleteCost();
	// System.err.println("Delete proposed " + proposedCost);
	if (proposedCost <= cost)
	{
	  reverseCommands.add(new Integer(AbstractEditCost.DELETE));
	  numFrom--;
	  continue;
	}
      }
      if (numTo > 0)
      {
	// perhaps it was an insert
	double proposedCost = step[numTo - 1].get(numFrom) +
	  getInsertCost();
	// System.err.println("Insert proposed " + proposedCost);
	if (proposedCost <= cost)
	{
	  numTo--;
	  reverseCommands.add(new Integer(oldTo[numTo]));
	  reverseCommands.add(new Integer(AbstractEditCost.INSERT));
	  continue;
	}
        if (numFrom > 0)
	{
	  // Must have been a replace or a copy
	  numFrom--;
	  numTo--;
	  if (oldFrom[numFrom] == oldTo[numTo])
	  {
	    proposedCost = step[numTo].get(numFrom) +
	      getCopyCost();
	    // System.err.println("Copy proposed " + proposedCost);
	    if (proposedCost <= cost)
	    {
	      reverseCommands.add(new Integer(AbstractEditCost.COPY));
	      continue;
	    }
	  }
	  // had better be a replace
	  proposedCost = step[numTo].get(numFrom) + getReplaceCost();
	  /*
	  System.err.println("Step " + numTo + ", " + numFrom + " is " +
	   step[numTo].get(numFrom));
	  System.err.println("Replace proposed " + proposedCost);
	  */
	  if (proposedCost <= cost)
	  {
	    reverseCommands.add(new Integer(oldTo[numTo]));
	    reverseCommands.add(new Integer(AbstractEditCost.REPLACE));
	    continue;
	  }
	}
	throw new IllegalStateException("Cannot trace back");
      }
    }
    int[] result = new int[reverseCommands.size()];
    final int r1 = result.length - 1;
    for (int i = 0; i < result.length; i++)
    {
      result[r1 - i] = ((Integer)reverseCommands.get(i)).intValue();
    }
    return result;
  }
  public static void main(String[] s)
  {
    SparseEditCost ec = new SparseEditCost(false);
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
