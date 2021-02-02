package uk.co.demon.mcdowella.algorithms;

import java.util.Arrays;
import java.util.Random;

/** This class wraps the operations of matrix * vector and
    vector * matrix. For the moment we use it to be more
    scrupulous about floating point rounding. We might later
    produce a version that takes note of sparsity. */
public class MatWrapper
{
  /** return dot product of two dense vectors */
  public static double dot(int n, int off1, int stride1, double[] d1,
    int off2, int stride2, double[] d2)
  {
    final int o1 = off1;
    final int o2 = off2;
    double total = 0.0;
    // Standard version has rounding problems
    for (int i = 0; i < n; i++)
    {
      total += d1[off1] * d2[off2];
      off1 += stride1;
      off2 += stride2;
    }
    // which we hope to reduce by treating everything as an offset
    // from the mean
    double meanTotal = total / n;
    off1 = o1;
    off2 = o2;
    double correction = 0.0;
    for (int i = 0; i < n; i++)
    {
      correction += (d1[off1] * d2[off2] - meanTotal);
      off1 += stride1;
      off2 += stride2;
    }
    return total + correction;
  }
  /** column index of non-zero elements by row */
  private int[][] xcoord;
  /** row index of non-zero rows, with row 0 at the top */
  private int[] ycoord;
  /** values of non-zero elements by row, parallel to coord */
  private double[][] values;
  /** original number of rows */
  private final int numRows;
  /** original number of columns */
  private final int numCols;
  /** Create given a dense matrix, stored with rows contiguous */
  public MatWrapper(int forNumRows, int forNumCols, double[] data)
  {
    numRows = forNumRows;
    numCols = forNumCols;
    int[][] forXcoord = new int[numRows][];
    double[][] forValues = new double[numRows][];
    for (int i = 0; i < numRows; i++)
    {
      // count number of non-zero values first
      int numHere = 0;
      for (int j = 0; j < numCols; j++)
      {
        if (data[i * numCols + j] != 0.0)
	{
	  numHere++;
	}
      }
      if (numHere == 0)
      {
        continue;
      }
      int[] pos = new int[numHere];
      forXcoord[i] = pos;
      double[] val = new double[numHere];
      forValues[i] = val;
      numHere = 0;
      for (int j = 0; j < numCols; j++)
      {
	double d = data[i * numCols + j];
        if (data[i * numCols + j] != 0.0)
	{
	  pos[numHere] = j;
	  val[numHere] = d;
	  numHere++;
	}
      }
    }
    // now count occupied rows
    int numHere = 0;
    for (int i = 0; i < numRows; i++)
    {
      if (forXcoord[i] != null)
      {
        numHere++;
      }
    }
    xcoord = new int[numHere][];
    ycoord = new int[numHere];
    values = new double[numHere][];
    numHere = 0;
    for (int i = 0; i < numRows; i++)
    {
      if (forXcoord[i] == null)
      {
        continue;
      }
      xcoord[numHere] = forXcoord[i];
      ycoord[numHere] = i;
      values[numHere] = forValues[i];
      numHere++;
    }
  }
  /** Print routine */
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    int rp = 0;
    for (int i = 0; i < ycoord.length; i++, rp++)
    {
      while (rp < ycoord[i])
      {
        sb.append("()");
	rp++;
      }
      int[] xc = xcoord[i];
      double[] vs = values[i];
      sb.append("(");
      String sep = "";
      int vp = 0;
      for (int j = 0; j < xc.length; j++, vp++)
      {
	while (vp < xc[j])
	{
	  sb.append(sep);
	  sb.append("0");
	  sep = ",";
	  vp++;
	}
	sb.append(sep);
	sep = ",";
	sb.append(vs[j]);
      }
      for (int j = vp; j < numCols; j++)
      {
	  sb.append(sep);
	  sb.append("0");
	  sep = ",";
      }
      sb.append(")");
    }
    for (int i = ycoord.length; i < numRows; i++)
    {
      sb.append("()");
    }
    sb.append("]");
    return sb.toString();
  }
  /** private constructor for createColWrapper() and any others */
  private MatWrapper(int[][] forXCoord, int[] forYCoord, double[][] forValues,
    int forNumRows, int forNumCols)
  {
    numRows = forNumRows;
    numCols = forNumCols;
    xcoord = forXCoord;
    ycoord = forYCoord;
    values = forValues;
  }
  /** create matrix representing just a single column from the given 
      matrix. Set private since not needed for SimplexSparse and so
      not exercised.
      */
  private static MatWrapper createColWrapper(int numRows, int numCols,
    double[] data, int col)
  {
    int numHere = 0;
    for (int i = 0; i < numRows; i++)
    {
      if (data[i * numCols + col] != 0)
      {
        numHere++;
      }
    }
    int[][] xcoord = new int[numHere][];
    int[] ycoord = new int[numHere];
    double[][] values = new double[numHere][];
    numHere = 0;
    for (int i = 0; i < numRows; i++)
    {
      double d = data[i * numCols + col];
      if (d == 0)
      {
        continue;
      }
      xcoord[numHere] = new int[] {col};
      ycoord[numHere] = i;
      values[numHere] = new double[] {d};
    }
    return new MatWrapper(xcoord, ycoord, values, numRows, numCols);
  }
  /** pre-Multiply dense column vector by matrix */
  public void preMult(double[] incol, double[] outcol)
  {
    for (int i = 0; i < ycoord.length; i++)
    {
      double total = 0.0;
      int[] cx = xcoord[i];
      double[] v = values[i];
      for (int j = 0; j < cx.length; j++)
      {
        total += v[j] * incol[cx[j]];
      }
      double meanTotal = total / cx.length;
      double correction = 0.0;
      for (int j = 0; j < cx.length; j++)
      {
        correction += (v[j] * incol[cx[j]] - meanTotal);
      }
      outcol[ycoord[i]] = total + correction;
    }
  }
  /** post-multiply dense row vector by matrix */
  public void postMult(double[] inrow, double[] outrow)
  {
    // starts off as running total and ends up as mean
    double[] meanTotal = new double[numCols];
    // starts off as running count and then ends up saving total
    double[] count = new double[numCols];
    for (int i = 0; i < xcoord.length; i++)
    {
      int[] xc = xcoord[i];
      double[] vc = values[i];
      double in = inrow[ycoord[i]];
      for (int j = 0; j < xc.length; j++)
      {
	// Now looking at matrix entry M[ycoord[i], xc[j]]
	// So form inrow[ycoord[i]] * M[ycoord[i], xc[j]]
	int xpos = xc[j];
        meanTotal[xc[j]] += in * vc[j];
	count[xpos] += 1.0;
      }
    }
    for (int i = 0; i < meanTotal.length; i++)
    {
      outrow[i] = 0.0;
      double c = count[i];
      if (c > 0.0)
      {
	count[i] = meanTotal[i];
        meanTotal[i] = meanTotal[i] / c;
      }
    }
    // Count is now 0.0 either because that column has never been written
    // to, or because the total written to that column is 0.0
    for (int i = 0; i < xcoord.length; i++)
    {
      int[] xc = xcoord[i];
      double[] vc = values[i];
      double in = inrow[ycoord[i]];
      for (int j = 0; j < xc.length; j++)
      {
	// Now looking at matrix entry M[ycoord[i], xc[j]]
	// So form inrow[ycoord[i]] * M[ycoord[i], xc[j]]
	int xpos = xc[j];
        outrow[xpos] += (in * vc[j] - meanTotal[xpos]);
      }
    }
    for (int i = 0; i < meanTotal.length; i++)
    {
      outrow[i] += count[i];
    }
  }
  /** return (dense) row */
  double[] getRow(int rowNum)
  {
    int offset = -1;
    if (ycoord.length == numRows)
    {
      offset = rowNum;
    }
    else
    {
      for (int i = 0; i < ycoord.length; i++)
      {
        if (ycoord[i] == rowNum)
	{
	  offset = i;
	  break;
	}
	if (ycoord[i] > rowNum)
	{
	  return new double[numCols];
	}
      }
    }
    double[] result = new double[numCols];
    int[] x = xcoord[offset];
    double[] d = values[offset];
    for (int i = 0; i < x.length; i++)
    {
      result[x[i]] = d[i];
    }
    return result;
  }
  /** produce matrix by selecting specified columns */
  public MatWrapper selectColumns(int colnums[])
  {
    // System.out.println("Selecting " + Arrays.toString(colnums));
    boolean[] chosen = new boolean[numCols];
    for (int i = 0; i < colnums.length; i++)
    {
      int x = colnums[i];
      if (chosen[x])
      {
        throw new IllegalArgumentException("Column " + x + " already chosen");
      }
      chosen[x] = true;
    }
    int[][] forx = new int[xcoord.length][];
    double[][] forvalues = new double[xcoord.length][];
    int[] livesAt = new int[numCols];
    Arrays.fill(livesAt, -1);
    for (int i = 0; i < xcoord.length; i++)
    {
      int[] xc = xcoord[i];
      int numHere = 0;
      for (int j = 0; j < xc.length; j++)
      {
	int xHere = xc[j];
        livesAt[xHere] = j;
	if (chosen[xHere])
	{
	  numHere++;
	}
      }
      // System.out.println("LivesAt " + Arrays.toString(livesAt) + " numHere " + numHere);
      double[] xd = values[i];
      int[] tx = new int[numHere];
      forx[i] = tx;
      double[] td = new double[numHere];
      forvalues[i] = td;
      int wp = 0;
      for (int j = 0; j < colnums.length; j++)
      {
	int index = livesAt[colnums[j]];
        if (index >= 0)
	{
	  tx[wp] = j;
	  td[wp] = xd[index];
	  wp++;
	}
      }
      for (int j = 0; j < xc.length; j++)
      {
        livesAt[xc[j]] = -1;
      }
    }
    return new MatWrapper(forx, ycoord, forvalues, numRows, colnums.length);
  }
  /** produce inverse of given matrix */
  public MatWrapper inverse()
  {
    if (numRows != numCols)
    {
      throw new IllegalArgumentException("Trying to invert non-square matrix");
    }
    if (ycoord.length != numRows)
    {
      // matrix has at least one all-zero row
      throw new IllegalArgumentException("Trying to invert singular matrix");
    }
    // We can view a Matrix as something that operates on another
    // matrix by forcing it to add multiples of one row to another
    // and scale them. This is multiplication from the left, if
    // we consider the impact of one row in the left hand matrix
    // at a time. In this view, an inverse matrix to M is one which
    // operates on M in such a way as to reduce it to the identity
    // matrix. If we apply the same transformations starting with the
    // identity matrix we get the same as multiplying I by our inverse.
    // This produces the inverse out of thin air.

    // We perform our transformation by repeatedly finding the element of
    // largest absolute value in a row and column not taken, dividing that
    // row by it, and then subtracting multiples of that row from every other
    // row not taken so as to render the rest of that column 0.
    // This actually produces, not the identity matrix, but a permutation matrix.
    // We will deal with that later on.

    // Start by copying stuff we are going to invert
    int[][] xcoordHere = xcoord.clone();
    int[] ycoordHere = ycoord.clone();
    double[][] valuesHere = values.clone();
    for (int i = 0; i < valuesHere.length; i++)
    {
      valuesHere[i] = valuesHere[i].clone();
    }
    // Build the inverse matrix here
    int[][] forXCoord = new int[numRows][];
    double[][] forValues = new double[numRows][];
    // At stage i, the first i elements of this are rows we have not
    // yet pivoted on
    int[] rowsLeft = new int[numRows];
    for (int i = 0; i < numRows; i++)
    {
      forXCoord[i] = new int[] {i};
      forValues[i] = new double[] {1.0};
      rowsLeft[i] = i;
    }
    // record columns used so far
    boolean[] doneCol = new boolean[numCols];
    for (int i = 0; i < numRows; i++)
    {
      // find maximum absolute value
      int offsetInLeft = -1;
      int colWithMax = -1;
      double origValue = 0.0;
      double maxFound = 0.0;
      int pastLeft = numRows - i;
      for (int j = 0; j < pastLeft; j++)
      {
        int x = rowsLeft[j];
	double[] vs = valuesHere[x];
	int[] xc = xcoordHere[x];
	for (int k = 0; k < vs.length; k++)
	{
	  if (doneCol[xc[k]])
	  {
	    System.out.println("Skip col " + k);
	    continue;
	  }
	  double a = Math.abs(vs[k]);
	  // System.out.println("Check " + a);
	  if (a <= maxFound)
	  {
	    continue;
	  }
	  maxFound = a;
	  offsetInLeft = j;
	  // System.out.println("Maxfound " + a + " at " + j);
	  colWithMax = xc[k];
	  origValue = vs[k];
	}
      }
      if (offsetInLeft < 0)
      {
        throw new IllegalArgumentException("Matrix is singular");
      }
      doneCol[colWithMax] = true;
      int rowWithMax = rowsLeft[offsetInLeft];
      rowsLeft[offsetInLeft] = rowsLeft[numRows - i - 1];
      // System.out.println("Row with max is " + rowWithMax);
      double[] pivotValues = valuesHere[rowWithMax];
      int[] pivotCoords = xcoordHere[rowWithMax];
      int[] invCoords = forXCoord[rowWithMax];
      // Scale row containing value
      for (int j = 0; j < pivotCoords.length; j++)
      {
        pivotValues[j] = pivotValues[j] / origValue;
      }
      double[] invValues = forValues[rowWithMax];
      for (int j = 0; j < invValues.length; j++)
      {
        invValues[j] = invValues[j] / origValue;
      }
      // Subtract multiple of this row from every other row
      // to null out pivot column
      for (int j = 0; j < numRows; j++)
      {
	// System.out.println("I = " + i + " j = " + j);
	if (j == rowWithMax)
	{
	  continue;
	}
        int[] targetCoords = xcoordHere[j];
	int colOffset = -1;
	for (int k = 0; k < targetCoords.length; k++)
	{
	  int x = targetCoords[k];
	  if (x == colWithMax)
	  {
	    colOffset = k;
	    break;
	  }
	  if (x > colWithMax)
	  { // gone past target
	    break;
	  }
	}
	if (colOffset < 0)
	{ // already zero here
	  continue;
	}
	double subtractThis = valuesHere[j][colOffset];
	// do subtraction
	subtractMultiple(xcoordHere, valuesHere, j,
	  subtractThis, pivotCoords, pivotValues);
	// do the same thing to the inverse we are building
	/*
	System.out.println("Before subtract got " +
	  Arrays.toString(forValues[j]) + " at " + Arrays.toString(forXCoord[j]) +
	  " minus " + Arrays.toString(invValues) + " at " + Arrays.toString(invCoords));
	*/
	subtractMultiple(forXCoord, forValues, j,
	  subtractThis, invCoords, invValues);
	/*
	System.out.println("After subtract got " +
	  Arrays.toString(forValues[j]) + " at " + Arrays.toString(forXCoord[j]));
	System.out.println("Subtract out " + subtractThis + " of " +
	  rowWithMax + " from row " + j);
	*/
      }
    }
    // We now have a matrix represented in forXCoord and forValues that will
    // turn the original matrix into the permutation matrix represented in
    // xcoordHere and valuesHere. Again think of it as operating on the original
    // matrix by considering the impact of each of its rows, which produces a
    // row in the product of the form (0,0,....0,1,0,....0). All we need to do
    // to get the inverse matrix we want is to put those rows in the right order
    // in the result
    int[][] reorderedXCoord = new int[numRows][];
    double[][] reorderedValues = new double[numRows][];
    for (int i = 0; i < numRows; i++)
    {
      int closest = -1;
      double distance = Double.MAX_VALUE;
      double[] vh = valuesHere[i];
      int[] xc = xcoordHere[i];
      // System.out.println("Values at " + i + " are " + Arrays.toString(vh) + " at " +
      //   Arrays.toString(xc));
      for (int j = 0; j < vh.length; j++)
      {
        double d = Math.abs(vh[j] - 1.0);
	if (d < distance)
	{
	  distance = d;
	  closest = xc[j];
	}
      }
      if (closest < 0)
      {
        throw new IllegalArgumentException("Null row in inverted matrix");
      }
      reorderedXCoord[closest] = forXCoord[i];
      reorderedValues[closest] = forValues[i];
   }
    // We will (must) have all rows present
    int[] ypos = new int[numRows];
    for (int i = 0; i < numRows; i++)
    {
      ypos[i] = i;
    }
    // System.out.println("build from " + Arrays.toString(reorderedXCoord) + " y " + ypos +
    //   " v " + reorderedValues);
    return new MatWrapper(reorderedXCoord, ypos, reorderedValues, 
      numRows, numCols);
  }
  /** internal utility routine to subtract a multiple of one
      sparse vector from another */
  private static void subtractMultiple(int[][] xcoordHere, double[][] valuesHere,
    int rowNum, double subtractThis, int[] pivotCoords, double[] pivotValues)
  {
    int[] targetC = xcoordHere[rowNum];
    double[] targetV = valuesHere[rowNum];
    int[] forC = new int[targetC.length + pivotCoords.length];
    double[] forV = new double[forC.length];
    // What follows is really a merge
    int wp = 0;
    int rp1 = 0;
    int rp2 = 0;
    while ((rp1 < targetC.length) && (rp2 < pivotCoords.length))
    {
      final int p1 = targetC[rp1];
      final int p2 = pivotCoords[rp2];
      if (p1 == p2)
      {
	double d = targetV[rp1] - pivotValues[rp2] * subtractThis;
	if (d != 0.0)
	{
	  forC[wp] = p1;
	  forV[wp] = d;
	  wp++;
	}
	rp1++;
	rp2++;
	continue;
      }
      if (p1 < p2)
      {
        forC[wp] = p1;
	forV[wp] = targetV[rp1];
	rp1++;
	wp++;
	continue;
      }
      // p2 < p1
      forC[wp] = p2;
      forV[wp] = -pivotValues[rp2] * subtractThis;
      rp2++;
      wp++;
    }
    // At most one of the following two while loops will do something
    while (rp1 < targetC.length)
    {
      forC[wp] = targetC[rp1];
      forV[wp] = targetV[rp1];
      rp1++;
      wp++;
    }
    while (rp2 < pivotCoords.length)
    {
      forC[wp] = pivotCoords[rp2];
      forV[wp] = -pivotValues[rp2] * subtractThis;
      rp2++;
      wp++;
    }
    xcoordHere[rowNum] = new int[wp];
    System.arraycopy(forC, 0, xcoordHere[rowNum], 0, wp);
    valuesHere[rowNum] = new double[wp];
    System.arraycopy(forV, 0, valuesHere[rowNum], 0, wp);
  }
  /** update matrix to accept new column */
  public void updateMatrix(int columnChanged, double[] newCol)
  {
    // System.out.println("Update with " + Arrays.toString(newCol));
    int[][] newXcoords = new int[numRows][];
    double[][] newValues = new double[numRows][];
    int rp = 0;
    // Have to work row by row
    for (int i = 0; i < numRows; i++)
    {
      int found = -1;
      for(;rp < ycoord.length; rp++)
      {
	found = ycoord[rp];
        if (found >= i)
	{
	  break;
	}
      }
      double[] r;
      int[] x;
      if (found == i)
      {
        x = xcoord[rp];
	r = values[rp];
      }
      else
      {
        x = new int[0];
        r = new double[0];
      }
      // points to where new value should go
      int at = x.length;
      for (int j = 0; j < x.length; j++)
      {
        if (x[j] >= columnChanged)
	{
	  at = j;
	  break;
	}
      }
      double v = newCol[i];
      if ((at < x.length) && (x[at] == columnChanged))
      { // already have something at target column
	if (v == 0.0)
	{
	  /*
	  System.out.println("ColumnChanged " + columnChanged +
	    " x " + Arrays.toString(x) + " r " + Arrays.toString(r));
	  */
	  int[] nx = new int[x.length - 1];
	  double[] nr = new double[x.length - 1];
	  if (at > 0)
	  {
	    System.arraycopy(x, 0, nx, 0, at);
	    System.arraycopy(r, 0, nr, 0, at);
	  }
	  // if at = x.length - 1 then we just needed to
	  // take off the final element so we want atEnd = 0
	  int atEnd = x.length - at - 1;
	  if (atEnd > 0)
	  {
	    // System.out.println("at " + at + " atend " + atEnd);
	    System.arraycopy(x, at + 1, nx, at, atEnd);
	    System.arraycopy(r, at + 1, nr, at, atEnd);
	  }
	  x = nx;
	  r = nr;
	  /*
	  System.out.println("After ColumnChanged " + columnChanged +
	    " x " + Arrays.toString(x) + " r " + Arrays.toString(r));
	  */
	}
	else
	{
	  r[at] = v;
	}
      }
      else if (v != 0)
      {
	// make more room for new non-zero value
        int[] nx = new int[x.length + 1];
	double[] nr = new double[x.length + 1];
	if (at > 0)
	{
	  System.arraycopy(x, 0, nx, 0, at);
	  System.arraycopy(r, 0, nr, 0, at);
	}
	nx[at] = columnChanged;
	nr[at] = v;
	int left = x.length - at;
	if (left > 0)
	{
	  System.arraycopy(x, at, nx, at + 1, left);
	  System.arraycopy(r, at, nr, at + 1, left);
	}
	x = nx;
	r = nr;
      }
      newXcoords[i] = x;
      newValues[i] = r;
    }
    int numFilled = 0;
    for (int i = 0; i < newXcoords.length; i++)
    {
      if (newXcoords[i].length > 0)
      {
        numFilled++;
      }
    }
    ycoord = new int[numFilled];
    xcoord = new int[numFilled][];
    values = new double[numFilled][];
    int wp = 0;
    for (int i = 0; i < newXcoords.length; i++)
    {
      if (newXcoords[i].length == 0)
      {
        continue;
      }
      ycoord[wp] = i;
      xcoord[wp] = newXcoords[i];
      values[wp] = newValues[i];
      wp++;
    }
  }
  /** update inverse matrix to reflect change in column of matrix
      we are supposed to be the inverse of. We are told what combination
      of the old columns forms the new one. That allows us to work
      out how to change the inverse. We can think of B^-1 as a collection
      of columns, each of which tells us how to get a unit vector by
      adding together columns of B. This happens when we compute BB^-1.
      We then adjust B^-1 by altering it to produce the same collection
      of unit vectors by viewing the new column in B as a combination 
      of the old columns in B. This is what newColAsCombOfOld tells us (it 
      was produced by working out B^-1 * (new columns)).
    */
  public void updateInverse(int columnChanged, 
    double[] newColAsCombOfOld)
  {
    // Holds how much of the new column we need to
    // add in at each point to produce a unit vector
    double[] amountNewColumn = new double[numCols];
    // how much of the old column would need to be mixed 
    // in to produce the new column as a mix of the old. We think
    // of the new column as a polluted source with this much old
    // column in it - which we want - and a lot of the stuff from
    // other columns of B - which we need to compensate for.
    double amountOldInNew = newColAsCombOfOld[columnChanged];
    int[] xc = xcoord[columnChanged];
    double[] d = values[columnChanged];
    int rp = 0;
    for (int i = 0; (i < numCols) && (rp < xc.length); i++)
    {
      int co = -1;
      for (;rp < xc.length; rp++)
      {
	co = xc[rp];
        if (co >= i)
	{
	  break;
	}
      }
      if (co != i)
      { // don't need any of new column because we never used
        // any part of the old one
        continue;
      }
      // used valuesHere[rp] of the old column in B. Will use
      // enough of the new column in B that we get the same
      // amount of the old column as we had before, albeit with
      // a load of trash from the other columns in B.
      amountNewColumn[i] = d[rp] / amountOldInNew;
    }
    // Now work over the matrix row by row, because that is how
    // it is stored
    double[] newRow = new double[numCols];
    int[] newCoords = new int[numCols];
    for (int i = 0; i < numRows; i++)
    {
      xc = xcoord[i];
      d = values[i];
      // System.out.println("Xc " + Arrays.toString(xc));
      // System.out.println("D " + Arrays.toString(d));
      int wp = 0;
      rp = 0;
      // Row i in the new B^-1 tells us, for each unit column
      // we are producing, how much of column i of B to take.
      // We have committed to taking from the new column in B
      // and we view it as a polluted source which gives us
      // a load of the other columns of B, which we had already,
      // mixed in with some of the old column of B it is replacing.
      // This helps us work out how much of column i in B will leak
      // in from that source.
      double amountThisRowMixedIn = newColAsCombOfOld[i];
      for (int j = 0; j < numCols; j++)
      {
	int co = -1;
	for (;rp < xc.length; rp++)
	{
	  co = xc[rp];
	  if (co >= j)
	  {
	    break;
	  }
	}
	double valueHere;
	if (co != j)
	{
	  valueHere = 0.0;
	}
	else
	{
	  valueHere = d[rp];
	}
	if (i == columnChanged)
	{ // In each column, this row tells us how much of the new
	  // column we want in the mix, and we have already worked that out
	  valueHere = amountNewColumn[j];
	}
	else
	{ // Want the original amount, minus however much we are putting
	  // in because the new column is a mix of all sorts of stuff
	  // System.out.println("valueHere " + valueHere);
	  valueHere -= amountNewColumn[j] * amountThisRowMixedIn;
	  /*
	  System.out.println("Row " + i + " col " + j + " " + amountNewColumn[j] + 
	    " * " + amountThisRowMixedIn + " becomes " +  valueHere);
	  */
	}
	if (valueHere == 0.0)
	{
	  continue;
	}
	newCoords[wp] = j;
	newRow[wp] = valueHere;
	wp++;
      }
      // Shove in new info
      int[] trimmed = new int[wp];
      System.arraycopy(newCoords, 0, trimmed, 0, wp);
      xcoord[i] = trimmed;
      double[] trimmedD = new double[wp];
      System.arraycopy(newRow, 0, trimmedD, 0, wp);
      values[i] = trimmedD;
    }
  }
  /** test matrix inversion with a dense matrix */
  public static void testWithDense(int dim, Random r)
  {
    double[] mat = new double[dim * dim];
    for (int i = 0; i < mat.length; i++)
    {
      mat[i] = r.nextGaussian();
    }
    // System.out.println("Mat is " + Arrays.toString(mat));
    MatWrapper mw = new MatWrapper(dim, dim, mat);
    // System.out.println("produced " + mw);
    MatWrapper mi = mw.inverse();
    // System.out.println("Inverse of " + mw + " is " + mi);
    // Give mi columns of mw to pre-multiply
    double[] incol = new double[dim];
    double[] outcol = new double[dim];
    for (int i = 0; i < dim; i++)
    {
      for (int j = 0; j < dim; j++)
      {
        incol[j] = mat[j * dim + i];
      }
      // System.out.println("Incol is " + Arrays.toString(incol));
      mi.preMult(incol, outcol);
      for (int j = 0; j < dim; j++)
      {
        double d = outcol[j];
	if (j == i)
	{
	  d = d - 1.0;
	}
	if (Math.abs(d) > 1.0e-6)
	{
	  throw new IllegalArgumentException("Expected column of I got " +
	    Arrays.toString(outcol));
	}
      }
    }
    // Give mi rows of mw to pre-multiply
    for (int i = 0; i < dim; i++)
    {
      for (int j = 0; j < dim; j++)
      {
        incol[j] = mat[i * dim + j];
      }
      mi.postMult(incol, outcol);
      for (int j = 0; j < dim; j++)
      {
        double d = outcol[j];
	if (j == i)
	{
	  d = d - 1.0;
	}
	if (Math.abs(d) > 1.0e-6)
	{
	  throw new IllegalArgumentException("Expected row of I got " +
	    Arrays.toString(outcol));
	}
      }
    }
  }
  public static void main(String[] s)
  {
    Random r = new Random(42);
    for (int i = 0; i < 100; i++)
    {
      MatWrapper.testWithDense(i, r);
      System.out.println("Done i = " + i);
    }
  }
}
