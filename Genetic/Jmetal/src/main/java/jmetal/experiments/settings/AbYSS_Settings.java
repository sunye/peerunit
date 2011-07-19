/**
 * AbYSS_Settings.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 *
 * MOCell_Settings class of algorithm AbYSS
 */
package jmetal.experiments.settings;

import jmetal.metaheuristics.abyss.*;
import java.util.Properties;
import jmetal.base.Algorithm;
import jmetal.base.Operator;
import jmetal.base.Problem;
import jmetal.base.operator.crossover.CrossoverFactory;
import jmetal.base.operator.localSearch.MutationLocalSearch;
import jmetal.base.operator.mutation.MutationFactory;
import jmetal.base.operator.selection.SelectionFactory;
import jmetal.experiments.Settings;
import jmetal.problems.ProblemFactory;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.JMException;

/**
 * Constructor
 */
public class AbYSS_Settings extends Settings {
  // Default settings
  int populationSize_ = 100;
  int maxEvaluations_ = 25000;
  int archiveSize_ = 100;
  int refSet1Size_ = 20;
  int refSet2Size_ = 20;
  double mutationProbability_ = 1.0 / problem_.getNumberOfVariables();
  double crossoverProbability_ = 1.0;
  double distributionIndexForMutation_ = 20;
  double distributionIndexForCrossover_ = 20;
  int improvementRounds_ = 1;
  String paretoFrontFile_ = "";

  /**
   * Constructor
   */
  public AbYSS_Settings(Problem problem) {
    super(problem);
  } // MOCell_Settings

  /**
   * Configure the MOCell algorithm with default parameter settings
   * @return an algorithm object
   * @throws jmetal.util.JMException
   */
  public Algorithm configure() throws JMException {
    Algorithm algorithm;
    Operator crossover;
    Operator mutation;
    Operator improvement; // Operator for improvement

    QualityIndicator indicators;

    // Creating the problem
    algorithm = new AbYSS(problem_);

    // Algorithm parameters
    algorithm.setInputParameter("populationSize", 20);
    algorithm.setInputParameter("refSet1Size", 10);
    algorithm.setInputParameter("refSet2Size", 10);
    algorithm.setInputParameter("archiveSize", 100);
    algorithm.setInputParameter("maxEvaluations", 25000);

    // Mutation and Crossover for Real codification 
    crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover");
    crossover.setParameter("probability", crossoverProbability_);
    crossover.setParameter("distributionIndex", distributionIndexForCrossover_);

    mutation = MutationFactory.getMutationOperator("PolynomialMutation");
    mutation.setParameter("probability", mutationProbability_);
    mutation.setParameter("distributionIndex", distributionIndexForMutation_);

    // STEP 4. Specify and configure the crossover operator, used in the
    //         solution combination method of the scatter search
    crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover");
    crossover.setParameter("probability", crossoverProbability_);
    crossover.setParameter("distributionIndex", distributionIndexForCrossover_);

    // STEP 5. Specify and configure the improvement method. We use by default
    //         a polynomial mutation in this method.
    mutation = MutationFactory.getMutationOperator("PolynomialMutation");
    mutation.setParameter("probability", mutationProbability_);
    mutation.setParameter("distributionIndex", distributionIndexForMutation_);


    improvement = new MutationLocalSearch(problem_, mutation);
    improvement.setParameter("improvementRounds", improvementRounds_);

    // STEP 6. Add the operators to the algorithm
    algorithm.addOperator("crossover", crossover);
    algorithm.addOperator("improvement", improvement);

    // Creating the indicator object
    if (!paretoFrontFile_.equals("")) {
      indicators = new QualityIndicator(problem_, paretoFrontFile_);
      algorithm.setInputParameter("indicators", indicators);
    } // if
    return algorithm;
  } // Constructor

  /**
   * Configure an algorithm with user-defined parameter settings
   * @param settings
   * @return An algorithm
   * @throws jmetal.util.JMException
   */
  public Algorithm configure(Properties settings) throws JMException {
    if (settings != null) {
      populationSize_ = Integer.parseInt(settings.getProperty("POPULATION_SIZE", "" + populationSize_));
      maxEvaluations_ = Integer.parseInt(settings.getProperty("MAX_EVALUATIONS", "" + maxEvaluations_));
      archiveSize_ = Integer.parseInt(settings.getProperty("ARCHIVE_SIZE", "" + archiveSize_));
      refSet1Size_ = Integer.parseInt(settings.getProperty("REF_SET1_SIZE", "" + refSet1Size_));
      refSet2Size_ = Integer.parseInt(settings.getProperty("REF_SET2_SIZE", "" + refSet2Size_));
      improvementRounds_ = Integer.parseInt(settings.getProperty("IMPROVEMENT_ROUNDS", "" + improvementRounds_));

      crossoverProbability_ = Double.parseDouble(settings.getProperty("CROSSOVER_PROBABILITY",
              "" + crossoverProbability_));
      mutationProbability_ = Double.parseDouble(settings.getProperty("MUTATION_PROBABILITY",
              "" + mutationProbability_));
      distributionIndexForMutation_ =
              Double.parseDouble(settings.getProperty("DISTRIBUTION_INDEX_FOR_MUTATION",
              "" + distributionIndexForMutation_));
      distributionIndexForCrossover_ =
              Double.parseDouble(settings.getProperty("DISTRIBUTION_INDEX_FOR_CROSSOVER",
              "" + distributionIndexForCrossover_));
      paretoFrontFile_ = settings.getProperty("PARETO_FRONT_FILE", "");
    }

    return configure();
  }
} // AbYSS_Settings
