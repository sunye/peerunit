package fr.inria.mdca.mba;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.BaseSerieInstance;
import fr.inria.mdca.mba.ga.McdaFixer;
import fr.inria.mdca.mba.ga.NaiveGeneticAlgorithm;

public class BactereologicAlgorithm {
	
	static Logger logger = LogManager.getLogger(BactereologicAlgorithm.class);
	
	private BaseSerieInstance solution;

	private ArrayList<BaseInstance> medium;
	
	private FitnessFunction fitnessFunction;
	
	private MemorizationFunction memorizationFunction;
	
	private FilteringFunction filteringFunction;
	
	private StoppingCriterion stoppingCriterion;
	
	private MutationFunction mutationFunction;
	
	private BaseModel model;
	
	private HashMap<BaseInstance,Float> relFitnessCash = new HashMap<BaseInstance,Float> ();

	private int numberAlgTurn=0;

	private int maxAlgTurn;
	
	private float localsearchProb=0;
	
	private float bestFitness=0;
	private float oldfiness=-1;
	
	private int turns=0;
	
	private NaiveGeneticAlgorithm ga;

	private int turnLimi=5;
	
	private McdaFixer fixer;
	
	private StatisticTrace tracer;
	
	private boolean doLocalOptimization=true;
	
	public StatisticTrace getTracer() {
		return tracer;
	}



	public void setTracer(StatisticTrace tracer) {
		this.tracer = tracer;
	}



	public int getTurns() {
		return turns;
	}



	public void setTurns(int turns) {
		this.turns = turns;
	}



	public void run() {
		
		initialize();
	
		while ( (!(stoppingCriterion.run())) && (numberAlgTurn < maxAlgTurn) && !(medium.isEmpty()) ) {
			logger.debug("Turn: "+numberAlgTurn);
			computeOneGeneration();
			numberAlgTurn++;
		}
		
	}

	
	
	private void initialize() {
		this.fitnessFunction.initialize();	
		this.fixer=new McdaFixer();
		this.fixer.setModel(model);
	}



	/**
	 * The three main steps of a generation. If a bacterium is present since too many turns,
	 * it is removed.
	 */
	public void computeOneGeneration() {
		
		filter();
		
		float r = (float) Math.random();
		logger.debug("Stuck index: "+this.turns);
		logger.debug("Prob index: "+r);
		
		//turns=0;
		
		if( turns > this.turnLimi){
			logger.debug("Stucked in a local solution, running the solution fixer");
			this.fixer.fix(this.getSolution().getInstanceSet());
			this.turns=0;
			logger.debug("Local search turn... genetic optimization");
			this.bestFitness=this.fitnessFunction.fitness(this.solution.getInstanceSet());
			if(this.doLocalOptimization){
				ga.run();
				getFitnessFunction().reset();
				float fitness=getFitnessFunction().fitness(this.getSolution().getInstanceSet());
				logger.debug("bestSolution: "+fitness);
			}
			
		}
		else if(this.localsearchProb<r){
			logger.debug("Regular turn... mutation + filtering");
			mutate();
			memorize();
		}
		else{
			logger.debug("Local search turn... genetic optimization :"+this.localsearchProb+" < "+r);
			ga.run();
			getFitnessFunction().reset();
			float fitness=getFitnessFunction().fitness(this.getSolution().getInstanceSet());
			logger.debug("bestSolution: "+fitness);
		}
		
		if(this.tracer!=null){
			//this.tracer.addTrace(numberAlgTurn, bestFitness, this.getSolution().getInstanceSet().size(),0);
			this.tracer.addTrace( bestFitness, this.getSolution().getInstanceSet().size(),0);
		}
	}
	

	
	public MutationFunction getMutationFunction() {
		return mutationFunction;
	}



	public void setMutationFunction(MutationFunction mutationFunction) {
		this.mutationFunction = mutationFunction;
	}



	public int getNumberAlgTurn() {
		return numberAlgTurn;
	}



	public void setNumberAlgTurn(int numberAlgTurn) {
		this.numberAlgTurn = numberAlgTurn;
	}



	public int getMaxAlgTurn() {
		return maxAlgTurn;
	}



	public void setMaxAlgTurn(int maxAlgTurn) {
		this.maxAlgTurn = maxAlgTurn;
	}



	public float getLocalsearchProb() {
		return localsearchProb;
	}



	public void setLocalsearchProb(float localsearchProb) {
		this.localsearchProb = localsearchProb;
	}



