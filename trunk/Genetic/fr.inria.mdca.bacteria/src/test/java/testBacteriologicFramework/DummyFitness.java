/*
 * Created on 29 nov. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package testBacteriologicFramework;

import fr.irisa.triskell.bacteria.framework.Bacterium;
import fr.irisa.triskell.bacteria.framework.FitnessFunction;

/**
 * @author bbaudry
 * 29 nov. 2004
 */
public class DummyFitness extends FitnessFunction {
	
	public DummyFitness(){		
	}

	/* (non-Javadoc)
	 * @see bacteriologicFramework.FitnessFunction#fitness(bacteriologicFramework.Bacterium[])
	 */
	public float fitness(Bacterium[] medium) {
		int sum = 0;
		for (int i = 0; i<medium.length; i++){
			sum = sum + ((DummyBacterium)medium[i]).getNumber();
		}
		// TODO Auto-generated method stub
		return (float)sum;
	}

}
