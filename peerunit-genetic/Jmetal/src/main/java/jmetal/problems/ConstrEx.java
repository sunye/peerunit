/**
 * ConstrEx.java
 *
 * @author Antonio J. Nebro
 * @author Juan J. Durillo
 * @version 1.0
 */
package jmetal.problems;

import jmetal.base.Configuration.*;
import jmetal.base.*;
import jmetal.util.JMException;

/**
 * Class representing problem Constr_Ex
 */
public class ConstrEx extends Problem{
  /**
   * Constructor
   * Creates a default instance of the Constr_Ex problem
   * @param solutionType The solution type must "Real" or "BinaryReal".
   */
  public ConstrEx(String solutionType) {
    numberOfVariables_  = 2;
    numberOfObjectives_ = 2;
    numberOfConstraints_= 2;
    problemName_        = "Constr_Ex";
        
    lowerLimit_ = new double[numberOfVariables_];
    upperLimit_ = new double[numberOfVariables_];        
    lowerLimit_[0] = 0.1;
    lowerLimit_[1] = 0.0;        
    upperLimit_[0] = 1.0;
    upperLimit_[1] = 5.0;
        
    solutionType_ = Enum.valueOf(SolutionType_.class, solutionType) ; 
    
    // All the variables are of the same type, so the solutionType name is the
    // same than the variableType name
    variableType_ = new VariableType_[numberOfVariables_];
    for (int var = 0; var < numberOfVariables_; var++){
      variableType_[var] = Enum.valueOf(VariableType_.class, solutionType) ;    
    } // for
  } // ConstrEx
     
  /** 
  * Evaluates a solution 
  * @param solution The solution to evaluate
   * @throws JMException 
  */
  public void evaluate(Solution solution) throws JMException {
    DecisionVariables decisionVariables  = solution.getDecisionVariables();
       
    double [] f = new double[numberOfObjectives_];
    f[0] = decisionVariables.variables_[0].getValue();        
    f[1] = (1.0 + decisionVariables.variables_[1].getValue())/
                  decisionVariables.variables_[0].getValue();        
    
    solution.setObjective(0,f[0]);
    solution.setObjective(1,f[1]);
  } // evaluate

 /** 
  * Evaluates the constraint overhead of a solution 
  * @param solution The solution
 * @throws JMException 
  */  
  public void evaluateConstraints(Solution solution) throws JMException {
    double [] constraint = new double[this.getNumberOfConstraints()];

    double x1 = solution.getDecisionVariables().variables_[0].getValue();
    double x2 = solution.getDecisionVariables().variables_[1].getValue();
        
    constraint[0] =  (x2 + 9*x1 -6.0) ;
    constraint[1] =  (-x2 + 9*x1 -1.0);
        
    double total = 0.0;
    int number = 0;
    for (int i = 0; i < this.getNumberOfConstraints(); i++)
      if (constraint[i]<0.0){
        total+=constraint[i];
        number++;
      }
        
    solution.setOverallConstraintViolation(total);    
    solution.setNumberOfViolatedConstraint(number);         
  } // evaluateConstraints  
} // ConstrEx