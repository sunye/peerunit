package fr.inria.triskell.moga.example.first;

import java.util.ArrayList;

import org.jgap.Chromosome;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.DoubleGene;


import fr.inria.triskell.moga.jgap.SPEA2Configuration;
import fr.inria.triskell.moga.jgap.SPEA2Genotype;

public class First {

	/**
	 * @param args
	 * @throws InvalidConfigurationException 
	 */
	public static void main(String[] args) throws InvalidConfigurationException {
		SPEA2Configuration configuration=new SPEA2Configuration(10,10);
		F1 f1=new F1();
		F2 f2=new F2();
		configuration.getFitness().getObjectives().add(f1);
		configuration.getFitness().getObjectives().add(f2);
		configuration.setPopulationSize(10);
		Gene[] sampleGenes = new Gene[2];
		sampleGenes[0] = new DoubleGene(configuration,0,10);
		sampleGenes[1] = new DoubleGene(configuration,0, 10);
		//sampleGenes[2] = new DoubleGene(configuration,0, 10);
		//sampleGenes[3] = new DoubleGene(configuration,0, 10);
		 IChromosome sampleChromosome = new Chromosome(configuration, sampleGenes);
		 configuration.setSampleChromosome(sampleChromosome);
		 SPEA2Genotype population = SPEA2Genotype.randomInitialGenotype(configuration);
		 for (int i = 0;; i++) {
			population.evolveAlpha();
			if(i>=1000)
				break;
			population.evolveBeta();
		 }
		 ArrayList<IChromosome> solution = population.getSolution();
		 
		 for(IChromosome chrom:solution){
			 Gene[] genes=chrom.getGenes();
			 System.out.println("f1: "+f1.evaluate(chrom)+ " f2: "+f2.evaluate(chrom)+ " fitness: "+chrom.getFitnessValue()+" values:  "+genes[0]+" "+genes[1]+" "/*+genes[2]+" "+genes[3]*/);
			 
		 }
	}

}
