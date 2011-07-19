package Dummies;

import projetBacterioJava.Bacterium;

/**
 * The DummyBacterium Function is the class that extends the abstract class
 * Bacterium in order to define the object the bacteriologic algorithm will
 * treat, depending on the problem to solve. For this exemple, we have chosen
 * to represent the Bacterium by a number.
 */

public class DummyBacterium extends Bacterium {
	
	/**
	 * The only field of the class
	 */
	private int number;

	
	
	/**
	 * Constructor
	 * @param num the number representing the Bacterium
	 */
	public DummyBacterium(int num){
		number = num;
	}
	
	
	
	/**
	 * @return the number representing the Bacterium
	 */
	public int getNumber(){
		return number;
	}
	
	
	/**
	 * Overrides the toString() function of the java.lang.object class
	 * for debugging
	 */
	public String toString() {
		return (""+this.number);
	}
	

}
