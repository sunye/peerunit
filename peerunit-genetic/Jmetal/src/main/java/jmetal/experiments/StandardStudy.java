/**
 * StandardStudy.java
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
import jmetal.experiments.settings.GDE3_Settings;
import jmetal.experiments.settings.MOCell_Settings;
import jmetal.experiments.settings.NSGAII_Settings;
import jmetal.experiments.settings.SPEA2_Settings;
import jmetal.experiments.settings.SMPSO_Settings;

import jmetal.util.JMException;

/**
 * @author Antonio J. Nebro
 */
public class StandardStudy extends Experiment {

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
        algorithm_[4] = new GDE3_Settings(problem).configure(parameters[3]);
      } catch  (JMException ex) {
      Logger.getLogger(StandardStudy.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public static void main(String[] args) throws JMException, IOException {
    StandardStudy exp = new StandardStudy();

    exp.experimentName_ = "StandardStudy";
    exp.algorithmNameList_ = new String[]{"NSGAII", "SPEA2", "MOCell", "SMPSO", "GDE3"};
    exp.problemList_ = new String[]{"ZDT1", "ZDT2","ZDT3", "ZDT4","ZDT6",
                                    "DTLZ1","DTLZ2","DTLZ3","DTLZ4","DTLZ5",
                                    "DTLZ6","DTLZ7",
                                    "WFG1","WFG2","WFG3","WFG4","WFG5","WFG6",
                                    "WFG7","WFG8","WFG9"};
    exp.paretoFrontFile_ = new String[]{"ZDT1.pf", "ZDT2.pf","ZDT3.pf",
                                    "ZDT4.pf","ZDT6.pf",
                                    "DTLZ1.2D.pf","DTLZ2.2D.pf","DTLZ3.2D.pf",
                                    "DTLZ4.2D.pf","DTLZ5.2D.pf","DTLZ6.2D.pf",
                                    "DTLZ7.2D.pf",
                                    "WFG1.2D.pf","WFG2.2D.pf","WFG3.2D.pf",
                                    "WFG4.2D.pf","WFG5.2D.pf","WFG6.2D.pf",
                                    "WFG7.2D.pf","WFG8.2D.pf","WFG9.2D.pf"};

    exp.indicatorList_ = new String[]{"HV", "SPREAD", "EPSILON"};

    int numberOfAlgorithms = exp.algorithmNameList_.length;

    exp.experimentBaseDirectory_ = "/Users/antonio/Softw/pruebas/jmetal/" + exp.experimentName_;
    exp.paretoFrontDirectory_ = "/Users/antonio/Softw/pruebas/paretoFronts";

    exp.algorithmSettings_ = new Settings[numberOfAlgorithms];
    exp.algorithm_ = new Algorithm[numberOfAlgorithms];

    exp.independentRuns_ = 30;

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
    rows = 3 ;
    columns = 2 ;
    prefix = new String("ZDT");
    problems = new String[]{"ZDT1", "ZDT2","ZDT3", "ZDT4","ZDT6"} ;

    exp.generateRBoxplotScripts(rows, columns, problems, prefix) ;
    exp.generateRWilcoxonScripts(problems, prefix) ;

    // Configure scripts for DTLZ
    rows = 3 ;
    columns = 3 ;
    prefix = new String("DTLZ");
    problems = new String[]{"DTLZ1","DTLZ2","DTLZ3","DTLZ4","DTLZ5",
                                    "DTLZ6","DTLZ7"} ;

    exp.generateRBoxplotScripts(rows, columns, problems, prefix) ;
    exp.generateRWilcoxonScripts(problems, prefix) ;

    // Configure scripts for WFG
    rows = 3 ;
    columns = 3 ;
    prefix = new String("WFG");
    problems = new String[]{"WFG1","WFG2","WFG3","WFG4","WFG5","WFG6",
                                    "WFG7","WFG8","WFG9"} ;

    exp.generateRBoxplotScripts(rows, columns, problems, prefix) ;
    exp.generateRWilcoxonScripts(problems, prefix) ;
  }
} // StandardStudy


