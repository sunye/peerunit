/**
 * Sphere.java
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

public class Sphere extends Problem {
  /** 
   * Constructor
   * Creates a default instance of the Sphere problem
   * @param numberOfVariables Number of variables of the problem 
   * @param solutionType The solution type must "Real" or "BinaryReal". 
   */
  public Sphere(Integer numberOfVariables, String solutionType) {
    numberOfVariables_   = numberOfVariables ;
    numberOfObjectives_  = 1;
    numberOfConstraints_ = 0;
    problemName_         = "Sphere";
        
    upperLimit_ = new double[numberOfVariables_];
    lowerLimit_ = new double[numberOfVariables_];
    for (int var = 0; var < numberOfVariables_; var++){
      lowerLimit_[var] = -5.12;
      upperLimit_[var] = 5.12;
    } // for
        
    solutionType_ = Enum.valueOf(SolutionType_.class, solutionType) ; 
    
    // All the variables are of the same type, so the solutionType name is the
    // same than the variableType name
    variableType_ = new VariableType_[numberOfVariables_];
    for (int var = 0; var < numberOfVariables_; var++){
      variableType_[var] = Enum.valueOf(VariableType_.class, solutionType) ;    
    } // for
  } // Sphere
    
  /** 
  * Evaluates a solution 
  * @param solution The solution to evaluate
   * @throws JMException 
  */        
  public void evaluate(Solution solution) throws JMException {
    DecisionVariables decisionVariables  = solution.getDecisionVariables();

    double sum = 0.0;
    for (int var = 0; var < numberOfVariables_; var++) {
      sum += StrictMath.pow(decisionVariables.variables_[var].getValue(), 2.0);      
    }        
    solution.setObjective(0, sum);
  } // evaluate
} // Sphere

