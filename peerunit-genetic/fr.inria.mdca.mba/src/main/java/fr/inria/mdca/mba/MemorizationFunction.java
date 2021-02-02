package fr.inria.mdca.mba;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import fr.inria.mdca.core.model.BaseInstance;

public class MemorizationFunction {

	private BactereologicAlgorithm bactereologicAlgorithm;
	private float memorizationThreshold;
	
	public float getMemorizationThreshold() {
		return memorizationThreshold;
	}

	public void setMemorizationThreshold(float memorizationThreshold) {
		this.memorizationThreshold = memorizationThreshold;
	}

	static Logger logger = LogManager.getLogger(MemorizationFunction.class);
	
	public void run() {
		if(!this.bactereologicAlgorithm.getMedium().isEmpty()){
			logger.debug("The solution set before memorization : " + this.bactereologicAlgorithm.getSolution().toString());
			
			BaseInstance b = maxFitness(bactereologicAlgorithm.getMedium());
			
			logger.debug("The Bacterium having the best relative fitness : " + b.toString());
			
			float mem=this.bactereologicAlgorithm.getRelFitnessCash().get(b);
			
			logger.debug("Memorization step: "+mem);
			
			if (mem> memorizationThreshold){
				this.bactereologicAlgorithm.getSolution().getInstanceSet().add(b.clone());
			}
			
			this.bactereologicAlgorithm.setBestFitness(this.bactereologicAlgorithm.getFitnessFunction().fitness(this.bactereologicAlgorithm.getSolution().getInstanceSet()));
			
			logger.debug("The solution set after memorization : " + this.bactereologicAlgorithm.getSolution().toString());
			logger.debug("Medium size : " + this.bactereologicAlgorithm.getMedium().size());
			logger.debug("Solution set size : " + this.bactereologicAlgorithm.getSolution().getInstanceSet().size());
			logger.debug("Solution set fitness : " + this.bactereologicAlgorithm.getBestFitness());
		}
	}
	
	public BaseInstance maxFitness(ArrayList<BaseInstance> instances){
		if (instances.isEmpty()){return null;}
		float minFitnessVect=-11000;
		BaseInstance maxFitnessBact=null;
		for(BaseInstance i:instances){
			float currentFitness  = bactereologicAlgorithm.getFitnessFunction().relativeFitness(i);
			if(currentFitness > minFitnessVect) {
				minFitnessVect = currentFitness;
				maxFitnessBact = i;
			}
		}
		
		return maxFitnessBact;
	}


	public void setBactereologicAlgorithm(
			BactereologicAlgorithm bactereologicAlgorithm) {
		this.bactereologicAlgorithm=bactereologicAlgorithm;
		
	}

	public BactereologicAlgorithm getBactereologicAlgorithm() {
		return bactereologicAlgorithm;
	}
	
}
