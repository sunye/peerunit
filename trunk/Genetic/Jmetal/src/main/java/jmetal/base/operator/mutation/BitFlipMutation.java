/**
 * BitFlipMutation.java
 * @author Juan J. Durillo
 * @author Antonio J. Nebro
 * @version 1.1
 */
package jmetal.base.operator.mutation;

import jmetal.base.Configuration;
import jmetal.base.Solution;
import jmetal.base.Configuration.SolutionType_;
import jmetal.base.variable.*;
import jmetal.base.DecisionVariables;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.base.Operator;

/**
 * This class implements a bit flip mutation operator.
 * NOTE: the operator is applied to binary or integer solutions, considering the
 * whole solution as a single variable.
 */
public class BitFlipMutation extends Operator {

  /**
   * Constructor
   * Creates a new instance of the Bit Flip mutation operator
   */
  public BitFlipMutation() {
  } // BitFlipMutation

  /**
   * Perform the mutation operation
   * @param probability Mutation probability
   * @param solution The solution to mutate
   * @throws JMException
   */
  public void doMutation(double probability, Solution solution) throws JMException {
    try {
      if ((solution.getType() == SolutionType_.Binary) ||
              (solution.getType() == SolutionType_.BinaryReal)) {
        for (int i = 0; i < solution.getDecisionVariables().size(); i++) {
          for (int j = 0; j < ((Binary) solution.getDecisionVariables().variables_[i]).getNumberOfBits(); j++) {
            if (PseudoRandom.randDouble() < probability) {
              ((Binary) solution.getDecisionVariables().variables_[i]).bits_.flip(j);
            }
          }
        }

        for (int i = 0; i < solution.getDecisionVariables().size(); i++) {
          ((Binary) solution.getDecisionVariables().variables_[i]).decode();
        }
      } // if
      else { // Integer representation
         for (int i = 0; i < solution.getDecisionVariables().size(); i++)
            if (PseudoRandom.randDouble() < probability) {
              int value = (int) (PseudoRandom.randInt(
                       (int)solution.getDecisionVariables().variables_[i].getUpperBound(),
                       (int)solution.getDecisionVariables().variables_[i].getLowerBound()));
              solution.getDecisionVariables().variables_[i].setValue(value);
            } // if
      } // else
    } catch (ClassCastException e1) {
      Configuration.logger_.severe("BitFlipMutation.doMutation: " +
              "ClassCastException error" + e1.getMessage());
      Class cls = java.lang.String.class;
      String name = cls.getName();
      throw new JMException("Exception in " + name + ".doMutation()");
    }
  } // doMutation

  /**
   * Executes the operation
   * @param object An object containing a solution to mutate
   * @return An object containing the mutated solution
   * @throws JMException 
   */
  public Object execute(Object object) throws JMException {
    Solution solution = (Solution) object;

    if ((solution.getType() != SolutionType_.Binary) &&
            (solution.getType() != SolutionType_.BinaryReal) &&
            (solution.getType() != SolutionType_.Int)) {
      Configuration.logger_.severe("BitFlipMutation.execute: the solution " +
              "is not of the right type. The type should be 'Binary', " +
              "'BinaryReal' or 'Int', but " + solution.getType() + " is obtained");

      Class cls = java.lang.String.class;
      String name = cls.getName();
      throw new JMException("Exception in " + name + ".execute()");
    } // if 

    Double probability = (Double) getParameter("probability");
    if (probability == null) {
      Configuration.logger_.severe("BitFlipMutation.execute: probability not " +
              "specified");
      Class cls = java.lang.String.class;
      String name = cls.getName();
      throw new JMException("Exception in " + name + ".execute()");
    }

    doMutation(probability.doubleValue(), solution);
    return solution;
  } // execute
} // BitFlipMutation
