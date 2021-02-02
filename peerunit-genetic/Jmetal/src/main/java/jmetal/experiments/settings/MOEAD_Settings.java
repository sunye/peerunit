/**
 * GDE3_Settings.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 *
 * MOEAD_Settings class of algorithm MOEAD
 */
package jmetal.experiments.settings;

import jmetal.metaheuristics.moead.*;
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
public class MOEAD_Settings extends Settings {
  // Default settings
  double CR_ = 0.1;
  double F_ = 0.5;
  int populationSize_ = 300;
  int maxEvaluations_ = 300000;
 
  double mutationProbability_  = 1.0/problem_.getNumberOfVariables() ;
  double  distributionIndexForMutation_ = 20    ;
  
  String paretoFrontFile_ = "";

  /**
   * Constructor
   */
  public MOEAD_Settings(Problem problem) {
    super(problem);
  } // MOEAD_Settings

  /**
   * Configure the algorith with the especified parameter settings
   * @return an algorithm object
   * @throws jmetal.util.JMException
   */
  public Algorithm configure() throws JMException {
    Algorithm algorithm;
    Operator crossover;
    Operator mutation;

    QualityIndicator indicators;

    // Creating the problem
    algorithm = new MOEAD(problem_);

    // Algorithm parameters
    algorithm.setInputParameter("populationSize", populationSize_);
    algorithm.setInputParameter("maxEvaluations", maxEvaluations_);

    // Crossover operator 
    crossover = CrossoverFactory.getCrossoverOperator("DifferentialEvolutionCrossover");
    crossover.setParameter("CR", CR_);
    crossover.setParameter("F", F_);   
    
    // Mutation operator
    mutation = MutationFactory.getMutationOperator("PolynomialMutation");                    
    mutation.setParameter("probability", mutationProbability_);
    mutation.setParameter("distributionIndex", distributionIndexForMutation_); 
    
    algorithm.addOperator("crossover", crossover);
    algorithm.addOperator("mutation", mutation);
    
    // Creating the indicator object
    if (!paretoFrontFile_.equals("")) {
      indicators = new QualityIndicator(problem_, paretoFrontFile_);
      algorithm.setInputParameter("indicators", indicators);
    } // if

    return algorithm;
  }

  /**
   * Configure an algorithm with user-defined parameter settings
   * @param settings
   * @return An algorithm
   * @throws jmetal.util.JMException
   */
  public Algorithm configure(Properties settings) throws JMException {
    if (settings != null) {
      CR_ = Double.parseDouble(settings.getProperty("CR", "" + CR_));
      F_ = Double.parseDouble(settings.getProperty("F", "" + F_));
      populationSize_ = Integer.parseInt(settings.getProperty("POPULATION_SIZE", "" + populationSize_));
      maxEvaluations_ = Integer.parseInt(settings.getProperty("MAX_EVAlUATIONS", "" + maxEvaluations_));
      mutationProbability_ = Double.parseDouble(settings.getProperty("MUTATION_PROBABILITY", 
                                                    ""+mutationProbability_)) ;
      distributionIndexForMutation_ = 
            Double.parseDouble(settings.getProperty("DISTRIBUTION_INDEX_FOR_MUTATION", 
                                                    ""+distributionIndexForMutation_)) ;
      
      paretoFrontFile_ = settings.getProperty("PARETO_FRONT_FILE", "");
    }

    return configure();
  }
} // GDE3_Settings
