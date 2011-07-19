/*
 * Created on 30 nov. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package testBacteriologicFramework;

import java.util.*;

import fr.irisa.triskell.bacteria.framework.BacteriologicAlgorithm;
import fr.irisa.triskell.bacteria.framework.FilteringFunction;
import fr.irisa.triskell.bacteria.framework.MemorizationFunction;

import junit.framework.*;




/**
 * @author bbaudry
 * 30 nov. 2004
 */
public class FitnessFunctionTest extends TestCase {

	/**
	 *  
	 * @uml.property name="fitness"
	 * @uml.associationEnd multiplicity="(1 1)"
	 */
	private DummyFitness fitness;

	/**
	 *  
	 * @uml.property name="bacteriologicAlgorithm"
	 * @uml.associationEnd multiplicity="(1 1)"
	 */
	private BacteriologicAlgorithm bacteriologicAlgorithm;

	/**
	 *  
	 * @uml.property name="bact1"
	 * @uml.associationEnd multiplicity="(1 1)"
	 */
	private DummyBacterium bact1;

	/**
	 *  
	 * @uml.property name="bact2"
	 * @uml.associationEnd multiplicity="(1 1)"
	 */
	private DummyBacterium bact2;

	/**
	 *  
	 * @uml.property name="bact3"
	 * @uml.associationEnd multiplicity="(1 1)"
	 */
	private DummyBacterium bact3;

	private MemorizationFunction memorizationFunction;


	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		fitness = new DummyFitness();
		this.memorizationFunction = new MemorizationFunction((float)1);
		
		bact1 = new DummyBacterium(5);
		bact2 = new DummyBacterium(7);
		bact3 = new DummyBacterium(12);
		ArrayList medium = new ArrayList();
		medium.add(bact1);
		medium.add(bact2);
		medium.add(bact3);
		
		FilteringFunction filteringFunction = new FilteringFunction();

		bacteriologicAlgorithm = new BacteriologicAlgorithm(medium,fitness,memorizationFunction,null,filteringFunction,null);		
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Constructor for FitnessFunctionTest.
	 * @param arg0
	 */

	/*tests the updateSolutionSet method with an empty solutionSet*/
	public void testUpdateSolutionSet() {
		fitness.updateSolutionSet(this.bacteriologicAlgorithm);
		HashMap cache = fitness.getFitnessCache();
		assertTrue((((Float)cache.get(bact1)).floatValue()==(float)5)&&(((Float)cache.get(bact2)).floatValue()==(float)7)&&(((Float)cache.get(bact3)).floatValue()==(float)12));
	}

	/*tests the updateSolutionSet method with a solutionSet containing one Bacterium (bact3)*/
	public void testUpdateSolutionSet2() {
		ArrayList dummySolution = new ArrayList();
		dummySolution.add(bact3);
		this.bacteriologicAlgorithm.setSolutionSet(dummySolution);
		fitness.updateSolutionSet(this.bacteriologicAlgorithm);
		HashMap cache = fitness.getFitnessCache();
		assertTrue((((Float)cache.get(bact1)).floatValue()==(float)5)&&(((Float)cache.get(bact2)).floatValue()==(float)7)&&(((Float)cache.get(bact3)).floatValue()==(float)12));
	}

	/*after the creation of fitness, the bacteriologic association must be initialized 
	 * with the correct bacteriologicAlgorithm, and fitness should be registered as an observer on bacteriologicAlgorithm
	 * */
	public void testInit(){
		assertTrue(fitness.getBacteriologicAlgorithm()==this.bacteriologicAlgorithm);
		assertTrue(fitness.getBacteriologicAlgorithm().getBacteriologicAlgorithmObserver().size()==1);
		assertTrue(fitness.getBacteriologicAlgorithm().getBacteriologicAlgorithmObserver().contains(fitness));
	}
	
	/*tests the relativeFitness method with an empty solutionSet*/
	public void testRelativeFitness() {
		float result = fitness.relativeFitness(bact1);
		assertTrue(result==(float)5);
	}
	
	/*tests the relativeFitness method with a solutionSet containing one Bacterium (bact3)*/
	public void testRelativeFitness2() {
		ArrayList dummySolution = new ArrayList();
		dummySolution.add(bact3);
		this.bacteriologicAlgorithm.setSolutionSet(dummySolution);
		float result = fitness.relativeFitness(bact2);
		assertTrue(result==(float)7);
	}

	public void testRun() {
	}


}
