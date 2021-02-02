/**
 * MOCell_Settings.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 *
 * MOCell_Settings class of algorithm MOCell
 */
package jmetal.experiments.settings;

import jmetal.metaheuristics.mocell.*;
import jmetal.metaheuristics.nsgaII.*;
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
public class MOCell_Settings extends Settings{
  
  // Default settings
  int populationSize_ = 100   ;
  int maxEvaluations_ = 25000 ;
  int archiveSize_    = 100   ;
  int feedback_       = 20    ;
 
  double mutationProbability_  = 1.0/problem_.getNumberOfVariables() ;
  double crossoverProbability_ = 0.9 ;
  
  double  distributionIndexForMutation_ = 20    ;
  double  distributionIndexForCrossover_ = 20    ;
  
  String paretoFrontFile_ = "" ;
  
  /**
   * Constructor
   */
  public MOCell_Settings(Problem problem) {
    super(problem) ;
  } // MOCell_Settings
  
  /**
   * Configure the MOCell algorithm with default parameter settings
   * @return an algorithm object
   * @throws jmetal.util.JMException
   */
  public Algorithm configure() throws JMException {
    Algorithm algorithm ;
    Operator  selection ;
    Operator  crossover ;
    Operator  mutation  ;
    
    QualityIndicator indicators ;
    
    // Creating the problem: there are six MOCell variants
    //algorithm = new sMOCell1(problem_) ;
    //algorithm = new sMOCell2(problem_) ;
    //algorithm = new aMOCell1(problem_) ;
    //algorithm = new aMOCell2(problem_) ;
    //algorithm = new aMOCell3(problem_) ;
    algorithm = new aMOCell4(problem_) ;

    // Algorithm parameters
    algorithm.setInputParameter("populationSize", populationSize_);
    algorithm.setInputParameter("maxEvaluations", maxEvaluations_);
    algorithm.setInputParameter("archiveSize",archiveSize_ );
    algorithm.setInputParameter("feedBack",feedback_);
    
    // Mutation and Crossover for Real codification 
    crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover");                   
    crossover.setParameter("probability", crossoverProbability_);                   
    crossover.setParameter("distributionIndex",distributionIndexForCrossover_);

    mutation = MutationFactory.getMutationOperator("PolynomialMutation");                    
    mutation.setParameter("probability", mutationProbability_);
    mutation.setParameter("distributionIndex",distributionIndexForMutation_);    
    
    // Selection Operator 
    selection = SelectionFactory.getSelectionOperator("BinaryTournament") ;   
    
    // Add the operators to the algorithm
    algorithm.addOperator("crossover",crossover);
    algorithm.addOperator("mutation",mutation);
    algorithm.addOperator("selection",selection);
    
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
      populationSize_  = Integer.parseInt(settings.getProperty("POPULATION_SIZE", ""+populationSize_)) ;
      maxEvaluations_  = Integer.parseInt(settings.getProperty("MAX_EVALUATIONS", ""+maxEvaluations_)) ;
      archiveSize_     = Integer.parseInt(settings.getProperty("ARCHIVE_SIZE", ""+archiveSize_)) ;
      feedback_        = Integer.parseInt(settings.getProperty("FEEDBACK", ""+feedback_)) ;

      crossoverProbability_ = Double.parseDouble(settings.getProperty("CROSSOVER_PROBABILITY", 
                                                    ""+crossoverProbability_)) ;     
      mutationProbability_ = Double.parseDouble(settings.getProperty("MUTATION_PROBABILITY", 
                                                    ""+mutationProbability_)) ;
      distributionIndexForMutation_ = 
            Double.parseDouble(settings.getProperty("DISTRIBUTION_INDEX_FOR_MUTATION", 
                                                    ""+distributionIndexForMutation_)) ;
      distributionIndexForCrossover_ = 
            Double.parseDouble(settings.getProperty("DISTRIBUTION_INDEX_FOR_CROSSOVER", 
                                                    ""+distributionIndexForCrossover_)) ;
      paretoFrontFile_ = settings.getProperty("PARETO_FRONT_FILE", "") ;
    }
    
    return configure() ;
  }
} // MOCell_Settings
