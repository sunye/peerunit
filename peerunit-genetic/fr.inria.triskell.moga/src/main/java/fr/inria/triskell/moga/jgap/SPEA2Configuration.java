package fr.inria.triskell.moga.jgap;

import org.jgap.Configuration;
import org.jgap.InvalidConfigurationException;
import org.jgap.event.EventManager;
import org.jgap.impl.ChromosomePool;
import org.jgap.impl.CrossoverOperator;
import org.jgap.impl.MutationOperator;
import org.jgap.impl.StockRandomGenerator;

import fr.inria.triskell.moga.comparer.DefaultDistanceMeasure;
import fr.inria.triskell.moga.comparer.DefaultDominanceComparator;
import fr.inria.triskell.moga.comparer.DefaultDominanceSelector;

public class SPEA2Configuration extends Configuration {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7408760668049878166L;
	
	private SPEA2FitnessFunction fitness;
	
	public SPEA2FitnessFunction getFitness() {
		return fitness;
	}

	public void setFitness(SPEA2FitnessFunction fitness) {
		this.fitness = fitness;
	}

	public void setBreeder(ISPEA2Breeder breeder) {
		this.breeder = breeder;
	}

	public SPEA2Configuration(int archiveSize,
			int populationSize) {
		super();
		this.breeder = new SPEA2Breeder(new DefaultDominanceSelector());
		this.archiveSize = archiveSize;
		this.populationSize = populationSize;
		
		try {
			super.setPopulationSize(populationSize);
			setRandomGenerator(new StockRandomGenerator());
			addNaturalSelector(new BinaryTournamentSelectionWR(this), true);
			//set fitness evaluator
			setFitnessEvaluator(new SPEA2FitnessEvaluator());
			setEventManager(new EventManager());
			this.fitness=new SPEA2FitnessFunction();
			this.fitness.setComparator(new DefaultDominanceComparator());
			this.fitness.setMeasure(new DefaultDistanceMeasure());
			
			setBulkFitnessFunction(this.fitness);
			setChromosomePool(new ChromosomePool());
			// for the moment just use the default operators
			addGeneticOperator(new CrossoverOperator(this, 0.35d));
			addGeneticOperator(new MutationOperator(this, 12));
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ISPEA2Breeder breeder;
	
	private int archiveSize;
	
	private int populationSize;
	
	private int round=0;
	
	
	
	public int getArchiveSize() {
		return archiveSize;
	}

	public void setArchiveSize(int archiveSize) {
		this.archiveSize = archiveSize;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	public void setSPEA2Breeder(ISPEA2Breeder breeder) {
		this.breeder = breeder;
	}

	public ISPEA2Breeder getSPEA2Breeder() {
		return breeder;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public int getRound() {
		return round;
	}
}
