/**
 * ZDT5.java
 *
 * @author Antonio J. Nebro
 * @author Juanjo Durillo
 * @version 1.0
 */

package jmetal.problems.ZDT;

import jmetal.base.*;
import jmetal.base.Configuration.SolutionType_;
import jmetal.base.Configuration.VariableType_;
import jmetal.base.variable.*;

/**
 * Class representing problem ZDT5
 */
public class ZDT5 extends Problem{
     
 /**
  * Creates a default instance of problem ZDT5 (11 decision variables).
  * This problem allows only "Binary" representations.
  */
  public ZDT5() {
    this(11); // 11 variables by default
  } // ZDT5
  
 /** 
  * Creates a instance of problem ZDT5
  * @param numberOfVariables Number of variables.
  * This problem allows only "Binary" representations.
  */
  public ZDT5(Integer numberOfVariables) {
    numberOfVariables_  = numberOfVariables.intValue();
    numberOfObjectives_ = 2;
    numberOfConstraints_= 0;
    problemName_        = "ZDT5";    
    
    length_ = new int[numberOfVariables_];
    length_[0] = 30;
    for (int var = 1; var < numberOfVariables_; var++) {
      length_[var] = 5;
    }
        
    solutionType_ = SolutionType_.Binary ; 
    
    // All the variables of this problem are Binary
    variableType_ = new VariableType_[numberOfVariables_];
    for (int var = 0; var < numberOfVariables_; var++){
      variableType_[var] = VariableType_.Binary ;    
    } // for
  } //ZDT5

  /** 
  * Evaluates a solution 
  * @param solution The solution to evaluate
  */    
  public void evaluate(Solution solution) {        
    double [] f = new double[numberOfObjectives_] ; 
    f[0]        = 1 + u((Binary)solution.getDecisionVariables().variables_[0]);
    double g    = evalG(solution.getDecisionVariables())                 ;
    double h    = evalH(f[0],g)              ;
    f[1]        = h * g                           ;   
    
    solution.setObjective(0,f[0]);
    solution.setObjective(1,f[1]);
  } //evaluate
    
  /**
  * Returns the value of the ZDT5 function G.
  * @param decisionVariables The decision variables of the solution to 
  * evaluate.
  */
  public double evalG(DecisionVariables decisionVariables) {
    double res = 0.0;
    for (int var = 1; var < numberOfVariables_; var++) {
      res += evalV(u((Binary)decisionVariables.variables_[var]));
    }
    
    return res;
  } // evalG
  
  /**
   * Returns the value of the ZDT5 function V.
   * @param value The parameter of V function.
   */
  public double evalV(double value) {
    if (value < 5.0) {
      return 2.0 + value;
    } else {
      return 1.0;
    }    
  } // evalV
  
  /**
  * Returns the value of the ZDT5 function H.
  * @param f First argument of the function H.
  * @param g Second argument of the function H.
  */
  public double evalH(double f, double g) {
    return 1 / f;
  } // evalH
  
  /**
   * Returns the u value defined in ZDT5 for a variable.
   * @param The variable.
   */
  private double u(Binary variable) {
    return variable.bits_.cardinality();
  } // u
  
  /**
   * Returns the precision of the variable var
   * @param var The position of the variable
   */
  public int getPrecision(int var) {
    return precision_[var];
  } // getPrecision
} // ZDT5
