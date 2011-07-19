package fr.inria.mdca.mba;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.util.ColumnCombinationBuilder;

public abstract class FitnessFunction {

	
	static Logger logger = Logger.getLogger(FitnessFunction.class);
	
	
	private BactereologicAlgorithm bactereologicAlgorithm;

	private  float solutionSetFitness;
	
	public void setBactereologicAlgorithm(BactereologicAlgorithm bactereologicAlgorithm) {
		this.bactereologicAlgorithm = bactereologicAlgorithm;
	}
	public BactereologicAlgorithm getBactereologicAlgorithm() {
		return bactereologicAlgorithm;
	}
	public void setSolutionSetFitness(float solutionSetFitness) {
		this.solutionSetFitness = solutionSetFitness;
	}
	public float getSolutionSetFitness() {
		return solutionSetFitness;
	}
	
	public void updateSolutionSetFitness() {
		solutionSetFitness = fitness(bactereologicAlgorithm.getSolution().getInstanceSet());
	}

	/**
	 * calculates the fitness of the whole set of instances. It searched before in the cache to 
	 * @param instances
	 * @return
	 */
	public abstract float fitness(ArrayList<BaseInstance> instances);
	public abstract float fitness(ArrayList<BaseInstance> instances,boolean b);
	
	/**
	 * calculates the fitness of the set taking into account the modification introduced by the element in the position index
	 * @param instanceSet set of instances containing the old solution
	 * @param index position of the new instance, or the modified instance
	 * @param b indicates if the fitness calculated should be stored in the memory
	 * @return
	 */
	public abstract float fitness(ArrayList<BaseInstance> instanceSet, int index,boolean b);

	
	public float relativeFitness(BaseInstance instance) {
		
		//	calcultate the fitness of only the last solution tuple-column
	
		bactereologicAlgorithm.getSolution().getInstanceSet().add(instance);
		float tempFitness = fitness(bactereologicAlgorithm.getSolution().getInstanceSet(),false);
		bactereologicAlgorithm.getSolution().getInstanceSet().remove(bactereologicAlgorithm.getSolution().getInstanceSet().size()-1);
		
		// float solFitness = fitness(bacteriologicAlgorithm.getSolutionSet());
		return (tempFitness - solutionSetFitness);
	}
	public void initialize() {
		logger.debug("Initializing instance solution set");
		int order=this.getBactereologicAlgorithm().getModel().getOrder();
		ArrayList<ArrayList<BaseInstance>> elements = ColumnCombinationBuilder.computeCombinations(this.getBactereologicAlgorithm().getMedium(), order);
		float ftl=-1;
		ArrayList<BaseInstance> crn=null;
		for(ArrayList<BaseInstance> instances:elements){
			float ftns=this.fitness(instances,false);
			if(ftns>ftl){
				ftl=ftns;
				crn=instances;
			}
		}
		ArrayList<BaseInstance> crn2=new ArrayList<BaseInstance>();
		for(BaseInstance c:crn){
			crn2.add(c.clone());
		}
		this.getBactereologicAlgorithm().getSolution().getInstanceSet().addAll(crn2);
	}
	public abstract void reset();

}
