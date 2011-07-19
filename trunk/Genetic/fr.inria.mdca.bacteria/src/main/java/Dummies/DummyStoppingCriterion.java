package Dummies;
import java.util.Vector;

import projetBacterioJava.StoppingCriterion;

/**
 * DummyStoppingCriterion is the class that extends the abstract class StoppingCriterion
 * in order to implement the run() function.
 * In this example, the stopping criterion is based on the fitness value of the solution
 * set. Hier, we don't want an approximation of the number 'nbToObtain' (cf class
 * DummyBacterium) but the exact result.
 *  
 */

public class DummyStoppingCriterion extends StoppingCriterion{

	/**
	 * WE stop when the sum of the element from the solution set is equal to the number
	 * nbToObtain defined in the class DummyBacterium.
	 * @return true if the BacteriologicAlgorithm should stop its evolution
	 */
	public boolean run() {
		Vector vect = this.bacteriologicAlgorithm.getSolutionSet();
		return (this.bacteriologicAlgorithm.getFitnessFunction().fitness(vect) == 2);
	}
	
	
}
