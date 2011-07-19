/*
 * Created on 22 nov. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package fr.irisa.triskell.bacteria.framework;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/** * @author bbaudry *  *  * Window - Preferences - Java - Code Style - Code Templates */

public class BacteriologicAlgorithm extends Subject {



	/**
	 *  
	 * @uml.property name="bacteriaComparator"
	 * @uml.associationEnd multiplicity="(0 1)" inverse="bacteriologicAlgorithm:bacteriologicFramework.BacteriaComparator"
	 */
	private BacteriaComparator bacteriaComparator;

	/**
	 *  
	 * @uml.property name="bacteriologicMedium"
	 * @uml.associationEnd multiplicity="(0 -1)" ordering="ordered" aggregation="aggregate" elementType="bacteriologicFramework.Bacterium"
	 */
	private ArrayList bacteriologicMedium;

	/**
	 *  
	 * @uml.property name="filteringFunction"
	 * @uml.associationEnd multiplicity="(1 1)" aggregation="composite" inverse="bacteriologicAlgorithm:bacteriologicFramework.FilteringFunction"
	 */
	private FilteringFunction filteringFunction;

	/**
	 *  
	 * @uml.property name="fitnessFunction"
	 * @uml.associationEnd multiplicity="(1 1)" aggregation="composite" inverse="bacteriologicAlgorithm:bacteriologicFramework.FitnessFunction"
	 */
	private FitnessFunction fitnessFunction;

	/**
	 *  
	 * @uml.property name="memorizationFunction"
	 * @uml.associationEnd multiplicity="(1 1)" aggregation="composite" inverse="bacteriologicAlgorithm:bacteriologicFramework.MemorizationFunction"
	 */
	private MemorizationFunction memorizationFunction;

	/**
	 *  
	 * @uml.property name="mutationFunction"
	 * @uml.associationEnd multiplicity="(1 1)" aggregation="composite" inverse="bacteriologicAlgorithm:bacteriologicFramework.MutationFunction"
	 */
	private MutationFunction mutationFunction;

	/**
	 *  
	 * @uml.property name="solutionSet"
	 * @uml.associationEnd multiplicity="(0 -1)" ordering="ordered" aggregation="aggregate" elementType="bacteriologicFramework.Bacterium"
	 */
	private ArrayList solutionSet;

	/**
	 *  
	 * @uml.property name="stoppingCriterion"
	 * @uml.associationEnd multiplicity="(1 1)" aggregation="composite" inverse="bacteriologicAlgorithm:bacteriologicFramework.StoppingCriterion"
	 */
	private StoppingCriterion stoppingCriterion;

	public BacteriologicAlgorithm(
		ArrayList medium,
		FitnessFunction fitnessFunction,
		MemorizationFunction memorizationFunction,
		MutationFunction mutationFunction,
		FilteringFunction filteringFunction,
		StoppingCriterion stoppingCriterion) 
	{
		this.bacteriologicMedium = medium;
		this.fitnessFunction = fitnessFunction;
		fitnessFunction.setBacteriologicAlgorithm(this);
		this.memorizationFunction = memorizationFunction;
		memorizationFunction.setBacteriologicAlgorithm(this);
		this.mutationFunction = mutationFunction;
		this.filteringFunction = filteringFunction;
		filteringFunction.setBacteriologicAlgorithm(this);
		this.stoppingCriterion = stoppingCriterion;
		this.bacteriaComparator = new BacteriaComparator(this);
		this.solutionSet = new ArrayList();
		notifySolutionSetChange(this);
	}

	/**
	 * 
	 * @uml.property name="bacteriologicMedium"
	 */
	public boolean addBacteriologicMedium(
		fr.irisa.triskell.bacteria.framework.Bacterium element) {
		return bacteriologicMedium.add(element);
	}

	/**
	 * 
	 * @uml.property name="solutionSet"
	 */
	public boolean addSolutionSet(fr.irisa.triskell.bacteria.framework.Bacterium element) {
		return solutionSet.add(element);
	}

	/**
	 * 
	 * @uml.property name="bacteriologicMedium"
	 */
	public Iterator bacteriologicMediumIterator() {
		return bacteriologicMedium.iterator();
	}

	/**
	 * 
	 * @uml.property name="bacteriologicMedium"
	 */
	public int bacteriologicMediumSize() {
		return bacteriologicMedium.size();
	}

	/**
	 * 
	 * @uml.property name="bacteriologicMedium"
	 */
	public fr.irisa.triskell.bacteria.framework.Bacterium[] bacteriologicMediumToArray() {
		return (fr.irisa.triskell.bacteria.framework.Bacterium[]) bacteriologicMedium
			.toArray(new fr.irisa.triskell.bacteria.framework.Bacterium[bacteriologicMedium
				.size()]);
	}

	/**
	 * 
	 * @uml.property name="bacteriologicMedium"
	 */
	public void clearBacteriologicMedium() {
		bacteriologicMedium.clear();
	}

	/**
	 * 
	 * @uml.property name="solutionSet"
	 */
	public void clearSolutionSet() {
		solutionSet.clear();
	}

	public float computeFitness() {
		return 0;
	}


	/**
	 * One generation for the bacteriologic algorithm:
	 * 		memorizes the best bacterium in the solution set
	 * 		filters 
	 * 		mutates
	 *
	 */
	public void computeOneGeneration() {
		this.memorizationFunction.run(this.bacteriologicMediumToArray());
		this.filteringFunction.run();		
		this.mutationFunction.run();
	}

