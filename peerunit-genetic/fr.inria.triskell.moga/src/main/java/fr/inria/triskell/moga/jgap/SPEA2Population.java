package fr.inria.triskell.moga.jgap;

import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.Population;

public class SPEA2Population {
	
	private Population interal_population;
	
	private Population external_population;
	
	public SPEA2Population(SPEA2Configuration a_configuration,
			int population,int archive) throws InvalidConfigurationException {
		this.interal_population=new Population(a_configuration,population);
		this.external_population=new Population(a_configuration,archive);
	}
	

	public SPEA2Population(SPEA2Configuration a_configuration,
			IChromosome[] chromosomes) throws InvalidConfigurationException {
		this.interal_population=new Population(a_configuration,chromosomes);
		this.external_population=new Population(a_configuration,a_configuration.getArchiveSize());
	}


	public void setPopulation(Population interal_population) {
		this.interal_population = interal_population;
	}
	public Population getPopulation() {
		return interal_population;
	}
	public void setArchive(Population external_population) {
		this.external_population = external_population;
	}
	public Population getArchive() {
		return external_population;
	}
}
