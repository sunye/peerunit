/*
 * Created on 23 nov. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package fr.irisa.triskell.bacteria.framework;

/** * @author bbaudry *  * TODO To change the template for this generated type comment go to * Window - Preferences - Java - Code Style - Code Templates */
public abstract class MutationFunction {

	public abstract void run();

	/**
	 *  
	 * @uml.property name="bacteriologicAlgorithm"
	 * @uml.associationEnd multiplicity="(1 1)" inverse="mutationFunction:bacteriologicFramework.BacteriologicAlgorithm"
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
