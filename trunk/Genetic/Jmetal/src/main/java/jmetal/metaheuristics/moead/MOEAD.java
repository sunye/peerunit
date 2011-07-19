/**
 * MOEAD.java
 * @author Antonio J. Nebro
 * @version 1.0
 */
package jmetal.metaheuristics.moead;

import jmetal.base.*;
import jmetal.util.*;

import java.util.Vector;
import jmetal.base.Algorithm;
import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.util.PseudoRandom;

public class MOEAD extends Algorithm {

  /**
   * This class representing the original asynchronous MOCell algorithm
   * hybridized with Diferential evolutions (GDE3), called CellDE. To 
   * store non-dominated
   * solutions it uses an Archive based on spea2 fitness.
   */
  private Problem problem_;
  /**
   * Population size
   */
  private int populationSize_;
  /**
   * Stores the population
   */
  private SolutionSet population_;
  /**
   * Z vector (ideal point)
   */
  double[] z_;
  /**
   * Lambda vectors
   */
  //Vector<Vector<Double>> lambda_ ; 
  double[][] lambda_;
  /**
   * T: neighbour size
   */
  int T_;
  /**
   * Neighborhood
   */
  int[][] neighborhood_;
  /**
   * delta: probability that parent solutions are selected from neighbourhood
   */
  double delta_;
  /**
   * nr: maximal number of solutions replaced by each child solution
   */
  int nr_;
  int H_;
  Solution[] indArray_;
  String functionType_;
  int evaluations_;
  /**
   * Operators
   */
  Operator crossover_;
  Operator mutation_;

  /** 
   * Constructor
   * @param problem Problem to solve
   */
  public MOEAD(Problem problem) {
    problem_ = problem;

    functionType_ = "_TCHE1";

  } // DMOEA

  public SolutionSet execute() throws JMException {
    int maxEvaluations;

    evaluations_ = 0;
    maxEvaluations = ((Integer) this.getInputParameter("maxEvaluations")).intValue();
    populationSize_ = ((Integer) this.getInputParameter("populationSize")).intValue();

        populationSize_ = 300;

    population_ = new SolutionSet(populationSize_);
    indArray_ = new Solution[problem_.getNumberOfObjectives()];

    T_ = 20;
    delta_ = 0.9;
    nr_ = 2;
    H_ = 33;

    neighborhood_ = new int[populationSize_][T_];

    z_ = new double[problem_.getNumberOfObjectives()];
    //lambda_ = new Vector(problem_.getNumberOfObjectives()) ;
    lambda_ = new double[populationSize_][problem_.getNumberOfObjectives()];
    
    crossover_ = operators_.get("crossover"); // default: DE crossover
    mutation_ = operators_.get("mutation");  // default: polynomial mutation

    // STEP 1. Initialization
    // STEP 1.1. Compute euclidean distances between weight vectors and find T
    initUniformWeight();

    initNeighborhood();

    // STEP 1.2. Initialize population
    initPopulation();

    // STEP 1.3. Initialize z_
    initIdealPoint();

    // STEP 2. Update
    do {
      int[] permutation = new int[populationSize_];
      Utils.randomPermutation(permutation, populationSize_);

      for (int i = 0; i < populationSize_; i++) {
        int n = permutation[i]; // or int n = i;
        int type;
        double rnd = PseudoRandom.randDouble();

        // STEP 2.1. Mating selection based on probability
        if (rnd < delta_) // if (rnd < realb)    
        {
          type = 1;   // neighborhood
        } else {
          type = 2;   // whole population
        }
        Vector<Integer> p = new Vector();
        matingSelection(p, n, 2, type);

        // STEP 2.2. Reproduction
        Solution child;
        Solution[] parents = new Solution[3];

        parents[0] = population_.get(p.get(0));
        parents[1] = population_.get(p.get(1));
        parents[2] = population_.get(n);

        // Apply DE crossover 
        child = (Solution) crossover_.execute(new Object[]{population_.get(n), parents});
        //diff_evo_xover2(population[n].indiv,population[p[0]].indiv,population[p[1]].indiv,child);

        // Apply mutation
        mutation_.execute(child);

        // Evaluation
        problem_.evaluate(child);

        evaluations_++;

        // STEP 2.3. Repair. Not necessary

        // STEP 2.4. Update z_
        updateReference(child);

        // STEP 2.5. Update of solutions
        updateProblem(child, type, n);
      } // for 
    //System.exit(0) ;
    } while (evaluations_ < maxEvaluations);

    return population_;
  }

  /**
   * 
   */
  public void initUniformWeight() {
    if (problem_.getNumberOfObjectives() == 2) {
      for (int n = 0; n < populationSize_; n++) {
        double a = 1.0 * n / (populationSize_ - 1);
        lambda_[n][0] = a;
        lambda_[n][1] = 1 - a;
      } // for
    } // if
    else {
      for (int i = 0; i <= H_; i++) {
        for (int j = 0; j <= H_; j++) {
          if (i + j <= H_) {
            Vector<Integer> array = new Vector();
            array.addElement(i);
            array.addElement(j);
            array.addElement(H_ - i - j);
            for (int k = 0; k < array.size(); k++) {
              lambda_[i + j][k] = 1.0 * array.get(k) / H_; // TO REVISE
            // sub.namda.push_back(1.0 * sub.array[k] / unit);
            } // for
          } // if
        } // for
      } // for
    } // else
  } // initUniformWeight

