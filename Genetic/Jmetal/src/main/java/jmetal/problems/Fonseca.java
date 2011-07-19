/**
 * Fonseca.java
 *
 * @author Antonio J. Nebro
 * @author Juan J. Durillo
 * @version 1.0
 */
package jmetal.problems;

import jmetal.base.*;
import jmetal.base.Configuration.*;
import jmetal.util.JMException;

/** 
 * Class representing problem Fonseca
 */
public class Fonseca extends Problem {
   
  /** 
   * Constructor
   * Creates a default instance of the Fonseca problem
   * @param solutionType The solution type must "Real" or "BinaryReal".
   */
  public Fonseca(String solutionType) {
    numberOfVariables_   = 3;
    numberOfObjectives_  = 2;
    numberOfConstraints_ = 0;
    problemName_         = "Fonseca";
        
    upperLimit_ = new double[numberOfVariables_];
    lowerLimit_ = new double[numberOfVariables_];
    for (int var = 0; var < numberOfVariables_; var++){
      lowerLimit_[var] = -4.0;
      upperLimit_[var] = 4.0;
    } // for
        
    solutionType_ = Enum.valueOf(SolutionType_.class, solutionType) ; 
    
    // All the variables are of the same type, so the solutionType name is the
    // same than the variableType name
    variableType_ = new VariableType_[numberOfVariables_];
    for (int var = 0; var < numberOfVariables_; var++){
      variableType_[var] = Enum.valueOf(VariableType_.class, solutionType) ;    
    } // for
  } //Fonseca
    
  /** 
  * Evaluates a solution 
  * @param solution The solution to evaluate
   * @throws JMException 
  */        
  public void evaluate(Solution solution) throws JMException {
    DecisionVariables decisionVariables  = solution.getDecisionVariables();

    double [] f = new double[numberOfObjectives_];
    double sum1 = 0.0;
    for (int var = 0; var < numberOfVariables_; var++){
      sum1 += StrictMath.pow(decisionVariables.variables_[var].getValue() 
              - (1.0/StrictMath.sqrt((double)numberOfVariables_)),2.0);            
    }
    double exp1 = StrictMath.exp((-1.0)*sum1);
    f[0] = 1 - exp1;
        
    double sum2 = 0.0;        
    for (int var = 0; var < numberOfVariables_; var++){
      sum2 += StrictMath.pow(decisionVariables.variables_[var].getValue() 
              + (1.0/StrictMath.sqrt((double)numberOfVariables_)),2.0);
    }    
    double exp2 = StrictMath.exp((-1.0)*sum2);
    f[1] = 1 - exp2;
        
    solution.setObjective(0,f[0]);
    solution.setObjective(1,f[1]);
  } // evaluate
} // Fonseca
