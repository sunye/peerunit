package uk.co.demon.mcdowella.stats;

/**
 * This class is used to return significance tail probabilities
 */
public class SigProb
{
  /** Record results of computed significance probs
   * @param probLt is prob < the observed value
   * @param probEq is prob = the observed value
   * @param probGt is prob > the observed value
   */
  public SigProb(double probLt, double probEq, double probGt)
  {
    lt=probLt;
    eq=probEq;
    gt=probGt;
  }
  /**
   * @return prob < the observed value
   */
  public double getLt()
  {
    return lt;
  }
  /**
   * @return prob = the observed value
   */
  public double getEq()
  {
    return eq;
  }
  /**
   * @return prob = the observed value
   */
  public double getGt()
  {
    return gt;
  }
  public String toString()
  {
    return "<: "+lt+" =:"+eq+" >:"+gt;
  }
  private double lt,eq,gt;
}