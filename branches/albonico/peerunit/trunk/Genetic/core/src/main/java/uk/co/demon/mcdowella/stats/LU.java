package uk.co.demon.mcdowella.stats;
import java.util.*;
/**
 * Creates an object representing the LU decomposition of a square matrix M. This
 * object can then be used to solve y=Mx for x given y. All matrices involved are
 * held with rows contiguous in store.
 * We actually decompose a version of m which has had its rows and columns
 * permuted. Permuting the rows is equivalent to permuting y in y=Mx. Permuting
 * the columns is equivalent to permuting x in y=Mx. To solve for x given y
 * we need to apply a permutation to y, solve with our decomposed M, and then
 * apply a permutation to the answer. These permutations, and their total sign,
 * are also held in the object
 */
public class LU
{
  /**
   * @param n the dimension of the square matrix to decompose
   * @param m the matrix to decompose
   */
  public LU(int nn, double m[])
  {
    n=nn;
    rowperm=new int[n];
    colperm=new int[n];
    lu=new double[n*n];
    for(int i=0;i<n;i++)
    {
      rowperm[i]=colperm[i]=i; 
    }    
    System.arraycopy(m,0,lu,0,n*n);
    determinant=1.0;
    /* We will work out L and U from the top left corner of the
       matrix down. At each stage we have decomposed all but a
       square in the lower right of the matrix, We have set the
       leading diagonal of L to 1.0. This immediately gives us one
       new cell of L, which gives us an entire row of U, since the
       rest of that row of L is 0. We know that
       the rest of the U column is 0, which gives us an entire column
       of L. We can then work out the impact of the new row and column,
       and subtract off that from the remaining part of the matrix ready
       for the next stage */ 
    int forNullDim = 0;
    for(int i=0;i<n-1;i++)
    {
      /* first find the largest element left unsolved, and permute
         rows and columns to bring it to the top left */
      double val=Math.abs(lu[i*n+i]);
      int row=i;
      int col=i;
      for(int j=i;j<n;j++)
        for(int k=i;k<n;k++)
        {
          double v=Math.abs(lu[j*n+k]);
          if(v>val)
          {
            val=v;
            row=j;
            col=k;
          }
        }
      if(val==0.0)
      {
	forNullDim = n - i;
        break; /* if only 0s left, we can consider the matrix as already decomposed! */
      }
      /* Now permute rows and columns */
      if(row!=i)
      {
        /* We can just permute a row of lu here because we end up either
           permuting a part of remaining m or permuting a row of l - and
           we want to permute a row of l so that it is in step with the
           permuted m anyway */
        for(int j=0;j<n;j++)
        {
          double t=lu[row*n+j];
          lu[row*n+j]=lu[i*n+j];
          lu[i*n+j]=t;
        }
        determinant*=-1.0;
        /* We have swapped two rows of m, so we will have to swap those two values
           of any later y */
        int t=rowperm[row];
        rowperm[row]=rowperm[i];
        rowperm[i]=t;       
      }
      if(col!=i)
      {
        /* Column permutations go by the same argument */
        for(int j=0;j<n;j++)
        {
          double t=lu[j*n+col];
          lu[j*n+col]=lu[j*n+i];
          lu[j*n+i]=t;
        }
        determinant*=-1.0;
        /* These modifications to xperm record which x is attached to which 
           column */
        int t=colperm[col];
        colperm[col]=colperm[i];
        colperm[i]=t;
      }
      /* We now have the new row correct. The rest of the new column is divided
         by the new pivot. We can then subtract its contribution from the rest
         of the matrix */
      determinant*=lu[i*n+i];
      // System.out.println("Det times "+lu[i*n+i]+" at "+(i*n+i));
      double over=1.0/lu[i*n+i];
      for(int j=i+1;j<n;j++)
      {
        double colVal=lu[j*n+i]*over;
        lu[j*n+i]=colVal;
        for(int k=i+1;k<n;k++)
          lu[j*n+k]-=colVal*lu[i*n+k];
      } 
    }
    // Can break out early because last step would fix up a square of side 1 which
    // is OK already, but we need to include it in the determinant 
    determinant*=lu[n*n-1];
    // and null space
    // System.out.println("Last factor for determinant is " + lu[n*n-1]);
    if ((lu[n * n - 1] == 0.0) && (forNullDim <= 0))
    {
      forNullDim = 1;
    }
    nullDim = forNullDim;
    /*
    System.out.println("Decomposition:");
    for(int i=0;i<n;i++)
    {
      for(int j=0;j<n;j++)
        System.out.print(lu[i*n+j]+" ");
      System.out.println();
    }
    System.out.println();
    */
  }
  /** the dimension of the null space */
  private final int nullDim;
  /** return the dimension of the null space */
  public int getNullDim()
  {
    return nullDim;
  }
  
