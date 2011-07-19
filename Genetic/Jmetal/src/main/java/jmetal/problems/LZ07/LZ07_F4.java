/**
 * LZ07_F4.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 * Created on 17 de junio de 2006, 17:30
 */

package jmetal.problems.LZ07;

import java.util.Vector;

import jmetal.base.*;
import jmetal.base.Configuration.*;
import jmetal.util.JMException;

/** 
 * Class representing problem DTLZ1 
 */
public class LZ07_F4 extends Problem {   
	LZ07 lz07_ ; 
 /** 
  * Creates a default DTLZ1 problem (7 variables and 3 objectives)
  * @param solutionType The solution type must "Real" or "BinaryReal". 
  */
  public LZ07_F4(String solutionType){
    this(21, 1, 24, solutionType);
  } // LZ07_F4
  
  /** 
   * Creates a DTLZ1 problem instance
   * @param numberOfVariables Number of variables
   * @param numberOfObjectives Number of objective functions
   * @param solutionType The solution type must "Real" or "BinaryReal". 
   */
   public LZ07_F4(Integer ptype, 
   		            Integer dtype,
   		            Integer ltype,
   		            String solutionType) {
     numberOfVariables_  = 30;
     numberOfObjectives_ = 2;
     numberOfConstraints_= 0;
     problemName_        = "LZ07_F4";
         
   	 lz07_  = new LZ07(numberOfVariables_, 
   			               numberOfObjectives_, 
   			               ptype, 
   			               dtype, 
   			               ltype) ;

     lowerLimit_ = new double[numberOfVariables_];
     upperLimit_ = new double[numberOfVariables_];      
     lowerLimit_[0] = 0.0 ;
     upperLimit_[0] = 1.0 ;
     for (int var = 1; var < numberOfVariables_; var++){
       lowerLimit_[var] = -1.0;
       upperLimit_[var] = 1.0;
     } //for
         
     solutionType_ = Enum.valueOf(SolutionType_.class, solutionType) ; 
     
     // All the variables are of the same type, so the solutionType name is the
     // same than the variableType name
     variableType_ = new VariableType_[numberOfVariables_];
     for (int var = 0; var < numberOfVariables_; var++){
       variableType_[var] = Enum.valueOf(VariableType_.class, solutionType) ;    
     } // for              
   } // LZ07_F4
   
   /** 
    * Evaluates a solution 
    * @param solution The solution to evaluate
     * @throws JMException 
    */    
    public void evaluate(Solution solution) throws JMException {
      DecisionVariables gen  = solution.getDecisionVariables();
      
      Vector<Double> x = new Vector(numberOfVariables_) ;
      Vector<Double> y = new Vector(numberOfObjectives_);
      int k = numberOfVariables_ - numberOfObjectives_ + 1;
          
      for (int i = 0; i < numberOfVariables_; i++) {
      	x.addElement(gen.variables_[i].getValue());
      	y.addElement(0.0) ;
      } // for
        
      lz07_.objective(x, y) ;
      
      for (int i = 0; i < numberOfObjectives_; i++)
        solution.setObjective(i, y.get(i)); 
    } // evaluate
} // LZ07_F4


