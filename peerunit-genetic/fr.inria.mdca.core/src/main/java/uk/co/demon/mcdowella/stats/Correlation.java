package uk.co.demon.mcdowella.stats;

/** Work out correlation, dot product, sigmage of dot
 * product. See e.g. Cox+Hinkley, Example 6.2. The constructor
 * does nearly all the work here
 */
public class Correlation
{
    /** Work out the correlation etc.
     * @param n the number of pairs of (x,y) values to correlate
     * @param x the x values
     * @param y the associated y values
     */
    public Correlation(int n, double[] x, double[] y)
    {
      double cov;
      lt=eq=gt=0;
      nn=n;
      xx=x;
      yy=y;
      if (n<2)
      {
        corr=dot=sigmage=0.0;
        return; 
      }
      xmean=ymean=xvar=yvar=dot=0.0;
      for(int i=0;i<n;i++)
      {
        xmean+=x[i];
        ymean+=y[i];
        dot+=x[i]*y[i];
        xvar+=x[i]*x[i];
        yvar+=y[i]*y[i];
      }
      xmean=xmean/n;
      ymean=ymean/n;
      xvar=xvar/n-xmean*xmean;
      yvar=yvar/n-ymean*ymean;
      // System.out.println("x mean "+xmean+" x var "+xvar+" y mean "+ymean+
      //                    "y var "+yvar);
      cov=dot/n-xmean*ymean;  // dot - mean(dot) is n times this
      corr=cov/Math.sqrt(xvar*yvar);
      double ac=Math.abs(corr);
      if(ac>1.0001)
      { // here if big trouble
        throw new ArithmeticException("got correlation "+corr);
      }
      else if (ac>1.0)
      { // here if fp inaccuracy which could cause trouble later
        corr=Math.rint(corr); // round to either +1 or -1
      }
      // Variance of dot product is n*var(x)*n*var(y)/(n-1) so...
      sigmage=corr*Math.sqrt(n-1.0);
    }
    /**
     * @return the correlation
     */
    public double getCor()
    {
      return corr;
    }
    /**
     * @return the Sigmage of the correlation. That is, its
     * value, divided by the standard deviation of the correlation
     * under the distribution produced by randomly permuting the lists
     * of x and y values.
     */
    public double getSig()
    {
      return sigmage;
    }
    /** These are the usual linear predictors from a correlation.
     * @return the linear prediction of y, given x
     */
    public double linearY(double xval)
    {
      if(xvar==0) return ymean;
      return ymean+(xval-xmean)*corr*Math.sqrt(yvar/xvar);
    }
    /** predictive variance, accounts for variance due to noise in y
     * AND variance due to distance of x from mean times noise in
     * correlation see e.g. Dudewicz+Mishra eqn 14.3.7
     * @param xval the x value at which we plan to predict y
     * @return the predictive variance of y given x
     */
    public double predYVar(double xval)
    {
      if(nn<3) return 0.0;
      double diff=xval-xmean;
      return (1.0+1.0/nn+diff*diff/(nn*xvar))*
                       (nn*yvar*(1.0-corr*corr)/(nn-2.0));
    }
    /** predictive variance, accounts for variance due to noise in y
     * AND variance due to distance of x from mean times noise in
     * correlation see e.g. Dudewicz+Mishra eqn 14.3.7
     * @param yval the y value at which we plan to predict x
     * @return the predictive variance of x given y
     */
    public double predXVar(double yval)
    {
      if(nn<3) return 0.0;
      double diff=yval-ymean;
      return (1.0+1.0/nn+diff*diff/(nn*yvar))*
                       (nn*xvar*(1.0-corr*corr)/(nn-2.0));
    }
    /**
     * @return a prediction for x, given y
     */
    public double linearX(double yval)
    {
      if(yvar==0) return xmean;
      return xmean+(yval-ymean)*corr*Math.sqrt(xvar/yvar);
    }
    /**
     * @return the number of points
     */
    public int getN()
    {
      return nn;
    }
    /** @return the x values
     */
    public double[] getXX()
    {
      return xx;
    }
    /** @return the y values
     */
    public double[] getYY()
    {
      return yy;
    }
    private int nn;
    private double xx[], yy[], corr, dot, sigmage, xmean, ymean, xvar, yvar;
    private int lt,eq,gt;
}