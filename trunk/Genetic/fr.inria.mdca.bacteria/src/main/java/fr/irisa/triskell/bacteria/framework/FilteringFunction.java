/*
 * Created on 23 nov. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package fr.irisa.triskell.bacteria.framework;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/** * @author bbaudry *  * TODO To change the template for this generated type comment go to * Window - Preferences - Java - Code Style - Code Templates */
public class FilteringFunction {

	/*default behavior for the filtering function: it removes from the bacteriologic medium all bacteria that
	 * have a relative fitness equal to 0 
	 * */
	public void run() {
		HashMap relFitSet = bacteriologicAlgorithm.getFitnessFunction().getFitnessCache();
		Set bactSet = relFitSet.keySet();
		Iterator it = bactSet.iterator();
		while (it.hasNext()){
			Bacterium b = (Bacterium)it.next();
			Float relFit =(Float)relFitSet.get(b);
			if (relFit.floatValue()==0.0){
				bacteriologicAlgorithm.removeBacteriologicMedium(b);
			}
		}
	}

	/**
	 *  
	 * @uml.property name="bacteriologicAlgorithm"
	 * @uml.associationEnd multiplicity="(1 1)" inverse="filteringFunction:bacteriologicFramework.BacteriologicAlgorithm"
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
