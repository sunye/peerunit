package fr.inria.triskell.moga.jgap;


import java.util.*;

import org.jgap.*;

public class SPEA2Genotype{
	
	private SPEA2Population population;
	
	private SPEA2Configuration m_activeConfiguration;

	public SPEA2Genotype(SPEA2Configuration a_configuration,
			IChromosome[] a_initialChromosomes)
	throws InvalidConfigurationException {
		this(a_configuration, new SPEA2Population(a_configuration, a_initialChromosomes));
	}

	public SPEA2Genotype(SPEA2Configuration a_configuration, SPEA2Population a_population)
	throws InvalidConfigurationException {
		// Sanity checks: Make sure neither the Configuration, nor the array
		// of Chromosomes, nor any of the Genes inside the array is null.
		// -----------------------------------------------------------------
		if (a_configuration == null) {
			throw new IllegalArgumentException(
			"The Configuration instance must not be null.");
		}
		if (a_population == null) {
			throw new IllegalArgumentException(
			"The Population must not be null.");
		}
		/*		for (int i = 0; i < a_population.size(); i++) {
			if (a_population.getChromosome(i) == null) {
				throw new IllegalArgumentException(
						"The Chromosome instance at index " + i + " of the array of " +
						"Chromosomes is null. No Chromosomes instance in this array " +
				"must not be null.");
			}
		}*/
		m_activeConfiguration=a_configuration;
		setPopulation(a_population);
		// Lock the settings of the configuration object so that it cannot
		// be altered.
		// ---------------------------------------------------------------
	}
	public SPEA2Genotype(SPEA2Configuration a_configuration)
		throws InvalidConfigurationException {
		
	}

	public SPEA2Configuration getConfiguration() {
		return m_activeConfiguration;
	}
	public SPEA2Population getPopulation() {
		return population;
	}

	public void setPopulation(SPEA2Population population) {
		this.population = population;
	}

	public synchronized void evolveAlpha() throws InvalidConfigurationException {
		ISPEA2Breeder breeder = getConfiguration().getSPEA2Breeder();
		SPEA2Population newPop = breeder.evolveAlpha(getPopulation(), getConfiguration());
		setPopulation(newPop);
	}
	public synchronized void evolveBeta() throws InvalidConfigurationException {
		ISPEA2Breeder breeder = getConfiguration().getSPEA2Breeder();
		SPEA2Population newPop = breeder.evolveBeta(getPopulation(), getConfiguration());
		setPopulation(newPop);
	}

	public static SPEA2Genotype randomInitialGenotype(SPEA2Configuration
			a_configuration)
	throws InvalidConfigurationException {
		if (a_configuration == null) {
			throw new IllegalArgumentException(
			"The Configuration instance may not be null.");
		}

		// Create an array of chromosomes equal to the desired size in the
		// active Configuration and then populate that array with Chromosome
		// instances constructed according to the setup in the sample
		// Chromosome, but with random gene values (alleles). The Chromosome
		// class randomInitialChromosome() method will take care of that for
		// us.
		// ------------------------------------------------------------------
		int populationSize = a_configuration.getPopulationSize();
		int archiveSize=a_configuration.getArchiveSize();
		
		SPEA2Population pop = new SPEA2Population(a_configuration, populationSize,archiveSize);
		// Do randomized initialization.
		// -----------------------------
		SPEA2Genotype result = new SPEA2Genotype(a_configuration, pop);
		result.fillPopulation(populationSize);
		return result;
	}

	@SuppressWarnings("unchecked")
	public void fillPopulation(final int a_num)
		throws InvalidConfigurationException {
		IChromosome sampleChrom = getConfiguration().getSampleChromosome();
		Class<? extends IChromosome> sampleClass = sampleChrom.getClass();
		IInitializer chromIniter = getConfiguration().getJGAPFactory().getInitializerFor(sampleChrom, sampleClass);
		if (chromIniter == null) {
			throw new InvalidConfigurationException("No initializer found for class "+ sampleClass);
		}
		try {
			for (int i = 0; i < a_num; i++) {
				getPopulation().getPopulation().addChromosome( (IChromosome) chromIniter.perform(sampleChrom,sampleClass, null));
			}
		} catch (Exception ex) {
			// Try to propagate exception, see "bug" 1661635.
			// ----------------------------------------------
			if (ex.getCause() != null) {
				throw new IllegalStateException(ex.getCause().toString());
			}
			else {
				throw new IllegalStateException(ex.getMessage());
			}
		}
	}
	
	public ArrayList<IChromosome> getSolution(){
		Population archive=this.getPopulation().getArchive();
		List<IChromosome> ap=archive.getChromosomes();
		if(ap!=null){
			return new ArrayList<IChromosome>(archive.getChromosomes());
		}
		return new ArrayList<IChromosome>();
	}
}

