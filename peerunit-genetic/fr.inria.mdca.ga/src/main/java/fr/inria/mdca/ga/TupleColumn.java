package fr.inria.mdca.ga;

import java.util.ArrayList;

import fr.inria.mdca.core.model.BaseInstance;


public class TupleColumn {
	
	private ArrayList<BaseInstance> instances;
	
	private float fitness;

	public void setFitness(float fitness) {
		this.fitness = fitness;
	}

	public float getFitness() {
		return fitness;
	}

	public void setInstances(ArrayList<BaseInstance> instances) {
		this.instances = instances;
	}

	public ArrayList<BaseInstance> getInstances() {
		if(this.instances==null)
			this.instances=new ArrayList<BaseInstance>();
		return instances;
	}
	
	public boolean equals(Object o){
		if(!(o instanceof TupleColumn)){
			return false;
		}
		TupleColumn c=(TupleColumn)o;
		if(getInstances().containsAll(c.getInstances())&&getInstances().size()==c.getInstances().size()){
			return true;
		}
		return false;
	}
	
	
}
