/**
 * RandomSearch_main.java
 *
 * @author Juan J. Durillo
 * @version 1.0
 *   A simple algorithm that perform a random search.
 */
package jmetal.metaheuristics.randomSearch;

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
import java.io.IOException;

import java.util.logging.FileHandler;
import java.util.logging.Logger;

import jmetal.qualityIndicator.QualityIndicator;

public class RandomSearch_main {
  public static Logger      logger_ ;      // Logger object
  public static FileHandler fileHandler_ ; // FileHandler object

  /**
   * @param args Command line arguments.
   * @throws JMException
   * @throws IOException
   * @throws SecurityException
   * Usage: three options
   *      - jmetal.metaheuristics.randomSearch.RandomSearch_main
   *      - jmetal.metaheuristics.randomSearch.RandomSearch_main problemName
   */
  public static void main(String [] args) throws
                                  JMException, SecurityException, IOException {
    Problem   problem   ;         // The problem to solve
    Algorithm algorithm ;         // The algorithm to use
    Operator  crossover ;         // Crossover operator
    Operator  mutation  ;         // Mutation operator
    Operator  selection ;         // Selection operator

    if (args.length == 1) {
      Object [] params = {"Real"};
      problem = (new ProblemFactory()).getProblem(args[0],params);
    } // if
    else { // Default problem
      problem = new Kursawe(3, "Real");
      //problem = new Kursawe(3,"BinaryReal");
      //problem = new Water("Real");
      //problem = new ZDT4(10, "Real");
      //problem = new WFG1("Real");
      //problem = new DTLZ1("Real");
      //problem = new OKA2("Real") ;
    } // else

    algorithm = new RandomSearch(problem);

    // Algorithm parameters
    algorithm.setInputParameter("maxEvaluations",25000);

    // Execute the Algorithm
    long initTime = System.currentTimeMillis();
    SolutionSet population = algorithm.execute();
    long estimatedTime = System.currentTimeMillis() - initTime;

    // Result messages
    System.out.println(estimatedTime);
    //logger_.info("Variables values have been writen to file VAR");
    population.printVariablesToFile("VAR");
    //logger_.info("Objectives values have been writen to file FUN");
    population.printObjectivesToFile("FUN");

  } //main
} // Randomsearch_main
