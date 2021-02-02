/**
 * MOEAD_main.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 * 
 * This class executes the algorithm described in:
 *   H. Li and Q. Zhang, 
 *   "Multiobjective Optimization Problems with Complicated Pareto Sets,  MOEA/D 
 *   and NSGA-II" 
 *   IEEE Trans on Evolutionary Computation, April/2008. Accepted 
 */
package jmetal.metaheuristics.moead;

import jmetal.metaheuristics.cellde.*;
import jmetal.base.*;
import jmetal.base.operator.crossover.*   ;
import jmetal.base.operator.mutation.*    ; 
import jmetal.base.operator.selection.*   ;
import jmetal.problems.*                  ;
import jmetal.problems.DTLZ.*;
import jmetal.problems.ZDT.*;
import jmetal.problems.WFG.*;
import jmetal.problems.LZ07.* ;

import jmetal.util.JMException;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import jmetal.qualityIndicator.QualityIndicator;

public class MOEAD_main {
  public static Logger      logger_ ;      // Logger object
  public static FileHandler fileHandler_ ; // FileHandler object

  /**
   * @param args Command line arguments. The first (optional) argument specifies 
   *      the problem to solve.
   * @throws JMException 
   * @throws IOException 
   * @throws SecurityException 
   * Usage: three options
   *      - jmetal.metaheuristics.moead.MOEAD_main
   *      - jmetal.metaheuristics.moead.MOEAD_main problemName
   *      - jmetal.metaheuristics.moead.MOEAD_main problemName ParetoFrontFile
 
   */
  public static void main(String [] args) throws JMException, SecurityException, IOException {
    Problem   problem   ;         // The problem to solve
    Algorithm algorithm ;         // The algorithm to use
    Operator  selection ;
    Operator  crossover ;         // Crossover operator
    Operator  mutation  ;         // Mutation operator
     
    QualityIndicator indicators ; // Object to get quality indicators

    // Logger object and file to store log messages
    logger_      = Configuration.logger_ ;
    fileHandler_ = new FileHandler("MOEAD.log"); 
    logger_.addHandler(fileHandler_) ;
    
    indicators = null ;
    if (args.length == 1) {
      Object [] params = {"Real"};
      problem = (new ProblemFactory()).getProblem(args[0],params);
    } // if
    else if (args.length == 2) {
      Object [] params = {"Real"};
      problem = (new ProblemFactory()).getProblem(args[0],params);
      indicators = new QualityIndicator(problem, args[1]) ;
    } // if
    else { // Default problem
      problem = new Kursawe(3, "Real"); 
      //problem = new Water("Real");
      //problem = new ZDT4("Real");
      //problem = new ZDT3("Real");
      //problem = new WFG1("Real");
      //problem = new DTLZ1(7,4,"Real");
      //problem = new OKA2("Real") ;
    } // else
    
    algorithm = new MOEAD(problem);
    
    // Algorithm parameters
    algorithm.setInputParameter("populationSize",300);
    algorithm.setInputParameter("maxEvaluations",150000);
    
    // Crossover operator 
    crossover = CrossoverFactory.getCrossoverOperator("DifferentialEvolutionCrossover");                   
    crossover.setParameter("CR", 1.0);                   
    crossover.setParameter("F", 0.5);
    
    // Mutation operator
    mutation = MutationFactory.getMutationOperator("PolynomialMutation");                    
    mutation.setParameter("probability",1.0/problem.getNumberOfVariables());
    mutation.setParameter("distributionIndex",20.0);  
    
    algorithm.addOperator("crossover",crossover);
    algorithm.addOperator("mutation",mutation);
    
    // Execute the Algorithm
    long initTime = System.currentTimeMillis();
    SolutionSet population = algorithm.execute();
    long estimatedTime = System.currentTimeMillis() - initTime;
    
    // Result messages 
    logger_.info("Total execution time: "+estimatedTime + "ms");
    logger_.info("Objectives values have been writen to file FUN");
    population.printObjectivesToFile("FUN");
    logger_.info("Variables values have been writen to file VAR");
    population.printVariablesToFile("VAR");      
    
    if (indicators != null) {
      logger_.info("Quality indicators") ;
      logger_.info("Hypervolume: " + indicators.getHypervolume(population)) ;
      logger_.info("GD         : " + indicators.getGD(population)) ;
      logger_.info("IGD        : " + indicators.getIGD(population)) ;
      logger_.info("Spread     : " + indicators.getSpread(population)) ;
      logger_.info("Epsilon    : " + indicators.getEpsilon(population)) ;
    } // if          
  } //main
} // MOEAD_main
