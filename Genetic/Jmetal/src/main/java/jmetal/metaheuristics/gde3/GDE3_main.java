/**
 * GDE3_main.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 */
package jmetal.metaheuristics.gde3;

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

public class GDE3_main {
  public static Logger      logger_ ;      // Logger object
  public static FileHandler fileHandler_ ; // FileHandler object

  /**
   * @param args Command line arguments. The first (optional) argument specifies 
   *             the problem to solve.
   * @throws JMException 
 * @throws IOException 
 * @throws SecurityException 
   */
  public static void main(String [] args) throws JMException, SecurityException, IOException {
    Problem   problem   ;         // The problem to solve
    Algorithm algorithm ;         // The algorithm to use
    Operator  selection ;
    Operator  crossover ;
    
    // Logger object and file to store log messages
    logger_      = Configuration.logger_ ;
    fileHandler_ = new FileHandler("GDE3.log"); 
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
      //problem = new DTLZ1(7,4,"Real");
      //problem = new OKA2("Real") ;
    } // else
    
    //algorithm = new GDE3(problem);
    algorithm = new GDE3(problem);
    //algorithm = new aMOCellDE(problem);
    
    // Algorithm parameters
    algorithm.setInputParameter("populationSize",100);
    algorithm.setInputParameter("maxIterations",250);
    
    // Crossover operator 
    crossover = CrossoverFactory.getCrossoverOperator("DifferentialEvolutionCrossover");                   
    crossover.setParameter("CR", 0.1);                   
    crossover.setParameter("F", 0.5);
    
    // Add the operators to the algorithm
    selection = SelectionFactory.getSelectionOperator("DifferentialEvolutionSelection") ;

    algorithm.addOperator("crossover",crossover);
    algorithm.addOperator("selection",selection);
    
    // Execute the Algorithm 
    long initTime = System.currentTimeMillis();
    SolutionSet population = algorithm.execute();
    long estimatedTime = System.currentTimeMillis() - initTime;

    /* Result messages */   
    logger_.info("Total execution time: "+estimatedTime);
    logger_.info("Objectives values have been writen to file FUN");
    population.printObjectivesToFile("FUN");
    logger_.info("Variables values have been writen to file VAR");
    population.printVariablesToFile("VAR");          
  }//main
} // GDE3_main
