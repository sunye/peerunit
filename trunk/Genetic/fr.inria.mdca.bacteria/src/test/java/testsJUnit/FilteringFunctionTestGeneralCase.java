package testsJUnit;
import java.util.Vector;

import Dummies.DummyBacterium;
import Dummies.DummyFitness;
import Dummies.DummyMutationFunction;
import Dummies.DummyStoppingCriterion;

import projetBacterioJava.BacteriologicAlgorithm;

import junit.framework.TestCase;



/**
 *  FilteringFunction test in general case (5 bacteria negative and positive)
 */
public class FilteringFunctionTestGeneralCase extends TestCase {

	
	DummyBacterium Bact1;
	DummyBacterium Bact2;
	DummyBacterium Bact3;
	DummyBacterium Bact4;
	DummyBacterium Bact5;
	DummyFitness testFitnessFunction;
	DummyMutationFunction testMutationFunction;
	DummyStoppingCriterion testStoppingCriterion;
	BacteriologicAlgorithm testBactAlg;
	

	
	/**
	 * Test for filtering function at first round (no changes)
	 */
	public void testFilterFirstGeneralCase(){
		assertTrue(testBactAlg.getBacteriologicMedium().size() == 5);
		testBactAlg.filter();
		assertTrue(
				((DummyBacterium)testBactAlg.getBacteriologicMedium().get(0)).getNumber() == 1 &&
				((DummyBacterium)testBactAlg.getBacteriologicMedium().get(1)).getNumber() == 5 &&
				((DummyBacterium)testBactAlg.getBacteriologicMedium().get(2)).getNumber() == -2 &&
				((DummyBacterium)testBactAlg.getBacteriologicMedium().get(3)).getNumber() == -4 &&
				((DummyBacterium)testBactAlg.getBacteriologicMedium().get(4)).getNumber() == 7 );
		assertTrue(testBactAlg.getBacteriologicMedium().size() == 5);
	}
	
	
	
	/**
	 * Test for filtering function at second round (medium is filtered)
	 */
	public void testFilterSecondGeneralCase(){
		testBactAlg.filter();
		assertTrue(testBactAlg.getBacteriologicMedium().size() == 5);
		(testBactAlg.getSolutionSet()).add((DummyBacterium) Bact5);
		testBactAlg.filter();
		assertTrue(
				((DummyBacterium)testBactAlg.getBacteriologicMedium().get(0)).getNumber() == 1 &&
				((DummyBacterium)testBactAlg.getBacteriologicMedium().get(1)).getNumber() == 5 &&
				((DummyBacterium)testBactAlg.getBacteriologicMedium().get(2)).getNumber() == 7 );
		assertTrue(testBactAlg.getBacteriologicMedium().size() == 3);
	}
	
		
	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		Bact1 = new DummyBacterium(1);
		Bact2 = new DummyBacterium(5);
		Bact3 = new DummyBacterium(-2);
		Bact4 = new DummyBacterium(-4);
		Bact5 = new DummyBacterium(7);
		Vector vectMedium = new Vector();
		testFitnessFunction = new DummyFitness();
		testMutationFunction = new DummyMutationFunction(5);
		testStoppingCriterion = new DummyStoppingCriterion();
		vectMedium.add(Bact1);
		vectMedium.add(Bact2);
		vectMedium.add(Bact3);
		vectMedium.add(Bact4);
		vectMedium.add(Bact5);
		testBactAlg = new BacteriologicAlgorithm(
				vectMedium,
				testFitnessFunction,
				testMutationFunction,
				testStoppingCriterion, 3, 5, 100, 0f, 0f);
	}

}

