package projetBacterioJava;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;



/**
 * Each time a new bacterium is generated, it is added in the bacteriologic medium.
 * This set grows along the algorithm execution, and it is necessary to remove bacteria 
 * at some time to control the memory space during the execution. On the other hand, 
 * useful bacteria should not be removed. The goal of the filtering function is to 
 * deal with the trade-off between the growing size of the medium and the usefulness
 * of bacteria. It removes bacteria which have a null fitness value.
 */


public class FilteringFunction {
	
	
	/**
	 * The object used to obtain informations from other classes
	 */
	private BacteriologicAlgorithm bacteriologicAlgorithm;
	

	/**
	 * The minimum size of the medium 
	 */
	private int minMedium;
	
	
	/**
	 * The maximum number of turns a bacterium can stand in the medium
	 */
	private int maxTurnNumber;
	
	
	/**
	 * The list which contains the 'minMedium' best Bacterium, ordered by their relativeFitness
	 */
	private Vector vectBestBact;
	
	
	
	private float bactRelFitMin;
	
	
	
	/**
	 * Constuctor
	 * @param bactAlg the object used to obtain informations from other classes
	 * @param theMinMedium the minimum size of the medium
	 */
	public FilteringFunction (BacteriologicAlgorithm bactAlg, int theMinMedium, int theMaxTurn, float theBactRelFitMin){
		bacteriologicAlgorithm = bactAlg;
		minMedium = theMinMedium;
		maxTurnNumber = theMaxTurn;
		bactRelFitMin = theBactRelFitMin;
	}

	
	/**
	 * The function that filters the medium set. If the relativeFitness of a bacterium
	 * is negative or null, or if the maxNumberTurn is reached by this bacterium, it is
	 * removed from the set (if the set size is superior than minMedium).
	 */
	public void run() {
		
		System.out.println("Medium before filtering : " + bacteriologicAlgorithm.getBacteriologicMedium().toString());
		bacteriologicAlgorithm.getFitnessFunction().updateSolutionSetFitness();
		
		HashMap cash = new HashMap();
		vectBestBact = new Vector();
		
		Iterator iter = bacteriologicAlgorithm.getBacteriologicMedium().iterator();
		while(iter.hasNext()) {
			Bacterium b = (Bacterium)iter.next();
			float bactRelFit = bacteriologicAlgorithm.getFitnessFunction().relativeFitness(b);
			//System.out.print("relativeFitness value of the bacterium " + b.toString() + " : " + bactRelFit);
			if ( bactRelFit <= bactRelFitMin || (b.getTurnNumber() >= maxTurnNumber)){
				//System.out.println("  --> removed");
				iter.remove();
			} else {
				//System.out.println();
				cash.put(b, new Float(bactRelFit));
			}
			if(cash.size()<minMedium) {
				addVectBest(b);
			}
			b.incTurnNumber();	
		}
		if(cash.size()<minMedium) {
			bacteriologicAlgorithm.getBacteriologicMedium().removeAllElements();
			cash = new HashMap();
			Iterator it = vectBestBact.iterator();
			while(it.hasNext()) {
				Bacterium b = (Bacterium)it.next();
				float bactRelFit = bacteriologicAlgorithm.getFitnessFunction().relativeFitness(b);
				cash.put(b, new Float(bactRelFit));
				bacteriologicAlgorithm.getBacteriologicMedium().add(b);
			}
		}
		
		bacteriologicAlgorithm.getFitnessFunction().setCash(cash);
		System.out.println("Medium after filtering : " + bacteriologicAlgorithm.getBacteriologicMedium().toString());
	
	}
	
	
	
	/**
	 * There is at least one element in the medium when using this function
	 * Updates the Vector that contains the best Bacteria
	 * @param bact
	 */
	public void addVectBest(Bacterium bact) {
		if(vectBestBact.size()<minMedium) {
			vectBestBact.add(bact);
		} else {
			Iterator iter = vectBestBact.iterator();
			Bacterium minFitnessBact = (Bacterium)iter.next();
			float minFitnessVect = bacteriologicAlgorithm.getFitnessFunction().relativeFitness(minFitnessBact);
			int i = 0;
			while(iter.hasNext()) {
				Bacterium currentBact = (Bacterium) iter.next();
				float currentFitness = bacteriologicAlgorithm.getFitnessFunction().relativeFitness(minFitnessBact);
				if(currentFitness < minFitnessVect) {
					minFitnessVect = currentFitness;
					minFitnessBact = currentBact;
					i++;
				}	
			}
			float relFitBact = bacteriologicAlgorithm.getFitnessFunction().relativeFitness(bact);
			if(relFitBact<minFitnessVect) {
				vectBestBact.removeElementAt(i);
				vectBestBact.add(bact);
			}
			
		}		

	}
	
	
	/*
	System.out.println("Medium before filtering : " + bacteriologicAlgorithm.getBacteriologicMedium().toString());
	bacteriologicAlgorithm.getFitnessFunction().updateSolutionSetFitness();
	
	HashMap cash = new HashMap();
	
	Iterator iter = bacteriologicAlgorithm.getBacteriologicMedium().iterator();
	while(iter.hasNext()) {
		Bacterium b = (Bacterium)iter.next();
		float bactRelFit = bacteriologicAlgorithm.getFitnessFunction().relativeFitness(b);
		if (bacteriologicAlgorithm.getBacteriologicMedium().size() > minMedium){
			System.out.print("relativeFitness value of the bacterium " + b.toString() + " : " + bactRelFit);
			if ( bactRelFit <= 300 || (b.getTurnNumber() >= maxTurnNumber)){
				System.out.println("  --> removed");
				iter.remove();
			}
		}
		System.out.println();
		cash.put(b, new Float(bactRelFit));
		b.incTurnNumber();				
	}
	bacteriologicAlgorithm.getFitnessFunction().setCash(cash);
	System.out.println("Medium after filtering : " + bacteriologicAlgorithm.getBacteriologicMedium().toString());
*/
}
