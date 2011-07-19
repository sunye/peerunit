/**
 * PolynomialMutation.java
 * @author Juan J. Durillo
 * @version 1.0
 * 
 */
package jmetal.base.operator.mutation;

import jmetal.base.Configuration;
import jmetal.base.Solution;
import jmetal.base.DecisionVariables;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.base.Operator;
import jmetal.base.Configuration.SolutionType_;

/**
 * This class implements a polynomial mutation operator. 
 * NOTE: the operator is applied to Real solutions, so the type of the solutions
 * must be </code>SolutionType_.Real</code>.
 * NOTE: if you use the default constructor, the value of the etc_m parameter is
 * DEFAULT_INDEX_MUTATION. You can change it using the parameter 
 * "distributionIndex" before invoking the execute() method -- see lines 116-119
 */
public class PolynomialMutation extends Operator {
    
  /**
  * DEFAULT_INDEX_MUTATION defines a default index for mutation
  */
  public static final double DEFAULT_INDEX_MUTATION = 20.0;
    
  /**
  * eta_c stores the index for mutation to use
  */
  private double eta_m_;
   
  /**
  * Constructor
  * Creates a new instance of the polynomial mutation operator
  */
  public PolynomialMutation() {
    eta_m_ = DEFAULT_INDEX_MUTATION;
  } // PolynomialMutation
    
  /**
  * Constructor.
  * Create a new PolynomialMutation operator with an specific index
  */
  public PolynomialMutation(double eta_m) {
    eta_m_ = eta_m;
  }
  
  
  /**
  * Perform the mutation operation
  * @param probability Mutation probability
  * @param solution The solution to mutate
   * @throws JMException 
  */
  public void doMutation(double probability, Solution solution) throws JMException {        
    double rnd, delta1, delta2, mut_pow, deltaq;
    double y, yl, yu, val, xy;
    for (int var=0; var < solution.getDecisionVariables().size(); var++)
    {
      if (PseudoRandom.randDouble() <= probability)
      {
        y      = solution.getDecisionVariables().variables_[var].getValue();
        yl     = solution.getDecisionVariables().variables_[var].getLowerBound();                
        yu     = solution.getDecisionVariables().variables_[var].getUpperBound();
        delta1 = (y-yl)/(yu-yl);
        delta2 = (yu-y)/(yu-yl);
        rnd = PseudoRandom.randDouble();
        mut_pow = 1.0/(eta_m_+1.0);
        if (rnd <= 0.5)
        {
          xy     = 1.0-delta1;
          val    = 2.0*rnd+(1.0-2.0*rnd)*(Math.pow(xy,(eta_m_+1.0)));
          deltaq =  java.lang.Math.pow(val,mut_pow) - 1.0;
        }
        else
        {
          xy = 1.0-delta2;
          val = 2.0*(1.0-rnd)+2.0*(rnd-0.5)*(java.lang.Math.pow(xy,(eta_m_+1.0)));
          deltaq = 1.0 - (java.lang.Math.pow(val,mut_pow));
        }
        y = y + deltaq*(yu-yl);
        if (y<yl)
          y = yl;
        if (y>yu)
          y = yu;
        solution.getDecisionVariables().variables_[var].setValue(y);                           
      }
    }                
  } // doMutation
  
  /**
  * Executes the operation
  * @param object An object containing a solution
  * @return An object containing the mutated solution
   * @throws JMException 
  */  
  public Object execute(Object object) throws JMException {
    Solution solution = (Solution)object;

    if (solution.getType() != SolutionType_.Real) {
      Configuration.logger_.severe("PolynomialMutation.execute: the solution " +
          "is not of the right type. The type should be 'Real', but " +
          solution.getType() + " is obtained");

      Class cls = java.lang.String.class;
      String name = cls.getName(); 
      throw new JMException("Exception in " + name + ".execute()") ;
    } // if 
    
    Double probability = (Double)getParameter("probability");       
    if (probability == null)
    {
      Configuration.logger_.severe("PolynomialMutation.execute: probability " +
      "not specified");
      Class cls = java.lang.String.class;
      String name = cls.getName(); 
      throw new JMException("Exception in " + name + ".execute()") ;  
    }
        
    Double distributionIndex = (Double)getParameter("distributionIndex");
    if (distributionIndex != null) {
      eta_m_ = distributionIndex ;
    } // if
    
    doMutation(probability.doubleValue(),solution);
    return solution;      
  } // execute
 
} // PolynomialMutation
