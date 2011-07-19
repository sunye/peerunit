package projetBacterioJava;
import java.util.Vector;

/**
 * The BacteriologicAlgorithm class is the central class in the framework.
 * The principles of the algorithm are inspired by the biological process
 * of bacteriologic adaptation. It is an original adaptation of genetic
 * algorithms but it solves problems which was not so similar to evolution
 * than to adaptation. The general idea is that a population of bacteria
 * is able to adapt itself to a given environment. If bacteria are spread in
 * a new stable environment they will reproduce themselves so that they fit
 * better and better to the environment. At each generation, the bacteria
 * are slightly altered and, when a new bacterium fits well a particular of
 * the environment it is memorized.
 * From this principle, the bacteriologic algorithm takes an initial set of
 * bacteria as an input, and its evolution consits in series of mutations on
 * bacteria, to explore the whole scope of solutions. The final set is built
 * incrementally by adding bacteria that can improve the quality of the set.
 * Along the execution there are thus two sets, the solution set that is
 * being built, and the set of potential bacteria, that we call a
 * bacteriologic medium.
 * The global process is incremental and each step is called a generation. A
 * generation consits in three principal steps.
 * 		- filter : It removes from the medium bacteria that verify a given
 * 				   condition.
 * 		- mutate : It generates a new bacterium in the medium by slightly
 * 				   altering an ancestor bacterium.
 * 		- memorize : It adds a satisfaying bacterium to the solution set.
 * Those steps are based on the Fitness Function, which represents the
 * "intelligence" of the algorithm.
 */


public class BacteriologicAlgorithm {



	/**
	 * The set containing the solution of the problem
	 */
	private Vector solutionSet;
	
	/**
	 * The set of potential bacteria
	 */
	private Vector bacteriologicMedium;
	
	/**
	 * The object which implements the fitness() function
	 */
	private FitnessFunction fitnessFunction;
	
	/**
	 * The object which implements the memorize() function
	 */
	public MemorizationFunction memorizationFunction;
	
	/**
	 * The object which implements the filter() function
	 */
	private FilteringFunction filteringFunction;
	
	/**
	 * The object which implements the mutate() function
	 */
	private MutationFunction mutationFunction;
	
	/**
	 * The object which implements the stopping criterion
	 */
	private StoppingCriterion stoppingCriterion;
	
	/**
	 * Maximum number the algorithm can do
	 */
	private int maxAlgTurn;
	
	/**
	 * Number of turns the algorithm has already done
	 */
	private int numberAlgTurn = 0;
	
	
	
	
	/**
	 * Constructor
	 * @param theBacteriologicMedium the set of potential bacteria
	 * @param theFitnessFunction the object which implements the fitness()
	 * 		  function
	 * @param theMutationFunction the object which implements the mutate()
	 * 		  function
	 * @param theStoppingCriterion the object which implements the stopping
	 * 		  criterion
	 * @param theMinMedium the minimum number of bacteria in the medium
	 * @param theMaxBactTurn the maximum number of turns a bacterium can stand
	 * 		  in the medium
	 * @param theMaxAlgTurn the maximum number of turns the algorithm can do
	 * @param theMemorizationThreshold the minimum value of utility a bacterium
	 * 		  must have towards the solution in order to be memorized 
	 */
	public BacteriologicAlgorithm(  Vector theBacteriologicMedium,
									FitnessFunction theFitnessFunction,
									MutationFunction theMutationFunction,
									StoppingCriterion theStoppingCriterion,
									int theMinMedium,
									int theMaxBactTurn,
									int theMaxAlgTurn,
									float theMemorizationThreshold,
									float theBactRelFitMin) {
		
		bacteriologicMedium = theBacteriologicMedium;
		
		fitnessFunction = theFitnessFunction;
		fitnessFunction.setBacteriologicAlgorithm(this);
		
		mutationFunction = theMutationFunction;
		mutationFunction.setBacteriologicAlgorithm(this);
		
		stoppingCriterion = theStoppingCriterion;
		stoppingCriterion.setBacteriologicAlgorithm(this);
		
		solutionSet = new Vector();
		
		memorizationFunction = new MemorizationFunction(this, theMemorizationThreshold);
		filteringFunction = new FilteringFunction(this, theMinMedium, theMaxBactTurn, theBactRelFitMin);
		
		maxAlgTurn = theMaxAlgTurn;
	}
	
	
	/**
	 * Constructor
	 * @param theBacteriologicMedium the set of potential bacteria
	 * @param theFitnessFunction the object whose class implements the fitness()
	 * 		  function
	 * @param theMutationFunction the object whose class implements the mutate()
	 * 		  function
	 * @param theStoppingCriterion the object whose class implements the stopping
	 * 		  criterion
	 */
	public BacteriologicAlgorithm(  Vector theBacteriologicMedium,
									FitnessFunction theFitnessFunction,
									MutationFunction theMutationFunction,
									StoppingCriterion theStoppingCriterion
									) {
		
		this(theBacteriologicMedium, theFitnessFunction, theMutationFunction, theStoppingCriterion, 0, 1000, 1000, 0, 0);
	}
	
	
	
	/**
	 * The function called to run the BacteriologicAlgorithm.
	 * It continues to compute new generations of bacteria until the stopping
	 * criterion is true, or the medium is empty
	 */
	public void run() {
		while ( (!(stoppingCriterion.run())) && (numberAlgTurn < maxAlgTurn) && !(bacteriologicMedium.isEmpty()) ) {
			System.out.println("Turn: "+numberAlgTurn);
			computeOneGeneration();
			numberAlgTurn++;
		}
	}

	
	
	/**
	 * The three main steps of a generation. If a bacterium is present since too many turns,
	 * it is removed.
	 */
	public void computeOneGeneration() {
		filter();
		mutate();
		memorize();
	}
	

	
	/**
	 * Calls the main function in the FilteringFunction class
	 */
	public void filter() {
		System.out.println("\n\n--> We run BacteriologicAlgorithm.filter()");
		filteringFunction.run();
	}



	/**
	 * Calls the main function in the MemorizationFunction class
	 */
	public void memorize() {
		System.out.println("--> We run BacteriologicAlgorithm.memorize()");
		memorizationFunction.run();
	}



	/**
	 * Calls the main function in the MutationFunction class
	 */
	public void mutate() {
		System.out.println("--> We run BacteriologicAlgorithm.mutate()");
		mutationFunction.run();
	}


	/**
	 * @return the FitnessFunction object of the BacteriologicAlgorithm
	 */
	public FitnessFunction getFitnessFunction() { 
		return fitnessFunction;
	}


	/**
	 * @return the bacteriologic medium of the BacteriologicAlgorithm
	 */
	public Vector getBacteriologicMedium() {
		return bacteriologicMedium;
	}


	/**
	 * @return the set of Bacteria solving the problem
	 */
	public Vector getSolutionSet() {
		return solutionSet;
	}
	
	
	/**
	 * @return the number of turns the algoritm has already done
	 */
	public int getnumberAlgTurn(){
		return numberAlgTurn;
	}


}
