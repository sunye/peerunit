/**
 * Tanaka.java
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
 * Class representing problem Tanaka
 */
public class Tanaka extends Problem{    
   
 /**
  * Constructor.
  * Creates a default instance of the problem Tanaka
  * @param solutionType The solution type must "Real" or "BinaryReal". 
  */
  public Tanaka(String solutionType) {
    numberOfVariables_  = 2;
    numberOfObjectives_ = 2;
    numberOfConstraints_= 2;
    problemName_        = "Tanaka";
        
    upperLimit_ = new double[numberOfVariables_];
    lowerLimit_ = new double[numberOfVariables_];
    for (int var = 0; var < numberOfVariables_; var++){
      lowerLimit_[var] = 10e-5;
      upperLimit_[var] = Math.PI;
    } // for
        
    solutionType_ = Enum.valueOf(SolutionType_.class, solutionType) ; 
    
    // All the variables are of the same type, so the solutionType name is the
    // same than the variableType name
    variableType_ = new VariableType_[numberOfVariables_];
    for (int var = 0; var < numberOfVariables_; var++){
      variableType_[var] = Enum.valueOf(VariableType_.class, solutionType) ;    
    } // for
  }

  /** 
  * Evaluates a solution 
  * @param solution The solution to evaluate
   * @throws JMException 
  */
  public void evaluate(Solution solution) throws JMException {
    DecisionVariables decisionVariables  = solution.getDecisionVariables();
    
    double [] f = new double[numberOfObjectives_];
    f[0] = decisionVariables.variables_[0].getValue();
    f[1] = decisionVariables.variables_[1].getValue();
       
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
        
    constraint[0] = (x1*x1 + x2*x2 - 1.0 - 0.1*Math.cos(16.0*Math.atan(x1/x2)));
    constraint[1] = - 2.0 * ( (x1-0.5)*(x1-0.5) + (x2-0.5)*(x2-0.5) - 0.5);
        
    int number = 0;
    double total = 0.0;
    for (int i = 0; i < this.getNumberOfConstraints(); i++)
      if (constraint[i]<0.0){
        number++;
        total+=constraint[i];
      }
        
    solution.setOverallConstraintViolation(total);    
    solution.setNumberOfViolatedConstraint(number);
  } // evaluateConstraints   
} // Tanaka