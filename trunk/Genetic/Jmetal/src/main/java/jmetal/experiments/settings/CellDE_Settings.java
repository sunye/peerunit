/**
 * CellDE_Settings.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 *
 * CellDE_Settings class of algorithm CellDE
 */
package jmetal.experiments.settings;

import jmetal.metaheuristics.cellde.*;
import java.util.Properties;
import jmetal.base.Algorithm;
import jmetal.base.Operator;
import jmetal.base.Problem;
import jmetal.base.operator.crossover.CrossoverFactory;
import jmetal.base.operator.mutation.MutationFactory;
import jmetal.base.operator.selection.SelectionFactory;
import jmetal.experiments.Settings;
import jmetal.problems.ProblemFactory;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.JMException;

/**
 *
 * @author Antonio
 */
public class CellDE_Settings extends Settings{
  
  // Default settings
  double CR_          = 0.5;
  double F_           = 0.5    ;
  
  int populationSize_ = 100   ;
  int archiveSize_    = 100   ;
  int maxEvaluations_ = 25000 ;
  int archiveFeedback_= 20    ;
 
  boolean applyMutation_     = false ; // Polynomial mutation
  double  distributionIndex_ = 20    ;  
  double mutationProbability_ = 1.0 / problem_.getNumberOfVariables();

  String paretoFrontFile_ = "" ;
  
  /**
   * Constructor
   */
  public CellDE_Settings(Problem problem) {
    super(problem) ;
  } // CellDE_Settings
  
  /**
   * Configure the algorith with the especified parameter settings
   * @return an algorithm object
   * @throws jmetal.util.JMException
   */
  public Algorithm configure() throws JMException {
    Algorithm algorithm ;
    Operator  selection ;
    Operator  crossover ;
    Operator  mutation  ;
    
    QualityIndicator indicators ;
    
    // Creating the problem
    algorithm = new CellDE(problem_) ;
    
    // Algorithm parameters
    algorithm.setInputParameter("populationSize", populationSize_);
    algorithm.setInputParameter("archiveSize", archiveSize_);
    algorithm.setInputParameter("maxEvaluations",maxEvaluations_);
    algorithm.setInputParameter("feedBack", archiveFeedback_);
    
    // Crossover operator 
    crossover = CrossoverFactory.getCrossoverOperator("DifferentialEvolutionCrossover");                   
    crossover.setParameter("CR", CR_);                   
    crossover.setParameter("F", F_);
    
    // Add the operators to the algorithm
    selection = SelectionFactory.getSelectionOperator("BinaryTournament") ; 

    algorithm.addOperator("crossover",crossover);
    algorithm.addOperator("selection",selection);

    if (applyMutation_) {        
      mutation = MutationFactory.getMutationOperator("PolynomialMutation");                    
      mutation.setParameter("probability", mutationProbability_);
      mutation.setParameter("distributionIndex", distributionIndex_);  
 
      algorithm.addOperator("mutation",mutation);
   } // if
    
   // Creating the indicator object
   if (! paretoFrontFile_.equals("")) {
      indicators = new QualityIndicator(problem_, paretoFrontFile_);
      algorithm.setInputParameter("indicators", indicators) ;  
   } // if
    
    return algorithm ;
  }
  
  /**
   * Configure an algorithm with user-defined parameter settings
   * @param settings
   * @return An algorithm
   * @throws jmetal.util.JMException
   */
  public Algorithm configure(Properties settings) throws JMException {
    if (settings != null) {
      CR_          = Double.parseDouble(settings.getProperty("CR", ""+CR_)) ;
      F_           = Double.parseDouble(settings.getProperty("F", ""+F_)) ;
      populationSize_  = Integer.parseInt(settings.getProperty("POPULATION_SIZE", ""+populationSize_)) ;
      archiveSize_     = Integer.parseInt(settings.getProperty("ARCHIVE_SIZE", ""+archiveSize_)) ;
      maxEvaluations_  = Integer.parseInt(settings.getProperty("MAX_EVALUATIONS", ""+maxEvaluations_)) ;
      archiveFeedback_ = Integer.parseInt(settings.getProperty("ARCHIVE_FEEDBACK", ""+archiveFeedback_)) ;  
      mutationProbability_ = Double.parseDouble(settings.getProperty("MUTATION_PROBABILITY",
              "" + mutationProbability_));
      applyMutation_ = Boolean.parseBoolean(settings.getProperty("APPLY_MUTATION", ""+applyMutation_)) ;
      distributionIndex_ = Double.parseDouble(settings.getProperty("DISTRIBUTION_INDEX", ""+distributionIndex_)) ;
      paretoFrontFile_ = settings.getProperty("PARETO_FRONT_FILE", "") ;
    }
    
    return configure() ;
  }
} // CellDE_Settings
