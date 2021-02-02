package fr.inria.mdca.util.pairwise;

import java.util.ArrayList;

import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.BaseInstance;
import uk.co.demon.mcdowella.algorithms.AllPairs;

public class PairwiseCalculator {
	
	private int[] choices;
	private long seed=42;
	private boolean shuffle=true;
	int[][] result;
	
	public ArrayList<BaseInstance> generatePairs(BaseModel model){
		this.setup(model);
		this.seed=(long) (Math.random()*100);
		AllPairs ap = new AllPairs(choices, seed, shuffle);
		result=ap.generateViaPrime();
		AllPairs.showResult(result, null);
		ArrayList<BaseInstance> instances=this.createInstance(model);
		return instances;
	}

	private void setup(BaseModel model) {
		int size=model.getElements().size();
		choices=new int[size];
		for(int i=0;i<size;i++){
			choices[i]=model.getElements().get(i).getElementsNum();
		}
	}
	
	private ArrayList<BaseInstance> createInstance(BaseModel model){
		ArrayList<BaseInstance> instances=new ArrayList<BaseInstance>();
		for(int i=0;i<this.result.length;i++){
			BaseInstance instance=new BaseInstance(model);
			for(int j=0;j<this.result[i].length;j++){
				instance.getValues()[j]=this.result[i][j];
			}
			instances.add(instance);
		}
		return instances;
	}
}
