package uk.co.demon.mcdowella.stats;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.StreamTokenizer;
/** Simple class to do basic stats
 */

public class Deviant
{
  private int n=0;
  private double mean=0.0;
  private double sumSq=0.0;
  private double min;
  private double max;
  /** forget everything
   */
  public void reset()
  {
    n=0;
    mean=0.0;
    sumSq=0.0;
  }
  /** Accept a sample
   * @param s the sample
   */
  public void sample(double s)
  {
    if(n==0||s<min)
      min=s;
    if(n==0||s>max)
      max=s;
    // Recurrence relations from Knuth 4.2.2 Eqns 15,16
    n++;
    double newMean=mean+(s-mean)/n;
    sumSq+=(s-mean)*(s-newMean);
    mean=newMean;
  }
  /** @return the number of samples accepted so far */
  public int getN() {return n;}
  /** @return the mean */
  public double getMean() {return mean;}
  /** @return the sample variance */
  public double getVariance()
  {
    return sumSq/(n-1.0);
  }
  /** @return the maximum so far */
  public double getMax() {return max;}
  /** @return the minimum so far */
  public double getMin() {return min;}
  /** @return the sum */
  public double getSum() {return mean*n;}
  public String toString()
  {
    StringBuffer s=new StringBuffer();
    s.append("N="+getN()+" sum="+getSum());
    if(n>0) s.append(" Mean="+getMean()+
      " Max="+getMax()+" Min="+getMin());
    if(n>1) s.append(" variance="+getVariance()+
      " s.d.="+Math.sqrt(getVariance()));
    return s.toString();
  }
  public static void main(String[] s) throws IOException
  {
    Deviant d=new Deviant();
    StreamTokenizer ss=new StreamTokenizer(
      new BufferedReader(new InputStreamReader(System.in)));
    ss.commentChar('#');
    loop:for(;;)
    {
      int tok=ss.nextToken();
      switch(tok)
      {
           case StreamTokenizer.TT_EOF:
           break loop;
           case StreamTokenizer.TT_EOL:
           break;
           case StreamTokenizer.TT_NUMBER:
               d.sample(ss.nval);
           break;
           default:
               System.err.println("Unknown token " + tok);
               System.exit(1);
      }
    }
    System.out.println("N="+d.getN()+" Mean="+d.getMean()+" variance="+d.getVariance()+
      " sum="+d.getSum());
    System.out.println("Max="+d.getMax()+" Min="+d.getMin());
    System.out.println(d);
  }
}