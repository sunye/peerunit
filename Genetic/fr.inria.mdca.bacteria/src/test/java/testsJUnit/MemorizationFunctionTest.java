package testsJUnit;
import java.util.Vector;

import Dummies.DummyBacterium;
import Dummies.DummyFitness;
import Dummies.DummyMutationFunction;
import Dummies.DummyStoppingCriterion;

import projetBacterioJava.BacteriologicAlgorithm;

import junit.framework.TestCase;



/**
 * MemorizationFunction test
 */
public class MemorizationFunctionTest extends TestCase {

	
	DummyBacterium Bact1;
	DummyBacterium Bact2;
	DummyBacterium Bact3;
	DummyBacterium Bact4;
	DummyBacterium Bact5;
	DummyFitness testFitnessFunction;
	DummyMutationFunction testMutationFunction;
	DummyStoppingCriterion testStoppingCriterion;
	BacteriologicAlgorithm testBactAlg;
	Vector vectMedium;
	
	
	/**
	 * Control the MaxFitness function in general case
	 */
	public void testMaxFitness(){
		testBactAlg.filter();
		assertTrue(testBactAlg.memorizationFunction.maxFitness(testBactAlg.getBacteriologicMedium()) == ((DummyBacterium) Bact5));
	}
	
	
	/**
	 * Control if the best Bacterium have been memorized
	 */
	public void testMemorizationRun(){
		testBactAlg.filter();
		testBactAlg.memorizationFunction.run();
		assertTrue(((DummyBacterium)testBactAlg.getSolutionSet().elementAt(0)).getNumber() == 7);
	}
	
	
	/**
	 * Verify if after the memorization the solution set has grown
	 */
	public void testMemorizationRunSizeSolution(){
		testBactAlg.filter();
		assertTrue (testBactAlg.getSolutionSet().size() == 0);
		testBactAlg.memorizationFunction.run();
		assertTrue (testBactAlg.getSolutionSet().size() == 1);
	}
	
	
	/**
	 * Control the MaxFitness function with a null medium
	 */
	public void testMaxFitnessNullMedium(){
		vectMedium.remove(4);
		vectMedium.remove(3);
		vectMedium.remove(2);
		vectMedium.remove(1);
		vectMedium.remove(0);
		assertTrue(testBactAlg.memorizationFunction.maxFitness(testBactAlg.getBacteriologicMedium()) == null);
	}
	
	
	
	/**
	 * Verify if after the memorization the solution is the same
	 * (because the medium is null)
	 */
	public void testMemorizationRunSizeSolutionNullMedium(){
		assertTrue (testBactAlg.getSolutionSet().size() == 0);
		vectMedium.remove(4);
		vectMedium.remove(3);
		vectMedium.remove(2);
		vectMedium.remove(1);
		vectMedium.remove(0);
		testBactAlg.memorizationFunction.run();
		assertTrue (testBactAlg.getSolutionSet().size() == 0);
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
		vectMedium = new Vector();
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

