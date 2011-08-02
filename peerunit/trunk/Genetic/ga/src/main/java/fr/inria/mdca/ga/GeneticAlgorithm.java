package fr.inria.mdca.ga;

import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.util.QuickSortSpecial;
import fr.inria.mdca.util.RandomHelper;
import fr.inria.mdca.util.RandomInstanceGenerator;
import fr.inria.mdca.util.TupleCounter;
import fr.inria.mdca.util.WrongOrderException;

public class GeneticAlgorithm {

	static Logger logger = Logger.getLogger(GeneticAlgorithm.class);
	
	private int iteration;
	private int starvation;
	private int populationSize;
	private int generationBreed;
	private int solutionSize;
	private int maxRestarts;
	
	private float mutationrate=0.5f;
	private float crossoverrate=0.5f;
	
	private float currentFitness=0;
	
	private BaseModel model;
	
	Crossover crossover=new Crossover();
	
	Mutation mutation=new Mutation();
	
	FitnessFunction fitnessFunction;
	private StatisticTrace tracer;
	
	public float getCurrentFitness() {
		return currentFitness;
	}


	public void setCurrentFitness(float currentFitness) {
		this.currentFitness = currentFitness;
	}


	public BaseModel getModel() {
		return model;
	}


	public void setModel(BaseModel model) {
		this.model = model;
	}


	public ArrayList<BaseInstance> getSolution() {
		return solution;
	}


	public void setSolution(ArrayList<BaseInstance> solution) {
		this.solution = solution;
	}


	ArrayList<BaseInstance> solution;
	ArrayList<ArrayList<BaseInstance>> offspring=new ArrayList<ArrayList<BaseInstance>>();
	Hashtable<ArrayList<BaseInstance>,Float> scores=new Hashtable<ArrayList<BaseInstance>, Float>();
	
	
	
	public GeneticAlgorithm(BaseModel model,FitnessFunction function,
			int iteration, int starvation, int populationSize,
			int generationBreed, float mutationrate, float crossoverrate,int solutionSize,int maxRestarts) {
		super();
		this.model = model;
		this.iteration = iteration;
		this.starvation = starvation;
		this.populationSize = populationSize;
		this.generationBreed = generationBreed;
		this.mutationrate = mutationrate;
		this.crossoverrate = crossoverrate;
		this.fitnessFunction=function;
		this.solutionSize=solutionSize;
		this.maxRestarts=maxRestarts;
		this.fitnessFunction.setModel(model);
	}


	public void run() throws WrongOrderException{
		logger.debug("running the GA");
	
		
	
		this.initialize();
		this.solution=new ArrayList<BaseInstance>();
		
		float goal=TupleCounter.calculateToupleNumber(model,model.getTwise() ,model.getOrder());

		int starving=0;
		int restart=0;
		
		this.currentFitness=-1;
		float bestFitness=-1;
		
		for(int i=0;i<iteration;i++){
			logger.debug("iteration: "+i);
			logger.debug("starving: "+starving);
			logger.debug("current fitness: "+this.currentFitness+" / "+goal);
			if(starving>=starvation){
				
				if(restart>maxRestarts){
					logger.debug("breaking off");
					break;
				}
				else{
					logger.debug("restarting");
					restart++;
					i=0;
					this.initialize();
					starving=0;
				}
				
			}
			
			this.fitnessEvaluation();
			
			if(this.tracer!=null){
				//this.tracer.addTrace(numberAlgTurn, bestFitness, this.getSolution().getInstanceSet().size(),0);
				this.tracer.addTrace( currentFitness,this.solution.size(),0);
			}
			
			if(this.currentFitness==goal){
				logger.debug("best solution found: "+this.currentFitness);
				logger.debug("best solution: "+this.solution);
				break;
			}
			if(this.currentFitness==bestFitness){
				starving++;
			}else{
				bestFitness=this.currentFitness;
				logger.debug("new best fitness: "+this.currentFitness+" / "+goal);
				logger.debug("new best solution: "+this.solution+"");
				starving=0;
			}
			
			this.clean();
			
			this.breed();
			
			
		}
		
	}


