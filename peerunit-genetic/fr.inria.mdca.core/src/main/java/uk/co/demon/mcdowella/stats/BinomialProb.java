/* A.G.McDowell 20010623. NOT OF PRODUCTION QUALITY - NO WARRANTY
 Feel free to use this code as you will, and at your own risk,
 but I would appreciate an acknowledgement
*/
package uk.co.demon.mcdowella.stats;

class BinomialProb
{
  private double logSuccess;
  private double logFailure;
  private double logFactorial;
  private int trials;
  BinomialProb(int numTrials, double probSuccess)
  {
    trials = numTrials;
    logSuccess = Math.log(probSuccess);
    logFailure = Math.log(1.0 - probSuccess);
    logFactorial = LogFact.lF(trials);
  }
  double getProb(int successes)
  {
    int failures = trials - successes;
    double t1 = logSuccess * successes +
                logFailure * (failures) + logFactorial 
		- LogFact.lF(successes) - LogFact.lF(failures);
    return Math.exp(t1);
  }
}