  /**
   * 
   */
  public void initNeighborhood() {
    double[] x = new double[populationSize_];
    int[] idx = new int[populationSize_];

    for (int i = 0; i < populationSize_; i++) {
      // calculate the distances based on weight vectors
      for (int j = 0; j < populationSize_; j++) {
        x[j] = Utils.distVector(lambda_[i], lambda_[j]);
        //x[j] = dist_vector(population[i].namda,population[j].namda);
        idx[j] = j;
      //System.out.println("x["+j+"]: "+x[j]+ ". idx["+j+"]: "+idx[j]) ;
      } // for

      // find 'niche' nearest neighboring subproblems
      Utils.minFastSort(x, idx, populationSize_, T_);
      //minfastsort(x,idx,population.size(),niche);

      for (int k = 0; k < T_; k++) {
        neighborhood_[i][k] = idx[k];
      //System.out.println("neg["+i+","+k+"]: "+ neighborhood_[i][k]) ;
      }
    } // for
  } // initNeighborhood

  /**
   * 
   */
  public void initPopulation() throws JMException {
    for (int i = 0; i < populationSize_; i++) {
      Solution newSolution = new Solution(problem_);

      problem_.evaluate(newSolution);
      problem_.evaluateConstraints(newSolution);
      evaluations_++;
      population_.add(newSolution);

    //population[i].indiv.rnd_init();
    //population[i].indiv.obj_eval();
    //update_reference(population[i].indiv);
    //nfes++;
    } // for
  } // initPopulation

  /**
   * 
   */
  void initIdealPoint() throws JMException {
    for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
      z_[i] = 1.0e+30;
      indArray_[i] = new Solution(problem_);
      problem_.evaluate(indArray_[i]);
      evaluations_++;
    } // for

    for (int i = 0; i < populationSize_; i++) {
      updateReference(population_.get(i));
    //for (int j = 0; j < problem_.getNumberOfObjectives(); j++) {
    //  if (population_.get(i).getObjective(j) < z_[j]) {
    //    z_[j] = population_.get(i).getObjective(j);
    //  } // if
    //} // for
    } // for
  } // initIdealPoint

  /**
   * 
   */
  public void matingSelection(Vector<Integer> list, int cid, int size, int type) {
    // list : the set of the indexes of selected mating parents
    // cid  : the id of current subproblem
    // size : the number of selected mating parents
    // type : 1 - neighborhood; otherwise - whole population
    int ss;
    int r;
    int p;

    //ss   = population_[cid].table.size() ;
    ss = neighborhood_[cid].length;
//System.out.println("cid: " + cid + ". ss: " + ss + ". Type: "+type) ;
    while (list.size() < size) {
      if (type == 1) {
        r = PseudoRandom.randInt(0, ss - 1);
        p = neighborhood_[cid][r];
      //p = population[cid].table[r];
      } else {
        p = PseudoRandom.randInt(0, populationSize_ - 1);
      //p = int(population.size()*rnd_uni(&rnd_uni_init));
      }
//      System.out.println("   - p: " + p ) ;
      boolean flag = true;
      for (int i = 0; i < list.size(); i++) {
        if (list.get(i) == p) // p is in the list
        {
          flag = false;
          break;
        }
      }

      //if (flag) list.push_back(p);
      if (flag) {
        list.addElement(p);
      }
    }
//System.exit(9) ;
  } // matingSelection

  /**
   * 
   * @param individual
   */
  void updateReference(Solution individual) {
    for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
      if (individual.getObjective(n) < z_[n]) {
        z_[n] = individual.getObjective(n);

        indArray_[n] = individual;
      }
    }
  } // updateReference

  /**
   * @param individual
   * @param id
   * @param type
   */
  void updateProblem(Solution indiv, int id, int type) {
    // indiv: child solution
    // id:   the id of current subproblem
    // type: update solutions in - neighborhood (1) or whole population (otherwise)
    int size;
    int time;

    time = 0;

    if (type == 1) {
      size = neighborhood_[id].length;
    } else {
      size = population_.size();
    }
    int[] perm = new int[size];

    Utils.randomPermutation(perm, size);

    for (int i = 0; i < size; i++) {
      int k;
      if (type == 1) {
        k = neighborhood_[id][perm[i]];
      } else {
        k = perm[i];      // calculate the values of objective function regarding the current subproblem
      }
      double f1, f2;

      f1 = fitnessFunction(population_.get(k), lambda_[k]);
      f2 = fitnessFunction(indiv, lambda_[k]);

      //f1 = fitnessunction(population[k].indiv.y_obj, population[k].namda, ind_arr);
      //f2 = fitnessfunction(indiv.y_obj, population[k].namda, ind_arr);

      if (f2 < f1) {
        population_.replace(k, new Solution(indiv));
        //population[k].indiv = indiv;
        time++;
      }
      // the maximal number of solutions updated is not allowed to exceed 'limit'
      if (time >= nr_) {
        return;
      }
    }
  //delete [] perm;

  } // updateProblem

  double fitnessFunction(Solution individual, double[] lambda) {
    double fitness;
    fitness = 0.0;

    if (functionType_.equals("_TCHE1")) {
      double maxFun = -1.0e+30;

      for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
        double diff = Math.abs(individual.getObjective(n) - z_[n]);

        double feval;
        if (lambda[n] == 0) {
          feval = 0.0001 * diff;
        } else {
          feval = diff * lambda[n];
        }
        if (feval > maxFun) {
          maxFun = feval;
        }
      } // for

      fitness = maxFun;
    } // if
    else {
      System.out.println("MOEAD.fitnessFunction: unknown type " + functionType_);
      System.exit(-1);
    }
    return fitness;
  } // fitnessEvaluation
} // MOEAD

