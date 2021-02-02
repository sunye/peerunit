package uk.co.demon.mcdowella.stats;

/**
 * This is a rank-based predictor
 * We suppose that the residuals we get when we fit an affine function to
 * the data points, including the unknown value to be predicted, are
 * exchangeable. This means that the unknown residual will be of rank
 * 0..n in the set of n+1 augmented points with probability 1/(n+1)
 * Here we count the point of rank 0 as the lowest point, and we return
 * the y value which would make the residual from x a tie with the residual
 * which has rank <rank> within the old values - so rank has the range
 * 0..n-1 Since the residuals are exchangeable, we can predict that the
 * unknown value is higher than this with confidence (n-rank)/(n+1), and
 * lower than this with confidence rank/(n+1). - I think that ties in the
 * known residuals can only work to increase the
 * real confidence
 * To detect cases (e.g. extrapolation) where we cannot find a solution,
 * we will set bounds for the prediction and fail if we cannot bracket the
 * solution within those bounds.
 * <p>
 * If n+1 is even there is no obvious central point, so we get predictions
 * we know to be biased. We really want the old definition of a median for
 * even numbers - the average of the two central values. Allow CorPredictor
 * to take two ranks as arguments to allow for this. If we have an even
 * number of points these ranks will be the two ranks we average to get the
 * median. If not they will be the rank of the median.
 * </p>
*/

public class CorPredictor implements F1d // F1d should only be used internally
{
    /** Constructor does nearly all the work here - methods just read out
     *  the result
     * @param xval the value at which you wish to predict y
     * @param n the number of (x,y) pairs on which you wish to base your
     * predictions
     * @param xx the x values seen previously
     * @param yy the y values for the x values seen previously
     * @param lowRank the lower of two residual ranks used to
     * generate predictions
     * @param highRank the higher of two residual ranks used to
     * generate predictions
     * @param eps an accuracy parameter passed to the underlying equation
     * solver
     * @param maxiters the maximum number of iterations the equation solver
     * is allowed to do
     */
    public CorPredictor(double xval, int n, double xx[], double yy[], int lowRank,
      int highRank, double eps, int maxiters)
    {
      error=null;
      if (highRank < lowRank)
      {
        int t=highRank;
        highRank=lowRank;
        lowRank=t;
      }
      if ((lowRank<0)||(highRank>n-1))
      {
        error="rank not in [0..n-1]";
        return;
      }
      double xmean=xval;
      ysum=0.0;
      // X vector gains new xval, is reduced to unit vector of 0 mean
      // work out sum of known ys
      x=new double[n+1];
      y=new double[n+1];
      resid=new double[n+1];
      for(int i=0;i<n;i++)
      {
        // System.out.println("X at first is "+xx[i]);
        x[i]=xx[i];
        xmean+=x[i];
        y[i]=yy[i];
        ysum+=y[i];
      }
      xmean=xmean/(n+1.0);
      x[n]=xval;
      double xsq=0.0;
      for(int i=0;i<=n;i++)
      {
        x[i]-=xmean;
        // System.out.println("After x mean have "+x[i]);
        xsq+=x[i]*x[i];
      }
      if(xsq>0.0)
        xsq=1.0/Math.sqrt(xsq);
      for(int i=0;i<=n;i++)
      {
        x[i]*=xsq;
        // System.out.println("now unit vector have "+x[i]);
      }
      // Now solve the non-linear equation that says that our predicted value is that value
      // of y that makes the new residual the same as the n-th residual in rank. Start from
      // Normal predicted value (given modified xval)
      Correlation cor=new Correlation(n,x,y);
      
      lowTarget=lowRank;
      highTarget=highRank;
      nn=n;
      double delta=Math.sqrt(cor.predYVar(x[n]));
      double linpred=cor.linearY(x[n]);
      if(delta<Math.abs(linpred)*1.0e-6)
        delta=Math.abs(linpred)*1.0e-6;
      if(delta==0.0)
        delta=1.0e-20;
      // Surely Chebyshev's inequallity should make us suspicious if we predict something
      // more than 100 standard deviations away from the expected.
      r=new Root(linpred,delta,this,eps,maxiters,linpred-delta*100.0,linpred+delta*100.0);
      error=r.getError();
      if(error!=null)
        error="Trouble in root: "+error;
      result=r.getRoot();
    }
    /** Return the prediction result */
    public double getPrediction()
    {
      return result;
    }
    /** Return any error message from the equation solver */
    public String getError()
    {
      return error;
    }
    /** This routine should only be called internally. It is used to
     * satisfy the callback interface required by the equation solver
     */
    public double f(double ypred)
    { // Work out difference between residual given by this value, and the
      // residual of rank target in the rest of the array
      // System.out.println("Ysum is "+ysum);
      double sum=ysum+ypred;
      sum=sum/(nn+1.0);
      y[nn]=ypred;
      double dot=0.0;
      for(int i=0;i<=nn;i++)
      {
        resid[i]=y[i]-sum;
        // System.out.println("Balanced y is "+resid[i]);
        dot+=x[i]*resid[i];
      }
      for(int i=0;i<=nn;i++)
      {
        resid[i]-=dot*x[i];
        // System.out.println("x "+x[i]+" y "+y[i]+" resid "+resid[i]);
      }
      // Now have to work out which residual is of rank target
      GetRank.forceRank(0,nn,lowTarget,resid);
      if(lowTarget<highTarget)
        GetRank.forceRank(lowTarget+1,nn,highTarget-lowTarget-1,resid);
      return resid[nn]-0.5*(resid[lowTarget]+resid[highTarget]);
    }
    private String error;
    private double ysum,x[],y[],resid[],result;
    private Root r;
    int nn;
    private int lowTarget, highTarget;
}