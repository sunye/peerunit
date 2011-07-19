/*
 * Created on 29 nov. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package testBacteriologicFramework;

import fr.irisa.triskell.bacteria.framework.Bacterium;

/**
 * @author bbaudry
 * 29 nov. 2004
 */
public class DummyBacterium extends Bacterium {

	/**
	 * 
	 * @uml.property name="number"
	 */
	private int number;

	
	/*pre cond: i>0*/
	public DummyBacterium( int i){
		number = i;
	}

	/**
	 * 
	 * @uml.property name="number"
	 */
	public int getNumber() {
		return number;
	}

}
