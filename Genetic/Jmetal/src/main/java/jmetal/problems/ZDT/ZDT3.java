/**
 * ZDT3.java
 *
 * @author Antonio J. Nebro
 * @author Juan J. Durillo
 * @version 1.0
 */
package jmetal.problems.ZDT;

import jmetal.base.*;
import jmetal.base.Configuration.SolutionType_;
import jmetal.base.Configuration.VariableType_;
import jmetal.util.JMException;

/** 
 * Class representing problem ZDT3
 */
public class ZDT3 extends Problem {
    

 /**
  * Constructor.
  * Creates default instance of problem ZDT3 (30 decision variables.
  * @param representation The solution type must "Real" or "BinaryReal".
  */    
  public ZDT3(String representation) {
    this(30,representation); // 30 variables by default
  } // ZDT3
  
  
 /** 
  * Constructor.
  * Creates a instance of ZDT3 problem.
  * @param numberOfVariables Number of variables.
  * @param solutionType The solution type must "Real" or "BinaryReal".
  */    
  public ZDT3(Integer numberOfVariables, String solutionType) {
    numberOfVariables_  = numberOfVariables.intValue();
    numberOfObjectives_ =  2;
    numberOfConstraints_=  0;
    problemName_        = "ZDT3";
        
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
  } //ZDT3
      
  
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
    double h    = this.evalH(f[0],g)                 ;
    f[1]        = h * g                           ;
    
    solution.setObjective(0,f[0]);
    solution.setObjective(1,f[1]);
  } //evaluate
    
  /**
   * Returns the value of the ZDT2 function G.
   * @param decisionVariables The decision variables of the solution to 
   * evaluate.
   * @throws JMException 
   */    
  private double evalG(DecisionVariables decisionVariables) throws JMException {
    double g = 0.0;        
    for (int i = 1; i < decisionVariables.size();i++)
      g += decisionVariables.variables_[i].getValue();
    double constante = (9.0 / (numberOfVariables_-1));
    g = constante * g;
    g = g + 1.0;
    return g;
  } //evalG
   
  /**
  * Returns the value of the ZDT3 function H.
  * @param f First argument of the function H.
  * @param g Second argument of the function H.
  */
  public double evalH(double f, double g) {
    double h = 0.0;
    h = 1.0 - java.lang.Math.sqrt(f/g) 
        - (f/g)*java.lang.Math.sin(10.0*java.lang.Math.PI*f);
    return h;        
  } //evalH
} // ZDT3
