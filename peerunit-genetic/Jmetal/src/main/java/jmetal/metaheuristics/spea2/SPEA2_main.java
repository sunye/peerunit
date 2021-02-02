/**
 * SPEA2_main.java
 *
 * @author Juan J. Durillo
 * @version 1.0
 */
package jmetal.metaheuristics.spea2;

import java.io.IOException;
import jmetal.base.*;
import jmetal.base.operator.crossover.*   ;
import jmetal.base.operator.mutation.*    ; 
import jmetal.base.operator.selection.*   ;
import jmetal.problems.*                  ;
import jmetal.problems.DTLZ.*;
import jmetal.problems.ZDT.*;
import jmetal.problems.WFG.*;
import jmetal.problems.ZZJ07.*;
import jmetal.problems.LZ07.* ;

import jmetal.util.JMException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class SPEA2_main {
  public static Logger      logger_ ;      // Logger object
  public static FileHandler fileHandler_ ; // FileHandler object
  
  /**
   * @param args Command line arguments. The first (optional) argument specifies 
   *             the problem to solve.
   * @throws JMException 
   */
  public static void main(String [] args) throws JMException, IOException {
    Problem   problem   ;         // The problem to solve
    Algorithm algorithm ;         // The algorithm to use
    Operator  crossover ;         // Crossover operator
    Operator  mutation  ;         // Mutation operator
    Operator  selection ;         // Selection operator
        
    // Logger object and file to store log messages
    logger_      = Configuration.logger_ ;
    fileHandler_ = new FileHandler("SPEA2.log"); 
    logger_.addHandler(fileHandler_) ;
    
    if (args.length == 1) {
      Object [] params = {"Real"};
      problem = (new ProblemFactory()).getProblem(args[0],params);
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
    
    algorithm = new SPEA2(problem);
    
    // Algorithm params    
    algorithm.setInputParameter("populationSize",100);
    algorithm.setInputParameter("archiveSize",100);
    algorithm.setInputParameter("maxEvaluations",25000);
      
    // Mutation and Crossover for Real codification
    crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover");                   
    crossover.setParameter("probability",0.9);                   
    crossover.setParameter("distribuitionIndex",20.0);
    mutation = MutationFactory.getMutationOperator("PolynomialMutation");                    
    mutation.setParameter("probability",1.0/problem.getNumberOfVariables());
    mutation.setParameter("distributionIndex",20.0);
    
    /* Mutation and Crossover Binary codification */
    /*
    crossover = CrossoverFactory.getCrossoverOperator("SinglePointCrossover");                   
    crossover.setParameter("probability",0.9);                   
    mutation = MutationFactory.getMutationOperator("BitFlipMutation");                    
    mutation.setParameter("probability",1.0/80);
    */
    
    /* Selection Operator */
    selection = SelectionFactory.getSelectionOperator("BinaryTournament") ;                           
    
    // Add the operators to the algorithm
    algorithm.addOperator("crossover",crossover);
    algorithm.addOperator("mutation",mutation);
    algorithm.addOperator("selection",selection);
    
    // Execute the Algorithm
    long initTime = System.currentTimeMillis();
    SolutionSet population = algorithm.execute();
    long estimatedTime = System.currentTimeMillis() - initTime;

       // Result messages 
    logger_.info("Total execution time: "+estimatedTime);
    logger_.info("Objectives values have been writen to file FUN");
    population.printObjectivesToFile("FUN");
    logger_.info("Variables values have been writen to file VAR");
    population.printVariablesToFile("VAR");      
  }//main
} // SPEA2_main.java
