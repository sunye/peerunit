/**
 * ZZJ07_F3.java
 *
 * @author Antonio J. Nebro
 * @author Juan J. Durillo
 * @version 1.0
 */

package jmetal.problems.ZZJ07;

import jmetal.base.*;
import jmetal.base.Configuration.*;
import jmetal.util.JMException;

/** 
 * Class representing problem ZZJ07_F3
 */
public class ZZJ07_F3 extends Problem {
   
  /** 
   * Constructor
   * Creates a default instance of the ZZJ07_F3 problem
   * @param solutionType The solution type must "Real" or "BinaryReal".
   */
  public ZZJ07_F3(String solutionType) {
    this(30, solutionType); // 30 variables by default
  }
  /** 
   * Constructor
   * Creates a default instance of the ZZJ07_F1 problem
   * @param numberOfVariables Number of variables.
   * @param solutionType The solution type must "Real" or "BinaryReal".
   */
  public ZZJ07_F3(Integer numberOfVariables, String solutionType) {
    numberOfVariables_   = numberOfVariables.intValue() ;
    numberOfObjectives_  = 2;
    numberOfConstraints_ = 0;
    problemName_         = "ZZJ07_F3";
        
    upperLimit_ = new double[numberOfVariables_];
    lowerLimit_ = new double[numberOfVariables_];
    for (int var = 0; var < numberOfVariables_; var++){
      lowerLimit_[var] = 0.0;
      upperLimit_[var] = 1.0;
    } // for
        
    solutionType_ = Enum.valueOf(SolutionType_.class, solutionType) ; 
    
    // All the variables are of the same type, so the solutionType name is the
    // same than the variableType name
    variableType_ = new VariableType_[numberOfVariables_];
    for (int var = 0; var < numberOfVariables_; var++){
      variableType_[var] = Enum.valueOf(VariableType_.class, solutionType) ;    
    } // for
  } //ZZJ07_F3
    
  /** 
  * Evaluates a solution 
  * @param solution The solution to evaluate
   * @throws JMException 
  */        
  public void evaluate(Solution solution) throws JMException {
    DecisionVariables decisionVariables  = solution.getDecisionVariables();
    
    double [] x  = new double[numberOfVariables_] ; 
    double [] fx = new double[numberOfVariables_] ; 
    double g   ;
    double h   ;
    double sum ;
    for (int i = 0; i < numberOfVariables_; i++)
      x[i] = decisionVariables.variables_[i].getValue() ;
    
    fx[0] = 1.0-Math.exp(4.0*x[0])*Math.pow(Math.sin(6.0*Math.PI*x[0]),6.0);

    sum = 0.0 ;
    for (int i = 0; i < numberOfVariables_; i++)
      sum += (x[i]-x[0])*(x[i]-x[0]); 
      
    g = 1.0 + 9.0*Math.pow(sum/9,0.25);
    h = 1.0 - Math.pow(fx[0]/g,2.0);
    
    fx[1] = g * h ;
    
    solution.setObjective(0,fx[0]);
    solution.setObjective(1,fx[1]);
  } // evaluate
} // ZZJ07_F3


