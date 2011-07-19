
package projetBacterioJava;
/**
 *  This abstract class have to be extended in order to	define
 *  the object the bacteriologic algorithm will treat.
 * 	A bacterium represents the atomic element of the algorithm.
 */


public abstract class Bacterium {

	/**
	 * Number of turns of BacteriologicAlgorithm the bacterium has been in the medium
	 */
	protected int turnNumber = 0;
	
	
	/**
	 * @return the number of turms teh bacterium has already done
	 */
	public int getTurnNumber(){
		return turnNumber;
	}
	
	/**
	 * Increments the field turnNumber of the Bacterium
	 */
	public void incTurnNumber(){
		turnNumber++;
	}
	
}
