package projetBacterioJava;
import java.util.Iterator;



/**
 * The MutationFunction class generates a new bacterium by slightly altering an ancestor.
 * It is crucial for the algorithm, since it is the one that creates new information in the process.
 */
public abstract class MutationFunction {

	
	/**
	 * The array where are put all the probabilities
	 */
	public float[] proba;
	
	/**
	 * The object used to obtain informations from other classes
	 */
	private BacteriologicAlgorithm bacteriologicAlgorithm;
	
	
	/**
	 * The number of bacteria to alter
	 */
	public int bactNumber = 3;
	
	
	/**
	 * Default constructor
	 */
	public MutationFunction () {
	}
	
	
	/**
	 * Constructor
	 * @param theBactNumber the number of bacteria to alter
	 */
	public MutationFunction (int theBactNumber) {
		bactNumber = theBactNumber;
	}
	
	
	/**
	 * Function to implement
	 * @param bact the bacterium to mutate
	 * @return the new bacterium which is a mutated version of bact
	 */
	public abstract Bacterium mutate (Bacterium bact);
	
	
	/**
	 * Set the value of bacteriologicAlgorithm
	 * @param bactAlg the object used to obtain informations from other classes
	 */
	public void setBacteriologicAlgorithm(BacteriologicAlgorithm bactAlg) {
		bacteriologicAlgorithm = bactAlg;
	}
	
	
	/**
	 * Set the value of bactNumber
	 * @param theBactNumber the number of bacteria to alter
	 */
	public void setBactNumber(int theBactNumber) {
		bactNumber = theBactNumber;
	}
	
	
	/**
	 * Select a Bacterium among the medium set of bacteria following the probability
	 * for each to be selected.
	 * @param proba the array composed by the probability for each Bacterium to be selected
	 * @return the selected Bacterium
	 */
	private Bacterium select (float[] proba){
		Iterator iterMedium = bacteriologicAlgorithm.getBacteriologicMedium().iterator();
		int i = 0;
		float epsilon = 0.0001f;
		Bacterium bResult = null;
		float r = (float) Math.random();
		if(r < epsilon) return (Bacterium) iterMedium.next();
		float sommeCumul = 0;
		if (iterMedium.hasNext()){
			while (sommeCumul <= r-epsilon){
				sommeCumul += proba[i];
				i++;
				bResult = (Bacterium) iterMedium.next();
			}
		}
		return bResult;
	}
	

	/**
	 * Calculate the sum of the relativeFitnesses of the bacteria present in the medium set,
	 * and thus permits to compute the probability for each Bacterium to be selected.
	 * Then a Bacterium is selected from the medium set and is mutated. The mutant is added
	 * to the medium set. "bactNumber" bacteria are mutated.
	 */
	public void generate () {
		System.out.println("Medium before mutation : " +bacteriologicAlgorithm.getBacteriologicMedium().toString());
		float globalFitness = 0;
		int i = 0;
		int j = 1;
		proba = new float[bacteriologicAlgorithm.getBacteriologicMedium().size()];
		Iterator iterCumul = bacteriologicAlgorithm.getBacteriologicMedium().iterator();
		while(iterCumul.hasNext()){
			Bacterium b = (Bacterium) iterCumul.next();
			globalFitness += Float.parseFloat(((Float) bacteriologicAlgorithm.getFitnessFunction().cash.get(b)).toString());
		}
		Iterator iterProba = bacteriologicAlgorithm.getBacteriologicMedium().iterator();
		while(iterProba.hasNext()){
			Bacterium b = (Bacterium) iterProba.next();
			float relFitBact = Float.parseFloat(((Float) bacteriologicAlgorithm.getFitnessFunction().cash.get(b)).toString());
			if (globalFitness == 0){proba[i] = 1.0f / (bacteriologicAlgorithm.getBacteriologicMedium().size());}
			else {proba[i] = relFitBact / globalFitness;}
			//System.out.println("The probability for the Bacterium " + b.toString() + " to be selected in order to create a mutant : " + proba[i]);
			i++;			
		}
		while(j <= bactNumber){
			Bacterium b = mutate(select(proba));
			bacteriologicAlgorithm.getBacteriologicMedium().add(b);
			bacteriologicAlgorithm.getFitnessFunction().cash.put(b, new Float(bacteriologicAlgorithm.getFitnessFunction().relativeFitness(b)));
			j++;
		}
		System.out.println("Medium after mutation : " + bacteriologicAlgorithm.getBacteriologicMedium().toString());
	}
	
	
	
	/**
	 * Call the generate() function
	 */
	public void run(){
		if ( !(bacteriologicAlgorithm.getBacteriologicMedium().isEmpty()) ) {
			generate();
		}
	}


	
}
