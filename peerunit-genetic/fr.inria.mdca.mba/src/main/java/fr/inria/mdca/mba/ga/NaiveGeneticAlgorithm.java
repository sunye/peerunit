package fr.inria.mdca.mba.ga;

import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.mba.BactereologicAlgorithm;
import fr.inria.mdca.mba.FitnessFunction;
import fr.inria.mdca.util.QuickSortSpecial;
import fr.inria.mdca.util.RandomHelper;

public class NaiveGeneticAlgorithm {

	private BactereologicAlgorithm bactereologicAlgorithm;
	
	static Logger logger = Logger.getLogger(NaiveGeneticAlgorithm.class);
	
	private int iteration;
	private int starvation;
	private int populationSize;
	private int generationBreed;
	
	private float mutationrate=0.5f;
	private float crossoverrate=0.5f;
	
	private float currentFitness=0;
	
	Crossover crossover=new Crossover();
	
	Mutation mutation=new Mutation();
	
	FitnessFunction fitnessFunction;
	
	
	ArrayList<BaseInstance> solution;
	ArrayList<ArrayList<BaseInstance>> offspring=new ArrayList<ArrayList<BaseInstance>>();
	Hashtable<ArrayList<BaseInstance>,Float> scores=new Hashtable<ArrayList<BaseInstance>, Float>();
	
	
	
	public NaiveGeneticAlgorithm(BactereologicAlgorithm bactereologicAlgorithm,FitnessFunction function,
			int iteration, int starvation, int populationSize,
			int generationBreed, float mutationrate, float crossoverrate) {
		super();
		this.bactereologicAlgorithm = bactereologicAlgorithm;
		this.iteration = iteration;
		this.starvation = starvation;
		this.populationSize = populationSize;
		this.generationBreed = generationBreed;
		this.mutationrate = mutationrate;
		this.crossoverrate = crossoverrate;
		this.fitnessFunction=function;
	}


	public void run(){
		logger.debug("running the GA");
	
		solution=this.bactereologicAlgorithm.getSolution().getInstanceSet();
		this.currentFitness=this.bactereologicAlgorithm.getBestFitness();
		
		logger.debug("Solution fitness: "+this.currentFitness);
		
		this.initialize();
		int starving=0;
		int iter=0;
		for(int i=0;i<iteration;i++){
			if(starving>=starvation){
				break;
			}
			this.breed();
			this.fitnessEvaluation();
			this.clean();
			/*if(solution.equals(this.bactereologicAlgorithm.getSolution().getInstanceSet())){
				starving++;
			}*/
			if(this.bactereologicAlgorithm.getTracer()!=null){
			//	this.bactereologicAlgorithm.getTracer().addTrace(iter, currentFitness,this.solution.size(),1);
				this.bactereologicAlgorithm.getTracer().addTrace(currentFitness,this.solution.size(),1);
			}
			iter++;
		}

		if(!this.solution.containsAll(this.bactereologicAlgorithm.getSolution().getInstanceSet())){
			/*
			 * reinitialize the fitness function
			 */
			
			/*
			 * register new fitness for algorithm elements 
			 */
			
			this.bactereologicAlgorithm.setBestFitness(this.currentFitness);
			this.bactereologicAlgorithm.getSolution().setInstanceSet(solution);
			logger.debug("Updating Solution with best fitness: "+this.currentFitness);
			
		}
		
	}


	private void initialize() {
		this.offspring.clear();
		this.scores.clear();
//		logger.debug("initializing population");
		for(int i=0;i<populationSize;i++){
			float r = (float) Math.random();
			if(r>this.crossoverrate){
				if(this.offspring.size()<2){
					this.offspring.add(mutation.mutate(solution, this.bactereologicAlgorithm.getModel()));
				}
				else{
					int mutate=RandomHelper.randomValue(0, this.offspring.size()-1);
					this.offspring.add(mutation.mutate(this.offspring.get(mutate), this.bactereologicAlgorithm.getModel()));
				}
			}
			r = (float) Math.random();
			if(r>this.mutationrate){
				if(this.offspring.size()<2){
					this.offspring.add(crossover.permute(solution));
				}
				else{
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
	}


	private void clean() {
//		logger.debug("Cleaning the population");
		//delete the X worst results
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
//		logger.debug("evaluating individuals");
	
		for(ArrayList<BaseInstance> instances:offspring){
			float fitness=fitnessFunction.fitness(instances);
			this.scores.put(instances, new Float(fitness));
			if(fitness>this.currentFitness && !instances.containsAll(this.solution)){
				this.currentFitness=fitness;
				this.solution=instances;
			}
		}
		
	}


	public void breed(){
//		logger.debug("breeding");
		for(int i=this.offspring.size()-1;i<populationSize;i++){
			float r = (float) Math.random();
			if(r>this.crossoverrate){
				int mutate=RandomHelper.randomValue(0, this.offspring.size()-1);
				this.offspring.add(mutation.mutate(this.offspring.get(mutate), this.bactereologicAlgorithm.getModel()));
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
	
}
