package fr.inria.triskell.moga.jgap;

import java.util.Iterator;
import java.util.List;
import org.jgap.*;

import fr.inria.triskell.moga.comparer.DefaultDominanceSelector;
import fr.inria.triskell.moga.comparer.IDominanceSelector;

public class SPEA2Breeder implements ISPEA2Breeder {

	private IDominanceSelector selector;

	private  Population mating;

	public SPEA2Breeder(DefaultDominanceSelector defaultDominanceSelector) {
		this.selector=defaultDominanceSelector;
	}

	public SPEA2Population evolveAlpha(SPEA2Population population,
			SPEA2Configuration configuration) throws InvalidConfigurationException {

		this.mating=null;

		//assign fitness
		 BulkFitnessFunction bulkFunction = configuration.getBulkFitnessFunction();
		 if (bulkFunction != null) {
		      /**@todo utilize jobs: bulk fitness function is not so important for a
		       * prototype! */
			 Population p=new Population(configuration,configuration.getArchiveSize()+configuration.getPopulationSize());
			 
			 for(IChromosome chrom:(List<IChromosome>)population.getPopulation().getChromosomes()){
				 p.addChromosome(chrom);
			 }
			
			 for(IChromosome chrom:(List<IChromosome>)population.getArchive().getChromosomes()){
				 p.addChromosome(chrom);
			 }
			 
			 bulkFunction.evaluate(p);
		 }
		 

		//select the non-dominated individuals
		List<IChromosome> selected=this.selector.selectNonDominated(population);

		// put the into the archive
		SPEA2Population newPop=new SPEA2Population(configuration,
				configuration.getPopulationSize(),
				configuration.getArchiveSize());

		for(IChromosome chrom:selected){
			newPop.getArchive().addChromosome(chrom);
		}

		if(newPop.getArchive().size() > configuration.getArchiveSize()){
			//truncate the population
			Population p=new Population(configuration,configuration.getArchiveSize());
			for(int i=0;i<configuration.getArchiveSize();i++){
				p.addChromosome(newPop.getArchive().getChromosome(i));
			}
			newPop.setArchive(p);
			System.err.println("TRUNCATION");
		}
		else if(newPop.getArchive().size() < configuration.getArchiveSize()){
			List<IChromosome> dominated=this.selector.selectDominated(population);
			int missing=configuration.getArchiveSize()-newPop.getArchive().size();
			if(dominated.size() > missing){
				for(int i=0;i<missing;i++){
					newPop.getArchive().addChromosome(dominated.get(i));
				}
			}
			else{
				for(IChromosome chrom:dominated){
					newPop.getArchive().addChromosome(chrom);
				}
			}
		}
		return newPop;
	}

	public SPEA2Population evolveBeta(SPEA2Population population,
			SPEA2Configuration configuration)
		throws InvalidConfigurationException {
		
		// mating - natural selection=
		this.mating=this.applyNaturalSelectors(configuration, population.getArchive(), true);
		
		//genetic operators
		this.applyGeneticOperators(configuration, this.mating);

		//put in the right place
		if(this.mating.size() > configuration.getPopulationSize()){
			for(int i=0;i<configuration.getPopulationSize();i++){
				population.getPopulation().addChromosome(this.mating.getChromosome(i));
			}
		}
		else{
			for(IChromosome chrom:(List<IChromosome>)this.mating.getChromosomes()){
				population.getPopulation().addChromosome(chrom);
			}
		}
		//finish
		this.mating=null;
		return population;
	}

	protected void applyGeneticOperators(Configuration a_config, Population a_pop) {
		List geneticOperators = a_config.getGeneticOperators();
		Iterator operatorIterator = geneticOperators.iterator();
		while (operatorIterator.hasNext()) {
			GeneticOperator operator = (GeneticOperator) operatorIterator.next();
			/**@todo utilize jobs: integrate job into GeneticOperator*/
			operator.operate(a_pop, a_pop.getChromosomes());
		}
	}
	protected Population applyNaturalSelectors(Configuration a_config,
			Population a_pop, boolean a_processBeforeGeneticOperators) {
		/**@todo optionally use working pool*/
		try {
			// Process all natural selectors applicable before executing the
			// genetic operators (reproduction, crossing over, mutation...).
			// -------------------------------------------------------------
			int selectorSize = a_config.getNaturalSelectorsSize(
					a_processBeforeGeneticOperators);
			if (selectorSize > 0) {
				int population_size = a_config.getPopulationSize();
				// Only select part of the previous population into this generation.
				// -----------------------------------------------------------------
				/*population_size = (int) Math.round(population_size *
						a_config.getSelectFromPrevGen());
						*/
				int single_selection_size;
				Population new_population = new Population(a_config,population_size);
				NaturalSelector selector;
				// Repopulate the population of chromosomes with those selected
				// by the natural selector. Iterate over all natural selectors.
				// ------------------------------------------------------------
				for (int i = 0; i < selectorSize; i++) {
					selector = a_config.getNaturalSelector(a_processBeforeGeneticOperators, i);
					if (i == selectorSize - 1 && i > 0) {
						// Ensure the last NaturalSelector adds the remaining Chromosomes.
						// ---------------------------------------------------------------
						single_selection_size = population_size - new_population.size();
					}
					else {
						single_selection_size = population_size / selectorSize;
					}
					// Do selection of chromosomes.
					// ----------------------------
					/**@todo utilize jobs: integrate job into NaturalSelector!*/
					selector.select(single_selection_size, a_pop, new_population);
					// Clean up the natural selector.
					// ------------------------------
					selector.empty();
				}
				return new_population;
			}
			else {
				return a_pop;
			}
		} catch (InvalidConfigurationException iex) {
			// This exception should never be reached.
			// ---------------------------------------
			throw new IllegalStateException(iex);
		}
	}
}
