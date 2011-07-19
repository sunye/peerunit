package fr.inria.triskell.moga.comparer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jgap.Chromosome;
import org.jgap.IChromosome;

import fr.inria.triskell.moga.jgap.SPEA2Population;

public class DefaultDominanceSelector implements IDominanceSelector {

	public List<IChromosome> selectDominated(SPEA2Population population) {
		
		List<IChromosome> list=new ArrayList<IChromosome>();
		
		for(IChromosome chrom:(List<IChromosome>)population.getPopulation().getChromosomes()){
			Chromosome c=(Chromosome)chrom;
			int index=c.getMultiObjectives().size()-1;
			Double rawFitness=(Double) c.getMultiObjectives().get(index);
			if(rawFitness>0)
				list.add(chrom);
		}
		for(IChromosome chrom:(List<IChromosome>)population.getArchive().getChromosomes()){
			Chromosome c=(Chromosome)chrom;
			int index=c.getMultiObjectives().size()-1;
			Double rawFitness=(Double) c.getMultiObjectives().get(index);
			if(rawFitness>0)
				list.add(chrom);
		}
		Collections.sort(list,new DefaultComparator());
		return list;
	}

	public List<IChromosome> selectNonDominated(SPEA2Population population) {
		List<IChromosome> list=new ArrayList<IChromosome>();
		
		for(IChromosome chrom:(List<IChromosome>)population.getPopulation().getChromosomes()){
			Chromosome c=(Chromosome)chrom;
			int index=c.getMultiObjectives().size()-1;
			Double rawFitness=(Double) c.getMultiObjectives().get(index);
			if(rawFitness == 0){
				list.add(chrom);
			}
		}
		for(IChromosome chrom:(List<IChromosome>)population.getArchive().getChromosomes()){
			Chromosome c=(Chromosome)chrom;
			int index=c.getMultiObjectives().size()-1;
			Double rawFitness=(Double) c.getMultiObjectives().get(index);
			if(rawFitness == 0){
				list.add(chrom);
			}
		}
		Collections.sort(list,new DefaultComparator());
		return list;
	}

}
