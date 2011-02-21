package fr.inria.mdca.ga;

import java.util.ArrayList;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.Tuple;
import fr.inria.mdca.util.TupleBuilder;

public class FitnessFunction {
	
	private BaseModel model;
	
	private ArrayList<Tuple> tuples=new ArrayList<Tuple>();
	public  float fitness(ArrayList<BaseInstance> instances){
		float fitness=0;

		int order=model.getOrder();
		for(int i=0;i<instances.size();i++){
			if(i<=instances.size()-order)
				fitness+=this.fitness(instances, i);
		}
		this.tuples.clear();
		return fitness;
	}
	public  float fitness(ArrayList<BaseInstance> instanceSet, int index){
		int order=model.getOrder();
		int twise=model.getTwise();
		
		float fitness=0;
		
		/*
		 * for each case, the index can modify in different ways the score, this depends on the order of the transitions
		 */
		ArrayList<BaseInstance> instances=new ArrayList<BaseInstance>();
		for(int i=index;i<order+index;i++){
			instances.add(instanceSet.get(i));
		}
		ArrayList<Tuple> tuples=TupleBuilder.buildTuples(instances, twise);
		for(Tuple t:tuples){
			if(!this.tuples.contains(t)){
				this.tuples.add(t);
				fitness++;
			}
		}
		return fitness;
	}

	public void setModel(BaseModel model) {
		this.model = model;
	}

	public BaseModel getModel() {
		return model;
	}

}