  /**
   * Multiply a vector by a matrix
   * @param rows the number of rows in the matrix
   * @param cols the number of columns in the matrix 
   * @param m the matrix
   * @param x the vector
   * @param y where to put the output
   */
  public static void mult(int rows, int cols, double m[], double x[], double y[])
  {
    for(int i=0;i<rows;i++)
    {
      double s=0.0;
      for(int j=0;j<cols;j++)
        s+=m[i*cols+j]*x[j];
      y[i]=s;
    }
  }
  
  /**
   * The determinanent of the matrix M
   */
  public double getDeterminant()
  {
    return determinant;
  }
  /**
   * work out x, given y
   * @param y is the observed y in y=Mx
   * @param x is where we put out the x we solve for in y=Mx
   */
  public void solve(double y[], double x[])
  {
    /* Lower right corner will be zero iff matrix is singular - in fact if it
       has a null space of rank n it will have an n*n space of zeros in the
       lower right */
//    System.out.println("Check "+lu[n*n-1]+" at "+(n*n-1));
    if(lu[n*n-1]==0.0)
      throw new ArithmeticException("Attempt to solve for a singular matrix");
    /* First reformat y according to the rows that we swapped */
    for(int i=0;i<n;i++)
      x[i]=y[rowperm[i]];
    /* First solve for the effect of L, starting at the top left */
    for(int i=0;i<n;i++)
    {
      /* We know the new x already because the leading diagonal of L is 1 */
      double xn=x[i];
      /* subtract off its effect on others */
      for(int j=i+1;j<n;j++)
        x[j]-=xn*lu[j*n+i];
    }
    /*
    System.out.println("Mid:");
    for(int i=0;i<n;i++)
      System.out.print(x[i]+" ");
    System.out.println("");
    */
    /* Now for the effect of U, starting at the bottom right */
    for(int i=n-1;i>=0;i--)
    {
      double xn=x[i]/lu[i*n+i];
      x[i]=xn;
      for(int j=i-1;j>=0;j--)
      {
        x[j]-=xn*lu[j*n+i];
      } 
    } 
    /* We have an x, but it is for a reformatted matrix: undo this */
    double t[]=new double[n];
    for(int i=0;i<n;i++)
      t[i]=x[i];
    for(int i=0;i<n;i++)
      x[colperm[i]]=t[i];
  }
  /** return a vector in the null space. Must have 0 &le; 
    dimNo &lt; nullDim */
  public void getNullVector(int dimNo, double[] x)
  {
    // The vectors x in the null space have Mx = 0, or LU x = 0.
    // This means that Ux will have non-zero values only in the
    // lower elements of x, where L has only zeros. So we can
    // produce them by solving for Ux starting from such an element
    if ((dimNo < 0) || (dimNo >= nullDim))
    {
      throw new IllegalArgumentException("dimNo " + dimNo + " out of range");
    }
    for (int i = 0; i < n; i++)
    {
      x[i] = 0.0;
    }
    // Put something at the dimension we want
    x[n - 1 - dimNo] = 1.0;
    // and now adjust non-null dimensions to cancel this out and put
    // us in the null space
    // System.out.println("M is " + Arrays.toString(lu));
    for (int i = 0; i < n - nullDim; i++)
    {
      x[i] -= lu[i * n + n - 1 - dimNo];
    }
    // Carry on from here as with solve
    /* Now for the effect of U, starting at the bottom right */
    for(int i=n-nullDim-1;i>=0;i--)
    {
      double xn=x[i]/lu[i*n+i];
      x[i]=xn;
      for(int j=i-1;j>=0;j--)
      {
        x[j]-=xn*lu[j*n+i];
      } 
    } 
    System.out.println("x now " + Arrays.toString(x));
    /* We have an x, but it is for a reformatted matrix: undo this */
    double t[]=new double[n];
    for(int i=0;i<n;i++)
      t[i]=x[i];
    for(int i=0;i<n;i++)
      x[colperm[i]]=t[i];
  }
  /**
   * The dimension of the matrix
   */
  private int n;
  /**
   * The decomposed matices L and U, held in one square matrix. L has a leading
   * diagonal of 1.0
   */
  private double lu[];
  /**
   * permuation on rows
   */
  private int rowperm[];
  /**
   * permutation on columns
   */
  private int colperm[];
  /**
   * determinant of x
   */
  private double determinant;
  /** print a matrix out neatly */
  public static void print(int numCols, double[] mat)
  {
    int numRows = mat.length / numCols;
    if ((numRows * numCols) != mat.length)
    {
      throw new IllegalArgumentException("Length " + mat.length +
        " not divisible by " + numCols);
    }
    for (int i = 0; i < numRows; i++)
    {
      String sep = "";
      for (int j = 0; j < numCols; j++)
      {
        System.out.print(sep);
	sep = " ";
	System.out.print(mat[i * numCols + j]);
      }
      System.out.println();
    }
  }
  /**
   * The main method here is a test harness
   */
  public static void main(String args[])
  {
    int n=3;
    long seed=6;
    int tries=10;
    double relac=0.0001;
    boolean verbose=true;
    boolean trouble=false;
    for(int i=0;i<args.length;i++)
    {
      if (i<args.length-1 && args[i].equals("-seed"))
      {
        i++;
        try
        {
          seed=new Long(args[i]).longValue();
        }
        catch(NumberFormatException nn)
        {
          trouble=true;
          System.err.println("Could not read seed in "+args[i]);
        }
      }
      else if(i<args.length-1 && args[i].equals("-n"))
      {
        i++;
        try
        {
          n=new Integer(args[i]).intValue();
        }
        catch(NumberFormatException nn)
        {
          trouble=true;
          System.err.println("Could not read n in "+args[i]);
        }
      }
      else if(i<args.length-1 && args[i].equals("-tries"))
      {
        i++;
        try
        {
          tries=new Integer(args[i]).intValue();
        }
        catch(NumberFormatException nn)
        {
          trouble=true;
          System.err.println("Could not read tries in "+args[i]);
        }
      }
      else if(args[i].equals("-quiet"))
        verbose=false;
      else
      {
         System.err.println("Could not handle flag "+args[i]);
         trouble=true;
      }
    }
    if(trouble)
    {
      System.err.println("Usage is LU [-n #] [-seed #] [-tries #] [-quiet]");
      System.exit(1);
    }
    Random r=new Random(seed);
    double m[]=new double[n*n];
    double x[]=new double[n];
    double y[]=new double[n];
    double back[]=new double[n];
    double sqAcc=relac*relac;
    for(int i=0;i<tries;i++)
    {
      for(int j=0;j<n*n;j++)
        m[j]=r.nextDouble();
      if(verbose)
      {
        System.out.println("Matrix:");
        for(int j=0;j<n;j++)
        {
          for(int k=0;k<n;k++)
            System.out.print(m[j*n+k]+" ");
          System.out.println("");
        }
        System.out.println("");
      }
      for(int j=0;j<n;j++)
        x[j]=r.nextDouble();
      if(verbose)
      {
        System.out.println("Vector:");
        for(int j=0;j<n;j++)
          System.out.print(x[j]+" ");
        System.out.println("");
      }
      mult(n,n,m,x,y);
      if(verbose)
      {
        System.out.println("Result:");
        for(int j=0;j<n;j++)
          System.out.print(y[j]+" ");
        System.out.println("");
      }
      LU l=new LU(n,m);
      l.solve(y,back);
      System.out.println("Determinant is "+l.getDeterminant());
      if(verbose)
      {
        System.out.println("Back:");
        for(int j=0;j<n;j++)
          System.out.print(back[j]+" ");
        System.out.println("");
      }
      double xsize=0.0;
      for(int j=0;j<n;j++)
        xsize+=x[j]*x[j];
      for(int j=0;j<n;j++)
      {
        double diff=back[j]-x[j];
        if(diff*diff>sqAcc*xsize)
        {
          System.err.println("Trouble at position "+j+" of vector "+i);
          System.err.println("Got "+back[j]+" expected "+x[j]);
          System.exit(1);
        }
      }
      // Check null space stuff
      int nullDim = r.nextInt(n);
      int dimsLeft = n - nullDim;
      for (int j = n * dimsLeft; j < (n * n); j++)
      {
        m[j] = 0.0;
      }
      LU ln = new LU(n, m);
      int dimBack = ln.getNullDim();
      if (dimBack != nullDim)
      {
        System.err.println("Got null " + dimBack + " was " + nullDim);
	System.exit(1);
      }
      double[] nulls = new double[n * nullDim];
      double[] nullv = new double[n];
      for (int j = 0; j < nullDim; j++)
      {
        ln.getNullVector(j, nullv);
	for (int k = 0; k < dimsLeft; k++)
	{
	  double sofar = 0.0;
	  for (int p = 0; p < n; p++)
	  {
	    sofar += nullv[p] * m[k * n + p];
	  }
	  System.out.println("Null dot " + sofar);
	}
	double sofar = 0.0;
	for (int p = 0; p < n; p++)
	{
	  sofar += nullv[p] * nullv[p];
	}
	System.out.println("Null self " + sofar);
	System.arraycopy(nullv, 0, nulls, j * n, n);
      }
    }
  }
}
