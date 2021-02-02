package fr.inria.triskell.moga.jgap;

import org.jgap.FitnessEvaluator;
import org.jgap.IChromosome;

public class SPEA2FitnessEvaluator implements FitnessEvaluator {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3704612713181192988L;

	public boolean isFitter(double arg0, double arg1) {
		return arg0 > arg1;
	}

	public boolean isFitter(IChromosome arg0, IChromosome arg1) {
		// TODO Auto-generated method stub
		return false;
	}

}
