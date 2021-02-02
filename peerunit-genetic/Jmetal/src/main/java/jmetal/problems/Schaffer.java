/**
 * Schaffer.java
 *
 * @author Antonio J. Nebro
 * @author Juan J. Durillo
 * @version 1.0
 */
package jmetal.problems;

import jmetal.base.*;
import jmetal.base.Configuration.SolutionType_;
import jmetal.base.Configuration.VariableType_;
import jmetal.util.JMException;

/**
 * Class representing problem Schaffer
 */
public class Schaffer extends Problem {    

 /**
  * Constructor.
  * Creates a default instance of the Schaffer problem
  * @param solutionType The solution type must "Real" or "BinaryReal".s 
  */
  public Schaffer(String solutionType) {
    numberOfVariables_  = 1;
    numberOfObjectives_ = 2;
    numberOfConstraints_ =0;
    problemName_         = "Schaffer";
        
    lowerLimit_ = new double[numberOfVariables_];
    upperLimit_ = new double[numberOfVariables_];        
    lowerLimit_[0] = -1000;
    upperLimit_[0] =  1000;
    
    solutionType_ = Enum.valueOf(SolutionType_.class, solutionType) ; 
    
    // All the variables are of the same type, so the solutionType name is the
    // same than the variableType name
    variableType_ = new VariableType_[numberOfVariables_];
    for (int var = 0; var < numberOfVariables_; var++){
      variableType_[var] = Enum.valueOf(VariableType_.class, solutionType) ;    
    } // for
  } //Schaffer

    
  /** 
  * Evaluates a solution 
  * @param solution The solution to evaluate
   * @throws JMException 
  */
  public void evaluate(Solution solution) throws JMException {
    DecisionVariables decisionVariables  = solution.getDecisionVariables();
    
    double [] f = new double[numberOfObjectives_];
    f[0] = decisionVariables.variables_[0].getValue() * 
           decisionVariables.variables_[0].getValue();
    
    f[1] = (decisionVariables.variables_[0].getValue() - 2.0) * 
           (decisionVariables.variables_[0].getValue() - 2.0);
        
    solution.setObjective(0,f[0]);
    solution.setObjective(1,f[1]);
  } //evaluate    
} //Schaffer
