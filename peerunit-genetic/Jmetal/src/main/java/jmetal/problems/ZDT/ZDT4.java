/**
 * ZDT4.java
 *
 * @author Antonio J. Nebro
 * @author Juan J. Durillo
 * @version 1.0
 * Created on 16 de octubre de 2006, 17:30
 */

package jmetal.problems.ZDT;

import jmetal.base.*;
import jmetal.base.Configuration.SolutionType_;
import jmetal.base.Configuration.VariableType_;
import jmetal.util.JMException;

/**
 * Class representing problem ZDT4
 */
public class ZDT4 extends Problem{
     
 /**
  * Constructor.
  * Creates a default instance of problem ZDT4 (10 decision variables)
  * @param representation The solution type must "Real" or "BinaryReal".
  */
  public ZDT4(String representation) {
    this(10,representation); // 10 variables by default
  } // ZDT4
  
 /** 
  * Creates a instance of problem ZDT4.
  * @param numberOfVariables Number of variables.
  * @param solutionType The solution type must "Real" or "BinaryReal".
  */
  public ZDT4(Integer numberOfVariables, String solutionType) {
    numberOfVariables_  = numberOfVariables.intValue();
    numberOfObjectives_ = 2;
    numberOfConstraints_= 0;
    problemName_        = "ZDT4";

    upperLimit_ = new double[numberOfVariables_];
    lowerLimit_ = new double[numberOfVariables_];
        
    lowerLimit_[0] = 0.0;
    upperLimit_[0] = 1.0;
    for (int var = 1; var < numberOfVariables_; var++){
      lowerLimit_[var] = -5.0;
      upperLimit_[var] =  5.0;
    } //for
        
    solutionType_ = Enum.valueOf(SolutionType_.class, solutionType) ; 
    
    // All the variables are of the same type, so the solutionType name is the
    // same than the variableType name
    variableType_ = new VariableType_[numberOfVariables_];
    for (int var = 0; var < numberOfVariables_; var++){
      variableType_[var] = Enum.valueOf(VariableType_.class, solutionType) ;    
    } // for
  } //ZDT4

  /** 
  * Evaluates a solution 
  * @param solution The solution to evaluate
   * @throws JMException 
  */    
  public void evaluate(Solution solution) throws JMException {
    DecisionVariables decisionVariables  = solution.getDecisionVariables();
    
    double [] f = new double[numberOfObjectives_] ; 
    f[0]        = decisionVariables.variables_[0].getValue()     ;
    double g    = this.evalG(decisionVariables)                 ;
    double h    = this.evalH(f[0],g)              ;
    f[1]        = h * g                           ;   
    
    solution.setObjective(0,f[0]);
    solution.setObjective(1,f[1]);
  } //evaluate
    
  /**
  * Returns the value of the ZDT4 function G.
  * @param decisionVariables The decision variables of the solution to 
  * evaluate.
   * @throws JMException 
  */  
  public double evalG(DecisionVariables decisionVariables) throws JMException{
    double g = 0.0;
    for (int var = 1; var < numberOfVariables_; var++)
      g += Math.pow(decisionVariables.variables_[var].getValue(),2.0) + 
          - 10.0 * Math.cos(4.0*Math.PI*decisionVariables.variables_[var].getValue());
    
    double constante = 1.0 + 10.0*(numberOfVariables_ - 1);
    return g + constante;
  } // evalG
    
  /**
  * Returns the value of the ZDT4 function H.
  * @param f First argument of the function H.
  * @param g Second argument of the function H.
  */
  public double evalH(double f, double g){
    return 1.0 - Math.sqrt(f/g);
  } // evalH      
} // ZDT4
