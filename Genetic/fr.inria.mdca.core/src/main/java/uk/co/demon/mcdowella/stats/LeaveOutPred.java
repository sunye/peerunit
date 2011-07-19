package uk.co.demon.mcdowella.stats;

/** linear prediction, with a prediction confidence interval
 * based on leave-out-one cross-validation. Slow, but we're only
 * interested in its accuracy at the moment - there are clever
 * speedups available in this case
 */
public class LeaveOutPred
{
  double low,high,pred;
  /** Constructor does all the work
   * @param n the number of x-y pairs available to predict
   * @param x the x values
   * @param y the corresponding y values
   * @param at the x value at which you want a prediction
   */
  public LeaveOutPred(int n, double x[], double y[], double at)
  {
    double errors[]=new double[n];
    double ourX[]=new double[n-1];
    double ourY[]=new double[n-1];
    double sumErr=0.0;
    for(int i=0;i<n;i++)
    { // leave out i
      for(int j=0;j<i;j++)
      {
        ourX[j]=x[j];
        ourY[j]=y[j];
      }
      for(int j=i+1;j<n;j++)
      {
        ourX[j-1]=x[j];
        ourY[j-1]=y[j];
      }
      errors[i]=new Correlation(n-1,ourX,ourY).linearY(x[i])-y[i];
      // System.out.println("Error "+errors[i]);
      sumErr+=errors[i];
    }
    double guess=new Correlation(n,x,y).linearY(at);
    pred=guess-sumErr/n;
    // System.out.println("Guess "+guess);
    // Want range to include about 1/2 of the n possible slots
    int lowPos=n/4;
    int highPos=lowPos+(n+1)/2;
    int highPos2=lowPos+(n+2)/2;
    GetRank.forceRank(0,n,highPos2,errors);
    double e1=errors[highPos2];
    if(highPos<highPos2)
      GetRank.forceRank(0,highPos2,highPos,errors);
    low=guess-0.5*(errors[highPos]+e1); // subtract LARGE error to get
                                        // SMALL estimate!
    if(lowPos<highPos)
      GetRank.forceRank(0,highPos,lowPos,errors);
    high=guess-errors[lowPos];
    // System.out.println("High "+high+" low "+low);
  }
  /**
   * @return the prediction
   */
  public double getPred() {return pred;}
  /**
   * @return the lower end of a confidence interval for
   * which should contain the true value at least 1/2 the time
   */
  public double getLow() {return low;}
  /**
   * @return the higher end of a confidence interval for
   * which should contain the true value at least 1/2 the time
   */
  public double getHigh() {return high;}
}