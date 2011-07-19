/**
 * CEC2009_UF10.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 */

package jmetal.problems.cec2009Competition;

import jmetal.base.*;
import jmetal.base.Configuration.SolutionType_;
import jmetal.base.Configuration.VariableType_;
import jmetal.util.JMException;

/**
 * Class representing problem CEC2009_UF10
 */
public class CEC2009_UF10 extends Problem {
  
 /** 
  * Constructor.
  * Creates a default instance of problem CEC2009_UF10 (30 decision variables)
  * @param solutionType The solution type must "Real" or "BinaryReal".
  */
  public CEC2009_UF10(String solutionType) {
    this(30,  solutionType); // 30 variables by default
  } // CEC2009_UF10
  
 /**
  * Creates a new instance of problem CEC2009_UF10.
  * @param numberOfVariables Number of variables.
  * @param solutionType The solution type must "Real" or "BinaryReal".
  */
  public CEC2009_UF10(Integer numberOfVariables, String solutionType) {
    numberOfVariables_  = numberOfVariables.intValue();
    numberOfObjectives_ =  3;
    numberOfConstraints_=  0;
    problemName_        = "CEC2009_UF10";
    
    upperLimit_ = new double[numberOfVariables_];
    lowerLimit_ = new double[numberOfVariables_];

    //Establishes upper and lower limits for the variables
    for (int var = 0; var < numberOfVariables_; var++) {
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
  } // CEC2009_UF10
    
  /** 
   * Evaluates a solution.
   * @param solution The solution to evaluate.
   * @throws JMException 
   */
  public void evaluate(Solution solution) throws JMException {
    DecisionVariables decisionVariables  = solution.getDecisionVariables();
    
    double [] x = new double[numberOfVariables_] ;
    for (int i = 0; i < numberOfVariables_; i++)
      x[i] = decisionVariables.variables_[i].getValue() ;

  	int count1, count2, count3;
		double sum1, sum2, sum3, yj, hj;
		sum1   = sum2 = sum3 = 0.0;
		count1 = count2 = count3 = 0;
    
    for (int j = 3 ; j <= numberOfVariables_; j++) {
			yj = x[j-1] - 2.0*x[1]*Math.sin(2.0*Math.PI*x[0]+j*Math.PI/numberOfVariables_);
			hj = 4.0*yj*yj - Math.cos(8.0*Math.PI*yj) + 1.0;
			if(j % 3 == 1) {
				sum1  += hj;
				count1++;
			} else if(j % 3 == 2) {
				sum2  += hj;
				count2++;
			} else {
				sum3  += hj;
				count3++;
			}
    }
    
    solution.setObjective(0, Math.cos(0.5*Math.PI*x[0])*Math.cos(0.5*Math.PI*x[1]) + 2.0*sum1 / (double)count1);
    solution.setObjective(1, Math.cos(0.5*Math.PI*x[0])*Math.sin(0.5*Math.PI*x[1]) + 2.0*sum2 / (double)count2);
    solution.setObjective(2, Math.sin(0.5*Math.PI*x[0])                       + 2.0*sum3 / (double)count3) ;
  } // evaluate
} // CEC2009_UF10

