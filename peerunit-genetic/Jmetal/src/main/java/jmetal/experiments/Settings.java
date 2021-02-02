/**
 * Settings.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 *
 * Abstract Settings class
 */

package jmetal.experiments;

import java.util.Properties;
import jmetal.base.Algorithm;
import jmetal.base.Problem;
import jmetal.util.JMException;

/**
 * @author Antonio J. Nebro
 */
public abstract class Settings {
  protected Problem problem_ ;

    /**
   * Constructor
   */
  public Settings(Problem problem) {
    problem_ = problem ;
  } // Constructor
  
  /**
   * Default configure method
   * @return A problem with the default configuration
   * @throws jmetal.util.JMException
   */
  abstract public Algorithm configure() throws JMException ;
  
  /**
   * Configure method. Change the default configuration
   * @param settings
   * @return A problem with the settings indicated as argument
   * @throws jmetal.util.JMException
   */
  abstract public Algorithm configure(Properties settings) throws JMException ;

  /**
   * Change the problem to solve
   * @param problem
   */
  void setProblem(Problem problem) {
    problem_ = problem ;
  } // setProblem

} // Settings
