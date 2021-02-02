package uk.co.demon.mcdowella.stats;
/** This class exists to work out the root of an equation.
 */
public class Root
{
  /**
   * Here the constructor does all the work, and the methods just
   * return info the constructor set up for them. WARNING this doesn't
   * do anything sensible like throw an exception if there's an error:
   * you have to call a method to check yourself
   * This uses the method of False position, with some checks to stop
   * it being too silly on uncooperative functions
   * @param start The value at which to start looking for a root
   * @param delta Start trying to bracket the root at start+delta
   * @param f an object on which callbacks are made to evaluate the
   * function we are trying to find a root of
   * @param xeps the relative error we will accept in the answer
   * @param maxiters the maximum number of iterations to make
   * @param l The routine will not attempt to evaluate for values 
   * < this bound
   * @param r The routine will not attempt to evaluate for valus
   * > this bound
   */
  public Root(double start, double delta, F1d f, double xeps, int maxiters, double l,
              double r)
  {
    // System.out.println("Root start is "+start+" delta is "+delta);
    error=null;
    iters=0;
    a=start;
    if((l>r)||(a<l)||(a>r)||(a+delta<l)||(a+delta>r))
    {
      error="Bounds badly set up at start";
      return;
    } 
    A=f.f(a);
    iters++;
    // System.out.println("First A val "+A+" at "+a);
    if(A==0.0)
    {
      x=a;
      X=A;
      return;
    }
    b=a+delta;
    B=f.f(b);
    iters++;
    // System.out.println("First B val "+B+" at "+b);
    if(B==0.0)
    {
      x=b;
      X=B;
      return;
    }
    if(delta==0.0)
    {
      error="delta is zero";
      return;
    }
    boolean sidelined=false;
    while(iters<maxiters)
    {
      if(A>B)
      {
        double t=A;
        A=B;
        B=t;
        t=a;
        a=b;
        b=t;
      }
      if(A<0.0&&B>0.0)
        break;
      double c;
      if(A<0)
      {
        c=2.0*b-a;
      }
      else
      {
        c=2.0*a-b;
      }
      if(c<l)
      {
        sidelined=true;
        c=l;
      } 
      if(c>r)
      {
        sidelined=true;
        c=r;
      }
      double C=f.f(c);
      iters++;
      if(C==0.0)
      {
        x=c;
        X=C;
        return;
      }
      if(A<0.0)
      {
        if(C>0.0)
        {
          a=b;
          A=B;
          b=c;
          B=C;
          break;
        }
        b=c;
        B=C;
      }
      else
      {
        if(C<0.0)
        {
          b=a;
          B=A;
          a=c;
          A=C;
          break;
        }
        a=c;
        A=C;
      }
      // System.out.println("New A val "+A+" at "+a+" new B val "+B+" at "+b);
      if(sidelined)
      {
        error="Could not find bracket within bounds";
        return;
      }
    }
    boolean lucky=true;
    while(iters<maxiters)
    {
      if(Math.abs(a-b)<=xeps*0.5*(Math.abs(a)+Math.abs(b)))
      {
        if(Math.abs(A)<Math.abs(B))
        {
          x=a;
          X=A;
        }
        else
        {
          x=b;
          X=b;
        }
        return;
      }
      double probe=A/(A-B);   // next point, on scale a=0, b=1
      if(!lucky)
      {
        if(probe<0.1) probe=0.1; // make sure we don't get too close to end point
        if(probe>0.9) probe=0.9; // to guarantee decrease in interval searched
                                 // at least every other time. Don't check this
                                 // every time as would limit us to reducing by
                                 // factor of 10 every time - good but linear
      }
      probe=b*probe+a*(1.0-probe);
      double val=f.f(probe);
      // System.out.println("Inner val "+val+" at "+probe);
      iters++;
      if(val==0.0)
      {
        x=probe;
        X=val;
        return;
      }
      if(val<0.0)
      {
        lucky=Math.abs(b-probe)<=Math.abs(probe-a); // jumped into smallest of two intervals?
        a=probe;
        A=val;
      }
      else
      {
        lucky=Math.abs(probe-a)<=Math.abs(b-probe); // jumped into smallest of two intevals?
        b=probe;
        B=val;
      }
    }
    error="Never converged A="+A+" B="+B;
  }
  /**
   * @return null (no error) or a printable error message
   */
  public String getError()
  {
    return error;
  }
  /**
   * @return the root found
   */
  public double getRoot()
  {
    return x;
  }
  private int iters;
  private double a,A,b,B,x,X;
  private String error;
}