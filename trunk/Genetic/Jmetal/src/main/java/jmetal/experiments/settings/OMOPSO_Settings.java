/**
 * OMOPSO_Settings.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 *
 * OMOPSO_Settings class of algorithm OMOPSO
 */
package jmetal.experiments.settings;

import jmetal.metaheuristics.omopso.*;
import java.util.Properties;
import jmetal.base.Algorithm;
import jmetal.base.Problem;
import jmetal.experiments.Settings;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.JMException;

/**
 *
 * @author Antonio
 */
public class OMOPSO_Settings extends Settings{
  
  // Default settings
  int    swarmSize_         = 100 ;
  int    maxIterations_     = 250 ;
  int    archiveSize_       = 100 ;
  double perturbationIndex_ = 0.5 ;
  
  String paretoFrontFile_ = "" ;
  
  /**
   * Constructor
   */
  public OMOPSO_Settings(Problem problem) {
    super(problem) ;
  } // OMOPSO_Settings
  
  /**
   * Configure NSGAII with user-defined parameter settings
   * @return A NSGAII algorithm object
   * @throws jmetal.util.JMException
   */
  public Algorithm configure() throws JMException {
    Algorithm algorithm ;
    
    QualityIndicator indicators ;
    
    // Creating the problem
    algorithm = new OMOPSO(problem_) ;
    
    // Algorithm parameters
    algorithm.setInputParameter("swarmSize", swarmSize_);
    algorithm.setInputParameter("maxIterations", maxIterations_);
    algorithm.setInputParameter("archiveSize", archiveSize_);
    algorithm.setInputParameter("perturbationIndex", perturbationIndex_);
    
   // Creating the indicator object
   if (! paretoFrontFile_.equals("")) {
      indicators = new QualityIndicator(problem_, paretoFrontFile_);
      algorithm.setInputParameter("indicators", indicators) ;  
   } // if
    return algorithm ;
  }
  
  /**
   * Configure OMOPSO with user-defined parameter settings
   * @param settings
   * @return A NSGAII algorithm object
   * @throws jmetal.util.JMException
   */
  public Algorithm configure(Properties settings) throws JMException {
    if (settings != null) {
      swarmSize_  = Integer.parseInt(settings.getProperty("SWARM_SIZE", ""+swarmSize_)) ;
      maxIterations_  = Integer.parseInt(settings.getProperty("MAX_ITERATIONS", ""+maxIterations_)) ;
      archiveSize_  = Integer.parseInt(settings.getProperty("ARCHIVE_SIZE", ""+archiveSize_)) ;
      perturbationIndex_ = Double.parseDouble(settings.getProperty("PERTURBATION_INDEX", 
                                                    ""+perturbationIndex_)) ;
      
      paretoFrontFile_ = settings.getProperty("PARETO_FRONT_FILE", "") ;
    }
    
    return configure() ;
  }
} // OMOPSO_Settings
