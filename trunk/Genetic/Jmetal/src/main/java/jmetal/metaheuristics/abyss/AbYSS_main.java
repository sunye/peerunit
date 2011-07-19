/**
 * AbYSS_main.java
 *
 * @author Juan J. Durillo
 * @version 1.0
 * 
 * This class executes the algorithm described in:
 *   A.J. Nebro, F. Luna, E. Alba, B. Dorronsoro, J.J. Durillo, A. Beham 
 *   "AbYSS: Adapting Scatter Search to Multiobjective Optimization." 
 *   Accepted for publication in IEEE Transactions on Evolutionary Computation. 
 *   July 2007
 */
package jmetal.metaheuristics.abyss;

import java.io.IOException;
import jmetal.base.*;
import jmetal.base.operator.crossover.*   ;
import jmetal.base.operator.mutation.*    ; 
import jmetal.problems.*                  ;
import jmetal.problems.DTLZ.*             ;
import jmetal.problems.ZDT.*              ;
import jmetal.problems.WFG.*              ;
import jmetal.problems.ZZJ07.*;
import jmetal.problems.LZ07.* ;
import jmetal.util.JMException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import jmetal.base.operator.localSearch.MutationLocalSearch;
/**
 * This class is the main program used to configure and run AbYSS, a 
 * multiobjective scatter search metaheuristics.
 * Reference: A.J. Nebro, F. Luna, E. Alba, A. Beham, B. Dorronsoro "AbYSS: 
 *            Adapting Scatter Search for Multiobjective Optimization". 
 *            TechRep. ITI-2006-2, Departamento de Lenguajes y Ciencias de la 
 *            Computacion, University of Malaga. 
 * Comments: AbYSS is configured to work only with continuous decision 
 *           variables.
 */
public class AbYSS_main {
  public static Logger      logger_ ;      // Logger object
  public static FileHandler fileHandler_ ; // FileHandler object
  
  /**
   * @param args Command line arguments. The first (optional) argument specifies 
   *             the problem to solve.
   * @throws JMException 
   */
  public static void main(String [] args) throws 
                                 JMException, SecurityException, IOException {    
    Problem   problem     ; // The problem to solve
    Algorithm algorithm   ; // The algorithm to use
    Operator  crossover   ; // Crossover operator
    Operator  mutation    ; // Mutation operator
    Operator  improvement ; // Operator for improvement
            
    // Logger object and file to store log messages
    logger_      = Configuration.logger_ ;
    fileHandler_ = new FileHandler("AbySS.log"); 
    logger_.addHandler(fileHandler_) ;
    
    // STEP 1. Select the multiobjective optimization problem to solve
    if (args.length == 1) {
      Object [] params = {"Real"};
      problem = (new ProblemFactory()).getProblem(args[0], params);
    } // if
    else { // Default problem
      problem = new Kursawe(3, "Real"); 
      //problem = new Kursawe(3,"BinaryReal");
      //problem = new Water("Real");
      //problem = new ZDT4("Real");
      //problem = new WFG1("Real");
      //problem = new DTLZ1("Real");
      //problem = new OKA2("Real") ;
    } // else
    
    // STEP 2. Select the algorithm (AbYSS)
    algorithm = new AbYSS(problem) ;
    
    // STEP 3. Set the input parameters required by the metaheuristic
    algorithm.setInputParameter("populationSize", 20);
    algorithm.setInputParameter("refSet1Size"   , 10);
    algorithm.setInputParameter("refSet2Size"   , 10);
    algorithm.setInputParameter("archiveSize"   , 100);
    algorithm.setInputParameter("maxEvaluations", 25000);
      
    // STEP 4. Specify and configure the crossover operator, used in the
    //         solution combination method of the scatter search
    crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover");                   
    crossover.setParameter("probability"      , 1.0) ;                   
    crossover.setParameter("distributionIndex", 20.0) ;
    
    // STEP 5. Specify and configure the improvement method. We use by default
    //         a polynomial mutation in this method.
    mutation = MutationFactory.getMutationOperator("PolynomialMutation");
    mutation.setParameter("probability", 1.0/problem.getNumberOfVariables());
    
    improvement = new MutationLocalSearch(problem,mutation);
    improvement.setParameter("improvementRounds", 1);
          
    // STEP 6. Add the operators to the algorithm
    algorithm.addOperator("crossover",crossover);
    algorithm.addOperator("improvement",improvement);   
    
    long initTime      ;
    long estimatedTime ;    
    initTime = System.currentTimeMillis();
    
    // STEP 7. Run the algorithm 
    SolutionSet population = algorithm.execute();
    
    estimatedTime = System.currentTimeMillis() - initTime;
    logger_.info("Total execution time: "+ estimatedTime);

    // STEP 8. Print the results
    logger_.info("Objectives values have been writen to file FUN");
    population.printObjectivesToFile("FUN");
    logger_.info("Variables values have been writen to file VAR");
    population.printVariablesToFile("VAR");        
  }//main
}
