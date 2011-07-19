package testsJUnit;
import java.util.Vector;

import Dummies.DummyBacterium;
import Dummies.DummyFitness;
import Dummies.DummyMutationFunction;
import Dummies.DummyStoppingCriterion;

import projetBacterioJava.BacteriologicAlgorithm;

import junit.framework.TestCase;



/**
 * MutationFunction test
 */
public class MutationFunctionTest extends TestCase {

	
	/**
	 * The different attributs used to test
	 */
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
	 * Assert true if the sum of probabilities in the array proba
	 * values 1 (General Case)
	 */
	public void testSumProba(){
		testBactAlg.filter();
		testMutationFunction.run();
		float sum = 0;
		for (int i = 0; i < testMutationFunction.proba.length; i++){
			sum += testMutationFunction.proba[i];
		}
		assertTrue (near(sum, 1.0f) );
	}
	
	
	/**
	 * Control the size of the medium before and after the generate function
	 */
	public void testMediumSize(){
		testBactAlg.filter();
		assertTrue( testBactAlg.getBacteriologicMedium().size() == 5);
		testMutationFunction.run();
		assertTrue( testBactAlg.getBacteriologicMedium().size() == 10);
	}
	
	
	/**
	 * Test generate when the medium is null
	 */
	public void testNullMedium(){
		assertTrue( testBactAlg.getBacteriologicMedium().size() == 5);
		vectMedium.remove(4);
		vectMedium.remove(3);
		vectMedium.remove(2);
		vectMedium.remove(1);
		vectMedium.remove(0);
		assertTrue( testBactAlg.getBacteriologicMedium().size() == 0);
		testMutationFunction.run();
		assertTrue( testBactAlg.getBacteriologicMedium().size() == 0);
	}
	
	
	
	
	
	/**
	 * @param f1 a float
	 * @param f2 another float
	 * @return true if f1 is near f2
	 */
	public boolean near (float f1, float f2){
		return (Math.abs(f1-f2) < 0.0001);
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
