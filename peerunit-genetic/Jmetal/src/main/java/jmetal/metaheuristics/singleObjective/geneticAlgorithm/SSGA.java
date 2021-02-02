/**
 * SSGA.java
 * @author Antonio J. Nebro
 * @version 1.0
 */
package jmetal.metaheuristics.singleObjective.geneticAlgorithm;

import jmetal.base.*;
import jmetal.base.operator.comparator.* ;
import jmetal.base.variable.Permutation;
import jmetal.base.Algorithm;
import java.util.Comparator;
import jmetal.util.*;

/** 
 * Class implementing a steady state genetic algorithm
 */
public class SSGA extends Algorithm {
  private Problem           problem_;        
  
 /**
  *
  * Constructor
  * Create a new SSGA instance.
  * @param problem Problem to solve
  *
  */
  public SSGA(Problem problem){
    this.problem_ = problem;                        
  } // SSGA
  
 /**
  * Execute the SSGA algorithm
 * @throws JMException 
  */
  public SolutionSet execute() throws JMException {
    int populationSize ;
    int maxEvaluations ;
    int evaluations    ;

    SolutionSet population        ;
    Operator    mutationOperator  ;
    Operator    crossoverOperator ;
    Operator    selectionOperator ;
    
    Comparator  comparator        ;
    
    comparator = new ObjectiveComparator(0) ; // Single objective comparator
    
    // Read the params
    populationSize = ((Integer)this.getInputParameter("populationSize")).intValue();
    maxEvaluations = ((Integer)this.getInputParameter("maxEvaluations")).intValue();                
   
    // Initialize the variables
    population   = new SolutionSet(populationSize);        
    evaluations  = 0;                

    // Read the operators
    mutationOperator  = this.operators_.get("mutation");
    crossoverOperator = this.operators_.get("crossover");
    selectionOperator = this.operators_.get("selection");  

    // Create the initial population
    Solution newIndividual;
    for (int i = 0; i < populationSize; i++) {
      newIndividual = new Solution(problem_);                    
      problem_.evaluate(newIndividual);            
      evaluations++;
      population.add(newIndividual);
    } //for       

    while (evaluations < maxEvaluations) {
    //while (population.get(0).getObjective(0) > 0.0049) {
      if ((evaluations % 10000) == 0) {
        System.out.println(evaluations + ": " + population.get(0).getObjective(0)) ;
      } //
      Solution [] parents = new Solution[2];
      
      // Selection
      parents[0] = (Solution)selectionOperator.execute(population);
      parents[1] = (Solution)selectionOperator.execute(population);
 
      // Crossover
      Solution [] offspring = (Solution []) crossoverOperator.execute(parents);  

      // Mutation
      mutationOperator.execute(offspring[0]);

      // Evaluation of the new individual
      problem_.evaluate(offspring[0]);            
          
      evaluations ++;
    
      // Replacement: replace the last individual is the new one is better
      population.sort(comparator) ;

      Solution lastIndividual = population.get(populationSize - 1) ;
      
      if (lastIndividual.getObjective(0) > offspring[0].getObjective(0)) {
        population.remove(populationSize -1) ;
        population.add(offspring[0]);
      } // if
    } // while
    
    // Return a population with the best individual
    population.sort(comparator) ;

    SolutionSet resultPopulation = new SolutionSet(1) ;
    resultPopulation.add(population.get(0)) ;
    
    System.out.println("Evaluations: " + evaluations ) ;
    
    return resultPopulation ;
  } // execute
} // SSGA
