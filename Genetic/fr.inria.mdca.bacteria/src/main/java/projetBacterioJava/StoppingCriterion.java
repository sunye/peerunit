package projetBacterioJava;
/**
 * This abstract class have to be extended in order to the stopping criterion
 * class permit to stop the BacteriologicAlgorithm, depending on the implementation
 * of the run() function. For example, the stopping criterion can be based on
 * a fitness criterion.
 */


public abstract class StoppingCriterion {

	/**
	 * The object used to obtain informations from other classes
	 */
	protected BacteriologicAlgorithm bacteriologicAlgorithm;
	
	
	/**
	 * Function to implement
	 * @return true if the BacteriologicAlgorithm should stop its evolution
	 */
	public abstract boolean run();

	
	/**
	 * Sets the value of bacteriologicAlgorithm
	 * @param bactAlg the object used to obtain informations from other classes
	 */
	public void setBacteriologicAlgorithm(BacteriologicAlgorithm bactAlg) {
		bacteriologicAlgorithm = bactAlg;
	}
	
	

}
