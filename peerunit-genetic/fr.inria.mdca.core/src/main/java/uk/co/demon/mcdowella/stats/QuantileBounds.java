/* A.G.McDowell 20010623. NOT OF PRODUCTION QUALITY - NO WARRANTY
 Feel free to use this code as you will, and at your own risk,
 but I would appreciate an acknowledgement
*/
package uk.co.demon.mcdowella.stats;

/** This class receives a success probability, a number of trials,
 * and an error probability. It computes confidence regions for the pth
 * quartile. The pth quartile Qp is such that the probability
 * of observing a value > Qp is <= 1-p, and the probability
 * of observing a value < Qp is <= p. (See Conover: Practical
 * Non-Parametric Statistics). So if we rank observations
 * X1..Xn and choose as a confidence interval [Xa, Xb] then
 * Xa is too high iff n-a+1 or more events with probability
 * <= 1-p have occurred, for which an upper bound is the probability
 * of n-a+1 or more events with probability 1-p, which is the same 
 * probability as the probability of a-1 or fewer events with 
 * probability p.
 * Xb is too low iff b or more events with probability < p
 * have occurred. So to find good a and b we want to find
 * a and b such that the probability of a-1 or fewer successes
 * plus the probability of b or more successes is small
 */
public class QuantileBounds
{
  private int trials;
  private double probSuccess;
  private int upperBound;
  private int lowerBound;
  private double achievedTypeIError;
  /** Checks interrupt status as can take some time */
  public QuantileBounds(int numTrials,
    double pSuccess, double typeIError)
  {
    trials = numTrials;
    if (trials < 0)
    {
      throw new IllegalArgumentException("Nonsensical trials");
    }
    probSuccess = pSuccess;
    if (trials == 0)
    {
      upperBound = lowerBound = 0;
      achievedTypeIError = 1.0;
      return;
    }
    if (probSuccess == 0)
    { // Q0 is absolute lower limit. Only sensible region
      // for this is [lowest seen, lowest seen] but
      // if distribution continuous this MUST be wrong
      upperBound = lowerBound = 1;
      achievedTypeIError = 1.0;
      return;
    }
    if (probSuccess < 0)
    {
      throw new IllegalArgumentException("-ve success probability");
    }
    if (probSuccess == 1.0)
    {
      upperBound = lowerBound = trials;
      achievedTypeIError = 1.0;
      return;
    }
    if (probSuccess > 1.0)
    {
      throw new IllegalArgumentException("success probability > 1");
    }
    // devote half the allowed error to the lower bound
    double halfError = typeIError / 2.0;
    BinomialProb bp = new BinomialProb(trials, probSuccess);
    boolean gotLowerBound = false;
    double sofar = 0.0;
    for (int i = 0; i <= trials; i++)
    {
      double probHere = bp.getProb(i);
      sofar += probHere;
      if ((sofar <= halfError) || !gotLowerBound)
      { // either desparate or have good probability for <= i=a-1
        // successes: lower bound a = i + 1
        lowerBound = i + 1;
	achievedTypeIError = sofar;
	gotLowerBound = true;
      }
      else
      { // probability can only increase
        break;
      }
    }
    // remaining error budget
    double errorLeft = typeIError - achievedTypeIError; 
    boolean gotUpperBound = false;
    double upperError = 0.0;
    sofar = 0.0;
    double achievedUpperError = 0.0;
    for (int i = trials; i >= 0; i--)
    {
      double probHere = bp.getProb(i);
      upperError += probHere;
      if ((upperError <= errorLeft) || !gotUpperBound)
      { // prob here is probability of i=b or more successes
        upperBound = i;
	achievedUpperError = upperError;
	gotUpperBound = true;
      }
      else
      {
        break;
      }
    }
    achievedTypeIError += achievedUpperError;
  }
  public int getTrials()
  {
    return trials;
  }
  public double getProbSuccess()
  {
    return probSuccess;
  }
  public int getUpperBound()
  {
    return upperBound;
  }
  public int getLowerBound()
  {
    return lowerBound;
  }
  public double getTypeIError()
  {
    return achievedTypeIError;
  }
  public static void main(String[] s)
  {
    double typeIError = 0.05;
    int numTrials = 100;
    double pSuccess = 0.5;
    int a1 = s.length - 1;
    boolean trouble = false;
    int i = 0;
    try
    {
      for (; i < a1; i++)
      {
	if ("-samples".equals(s[i]))
	{
	  i++;
	  numTrials = Integer.parseInt(s[i].trim());
	}
	else if ("-quantile".equals(s[i]))
	{
	  i++;
	  pSuccess = Double.parseDouble(s[i].trim());
	}
	else if ("-error".equals(s[i]))
	{
	  i++;
	  typeIError = Double.parseDouble(s[i].trim());
	}
	else
	{
	  System.err.println("Could not handle flag " + s[i]);
	  trouble = true;
	}
      }
    }
    catch (NumberFormatException nfe)
    {
      System.err.println("Could not read number in " + s[i]);
      trouble = true;
    }
    if (i != s.length)
    {
      System.err.println("Final argument " + s[i] + " not used");
      trouble = true;
    }
    if (trouble)
    {
      System.err.println(
        "Args are [-error #] [-quantile #] [-samples #]");
      System.exit(1);
    }
    QuantileBounds qb = new QuantileBounds(numTrials, pSuccess,
      typeIError);
    System.out.println("For " + qb.getTrials() + " Lower bound " +
      qb.getLowerBound() + " upper bound " + qb.getUpperBound() +
      " prob not containing quantile for " + qb.getProbSuccess() +
      " is " + qb.getTypeIError());
  }
}
