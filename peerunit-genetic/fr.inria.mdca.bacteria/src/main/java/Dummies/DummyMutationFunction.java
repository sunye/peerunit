package Dummies;

import projetBacterioJava.Bacterium;
import projetBacterioJava.MutationFunction;

/**
 * DummyMutationFunction is the class that extends the abstract class
 * MutationFunction in order to implement the mutate(Bacterium) function. It
 * generates a new bacterium by slightly altering an ancestor. It is crucial
 * for the algorithm, since it is the one that creates new information in the
 * process.
 */

public class DummyMutationFunction extends MutationFunction{

	
	
	/**
	 * Default Constructor
	 */
	public DummyMutationFunction () {
	}
	

	/**
	 * Constructor
	 * @param theBactNumber the number of bacteria to mutate
	 */
	public DummyMutationFunction(int theBactNumber) {
		super(theBactNumber);
	}

	
	/**
	 * In this example, the Bacterium is represented by a number. The mutation
	 * of this number consists in a random variation of the value of this
	 * number.
	 * @param bact the bacterium to mutate
	 * @return the new bacterium which is a mutated version of bact
	 */
	public Bacterium mutate(Bacterium bact) {
		int mut = ((DummyBacterium) bact).getNumber();
		int rand = (int) (Math.random()*5);
		if (Math.random()<0.5) {
			rand = -rand;
		}
		DummyBacterium dumBact = new DummyBacterium(mut + rand);
		return dumBact;
	}


}
