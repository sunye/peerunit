/**
 * SinglePointCrossover.java
 * Class representing a single point crossover operator
 * @author Juan J. Durillo
 * @author Antonio J. Nebro
 * @version 1.1
 */
package jmetal.base.operator.crossover;

import jmetal.base.*;
import jmetal.base.Configuration.*;
import jmetal.base.variable.*;
import jmetal.base.*;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

/**
 * This class allows to apply a Single Point crossover operator using two parent
 * solutions.
 * NOTE: the operator is applied to binary or integer solutions, considering the
 * whole solution as a single variable.
 */
public class SinglePointCrossover extends Operator {

  /**
   * Constructor
   * Creates a new instance of the single point crossover operator
   */
  public SinglePointCrossover() {
  } // SinglePointCrossover

  /**
   * Perform the crossover operation.
   * @param probability Crossover probability
   * @param parent1 The first parent
   * @param parent2 The second parent
   * @return An array containig the two offsprings
   * @throws JMException
   */
  public Solution[] doCrossover(double probability,
          Solution parent1,
          Solution parent2) throws JMException {
    Solution[] offSpring = new Solution[2];
    offSpring[0] = new Solution(parent1);
    offSpring[1] = new Solution(parent2);
    try {
      if (PseudoRandom.randDouble() < probability) {
        if ((parent1.getType() == SolutionType_.Binary) ||
                (parent1.getType() == SolutionType_.BinaryReal)) {
          //1. Compute the total number of bits
          int totalNumberOfBits = 0;
          for (int i = 0; i < parent1.getDecisionVariables().size(); i++) {
            totalNumberOfBits +=
                    ((Binary) parent1.getDecisionVariables().variables_[i]).getNumberOfBits();
          }

          //2. Calcule the point to make the crossover
          int crossoverPoint = PseudoRandom.randInt(0, totalNumberOfBits - 1);

          //3. Compute the variable that containt the crossoverPoint bit
          int variable = 0;
          int acountBits =
                  ((Binary) parent1.getDecisionVariables().variables_[variable]).getNumberOfBits();

          while (acountBits < (crossoverPoint + 1)) {
            variable++;
            acountBits +=
                    ((Binary) parent1.getDecisionVariables().variables_[variable]).getNumberOfBits();
          }

          //4. Compute the bit into the variable selected
          int diff = acountBits - crossoverPoint;
          int intoVariableCrossoverPoint =
                  ((Binary) parent1.getDecisionVariables().variables_[variable]).getNumberOfBits() - diff;

          //5. Make the crossover into the the gene;
          Binary offSpring1, offSpring2;
          offSpring1 =
                  (Binary) parent1.getDecisionVariables().variables_[variable].deepCopy();
          offSpring2 =
                  (Binary) parent2.getDecisionVariables().variables_[variable].deepCopy();

          for (int i = intoVariableCrossoverPoint;
                  i < offSpring1.getNumberOfBits();
                  i++) {
            boolean swap = offSpring1.bits_.get(i);
            offSpring1.bits_.set(i, offSpring2.bits_.get(i));
            offSpring2.bits_.set(i, swap);
          }

          offSpring[0].getDecisionVariables().variables_[variable] = offSpring1;
          offSpring[1].getDecisionVariables().variables_[variable] = offSpring2;

          //6. Apply the crossover to the other variables
          for (int i = 0; i < variable; i++) {
            offSpring[0].getDecisionVariables().variables_[i] =
                    parent2.getDecisionVariables().variables_[i].deepCopy();

            offSpring[1].getDecisionVariables().variables_[i] =
                    parent1.getDecisionVariables().variables_[i].deepCopy();

          }

          //7. Decode the results
          for (int i = 0; i < offSpring[0].getDecisionVariables().size(); i++) {
            ((Binary) offSpring[0].getDecisionVariables().variables_[i]).decode();
            ((Binary) offSpring[1].getDecisionVariables().variables_[i]).decode();
          }
        } // Binary or BinaryReal
        else { // Integer representation
          int crossoverPoint = PseudoRandom.randInt(0, parent1.numberOfVariables() - 1);
          int valueX1;
          int valueX2;
          for (int i = crossoverPoint; i < parent1.numberOfVariables(); i++) {
            valueX1 = (int) parent1.getDecisionVariables().variables_[i].getValue();
            valueX2 = (int) parent2.getDecisionVariables().variables_[i].getValue();
            offSpring[0].getDecisionVariables().variables_[i].setValue(valueX2);
            offSpring[1].getDecisionVariables().variables_[i].setValue(valueX1);
          } // for
        } // Int representation
      }
    } catch (ClassCastException e1) {
      Configuration.logger_.severe("SinglePointCrossover.doCrossover: Cannot perfom " +
              "SinglePointCrossover");
      Class cls = java.lang.String.class;
      String name = cls.getName();
      throw new JMException("Exception in " + name + ".doCrossover()");
    }
    return offSpring;
  } // doCrossover

  /**
   * Executes the operation
   * @param object An object containing an array of two solutions
   * @return An object containing an array with the offSprings
   * @throws JMException
   */
  public Object execute(Object object) throws JMException {
    Solution[] parents = (Solution[]) object;

    if (((parents[0].getType() != SolutionType_.Binary) ||
            (parents[1].getType() != SolutionType_.Binary)) &&
            ((parents[0].getType() != SolutionType_.BinaryReal) ||
            (parents[1].getType() != SolutionType_.BinaryReal)) &&
            ((parents[0].getType() != SolutionType_.Int) ||
            (parents[1].getType() != SolutionType_.Int))) {

      Configuration.logger_.severe("SinglePointCrossover.execute: the solutions " +
              "are not of the right type. The type should be 'Binary' or 'Int', but " +
              parents[0].getType() + " and " +
              parents[1].getType() + " are obtained");

      Class cls = java.lang.String.class;
      String name = cls.getName();
      throw new JMException("Exception in " + name + ".execute()");
    } // if

    Double probability = (Double) getParameter("probability");
    if (parents.length < 2) {
      Configuration.logger_.severe("SinglePointCrossover.execute: operator " +
              "needs two parents");
      Class cls = java.lang.String.class;
      String name = cls.getName();
      throw new JMException("Exception in " + name + ".execute()");
    } else if (probability == null) {
      Configuration.logger_.severe("SinglePointCrossover.execute: probability " +
              "not specified");
      Class cls = java.lang.String.class;
      String name = cls.getName();
      throw new JMException("Exception in " + name + ".execute()");
    }

    Solution[] offSpring;
    offSpring = doCrossover(probability.doubleValue(),
            parents[0],
            parents[1]);

    //-> Update the offSpring solutions
    for (int i = 0; i < offSpring.length; i++) {
      offSpring[i].setCrowdingDistance(0.0);
      offSpring[i].setRank(0);
    }
    return offSpring;//*/
  } // execute
} // SinglePointCrossover
