package projetBacterioJava;
import java.util.Iterator;
import java.util.Vector;

import Dummies.DummyBacterium;
import Dummies.DummyFitness;
import Dummies.DummyMutationFunction;
import Dummies.DummyStoppingCriterion;


/**
 * Test class which consists in creating 5 Bacteria. Each Bacteria is a number
 * and is add to the medium. All attributes are initialized and the different
 * functions-objects are created. Then, the algorithm is lunched. At the end,
 * final results are recovered.
 */

public class BacterioTest {
	
	
	public static void main (String[] arg){
		
		
		/**
		 * Bacteria's creation
		 */
		DummyBacterium Bact1 = new DummyBacterium(1);
		DummyBacterium Bact2 = new DummyBacterium(5);
		DummyBacterium Bact3 = new DummyBacterium(-2);
		DummyBacterium Bact4 = new DummyBacterium(-4);
		DummyBacterium Bact5 = new DummyBacterium(7);
		
		
		/**
		 * The number of bacteria to alter at each turn
		 */
		int bactNumber = 5;
		
		
		/**
		 * The minimum size of the medium 
		 */
		int testMinMedium = 3;
		
		
		/**
		 * The maximum number of turns a bacterium can stand in the medium
		 */
		int testMaxBactTurn = 5;
		
		
		/**
		 * Maximum number the algorithm can do before stopping
		 */
		int testMaxAlgTurn = 100;
		
		
		/**
		 * The minimum value the relative fitness of a Bacterium must
		 * have to be considered
		 */
		float testMemorizationThreshold = 0f;
		
		
		/**
		 * Medium set's creation
		 */
		Vector vectMedium = new Vector();
		
		
		/**
		 * Bacteria are put into the medium set
		 */
		vectMedium.add(Bact1);
		vectMedium.add(Bact2);
		vectMedium.add(Bact3);
		vectMedium.add(Bact4);
		vectMedium.add(Bact5);
		
		
		/**
		 * Creation of the objects whose class implements functions used
		 * by the algorithm
		 */
		DummyFitness testFitnessFunction = new DummyFitness();
		DummyMutationFunction testMutationFunction = new DummyMutationFunction(bactNumber);
		DummyStoppingCriterion testStoppingCriterion = new DummyStoppingCriterion();
		
		
		
		/**
		 * Creation of the algorithm with all its paramaters
		 */
		BacteriologicAlgorithm testAlgo = new BacteriologicAlgorithm(
				vectMedium,
				testFitnessFunction,
				testMutationFunction,
				testStoppingCriterion,
				testMinMedium,
				testMaxBactTurn,
				testMaxAlgTurn,
				testMemorizationThreshold,
				0f
		);
		
		
		System.out.println("At the beginning, the medium is : " + vectMedium.toString());
		
		/**
		 * The algorithm is lunched 
		 */
		testAlgo.run();
		
		
		System.out.println("\n\n\nNumber of times the algorithm has been lunched : " + testAlgo.getnumberAlgTurn());
		Iterator iterSol = testAlgo.getSolutionSet().iterator();
		float testSum = 0;
		while(iterSol.hasNext()){
			DummyBacterium bact = (DummyBacterium) iterSol.next();
			testSum += bact.getNumber();
			System.out.print(bact.getNumber() + " ");
		}
		System.out.println(" = " + testSum);
		System.out.println("At the end, medium is : " + vectMedium.toString());
		System.out.println("At the end, solutionSet is : " + testAlgo.getSolutionSet());
	
	}
	

}
