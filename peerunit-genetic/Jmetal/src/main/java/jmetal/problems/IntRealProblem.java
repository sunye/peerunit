/**
 * IntRealProblem.java
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
 * Class representing a problem having N integer and M real variables.
 * This is not a true problem; it is only intended as an example
 */
public class IntRealProblem extends Problem {  
    
  int intVariables_  ;
  int realVariables_ ;
  
  /** 
   * Constructor.
   * Creates a default instance of the Kursawe problem.
   */
  public IntRealProblem() {
    this(3, 3);
  } // IntRealProblem
  
  /** 
   * Constructor.
   * Creates a new instance of the Kursawe problem.
   * @param intVariables Number of integer variables of the problem 
   * @param realVariables Number of real variables of the problem 
   */
  public IntRealProblem(int intVariables, int realVariables) {
    intVariables_  = intVariables  ;
    realVariables_ = realVariables ;
    
    numberOfVariables_   = intVariables_ + realVariables_ ;
    numberOfObjectives_  = 2                              ;
    numberOfConstraints_ = 0                              ;
    problemName_         = "IntRealProblem"               ;
        
    upperLimit_ = new double[numberOfVariables_] ;
    lowerLimit_ = new double[numberOfVariables_] ;
       
    for (int i = 0; i < intVariables; i++) {
      lowerLimit_[i] = -5 ;
      upperLimit_[i] =  5 ;
    } // for
        
    for (int i = intVariables; i < (intVariables + realVariables); i++) {
      lowerLimit_[i] = -5.0 ;
      upperLimit_[i] =  5.0  ;
    } // for
    
    solutionType_ = SolutionType_.IntReal ; 
    
    // Creating the array indicating the types of the variables
    variableType_ = new VariableType_[numberOfVariables_];
    
    // Initializing the types of the variables
    for (int i = 0; i < intVariables_; i++) 
      variableType_[i] = VariableType_.Int ; 
        
    for (int i = intVariables_; i < (intVariables_ + realVariables_); i++) {
      variableType_[i] = VariableType_.Real ; 
    } // for    

  } // Kursawe
    
  /** 
  * Evaluates a solution 
  * @param solution The solution to evaluate
   * @throws JMException 
  */
  public void evaluate(Solution solution) throws JMException {
    DecisionVariables decisionVariables  = solution.getDecisionVariables();
        
    double [] fx = new double[2] ; // function values     
   
    fx[0] = 0.0 ; 
    for (int var = 0; var < intVariables_ ; var++) {        
      fx[0] += (int)decisionVariables.variables_[var].getValue() ;
    } // for
        
    fx[1] = 0.0 ; 
    for (int var = intVariables_; var < numberOfVariables_ ; var++) {        
      fx[0] += decisionVariables.variables_[var].getValue() ;
    } // for
        
    solution.setObjective(0, fx[0]);
    solution.setObjective(1, fx[1]);
  } // evaluate
} // IntRealProblem
