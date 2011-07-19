/**
 * UniformMutation.java
 * Class representing a uniform mutation operator
 * @author Antonio J.Nebro
 * @version 1.0
 */
package jmetal.base.operator.mutation;

import jmetal.base.Solution;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.base.Configuration;
import jmetal.base.Operator;
import jmetal.base.DecisionVariables;
import jmetal.base.Configuration.SolutionType_;

/**
 * This class implements a uniform mutation operator.
 * NOTE: the type of the solutions must be <code>SolutionType_.Real</code>
 */
public class UniformMutation extends Operator{
    
  /**
   * Stores the value used in a uniform mutation operator
   */
  private Double perturbation_;
    
    
  /** 
   * Constructor
   * Creates a new uniform mutation operator instance
   */
  public UniformMutation()
  {
  } // UniformMutation

  /**
  * Performs the operation
  * @param probability Mutation probability
  * @param solution The solution to mutate
   * @throws JMException 
  */
  public void doMutation(double probability, Solution solution) throws JMException {                        
    for (int var = 0; var < solution.getDecisionVariables().size(); var++)
    {
      if (PseudoRandom.randDouble() < probability)
      {
        double rand = PseudoRandom.randDouble();
        double tmp = (rand - 0.5)*perturbation_.doubleValue();
                                
        tmp += solution.getDecisionVariables().variables_[var].getValue();
                
        if (tmp < solution.getDecisionVariables().variables_[var].getLowerBound())
            tmp = solution.getDecisionVariables().variables_[var].getLowerBound();                    
        else if (tmp > solution.getDecisionVariables().variables_[var].getUpperBound())
            tmp = solution.getDecisionVariables().variables_[var].getUpperBound();                    
                
        solution.getDecisionVariables().variables_[var].setValue(tmp);                             
      }
    }
  } // doMutation
  
  /**
  * Executes the operation
  * @param object An object containing the solution to mutate
   * @throws JMException 
  */
  public Object execute(Object object) throws JMException {
    Solution solution = (Solution )object;
    
    if (solution.getType() != SolutionType_.Real) {
      Configuration.logger_.severe("UniformMutation.execute: the solution " +
          "is not of the right type. The type should be 'Real', but " +
          solution.getType() + " is obtained");

      Class cls = java.lang.String.class;
      String name = cls.getName(); 
      throw new JMException("Exception in " + name + ".execute()") ;
    } // if 
    
    Double probability;
        
    if (perturbation_ == null)
      perturbation_ = (Double)getParameter("perturbationIndex");
        
    probability = (Double)getParameter("probability");
    if (probability == null)
    {
      Configuration.logger_.severe("UniformMutation.execute: probability " +
      "not specified");
      Class cls = java.lang.String.class;
      String name = cls.getName(); 
      throw new JMException("Exception in " + name + ".execute()") ;  
    }
    
    doMutation(probability.doubleValue(),solution);
        
    return solution;
  } // execute                  
} // UniformMutation
