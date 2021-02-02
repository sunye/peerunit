package projetBacterioJava;
import java.util.HashMap;
import java.util.Vector;


/**
 * The fitness function evaluates the quality of a solution regarding the global objective.
 * Since a solution for the bacteriologic algorithm is a set of a bacteria, the fitness 
 * function computes the fitness of a set of bacteria. The quality of a bacterium at a given
 * moment corresponds to the fitness value the solution set would have if this bacterium was added.
 * The fitness function for a bacterium is called relativeFitness and is computed to the fitness
 * of a solution set.
 */


public abstract class FitnessFunction {

	
	/**
	 * The object used to obtain informations from other classes
	 */
	protected BacteriologicAlgorithm bacteriologicAlgorithm;
	
	protected float solutionSetFitness;
	
	/**
	 * 
	 */
	protected HashMap cash;
	
	public void setCash(HashMap aCash) {
		cash = aCash;
	}
	
	/**
	 * Sets the value of bacteriologicAlgorithm
	 * @param bactAlg the object used to obtain informations from other classes
	 */
	public void setBacteriologicAlgorithm(BacteriologicAlgorithm bactAlg) {
		bacteriologicAlgorithm = bactAlg;
	}
	
	public void updateSolutionSetFitness() {
		solutionSetFitness = fitness(bacteriologicAlgorithm.getSolutionSet());
	}
	
	/**
	 * Function to implement
	 * @param vectBact the set to evaluate
	 * @return the quality of the set relatively to the problem
	 */
	public abstract float fitness(Vector vectBact);
	
	
	
	
	/**
	 * The relativeFitness is the difference between the fitness value the solution set would have 
	 * if a bacterium was added and the fitness value of the solution set.
	 * relativeFitness(solutionSet, Bacterium) = Fitness(solutionSet + Bacterium) - Fitness(solutionSet)
	 * @param bact the Bacterium added to the solution set
	 * @return the usefulness of the bacterium relatively to the solution set
	 */
	public float relativeFitness(Bacterium bact) {
		bacteriologicAlgorithm.getSolutionSet().add(bact);
		float tempFitness = fitness(bacteriologicAlgorithm.getSolutionSet());
		bacteriologicAlgorithm.getSolutionSet().removeElementAt(bacteriologicAlgorithm.getSolutionSet().size() - 1);
		// float solFitness = fitness(bacteriologicAlgorithm.getSolutionSet());
		return (tempFitness - solutionSetFitness);
	}
	
}
