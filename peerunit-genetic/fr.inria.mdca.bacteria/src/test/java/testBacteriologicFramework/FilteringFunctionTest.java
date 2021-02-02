/*
 * Created on 22 déc. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package testBacteriologicFramework;

import java.util.ArrayList;

import fr.irisa.triskell.bacteria.framework.BacteriologicAlgorithm;
import fr.irisa.triskell.bacteria.framework.FilteringFunction;
import fr.irisa.triskell.bacteria.framework.MemorizationFunction;

import junit.framework.TestCase;




/**
 * @author bbaudry
 * 22 déc. 2004
 */
public class FilteringFunctionTest extends TestCase {

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		
		fitness = new DummyFitness2();
		this.memorizationFunction = new MemorizationFunction((float)1);
		
		bact1 = new DummyBacterium(5);
		bact2 = new DummyBacterium(12);
		bact3 = new DummyBacterium(12);
		ArrayList medium = new ArrayList();
		medium.add(bact1);
		medium.add(bact2);
		medium.add(bact3);
		
		filteringFunction = new FilteringFunction();
		
		bacteriologicAlgorithm = new BacteriologicAlgorithm(medium,fitness,memorizationFunction,null,filteringFunction,null);		

	}

	/*default test: just after initialization the filtering function should not modify the medium*/
	public void testRun() {
		ArrayList saveMedium = bacteriologicAlgorithm.getBacteriologicMedium();
		filteringFunction.run();
		assertTrue(saveMedium == bacteriologicAlgorithm.getBacteriologicMedium());		
	}
	
	/*after one run of the memorization function bact 2 should be saved in the solution set and
	 * bact3 should be removed from the medium by the filtering function
	 * after filtering, the medium should contain only bacterium: bact1
	 * */
	public void testRun2(){
		memorizationFunction.run(bacteriologicAlgorithm.bacteriologicMediumToArray());
		filteringFunction.run();
		assertTrue(bacteriologicAlgorithm.bacteriologicMediumSize()==1);
		assertTrue(bacteriologicAlgorithm.containsBacteriologicMedium(bact1));
	}
	
	/**
	 *  
	 * @uml.property name="bacteriologicAlgorithm"
	 * @uml.associationEnd multiplicity="(1 1)"
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
	}

	/**
	 *  
	 * @uml.property name="filteringFunction"
	 * @uml.associationEnd multiplicity="(1 1)"
	 */
	private FilteringFunction filteringFunction;

	/**
	 *  
	 * @uml.property name="filteringFunction"
	 */
	public FilteringFunction getFilteringFunction() {
		return filteringFunction;
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
	 * @uml.property name="bact1"
	 * @uml.associationEnd multiplicity="(1 1)"
	 */
	private DummyBacterium bact1;

	/**
	 *  
	 * @uml.property name="bact1"
	 */
	public DummyBacterium getBact1() {
		return bact1;
	}

	/**
	 *  
	 * @uml.property name="bact1"
	 */
	public void setBact1(DummyBacterium bact1) {
		this.bact1 = bact1;
	}

	/**
	 *  
	 * @uml.property name="bact2"
	 * @uml.associationEnd multiplicity="(1 1)"
	 */
	private DummyBacterium bact2;

	/**
	 *  
	 * @uml.property name="bact2"
	 */
	public DummyBacterium getBact2() {
		return bact2;
	}

	/**
	 *  
	 * @uml.property name="bact2"
	 */
	public void setBact2(DummyBacterium bact2) {
		this.bact2 = bact2;
	}

	/**
	 *  
	 * @uml.property name="bact3"
	 * @uml.associationEnd multiplicity="(1 1)"
	 */
	private DummyBacterium bact3;

	/**
	 *  
	 * @uml.property name="bact3"
	 */
	public DummyBacterium getBact3() {
		return bact3;
	}

	/**
	 *  
	 * @uml.property name="bact3"
	 */
	public void setBact3(DummyBacterium bact3) {
		this.bact3 = bact3;
	}

	/**
	 *  
	 * @uml.property name="memorizationFunction"
	 * @uml.associationEnd multiplicity="(1 1)"
	 */
	private MemorizationFunction memorizationFunction;

	/**
	 *  
	 * @uml.property name="memorizationFunction"
	 */
	public MemorizationFunction getMemorizationFunction() {
		return memorizationFunction;
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
	 * @uml.property name="fitness"
	 * @uml.associationEnd multiplicity="(1 1)"
	 */
	private DummyFitness2 fitness;

	/**
	 *  
	 * @uml.property name="fitness"
	 */
	public DummyFitness2 getFitness() {
		return fitness;
	}

	/**
	 *  
	 * @uml.property name="fitness"
	 */
	public void setFitness(DummyFitness2 fitness) {
		this.fitness = fitness;
	}

}
