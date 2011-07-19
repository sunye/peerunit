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

import java.util.HashMap;

import java.util.Set;

/**
 * @author bbaudry
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */

/*This class provides methods to compute the fitness of a bacteriologic medium and the relative 
 * fitness of a medium  
 * */

public abstract class FitnessFunction extends BacteriologicAlgorithmObserver {

	public FitnessFunction() {
		super();
		fitnessCache = new HashMap();
	}

	/**
	 * @param bacterium
	 * @return the relative fitness value of bacterium
	 * This class keeps a cache with the bacteria relative fitness values
	 * 		if the relative fitness of bacterium is in the cache it is returned
	 * 		if it is not in the cache it is computed by computeRelFitness
	 */
	public float relativeFitness(Bacterium bacterium) {
		if (fitnessCache.containsKey(bacterium)) {
			return ((Float) fitnessCache.get(bacterium)).floatValue();
		} else {
			float result = computeRelFitness(bacterium);
			fitnessCache.put(bacterium, new Float(result));
			return result;
		}
	}

	/*Computes the relative fitness of bacterium
	 * this relative fitness is the enhancement bacterium would provide if it was added in the solution set
	 * let S be the solution set, then: relFitness(b, S) = fitness(S \/ {b}) - fitness(S) 
	 * */
	private float computeRelFitness(Bacterium bacterium) {
		float result;

		/*
		 * build the union of the solution set and bacterium in the variable
		 * newSolution
		 */
		ArrayList newSolution = new ArrayList(this.bacteriologicAlgorithm
				.getSolutionSet());
		newSolution.add(bacterium);

		/* build the relative fitness of bacterium */
		float solutionFitness = fitness(this.bacteriologicAlgorithm
				.solutionSetToArray());
		Bacterium[] newSolutionTab = (Bacterium[]) newSolution
				.toArray(new Bacterium[newSolution.size()]);
		float newSolutionFitness = fitness(newSolutionTab);
		float fitnessDiff = newSolutionFitness - solutionFitness;
		if (fitnessDiff > 0)
			result = fitnessDiff;
		else
			result = 0;

		return result;
	}

	/*This abstract method has to be implemented to actually compute the fitness of a bacteriologic medium
	 *(a set of bacteria) 
	 * */
	public abstract float fitness(Bacterium[] medium);

	public float run(Bacterium[] medium) {
		return 0;
	}

	/**
	 *  
	 * @uml.property name="bacteriologicAlgorithm"
	 * @uml.associationEnd multiplicity="(1 1)" inverse="fitnessFunction:bacteriologicFramework.BacteriologicAlgorithm"
	 */
	private BacteriologicAlgorithm bacteriologicAlgorithm;

	/**
	 *  
	 * @uml.property name="bacteriologicAlgorithm"
	 */
	public BacteriologicAlgorithm getBacteriologicAlgorithm() {
		return bacteriologicAlgorithm;
	}

	/**
	 *  
	 * @uml.property name="bacteriologicAlgorithm"
	 */
	public void setBacteriologicAlgorithm(
		BacteriologicAlgorithm bacteriologicAlgorithm) {
		this.bacteriologicAlgorithm = bacteriologicAlgorithm;
		this.bacteriologicAlgorithm.addObserver(this);
	}

/*This method updates the fitness values in the cache
	 * This method is called each time the solution set is modified, since all relative fitnesses of bacteria 
	 * depend on the contents of the solution set. 
	 */
	public void updateSolutionSet(BacteriologicAlgorithm bacteriologicAlgorithm) {
		fitnessCache.clear();
		Iterator it = bacteriologicAlgorithm.bacteriologicMediumIterator();
		while (it.hasNext()) {
			Bacterium b = (Bacterium) it.next();
			//the relativeFitness method computes the relative fitness for b and puts the value in the fitnessCache
			bacteriologicAlgorithm.getFitnessFunction().relativeFitness(b);
		}
	}

	/**
	 * 
	 * @uml.property name="fitnessCache"
	 */
	private HashMap fitnessCache;

	/**
	 * 
	 * @uml.property name="fitnessCache"
	 */
	public java.util.HashMap getFitnessCache() {
		return fitnessCache;
	}

	/**
	 * 
	 * @uml.property name="fitnessCache"
	 */

	public void setFitnessCache(java.util.HashMap value) {
		fitnessCache = value;
	}

	/**
	 * 
	 * @uml.property name="fitnessCache"
	 */
	public Set fitnessCacheKeySet() {
		return fitnessCache.keySet();
	}

	/**
	 * 
	 * @uml.property name="fitnessCache"
	 */
	public Collection fitnessCacheValues() {
		return fitnessCache.values();
	}

	/**
	 * 
	 * @uml.property name="fitnessCache"
	 */
	public boolean fitnessCacheContainsKey(java.lang.Object key) {
		return fitnessCache.containsKey(key);
	}

	/**
	 * 
	 * @uml.property name="fitnessCache"
	 */
	public boolean fitnessCacheContainsValue(java.lang.Object value) {
		return fitnessCache.containsValue(value);
	}

	/**
	 * 
	 * @uml.property name="fitnessCache"
	 */
	public java.lang.Object getFitnessCache(java.lang.Object key) {
		return (java.lang.Object) fitnessCache.get(key);
	}

	/**
	 * 
	 * @uml.property name="fitnessCache"
	 */
	public java.lang.Object putFitnessCache(java.lang.Object key,
			java.lang.Object value) {
		return (java.lang.Object) fitnessCache.put(key, value);
	}

	/**
	 * 
	 * @uml.property name="fitnessCache"
	 */
	public java.lang.Object removeFitnessCache(java.lang.Object key) {
		return (java.lang.Object) fitnessCache.remove(key);
	}

	/**
	 * 
	 * @uml.property name="fitnessCache"
	 */
	public void clearFitnessCache() {
		fitnessCache.clear();
	}
}