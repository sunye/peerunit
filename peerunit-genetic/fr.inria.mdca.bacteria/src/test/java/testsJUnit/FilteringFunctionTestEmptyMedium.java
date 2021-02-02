package testsJUnit;
import java.util.Vector;

import Dummies.DummyBacterium;
import Dummies.DummyFitness;
import Dummies.DummyMutationFunction;
import Dummies.DummyStoppingCriterion;

import projetBacterioJava.BacteriologicAlgorithm;

import junit.framework.TestCase;



/**
 *  FilteringFunction test with an empty medium 
 */
public class FilteringFunctionTestEmptyMedium extends TestCase {

	
	DummyFitness testFitnessFunction;
	DummyMutationFunction testMutationFunction;
	DummyStoppingCriterion testStoppingCriterion;
	BacteriologicAlgorithm testBactAlg;
	DummyBacterium BactTest;
	
		
	
	/**
	 * Test for filtering function at first round (no changes)
	 */
	public void testFilterEmptyMedium(){
		assertTrue(testBactAlg.getBacteriologicMedium().size() == 0);
		testBactAlg.filter();
		assertTrue(testBactAlg.getBacteriologicMedium().size() == 0);
	}
	
	
	/**
	 * Test for filtering function at second round (no changes)
	 */
	public void testFilterEmptyMedium2(){
		assertTrue(testBactAlg.getBacteriologicMedium().size() == 0);
		testBactAlg.filter();
		(testBactAlg.getSolutionSet()).add((DummyBacterium) BactTest);
		testBactAlg.filter();
		assertTrue(testBactAlg.getBacteriologicMedium().size() == 0);
	}
	
	
		
		
	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		Vector vectMedium = new Vector();
		BactTest = new DummyBacterium(7);
		testFitnessFunction = new DummyFitness();
		testMutationFunction = new DummyMutationFunction(5);
		testStoppingCriterion = new DummyStoppingCriterion();
		testBactAlg = new BacteriologicAlgorithm(
				vectMedium,
				testFitnessFunction,
				testMutationFunction,
				testStoppingCriterion, 3, 5, 100, 0f, 0f);
	}

}

