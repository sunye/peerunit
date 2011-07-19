/**
 * HUXCrossover.java
 * @author Juan J. Durillo
 * @version 1.0
 * Class representing a HUX crossover operator
 */
package jmetal.base.operator.crossover;

import jmetal.base.*;
import jmetal.base.Configuration.SolutionType_;
import jmetal.base.variable.*;
import jmetal.base.*;    
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

/**
 * This class allows to apply a HUX crossover operator using two parent
 * solutions.
 * NOTE: the operator is applied to the first variable of the solutions, and 
 * the type of the solutions must be binary 
 * (e.g., <code>SolutionType_.Binary</code> or 
 * <code>SolutionType_.BinaryReal</code>.
 */
public class HUXCrossover extends Operator{

  /**
   * Constructor
   * Create a new intance of the HUX crossover operator.
   */
  public HUXCrossover() {
  } // HUXCrossover


  /**
   * Perform the crossover operation
   * @param probability Crossover probability
   * @param parent1 The first parent
   * @param parent2 The second parent
   * @return An array containig the two offsprings
   * @throws JMException 
   */
  public Solution[] doCrossover(double   probability, 
                                Solution parent1, 
                                Solution parent2) throws JMException {
    Solution [] offSpring = new Solution[2];
    offSpring[0] = new Solution(parent1);
    offSpring[1] = new Solution(parent2);
    try {         
      if (PseudoRandom.randDouble() < probability)
      {
        for (int var = 0; var < parent1.getDecisionVariables().variables_.length; var++) {
          Binary p1 = (Binary)parent1.getDecisionVariables().variables_[var];
          Binary p2 = (Binary)parent2.getDecisionVariables().variables_[var];

          for (int bit = 0; bit < p1.getNumberOfBits(); bit++) {
            if (p1.bits_.get(bit) != p2.bits_.get(bit)) {
              if (PseudoRandom.randDouble() < 0.5) {
                ((Binary)offSpring[0].getDecisionVariables().variables_[var])
                .bits_.set(bit,p2.bits_.get(bit));
                ((Binary)offSpring[1].getDecisionVariables().variables_[var])
                .bits_.set(bit,p1.bits_.get(bit));
              }
            }
          }
        }  
        //7. Decode the results
        for (int i = 0; i < offSpring[0].getDecisionVariables().size(); i++)
        {
          ((Binary)offSpring[0].getDecisionVariables().variables_[i]).decode();
          ((Binary)offSpring[1].getDecisionVariables().variables_[i]).decode();
        }
      }          
    }catch (ClassCastException e1) {
      
      Configuration.logger_.severe("HUXCrossover.doCrossover: Cannot perfom " +
          "SinglePointCrossover ") ;
      Class cls = java.lang.String.class;
      String name = cls.getName(); 
      throw new JMException("Exception in " + name + ".doCrossover()") ;
    }        
    return offSpring;                                                                                      
  } // doCrossover

  
  /**
  * Executes the operation
  * @param object An object containing an array of two solutions 
  * @return An object containing the offSprings
  */
  public Object execute(Object object) throws JMException {
    Solution [] parents = (Solution [])object;
    
    if ( ((parents[0].getType() != SolutionType_.Binary) ||
          (parents[1].getType() != SolutionType_.Binary)) && 
         ((parents[0].getType() != SolutionType_.BinaryReal) ||
          (parents[1].getType() != SolutionType_.BinaryReal))) {
      
      Configuration.logger_.severe("HUXCrossover.execute: the solutions " +
          "are not of the right type. The type should be 'Binary' of " +
          "'BinaryReal', but " +
          parents[0].getType() + " and " + 
          parents[1].getType() + " are obtained");

      Class cls = java.lang.String.class;
      String name = cls.getName(); 
      throw new JMException("Exception in " + name + ".execute()") ;

    } // if 
    
    Double probability = (Double)getParameter("probability");
    if (parents.length < 2)
    {
      Configuration.logger_.severe("HUXCrossover.execute: operator needs two " +
          "parents");
      Class cls = java.lang.String.class;
      String name = cls.getName(); 
      throw new JMException("Exception in " + name + ".execute()") ;      
    }
    else if (probability == null)
    {
      Configuration.logger_.severe("HUXCrossover.execute: probability not " +
      "specified");
      Class cls = java.lang.String.class;
      String name = cls.getName(); 
      throw new JMException("Exception in " + name + ".execute()") ;  
    }         
    
    Solution [] offSpring = doCrossover(probability.doubleValue(),
                                                       parents[0],
                                                       parents[1]);
    
    for (int i = 0; i < offSpring.length; i++)
    {
      offSpring[i].setCrowdingDistance(0.0);
      offSpring[i].setRank(0);
    } 
    return offSpring;
    
  } // execute
} // HUXCrossover
