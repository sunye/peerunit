package fr.inria.bacteria.impl;

import java.util.ArrayList;

import fr.inria.bacteria.IBacterium;
import fr.inria.bacteria.IObjectiveValue;
import fr.inria.bacteria.ISolution;

public abstract class AbstractSolution implements ISolution {

	private double fitness;

	private ArrayList<IObjectiveValue> objectives;
	
	private ArrayList<IBacterium> solutionSet;
	
	public double getFitness() {
		return fitness;
	}

	public ArrayList<IBacterium> getSolution() {
		if(solutionSet==null){
			this.solutionSet=new ArrayList<IBacterium>();
		}
		return solutionSet;
	}

	public void setFitness(double fitness) {
		this.fitness=fitness;
	}

	public ArrayList<IObjectiveValue> getObjectives() {
		if(this.objectives==null){
			this.objectives=new ArrayList<IObjectiveValue>();
		}
		return this.objectives;
	}

	public abstract ISolution clone();
}