	private void initialize() {
		
		logger.debug("initializing population");
		
		this.offspring.clear();
		this.scores.clear();
		
		for(int i=0;i<populationSize;i++){
			RandomInstanceGenerator generator=new RandomInstanceGenerator(model);
			this.offspring.add(generator.generateRandom(this.solutionSize));
		}
	}


	private void clean() {
		logger.debug("Cleaning the population");
		double[] element=new double[this.scores.values().size()];
		ArrayList<Float> fs=new ArrayList<Float>(this.scores.values());
		for(int i=0;i<fs.size();i++){
			Float f=fs.get(i);
			element[i]=f.doubleValue();
		}
		QuickSortSpecial.quicksort(element);
		this.offspring=new ArrayList<ArrayList<BaseInstance>>();
		boolean stop=false;
		int j=0;
		int reverseIndex=element.length-1;
		while(!stop){
			for(ArrayList<BaseInstance> instances:this.scores.keySet()){
				if(j>(this.populationSize-this.generationBreed)){
					stop=true;
					break;
				}
				Float f=this.scores.get(instances);
				
				if(f.doubleValue()==element[reverseIndex-j]){
					
					this.offspring.add(instances);
					j++;
				}
			}
		}
		this.scores.clear();
	}


	private void fitnessEvaluation() {
		logger.debug("evaluating individuals");
		
		for(ArrayList<BaseInstance> instances:offspring){
			float fitness=fitnessFunction.fitness(instances);
			this.scores.put(instances, new Float(fitness));
			if(fitness>this.currentFitness){
				this.currentFitness=fitness;
				this.solution=instances;
				logger.debug("Better solution fitness: "+this.currentFitness);
			}
		}
		
	}


	public void breed(){
		logger.debug("breeding");
		for(int i=this.offspring.size()-1;i<populationSize;i++){
			float r = (float) Math.random();
			if(r>this.crossoverrate){
				int mutate=RandomHelper.randomValue(0, this.offspring.size()-1);
				this.offspring.add(mutation.mutate(this.offspring.get(mutate), model));
			}
			r = (float) Math.random();
			if(r>this.mutationrate){
				r = (float) Math.random();
				if(r>0.6f){
					int mutate=RandomHelper.randomValue(0, this.offspring.size()-1);
					this.offspring.add(crossover.permute(this.offspring.get(mutate)));
				}
				else{
					int a=RandomHelper.randomValue(0, this.offspring.size()-1);
					int b=RandomHelper.randomValue(0, this.offspring.size()-1);
					while(a==b){
						a=RandomHelper.randomValue(0, this.offspring.size()-1);
						b=RandomHelper.randomValue(0, this.offspring.size()-1);
					}
					this.offspring.add(crossover.crossover(this.offspring.get(a), this.offspring.get(b)));
				}
			}
		}
	}

	public void setIteration(int iteration) {
		this.iteration = iteration;
	}

	public int getIteration() {
		return iteration;
	}

	public void setStarvation(int starvation) {
		this.starvation = starvation;
	}

	public int getStarvation() {
		return starvation;
	}


	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}


	public int getPopulationSize() {
		return populationSize;
	}


	public void setMutationrate(float mutationrate) {
		this.mutationrate = mutationrate;
	}


	public float getMutationrate() {
		return mutationrate;
	}


	public void setCrossoverrate(float crossoverrate) {
		this.crossoverrate = crossoverrate;
	}


	public float getCrossoverrate() {
		return crossoverrate;
	}


	public void setGenerationBreed(int generationBreed) {
		this.generationBreed = generationBreed;
	}


	public int getGenerationBreed() {
		return generationBreed;
	}


	public void setTracer(StatisticTrace tracer) {
		this.tracer = tracer;
	}


	public StatisticTrace getTracer() {
		return tracer;
	}
	
}
