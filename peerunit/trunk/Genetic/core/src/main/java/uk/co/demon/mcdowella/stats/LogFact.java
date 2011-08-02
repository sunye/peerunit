package uk.co.demon.mcdowella.stats;

import java.util.ArrayList;
import java.util.List;

/** Provide function for log factorial that keeps track of previous log
 * factorials
 */
public class LogFact
{
  /**
   * This routine is NOT thread safe because it uses ArrayList for speed,
   * and doesn't lock anything.
   * @return log(v!)
   */
  public static double lF(int v)
  {
    if(v<0)
      throw new ArithmeticException("Attempted to work out factorial of -ve integer "+v);
    int s=lfs.size();
    double sofar;
    if(s==0)
    {
      sofar=0.0; // log of 0!
      lfs.add(new Double(sofar));
      s++;
    }
    else
      sofar=((Double)lfs.get(s-1)).doubleValue();
    for(;s<=v;s++)
    {
      sofar+=Math.log(s);
      lfs.add(new Double(sofar));
    }
    return ((Double)lfs.get(v)).doubleValue();
  }
  private static List<Double> lfs=new ArrayList<Double>();
}
