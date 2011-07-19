/**
 * DifferentialEvolutionCrossover.java
 * Class representing the crossover operator used in differential evolution
 * @author Antonio J. Nebro
 * @version 1.0
 */
package jmetal.base.operator.crossover;

import jmetal.base.Configuration;
import jmetal.base.Operator;
import jmetal.base.Solution;
import jmetal.base.Configuration.SolutionType_;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

public class DifferentialEvolutionCrossover extends Operator {
  /**
   * DEFAULT_CR defines a default CR (crossover operation control) value
   */
  public static final double DEFAULT_CR = 0.1; 
  
  /**
   * DEFAULT_F defines the default F (Scaling factor for mutation) value
   */
  private static final double DEFAULT_F = 0.5;
  
  private double CR_ ;
  private double F_  ;
  
  /**
   * Constructor
   */
  DifferentialEvolutionCrossover() {
    CR_ = DEFAULT_CR ;
    F_  = DEFAULT_F  ;
  } // Constructor
  
  
  /**
   * Executes the operation
   * @param object An object containing an array of three parents
   * @return An object containing the offSprings
   */
   public Object execute(Object object) throws JMException {
     Object[] parameters = (Object[])object ;
     Solution current   = (Solution) parameters[0];
     Solution [] parent = (Solution [])parameters[1];
     
     Solution child ;
     
     if ((parent[0].getType() != SolutionType_.Real) ||
         (parent[1].getType() != SolutionType_.Real)||
         (parent[2].getType() != SolutionType_.Real) ) {

       Configuration.logger_.severe("DifferentialEvolutionCrossover.execute: " +
       		" the solutions " +
           "are not of the right type. The type should be 'Real', but " +
           parent[0].getType() + " and " + 
           parent[1].getType() + " and " + 
           parent[2].getType() + " are obtained");

       Class cls = java.lang.String.class;
       String name = cls.getName(); 
       throw new JMException("Exception in " + name + ".execute()") ;
     } // if 
     
     Double CR = (Double)getParameter("CR");
     if (CR != null) {
       CR_ = CR ;
     } // if
     Double F = (Double)getParameter("F");
     if (F != null) {
       F_ = F ;
     } // if
     
     int jrand ;

     int numberOfVariables = parent[0].getDecisionVariables().variables_.length ;
     jrand = (int)(PseudoRandom.randInt(0, numberOfVariables - 1)) ;
     
     child = new Solution(current) ;
     for (int j=0; j < numberOfVariables; j++) {
        if (PseudoRandom.randDouble(0, 1) < CR_ || j == jrand) {
          double value ;
          value = parent[2].getDecisionVariables().variables_[j].getValue()  +
                  F_ * (parent[0].getDecisionVariables().variables_[j].getValue() -
                       parent[1].getDecisionVariables().variables_[j].getValue()) ;
          
          if (value < child.getDecisionVariables().variables_[j].getLowerBound())
            value =  child.getDecisionVariables().variables_[j].getLowerBound() ;
          if (value > child.getDecisionVariables().variables_[j].getUpperBound())
            value = child.getDecisionVariables().variables_[j].getUpperBound() ;
            
          child.getDecisionVariables().variables_[j].setValue(value) ;
        }
        else {
          double value ;
          value = current.getDecisionVariables().variables_[j].getValue();
          child.getDecisionVariables().variables_[j].setValue(value) ;
        } // else
     }
     
     return child ;
   }
}
