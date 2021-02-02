package fr.inria.mdca.mba.impl;

import java.util.ArrayList;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.Tuple;
import fr.inria.mdca.mba.FitnessFunction;
import fr.inria.mdca.util.TupleBuilder;

public class McdaFitnessFunction extends FitnessFunction{

	private ArrayList<Tuple> tuples=new ArrayList<Tuple>();

	@Override
	public  float fitness(ArrayList<BaseInstance> instances){
		return this.fitness(instances,true);
	}
	public  float fitness(ArrayList<BaseInstance> instances,boolean b){
		float fitness=0;

		int order=super.getBactereologicAlgorithm().getModel().getOrder();
		
		for(int i=0;i<instances.size();i++){
			if(i<=instances.size()-order)
				fitness+=this.fitness(instances, i,b);
		}
		/**/
		this.reset();
		/**/
		return fitness;
	}


	/**
	 * calculates the fitness of the set taking into account the modification introduced by the element in the position index.
	 * @param instanceSet set of instances containing the old solution
	 * @param index position of the new instance, or the modified instance
	 * @param b 
	 * @return
	 */
	@Override
	public  float fitness(ArrayList<BaseInstance> instanceSet, int index, boolean b){
		/*
		 * 
		 * |   |   |   |   | 
		 *   ^ 0000
		 *   
		 *       ^ 
		 *           ^ 
		 *               ^ the last (and may be the first) element of the instance set has the problem that it belongs twice to the same tuplecolumn
		 */
		int order=super.getBactereologicAlgorithm().getModel().getOrder();
		int twise=super.getBactereologicAlgorithm().getModel().getTwise();
		
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
	@Override
	public void reset() {
		this.tuples.clear();
	}

}
