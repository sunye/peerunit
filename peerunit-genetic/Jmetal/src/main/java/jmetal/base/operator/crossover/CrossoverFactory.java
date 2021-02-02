/**
 * CrossoverFactory.java
 *
 * @author Juanjo Durillo
 * @version 1.0
 */

package jmetal.base.operator.crossover;

import jmetal.base.Configuration;
import jmetal.base.Operator;
import jmetal.util.JMException;

/**
 * Class implementing a crossover factory.
 */
public class CrossoverFactory {
    
  /**
   * Gets a crossover operator through its name.
   * @param name Name of the operator
   * @return The operator
   */
  public static Operator getCrossoverOperator(String name) throws JMException {
    if (name.equalsIgnoreCase("SBXCrossover"))
      return new SBXCrossover(20);
    else if (name.equalsIgnoreCase("SinglePointCrossover"))
        return new SinglePointCrossover();
    else if (name.equalsIgnoreCase("PMXCrossover"))
      return new PMXCrossover();
    else if (name.equalsIgnoreCase("TwoPointsCrossover"))
      return new TwoPointsCrossover();
    else if (name.equalsIgnoreCase("HUXCrossover"))
      return new HUXCrossover();
    else if (name.equalsIgnoreCase("DifferentialEvolutionCrossover"))
      return new DifferentialEvolutionCrossover();
    else {
      Configuration.logger_.severe("CrossoverFactory.getCrossoverOperator. " +
          "Operator '" + name + "' not found ");
      throw new JMException("Exception in " + name + ".getCrossoverOperator()") ;
    } // else        
  } // getCrossoverOperator
    
} // CrossoverFactory
