/*
 * Created on 30 nov. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package testBacteriologicFramework;

import java.util.ArrayList;
import java.util.HashMap;

import fr.irisa.triskell.bacteria.framework.BacteriologicAlgorithm;
import fr.irisa.triskell.bacteria.framework.Bacterium;
import fr.irisa.triskell.bacteria.framework.FilteringFunction;
import fr.irisa.triskell.bacteria.framework.MemorizationFunction;

import junit.framework.TestCase;




/**
 * @author bbaudry
 * 30 nov. 2004
 */
public class MemorizationFunctionTest extends TestCase {

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

	/**
	 *  
	 * @uml.property name="fitness"
	 * @uml.associationEnd multiplicity="(1 1)"
	 */
	private DummyFitness fitness;
	

	/*test the selectBest method with an empty solution set. 
	 * the method should select bact3 and add it to the solution set*/
	public void testSelectBest(){
		Bacterium[] medium = bacteriologicAlgorithm.bacteriologicMediumToArray();
		memorizationFunction.run(medium);
		/*the size of the solution set must be 1*/
		assertTrue(bacteriologicAlgorithm.getSolutionSet().size()==1);
		/*the solution set must contain bact3*/
		assertTrue(bacteriologicAlgorithm.getSolutionSet().contains(bact3));
	}

	/*tests another aspect of the selectBest method with an empty solution set. 
	 * the cache of relative fitness values in FitnessFunction should have changed*/
	public void testSelectBest2(){
		Bacterium[] medium = bacteriologicAlgorithm.bacteriologicMediumToArray();
		HashMap cache = new HashMap(fitness.getFitnessCache());
		memorizationFunction.run(medium);
		/*the cache of relative fitness values must have changed after selecting the best*/
		assertTrue(cache!=fitness.getFitnessCache());
		/*the size of the cache of relative fitness values must be 2*/
		assertTrue(fitness.getFitnessCache().size()==2);
		/*the cache of relative fitness values must contain bact1 and bact2*/
		assertTrue(fitness.getFitnessCache().containsKey(bact1));
		assertTrue(fitness.getFitnessCache().containsKey(bact2));		
	}
	
	/*here, the run method is called twice 
	 * after the second run, the solution set must contain two bacteria: bact3 and bact2*/
	public void testSelectBest3(){
		Bacterium[] medium = bacteriologicAlgorithm.bacteriologicMediumToArray();
		memorizationFunction.run(medium);
		memorizationFunction.run(medium);
		/*the size of the solution set must be 1*/
		assertTrue(bacteriologicAlgorithm.getSolutionSet().size()==2);
		/*the solution set must contain bact3*/
		assertTrue(bacteriologicAlgorithm.getSolutionSet().contains(bact3)&&bacteriologicAlgorithm.getSolutionSet().contains(bact2));		
	}
	
	/*here, the run method is called twice 
	 * after the second run, contains two bacteria: bact3 and bact2. the medium contains only one bacterium, the FritnessFunction
	 * contains a cache with the relative fitness of the only bacterium in the medium*/
	public void testSelectBest4(){
		Bacterium[] medium = bacteriologicAlgorithm.bacteriologicMediumToArray();
		HashMap cache = new HashMap(fitness.getFitnessCache());
		memorizationFunction.run(medium);
		memorizationFunction.run(medium);
		/*the cache of relative fitness values must have changed after selecting the best*/
		assertTrue(cache!=fitness.getFitnessCache());
		/*the size of the cache of relative fitness values must be 2*/
		assertTrue(fitness.getFitnessCache().size()==1);
		/*the cache of relative fitness values must contain bact1 and bact2*/
		assertTrue(fitness.getFitnessCache().containsKey(bact1));
	}
}
