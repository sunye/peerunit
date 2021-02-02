/**
 * Griewank.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 */
package jmetal.problems.singleObjective;

import jmetal.base.DecisionVariables;
import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.Configuration.SolutionType_;
import jmetal.base.Configuration.VariableType_;
import jmetal.util.JMException;

public class Griewank extends Problem {
  /** 
   * Constructor
   * Creates a default instance of the Griewank problem
   * @param numberOfVariables Number of variables of the problem 
   * @param solutionType The solution type must "Real" or "BinaryReal". 
   */
  public Griewank(Integer numberOfVariables, String solutionType) {
    numberOfVariables_   = numberOfVariables;
    numberOfObjectives_  = 1;
    numberOfConstraints_ = 0;
    problemName_         = "Sphere";
        
    upperLimit_ = new double[numberOfVariables_];
    lowerLimit_ = new double[numberOfVariables_];
    for (int var = 0; var < numberOfVariables_; var++){
      lowerLimit_[var] = -600.0;
      upperLimit_[var] = 600.0;
    } // for
        
    solutionType_ = Enum.valueOf(SolutionType_.class, solutionType) ; 
    
    // All the variables are of the same type, so the solutionType name is the
    // same than the variableType name
    variableType_ = new VariableType_[numberOfVariables_];
    for (int var = 0; var < numberOfVariables_; var++){
      variableType_[var] = Enum.valueOf(VariableType_.class, solutionType) ;    
    } // for
  } // Griewank
    
  /** 
  * Evaluates a solution 
  * @param solution The solution to evaluate
   * @throws JMException 
  */        
  public void evaluate(Solution solution) throws JMException {
    DecisionVariables decisionVariables  = solution.getDecisionVariables();

    double sum  = 0.0    ;
    double mult = 0.0    ;
    double d    = 4000.0 ;
    for (int var = 0; var < numberOfVariables_; var++) {
      sum += decisionVariables.variables_[var].getValue() * 
             decisionVariables.variables_[var].getValue() / d ;    
      mult *= Math.cos(decisionVariables.variables_[var].getValue()/Math.sqrt(var)) ;    
    }        


    solution.setObjective(0, 1.0 + sum - mult) ;
  } // evaluate
} // Griewank

