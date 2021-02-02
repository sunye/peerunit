package testsJUnit;
import java.util.Vector;

import Dummies.DummyBacterium;
import Dummies.DummyFitness;
import Dummies.DummyMutationFunction;
import Dummies.DummyStoppingCriterion;

import projetBacterioJava.BacteriologicAlgorithm;

import junit.framework.TestCase;



/**
 *  FilteringFunction test with only one bacterium which is negative
 */
public class FilteringFunctionTestOneNegativeBacterium extends TestCase {

	
	DummyBacterium Bact1;
	DummyBacterium BactTest;
	DummyFitness testFitnessFunction;
	DummyMutationFunction testMutationFunction;
	DummyStoppingCriterion testStoppingCriterion;
	BacteriologicAlgorithm testBactAlg;
	

	
	/**
	 * Test for filtering function at first round (no changes)
	 */
	public void testFilterFirstOneNegativeBacterium(){
		assertTrue(testBactAlg.getBacteriologicMedium().size() == 1);
		testBactAlg.filter();
		assertTrue(
				((DummyBacterium)testBactAlg.getBacteriologicMedium().get(0)).getNumber() == -3 );
		assertTrue(testBactAlg.getBacteriologicMedium().size() == 1);
	}
	
	
	
	/**
	 * Test for filtering function at second round (no changes too)
	 */
	public void testFilterSecondOneNegativeBacterium(){
		testBactAlg.filter();
		assertTrue(testBactAlg.getBacteriologicMedium().size() == 1);
		(testBactAlg.getSolutionSet()).add((DummyBacterium) BactTest);
		testBactAlg.filter();
		// The Bacterium is not removed because of the medium's minimum size
		assertTrue(testBactAlg.getBacteriologicMedium().size() == 1);
	}
	
	
		
	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		Bact1 = new DummyBacterium(-3);
		BactTest = new DummyBacterium(12);
		Vector vectMedium = new Vector();
		testFitnessFunction = new DummyFitness();
		testMutationFunction = new DummyMutationFunction(5);
		testStoppingCriterion = new DummyStoppingCriterion();
		vectMedium.add(Bact1);
		testBactAlg = new BacteriologicAlgorithm(
				vectMedium,
				testFitnessFunction,
				testMutationFunction,
				testStoppingCriterion, 3, 5, 100, 0f, 0f);
	}

}

