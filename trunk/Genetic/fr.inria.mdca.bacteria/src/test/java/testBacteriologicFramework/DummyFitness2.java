/*
 * Created on 22 déc. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package testBacteriologicFramework;

import java.util.ArrayList;

import fr.irisa.triskell.bacteria.framework.Bacterium;
import fr.irisa.triskell.bacteria.framework.FitnessFunction;


/**
 * @author bbaudry
 * 22 déc. 2004
 */
public class DummyFitness2 extends FitnessFunction {


	/* this method computes the fitness value for a medium
	 * the bacteria in the medium all have one integer value
	 * the method sums the values of all bacteria that have a different value (if ther a two bacteria with the same 
	 * value, this value is added only once in the computation of the fitness)
	 * 
	 * remark: this fitness function is useful for testing the filtering function because it is possible to have
	 * bacteria with a relative fitness equal to 0 (a bacterium which has a value equal to the value of a bacterium
	 * already present in the solution set) 
	 * (non-Javadoc)
	 * @see bacteriologicFramework.FitnessFunction#fitness(bacteriologicFramework.Bacterium[])
	 */
	public float fitness(Bacterium[] medium) {
		
		//this list stores the values that already have been summed 
		ArrayList usefulBacteria = new ArrayList();
	
		int sum = 0;
		for (int i = 0; i<medium.length; i++){
			DummyBacterium currentBact = (DummyBacterium)medium[i];
			int bactVal = (currentBact).getNumber();
			if (!usefulBacteria.contains(new Integer(bactVal))){
				usefulBacteria.add(new Integer(bactVal));
				sum = sum + bactVal;
			}			
		}
		return (float)sum;
	}

}
