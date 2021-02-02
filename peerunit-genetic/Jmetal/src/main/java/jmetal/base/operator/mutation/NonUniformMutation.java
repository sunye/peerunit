/**
 * NonUniformMutation.java
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
  * This class implements a non-uniform mutation operator.
  * NOTE: the type of the solutions must be <code>SolutionType_.Real</code>
  */
public class NonUniformMutation extends Operator{
    
  /**
   * perturbation_ stores the perturbation value used in the Non Uniform 
   * mutation operator
   */
  private Double perturbation_ = null;
  
  /**
   * maxIterations_ stores the maximun number of iterations. 
   */
  private Integer maxIterations_ = null;    
  
  /**
   * actualIteration_ stores the iteration in which the operator is going to be
   * applied
   */
  private Integer actualIteration_ = null;
         
  /** 
  * Constructor
  * Creates a new instance of the non uniform mutation
  */
  public NonUniformMutation() 
  {        
  } // NonUniformMutation
            
 
  /**
  * Perform the mutation operation
  * @param probability Mutation probability
  * @param solution The solution to mutate
   * @throws JMException 
  */
  public void doMutation(double probability, Solution solution) throws JMException {                

    for (int var = 0; var < solution.getDecisionVariables().size(); var++) {         
      if (PseudoRandom.randDouble() < probability) {
        double rand = PseudoRandom.randDouble();
        double tmp;
                
        if (rand <= 0.5)
        {
          tmp = delta(
                  solution.getDecisionVariables().variables_[var].getUpperBound() -
                  solution.getDecisionVariables().variables_[var].getValue(),
                  perturbation_.doubleValue());
          tmp += solution.getDecisionVariables().variables_[var].getValue();
        }
        else
        {
          tmp = delta(
                  solution.getDecisionVariables().variables_[var].getLowerBound() - 
                  solution.getDecisionVariables().variables_[var].getValue(),
                  perturbation_.doubleValue());
          tmp += solution.getDecisionVariables().variables_[var].getValue();
        }
                
        if (tmp < solution.getDecisionVariables().variables_[var].getLowerBound())
          tmp = solution.getDecisionVariables().variables_[var].getLowerBound();
        else if (tmp > solution.getDecisionVariables().variables_[var].getUpperBound())
          tmp = solution.getDecisionVariables().variables_[var].getUpperBound();
                
        solution.getDecisionVariables().variables_[var].setValue(tmp);
      }
    }
  } // doMutation
    

  /**
   * Calculates the delta value used in NonUniform mutation operator
   */
  private double delta(double y, double bMutationParameter) {
    double rand = PseudoRandom.randDouble();
    int it,maxIt;
    it    = actualIteration_.intValue();
    maxIt = maxIterations_.intValue();
        
    return (y * (1.0 - 
                Math.pow(rand,
                         Math.pow((1.0 - it /(double) maxIt),bMutationParameter)
                         )));
  } // delta

  /**
  * Executes the operation
  * @param object An object containing a solution
  * @return An object containing the mutated solution
   * @throws JMException 
  */
  public Object execute(Object object) throws JMException {
    Solution solution = (Solution )object;
    
    if (solution.getType() != SolutionType_.Real) {
      Configuration.logger_.severe("NonUniformMutation.execute: the solution " +
          "is not of the right type. The type should be 'Real', but " +
          solution.getType() + " is obtained");

      Class cls = java.lang.String.class;
      String name = cls.getName(); 
      throw new JMException("Exception in " + name + ".execute()") ;
    } // if 
    
    Double probability;
      
    if (perturbation_ == null)
      perturbation_ = (Double) getParameter("perturbationIndex");
        
    if (maxIterations_ == null)
      maxIterations_ = (Integer) getParameter("maxIterations");
        
    actualIteration_ = (Integer) getParameter("currentIteration");
    probability =(Double) parameters_.get("probability");
    
    if (probability == null)
    {
      Configuration.logger_.severe("NonUniformMutation.execute: probability " +
          "not specified");
      Class cls = java.lang.String.class;
      String name = cls.getName(); 
      throw new JMException("Exception in " + name + ".execute()") ;  
    }         
    
    doMutation(probability.doubleValue(),solution);
        
    return solution;    
  } // execute
} // NonUniformMutation
