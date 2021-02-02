/*
 * Created on 23 nov. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package fr.irisa.triskell.bacteria.framework;

import java.util.Iterator;

/**
 * @author bbaudry *  * TODO To change the template for this generated type comment go to * Window - Preferences - Java - Code Style - Code Templates
 * 
 * @uml.dependency supplier="bacteriologicFramework.FitnessFunction"
 */

public class MemorizationFunction {

	public void run(Bacterium[] medium) {
		Bacterium bestBacterium = this.selectBest(medium);
		bacteriologicAlgorithm.addSolutionSet(bestBacterium);
		bacteriologicAlgorithm.removeBacteriologicMedium(bestBacterium);
		bacteriologicAlgorithm.notifySolutionSetChange(this.bacteriologicAlgorithm);
	}

	/**
	 *  
	 * @uml.property name="bacteriologicAlgorithm"
	 * @uml.associationEnd multiplicity="(1 1)" inverse="memorizationFunction:bacteriologicFramework.BacteriologicAlgorithm"
	 */
	private BacteriologicAlgorithm bacteriologicAlgorithm;
	
	/**
	 *  
	 * @uml.property name="bacteriologicAlgorithm"
	 */
	public BacteriologicAlgorithm getBacteriologicAlgorithm() {
		return bacteriologicAlgorithm;
	}

	/**
	 *  This method is called by the creator by the creator of BacteriologicAlgorithm to initialize
	 * the link.
	 * @uml.property name="bacteriologicAlgorithm"
	 */
	public void setBacteriologicAlgorithm(
		BacteriologicAlgorithm bacteriologicAlgorithm) {
		this.bacteriologicAlgorithm = bacteriologicAlgorithm;
	}

	/**
	 * This a default implementation of selectBest which selects the best bacterium in the medium which 
	 * fitness is greater than the memorization threshold
	 * @param medium : the bacteriologic medium from which the best bacterium is selected
	 * @param mediumFitness : the fitness of the medium
	 * @return the best bacterium in the medium
	 */
	private Bacterium selectBest(Bacterium[] medium) {
		Iterator iterator = bacteriologicAlgorithm
			.bacteriologicMediumIterator();
		float bestRelFitness = this.memorizationThreshold;
		Bacterium bestBacterium = null;
		while (iterator.hasNext()) {
			Bacterium b = (Bacterium) iterator.next();
			float fitness = bacteriologicAlgorithm.getFitnessFunction()
				.relativeFitness(b);
			if (fitness > bestRelFitness) {
				bestRelFitness = fitness;
				bestBacterium = b;
			}
		}
		return bestBacterium;
	}

	/**
	 *  
	 * @uml.property name="memorizationThreshold"
	 */
	private float memorizationThreshold;

	/**
	 *  
	 * @uml.property name="memorizationThreshold"
	 */
	public float getMemorizationThreshold() {
		return memorizationThreshold;
	}

	/**
	 *  
	 * @uml.property name="memorizationThreshold"
	 */
	public void setMemorizationThreshold(float memorizationThreshold) {
		this.memorizationThreshold = memorizationThreshold;
	}

	public MemorizationFunction(float memorizationThreshold) {
		this.memorizationThreshold = memorizationThreshold;
	}

}