	/**
	 * 
	 * @uml.property name="bacteriologicMedium"
	 */
	public boolean containsAllBacteriologicMedium(Collection elements) {
		return bacteriologicMedium.containsAll(elements);
	}

	/**
	 * 
	 * @uml.property name="solutionSet"
	 */
	public boolean containsAllSolutionSet(Collection elements) {
		return solutionSet.containsAll(elements);
	}

	/**
	 * 
	 * @uml.property name="bacteriologicMedium"
	 */
	public boolean containsBacteriologicMedium(
		fr.irisa.triskell.bacteria.framework.Bacterium element) {
		return bacteriologicMedium.contains(element);
	}

	/**
	 * 
	 * @uml.property name="solutionSet"
	 */
	public boolean containsSolutionSet(fr.irisa.triskell.bacteria.framework.Bacterium element) {
		return solutionSet.contains(element);
	}

	public void filter() {
	}

	/**
	 *  
	 * @uml.property name="bacteriaComparator"
	 */
	public BacteriaComparator getBacteriaComparator() {
		return bacteriaComparator;
	}

	/**
	 * 
	 * @uml.property name="bacteriologicMedium"
	 */
	public java.util.ArrayList getBacteriologicMedium() {
		return bacteriologicMedium;
	}

	/**
	 *  
	 * @uml.property name="filteringFunction"
	 */
	public FilteringFunction getFilteringFunction() {
		return filteringFunction;
	}

	/**
	 *  
	 * @uml.property name="fitnessFunction"
	 */
	public FitnessFunction getFitnessFunction() {
		return fitnessFunction;
	}

	/**
	 *  
	 * @uml.property name="memorizationFunction"
	 */
	public MemorizationFunction getMemorizationFunction() {
		return memorizationFunction;
	}

/**
	 *  
	 * @uml.property name="mutationFunction"
	 */
	public MutationFunction getMutationFunction() {
		return mutationFunction;
	}

	/**
	 * 
	 * @uml.property name="solutionSet"
	 */
	public java.util.ArrayList getSolutionSet() {
		return solutionSet;
	}

/**
	 *  
	 * @uml.property name="stoppingCriterion"
	 */
	public StoppingCriterion getStoppingCriterion() {
		return stoppingCriterion;
	}

	/**
	 * 
	 * @uml.property name="bacteriologicMedium"
	 */
	public boolean isBacteriologicMediumEmpty() {
		return bacteriologicMedium.isEmpty();
	}

	/**
	 * 
	 * @uml.property name="solutionSet"
	 */
	public boolean isSolutionSetEmpty() {
		return solutionSet.isEmpty();
	}

	public void memorize() {
	}

	public void mutate() {
	}

	/**
	 * 
	 * @uml.property name="bacteriologicMedium"
	 */
	public boolean removeBacteriologicMedium(
		fr.irisa.triskell.bacteria.framework.Bacterium element) {
		return bacteriologicMedium.remove(element);
	}

	/**
	 * 
	 * @uml.property name="solutionSet"
	 */
	public boolean removeSolutionSet(fr.irisa.triskell.bacteria.framework.Bacterium element) {
		return solutionSet.remove(element);
	}

	public void run() {
		while (!stoppingCriterion.run()){
			computeOneGeneration();
		}
	}

	/**
	 *  
	 * @uml.property name="bacteriaComparator"
	 */
	public void setBacteriaComparator(BacteriaComparator bacteriaComparator) {
		this.bacteriaComparator = bacteriaComparator;
	}

	/**
	 * 
	 * @uml.property name="bacteriologicMedium"
	 */
	public void setBacteriologicMedium(java.util.ArrayList value) {
		bacteriologicMedium = value;
	}

	/**
	 *  
	 * @uml.property name="filteringFunction"
	 */
	public void setFilteringFunction(FilteringFunction filteringFunction) {
		this.filteringFunction = filteringFunction;
	}

	/**
	 *  
	 * @uml.property name="fitnessFunction"
	 */
	public void setFitnessFunction(FitnessFunction fitnessFunction) {
		this.fitnessFunction = fitnessFunction;
	}



	/**
	 *  
	 * @uml.property name="memorizationFunction"
	 */
	public void setMemorizationFunction(
		MemorizationFunction memorizationFunction) {
		this.memorizationFunction = memorizationFunction;
	}




	/**
	 *  
	 * @uml.property name="mutationFunction"
	 */
	public void setMutationFunction(MutationFunction mutationFunction) {
		this.mutationFunction = mutationFunction;
	}

	/**
	 * 
	 * @uml.property name="solutionSet"
	 */
	public void setSolutionSet(java.util.ArrayList value) {
		solutionSet = value;
	}

	/**
	 *  
	 * @uml.property name="stoppingCriterion"
	 */
	public void setStoppingCriterion(StoppingCriterion stoppingCriterion) {
		this.stoppingCriterion = stoppingCriterion;
	}

	/**
	 * 
	 * @uml.property name="solutionSet"
	 */
	public Iterator solutionSetIterator() {
		return solutionSet.iterator();
	}

	/**
	 * 
	 * @uml.property name="solutionSet"
	 */
	public int solutionSetSize() {
		return solutionSet.size();
	}

	/**
	 * 
	 * @uml.property name="solutionSet"
	 */
	public fr.irisa.triskell.bacteria.framework.Bacterium[] solutionSetToArray() {
		return (fr.irisa.triskell.bacteria.framework.Bacterium[]) solutionSet
			.toArray(new fr.irisa.triskell.bacteria.framework.Bacterium[solutionSet.size()]);
	}

}
