package Dummies;
import java.util.Iterator;
import java.util.Vector;

import projetBacterioJava.FitnessFunction;

/**
 * DummyFitnessFunction is the class that extends the abstract class
 * FitnessFunction in order to implement the fitness(Vector) function.
 * The fitness function evaluates the quality of a solution regarding the global objective.
 * Since a solution for the bacteriologic algorithm is a set of a bacteria, the fitness 
 * function computes the fitness of a set of bacteria.
 * In this example, the objective is to obtain a vector of bacteria (numbers) whose sum
 * is nbToObtain. For instance, at the end, the solution set can contain [9, 13, 15, 13]
 * because 9+13+15+13=50. As the algorithm is depending on the fitness function and the
 * mutation operator, it is possible that the algorithm can not arrive to the solution and
 * you will have a sum of 51 as a result.
 */

public class DummyFitness extends FitnessFunction {

	/**
	 * The number we want to obtain
	 */
	private float nbToObtain = 55;

	
	/**
	 * @return the number to obtain
	 */
	public float getNbToObtain(){
		return nbToObtain;
	}

	/**
	 * The more the sum of the element from the solution set are near the number
	 * 'nbToObtain' (Math.abs(nbToObtain-sum) is minimum), the more we want an important
	 * result for the fitness function.
	 * Here, we will obtain 2 with a sum of 50, 1 with a sum of 49 or 51, 0.5 with a sum
	 * of 48 or 52, etc...
	 * @param vectBact the set to evaluate
	 * @return the quality of the set relatively to the problem
	 */
	public float fitness(Vector vectBact) {
		// we consider an empty list has minimal fitness value to treat negative numbers
		// In the example, if we enter only negative values in the initial  medium, the
		// algorithme will consider that any Bacterium improve the medium set.
		if (vectBact.isEmpty()) return -1000;
		
		Iterator iter = vectBact.iterator();
		float sum = 0;
		while(iter.hasNext()){
			sum += ((DummyBacterium) iter.next()).getNumber();
		}
		if (nbToObtain-sum == 0)
			{return 2;}
		else {
			return 1 / (Math.abs(nbToObtain-sum));
		}
		
	}
	
	
}
