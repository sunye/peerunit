package fr.inria.bacteria.impl;

import java.util.ArrayList;

import fr.inria.bacteria.IBacterium;
import fr.inria.bacteria.IObjectiveValue;

/**

 * @author freddy
 *
 */
public abstract class AbstractBacterium implements IBacterium {

	private double fitness;
	
	private ArrayList<IObjectiveValue> objectives;
	
	public double getFitness() {
		return fitness;
	}
	/**
	 * typically a bacterium has only 1 fitness value and no objectives, however, in some cases is
	 * desirable a single bacterium to have multiple objectives
	 */
	public ArrayList<IObjectiveValue> getObjectives() {
		if(this.objectives==null){
			this.objectives=new ArrayList<IObjectiveValue>();
		}
		return this.objectives;
	}

	public void setFitness(double fitness) {
		this.fitness=fitness;
	}
	
	public abstract IBacterium clone();
}