	/**
	 * Calls the main function in the FilteringFunction class
	 */
	public void filter() {
		logger.debug("\n\n--> We run BacteriologicAlgorithm.filter()");
		filteringFunction.run();
	}



	/**
	 * Calls the main function in the MemorizationFunction class
	 */
	public void memorize() {
		logger.debug("--> We run BacteriologicAlgorithm.memorize()");
		memorizationFunction.run();
		if(this.bestFitness>this.oldfiness){
			this.oldfiness=this.bestFitness;
			this.turns=0;
		}
		else{
			this.turns++;
		}
		
	}



	/**
	 * Calls the main function in the MutationFunction class
	 */
	public void mutate() {
		logger.debug("--> We run BacteriologicAlgorithm.mutate()");
		mutationFunction.run();
	}
	
	public int getnumberAlgTurn(){
		return numberAlgTurn;
	}

	
	
	
	public BactereologicAlgorithm(
			ArrayList<BaseInstance> medium, 
			FitnessFunction fitnessFunction,
			FitnessFunction gafitnessFunction,
			MemorizationFunction memorizationFunction,
			FilteringFunction filteringFunction,
			StoppingCriterion stoppingCriterion,
			MutationFunction mutationFunction,
			BaseModel model) {
		super();
		this.medium = medium;
		this.fitnessFunction = fitnessFunction;
		this.fitnessFunction.setBactereologicAlgorithm(this);
		this.memorizationFunction = memorizationFunction;
		this.memorizationFunction.setBactereologicAlgorithm(this);
		this.filteringFunction = filteringFunction;
		this.filteringFunction.setBactereologicAlgorithm(this);
		this.stoppingCriterion = stoppingCriterion;
		this.stoppingCriterion.setBactereologicAlgorithm(this);
		this.mutationFunction = mutationFunction;
		this.mutationFunction.setBactereologicAlgorithm(this);
		gafitnessFunction.setBactereologicAlgorithm(this);
		this.setModel(model);
		
		this.ga=new NaiveGeneticAlgorithm(this,gafitnessFunction,100,20,40,20,0.2f,0.3f);
	}
	
	/**
	 * 
	 * use only to mock the results
	 */
	public BactereologicAlgorithm() {
		
	}



	public BaseSerieInstance getSolution() {
		if(solution==null)
			solution=new BaseSerieInstance();
		return solution;
	}

	public void setSolution(BaseSerieInstance solution) {
		this.solution = solution;
	}

	public ArrayList<BaseInstance> getMedium() {
		if(medium==null)
			medium=new ArrayList<BaseInstance>();
		return medium;
	}

	public void setMedium(ArrayList<BaseInstance> medium) {
		this.medium = medium;
	}

	public FitnessFunction getFitnessFunction() {
		return fitnessFunction;
	}

	public void setFitnessFunction(FitnessFunction fitnessFunction) {
		this.fitnessFunction = fitnessFunction;
	}

	public MemorizationFunction getMemorizationFunction() {
		return memorizationFunction;
	}

	public void setMemorizationFunction(MemorizationFunction memorizationFunction) {
		this.memorizationFunction = memorizationFunction;
	}

	public FilteringFunction getFilteringFunction() {
		return filteringFunction;
	}

	public void setFilteringFunction(FilteringFunction filteringFunction) {
		this.filteringFunction = filteringFunction;
	}

	public StoppingCriterion getStoppingCriterion() {
		return stoppingCriterion;
	}

	public void setStoppingCriterion(StoppingCriterion stoppingCriterion) {
		this.stoppingCriterion = stoppingCriterion;
	}



	public void setModel(BaseModel model) {
		this.model = model;
	}



	public BaseModel getModel() {
		return model;
	}



	public void setRelFitnessCash(HashMap<BaseInstance,Float>  relFitnessCash) {
		this.relFitnessCash = relFitnessCash;
	}



	public HashMap<BaseInstance,Float>  getRelFitnessCash() {
		return relFitnessCash;
	}
	
	public void updateSolutionFitness(){
		this.getFitnessFunction().updateSolutionSetFitness();
	}



	public void setGa(NaiveGeneticAlgorithm ga) {
		this.ga = ga;
	}



	public NaiveGeneticAlgorithm getGa() {
		return ga;
	}



	public void setBestFitness(float bestFitness) {
		this.bestFitness = bestFitness;
	}



	public float getBestFitness() {
		return bestFitness;
	}



	public void setDoLocalOptimization(boolean doLocalOptimization) {
		this.doLocalOptimization = doLocalOptimization;
	}



	public boolean isDoLocalOptimization() {
		return doLocalOptimization;
	}
	
}
