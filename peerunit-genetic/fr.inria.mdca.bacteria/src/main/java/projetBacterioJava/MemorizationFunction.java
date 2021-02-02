package projetBacterioJava;
import java.util.Iterator;
import java.util.Vector;



/**
 * The MemorizationFunction class permit to choose the bacterium in a set that have
 * the best fitness value. If this value is good enough (depending on the memorization
 * threshold), the bacterium is memorized (it is added to a result set)
 */

public class MemorizationFunction {
	
	
	/**
	 * The object used to obtain informations from other classes
	 */
	private BacteriologicAlgorithm bacteriologicAlgorithm;
	
	
	/**
	 * The minimum value the relative fitness of a Bacterium must
	 * have to be considered
	 */
	private float memorizationThreshold;
	

	/**
	 * Sets the value of memorizationThreshold with a new one
	 * @param paramMemorizationThreshold the new value for memorizationThreshold
	 */
	public void setMemorizationThreshold(int paramMemorizationThreshold) {
		memorizationThreshold = paramMemorizationThreshold;
	}
	
	
	/**
	 * Constructor
	 * @param bactAlg the object used to obtain informations from other classes
	 * @param theMemorizationThreshold the minimum value that the fitness of a set must have
	 */
	public MemorizationFunction (BacteriologicAlgorithm bactAlg, float theMemorizationThreshold){
		bacteriologicAlgorithm = bactAlg;
		memorizationThreshold = theMemorizationThreshold;
	}
	
	
	/**
	 * Determines the bacterium of a set that have the best relativeFitness
	 * @param vect the vector that must be iterated
	 * @return the Bacterium of the set that have the best relativeFitness
	 */
	public Bacterium maxFitness (Vector vect) {
		if (vect.isEmpty()){return null;}
		
		float currentMax;
		Iterator iter = vect.iterator();
		Bacterium currentMaxBact, currentBact;
		
		currentBact = (Bacterium) iter.next();
		currentMaxBact = currentBact;
		currentMax = Float.parseFloat(((Float) bacteriologicAlgorithm.getFitnessFunction().cash.get(currentBact)).toString());
		while (iter.hasNext()) {
			currentBact = (Bacterium) iter.next();
			float temp = Float.parseFloat(((Float) bacteriologicAlgorithm.getFitnessFunction().cash.get(currentBact)).toString());
			if (temp > currentMax) {
				currentMax = temp;
				currentMaxBact = currentBact;
			}
		}
		return currentMaxBact;
	}
	
	

	/**
	 * Add the best Bacterium of the medium into the solution set in order to keep it.
	 * If the bacterium doesn't improve the solution set enough, it is removed from it.
	 */
	public void run (){
		if (! this.bacteriologicAlgorithm.getBacteriologicMedium().isEmpty()) {
			System.out.println("The solution set before memorization : " + bacteriologicAlgorithm.getSolutionSet().toString());
			Bacterium b = maxFitness(bacteriologicAlgorithm.getBacteriologicMedium());
			System.out.println("The Bacterium having the best relative fitness : " + b.toString());
			float mem=(Float.parseFloat(((Float) bacteriologicAlgorithm.getFitnessFunction().cash.get(b)).toString())) ;
			System.out.println("Memorization step: "+mem);
			if (mem> memorizationThreshold){
				bacteriologicAlgorithm.getSolutionSet().add(b);
			}
		}
		System.out.println("The solution set after memorization : " + bacteriologicAlgorithm.getSolutionSet().toString());
		System.out.println("Medium size : " + bacteriologicAlgorithm.getBacteriologicMedium().size());
		System.out.println("Solution set size : " + bacteriologicAlgorithm.getSolutionSet().size());
	}

	
}
