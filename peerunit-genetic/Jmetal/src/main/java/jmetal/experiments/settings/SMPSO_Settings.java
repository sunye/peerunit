/**
 * SMPSO_Settings.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 *
 * SMPSO_Settings class of algorithm SMPSO
 */
package jmetal.experiments.settings;

import jmetal.metaheuristics.smpso.*;
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
public class SMPSO_Settings extends Settings{
  
  // Default settings
  int    swarmSize_         = 100 ;
  int    maxIterations_     = 250 ;
  int    archiveSize_       = 100 ;
  double perturbationIndex_ = 0.5 ;
  double mutationDistributionIndex_ = 20.0 ;
  
  String paretoFrontFile_ = "" ;
  
  /**
   * Constructor
   */
  public SMPSO_Settings(Problem problem) {
    super(problem) ;
  } // SMPSO_Settings
  
  /**
   * Configure NSGAII with user-defined parameter settings
   * @return A NSGAII algorithm object
   * @throws jmetal.util.JMException
   */
  public Algorithm configure() throws JMException {
    Algorithm algorithm ;
    
    QualityIndicator indicators ;
    
    // Creating the problem
    algorithm = new SMPSO(problem_) ;
    
    // Algorithm parameters
    algorithm.setInputParameter("swarmSize", swarmSize_);
    algorithm.setInputParameter("maxIterations", maxIterations_);
    algorithm.setInputParameter("archiveSize", archiveSize_);
    algorithm.setInputParameter("perturbationIndex", perturbationIndex_);
    algorithm.setInputParameter("mutationDistributionIndex", mutationDistributionIndex_);
    
   // Creating the indicator object
   if (! paretoFrontFile_.equals("")) {
      indicators = new QualityIndicator(problem_, paretoFrontFile_);
      algorithm.setInputParameter("indicators", indicators) ;  
   } // if
    return algorithm ;
  }
  
  /**
   * Configure SMPSO with user-defined parameter settings
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
      mutationDistributionIndex_ = Double.parseDouble(settings.getProperty("MUTATION_DISTRIBUTION_INDEX",
                                                    ""+mutationDistributionIndex_)) ;
      
      paretoFrontFile_ = settings.getProperty("PARETO_FRONT_FILE", "") ;
    }
    
    return configure() ;
  }
} // SMPSO_Settings
