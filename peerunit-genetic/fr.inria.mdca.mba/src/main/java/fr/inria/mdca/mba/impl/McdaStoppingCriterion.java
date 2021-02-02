package fr.inria.mdca.mba.impl;

import org.apache.log4j.Logger;

import fr.inria.mdca.mba.StoppingCriterion;

public class McdaStoppingCriterion extends StoppingCriterion {

	private float expectedFitness=0;
	
	static Logger logger = Logger.getLogger(McdaStoppingCriterion.class);
	
	public McdaStoppingCriterion(float expectedFitness) {
		super();
		this.expectedFitness = expectedFitness;
	}

	public float getExpectedFitness() {
		return expectedFitness;
	}

	public void setExpectedFitness(float expectedFitness) {
		this.expectedFitness = expectedFitness;
	}

	@Override
	public boolean run() {
		float f=super.getBactereologicAlgorithm().getBestFitness();
		
		logger.debug("must stop: "+(f==expectedFitness)+" = "+f+"/"+expectedFitness);
		return f==expectedFitness;
	}

}
