/*
 * Created on 29 nov. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package fr.irisa.triskell.bacteria.framework;

import java.util.Comparator;

/** * @author bbaudry * 29 nov. 2004 */
public class BacteriaComparator implements Comparator {

	/**
	 * @param bacteriologicAlgorithm
	 */
	public BacteriaComparator(BacteriologicAlgorithm bacteriologicAlgorithm) {
		super();
		this.bacteriologicAlgorithm = bacteriologicAlgorithm;
	}
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object arg0, Object arg1) {
		Bacterium bact0 = (Bacterium)arg0;
		Bacterium bact1 = (Bacterium)arg1;
		if (this.bacteriologicAlgorithm.getFitnessFunction().relativeFitness(bact0)<this.bacteriologicAlgorithm.getFitnessFunction().relativeFitness(bact1))
			return -1;
		else
			if (this.bacteriologicAlgorithm.getFitnessFunction().relativeFitness(bact0)>this.bacteriologicAlgorithm.getFitnessFunction().relativeFitness(bact1))
				return 1;
			else return 0;
	}

	/**
	 *  
	 * @uml.property name="bacteriologicAlgorithm"
	 * @uml.associationEnd multiplicity="(1 1)" aggregation="composite" inverse="bacteriaComparator:bacteriologicFramework.BacteriologicAlgorithm"
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
	 *  
	 * @uml.property name="bacteriologicAlgorithm"
	 */
	public void setBacteriologicAlgorithm(
		BacteriologicAlgorithm bacteriologicAlgorithm) {
		this.bacteriologicAlgorithm = bacteriologicAlgorithm;
	}

}
