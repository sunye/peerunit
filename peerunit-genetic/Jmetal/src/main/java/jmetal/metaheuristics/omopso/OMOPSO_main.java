/**
 * OMOPSO_main.java
 * 
 * @author Juan J. Durillo
 * @version 1.0
 */

package jmetal.metaheuristics.omopso;

import java.io.IOException;
import jmetal.base.*;
import jmetal.problems.*;
import jmetal.problems.DTLZ.*;
import jmetal.problems.ZDT.*;
import jmetal.problems.WFG.*;
import jmetal.problems.LZ07.* ;
import jmetal.util.JMException ;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import jmetal.qualityIndicator.QualityIndicator;

public class OMOPSO_main {
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
    
    QualityIndicator indicators ; // Object to get quality indicators
        
    // Logger object and file to store log messages
    logger_      = Configuration.logger_ ;
    fileHandler_ = new FileHandler("OMOPSO_main.log"); 
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
      //problem = new Kursawe(3,"BinaryReal");
      //problem = new Water("Real");
      //problem = new ZDT4("Real");
      //problem = new WFG1("Real");
      //problem = new DTLZ1("Real");
      //problem = new OKA2("Real") ;
    }
    
    algorithm = new OMOPSO(problem) ;
    
    // Algorithm parameters
    algorithm.setInputParameter("swarmSize",100);
    algorithm.setInputParameter("archiveSize",100);
    algorithm.setInputParameter("maxIterations",250);
    algorithm.setInputParameter("perturbationIndex",0.5);
    
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
}
