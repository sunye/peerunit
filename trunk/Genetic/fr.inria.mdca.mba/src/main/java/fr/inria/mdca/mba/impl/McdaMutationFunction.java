package fr.inria.mdca.mba.impl;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.mba.MutationFunction;
import fr.inria.mdca.util.RandomHelper;

public class McdaMutationFunction extends MutationFunction {

	private float mutationProb;

	public float getMutationProb() {
		return mutationProb;
	}

	public void setMutationProb(float mutationProb) {
		this.mutationProb = mutationProb;
	}

	@Override
	public BaseInstance mutate(BaseInstance instance0) {
		BaseModel model = super.getBactereologicAlgorithm().getModel();	
	
		BaseInstance instance=instance0.clone();
		int[] ixs = instance.getValues().clone();
		
	/*	int mutate=RandomHelper.randomValue(1, model.getElements().size()-1);
		int ub=model.getElements().get(mutate).getElementsNum();
		int v=RandomHelper.randomValue(1, ub);
		instance.getValues()[mutate]=v;
	*/	
		
		int cnt=0;
		for(int j=0;j<ixs.length;j++){
			float r = (float) Math.random();
			if(r > mutationProb){
				int ub=model.getElements().get(j).getElementsNum();
				int v=RandomHelper.randomValue(1, ub);
				while(v==instance.getValues()[j]){
					v=RandomHelper.randomValue(1, ub);
				}
				instance.getValues()[j]=v;
				cnt++;
			}
		}
		if(cnt==0){
			int j=RandomHelper.randomValue(1, model.getElements().size()-1);
			int ub=model.getElements().get(j).getElementsNum();
			int v=RandomHelper.randomValue(1, ub);
			while(v==instance.getValues()[j]){
				v=RandomHelper.randomValue(1, ub);
			}
			instance.getValues()[j]=v;
		}
		
		return instance;
	}

}
