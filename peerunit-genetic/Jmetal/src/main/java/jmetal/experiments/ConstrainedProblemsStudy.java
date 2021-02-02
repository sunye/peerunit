/**
 * ConstrainedProblemStudy.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 */
package jmetal.experiments;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.util.Properties;
import jmetal.base.Algorithm;
import jmetal.base.Problem;
import jmetal.experiments.settings.MOCell_Settings;
import jmetal.experiments.settings.NSGAII_Settings;
import jmetal.experiments.settings.SPEA2_Settings;
import jmetal.experiments.settings.OMOPSO_Settings;

import jmetal.experiments.settings.SMPSO_Settings;
import jmetal.util.JMException;

/**
 * @author Antonio J. Nebro
 */
public class ConstrainedProblemsStudy extends Experiment {

  /**
   * Configures the algorithms in each independent run
   * @param problem The problem to solve
   * @param problemIndex
   */
  public void algorithmSettings(Problem problem, int problemIndex) {
    try {
      int numberOfAlgorithms = algorithmNameList_.length;

      Properties[] parameters = new Properties[numberOfAlgorithms];

      for (int i = 0; i < numberOfAlgorithms; i++) {
        parameters[i] = new Properties();
      }

      if (!paretoFrontFile_[problemIndex].equals("")) {
        for (int i = 0; i < numberOfAlgorithms; i++) 
          parameters[i].setProperty("PARETO_FRONT_FILE", paretoFrontFile_[problemIndex]);
        } // if

        algorithm_[0] = new NSGAII_Settings(problem).configure(parameters[0]);
        algorithm_[1] = new SPEA2_Settings(problem).configure(parameters[1]);
        algorithm_[2] = new MOCell_Settings(problem).configure(parameters[2]);
        algorithm_[3] = new SMPSO_Settings(problem).configure(parameters[3]);
      } catch  (JMException ex) {
      Logger.getLogger(ConstrainedProblemsStudy.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public static void main(String[] args) throws JMException, IOException {
    ConstrainedProblemsStudy exp = new ConstrainedProblemsStudy();

    exp.experimentName_ = "ConstrainedProblemsStudy";
    exp.algorithmNameList_ = new String[]{
      "NSGAII", "SPEA2", "MOCell", "SMPSO"};
    exp.problemList_ = new String[]{
      "ConstrEx", "Golinski", "Srinivas","Tanaka"};
    exp.paretoFrontFile_ = new String[]{
      "ConstrEx.pf", "Golinski.pf", "Srinivas.pf", "Tanaka.pf"};
    
    exp.indicatorList_ = new String[]{"EPSILON", "SPREAD", "HV"};

    int numberOfAlgorithms = exp.algorithmNameList_.length;

    exp.experimentBaseDirectory_ = "/Users/antonio/Softw/pruebas/pruebas/" + exp.experimentName_;
    exp.paretoFrontDirectory_ = "/Users/antonio/Softw/pruebas/paretoFronts";

    exp.algorithmSettings_ = new Settings[numberOfAlgorithms];
    exp.algorithm_ = new Algorithm[numberOfAlgorithms];

    exp.independentRuns_ = 100;

    // Run the experiments
    exp.runExperiment() ;
    
    // Generate latex tables
    exp.generateLatexTables() ;
    
    // Configure the R scripts to be generated
    int rows  ;
    int columns  ;
    String prefix ;
    String [] problems ;

    // Configuring scripts for ZDT
    rows = 2 ;
    columns = 2 ;
    prefix = new String("Constrained");
    problems = new String[]{"ConstrEx", "Golinski", "Srinivas","Tanaka"} ;
    exp.generateRBoxplotScripts(rows, columns, problems, prefix) ;
  }
} // ConstrainedProblemsStudy


