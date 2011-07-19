package fr.inria.mdca.mba;

import java.util.Iterator;

import fr.inria.mdca.core.model.BaseInstance;

public abstract class MutationFunction {
	
	public int getBactNumber() {
		return bactNumber;
	}
	public void setBactNumber(int bactNumber) {
		this.bactNumber = bactNumber;
	}


	public float[] proba;
	
	public int bactNumber = 3;
	
	private BactereologicAlgorithm bactereologicAlgorithm;

	public void setBactereologicAlgorithm(BactereologicAlgorithm bactereologicAlgorithm) {
		this.bactereologicAlgorithm = bactereologicAlgorithm;
	}
	public BactereologicAlgorithm getBactereologicAlgorithm() {
		return bactereologicAlgorithm;
	}

	public abstract BaseInstance mutate(BaseInstance instance);
	
	private BaseInstance select (float[] proba){
		Iterator<BaseInstance> iterMedium = bactereologicAlgorithm.getMedium().iterator();
		int i = 0;
		float epsilon = 0.0001f;
		BaseInstance bResult = null;
		float r = (float) Math.random();
		if(r < epsilon) return (BaseInstance) iterMedium.next();
		float sommeCumul = 0;
		if (iterMedium.hasNext()){
			while (sommeCumul <= r-epsilon){
				sommeCumul += proba[i];
				i++;
				bResult = (BaseInstance) iterMedium.next();
			}
		}
		return bResult;
	}
	
	
	public void run() {
		proba = new float[bactereologicAlgorithm.getMedium().size()];
		float globalFitness = 0;
		for(BaseInstance i:bactereologicAlgorithm.getMedium()){
			globalFitness += bactereologicAlgorithm.getRelFitnessCash().get(i);
		}
		int j=0;
		for(BaseInstance i:bactereologicAlgorithm.getMedium()){
			float relFitBact = bactereologicAlgorithm.getRelFitnessCash().get(i);
			if (globalFitness == 0){proba[j] = 1.0f / (bactereologicAlgorithm.getMedium().size());}
			else {proba[j] = relFitBact / globalFitness;}
			j++;
		}
		j=0;
		while(j <= bactNumber){
			BaseInstance b = mutate(select(proba));
			bactereologicAlgorithm.getMedium().add(b);
			bactereologicAlgorithm.getRelFitnessCash().put(b, new Float(bactereologicAlgorithm.getFitnessFunction().relativeFitness(b)));
			j++;
		}
	}


}
