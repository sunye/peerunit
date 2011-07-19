package fr.inria.triskell.moga.comparer;

import java.util.Comparator;

import org.jgap.IChromosome;

public class DefaultComparator implements Comparator<IChromosome> {

	public int compare(IChromosome o1, IChromosome o2) {
		
		if(o1.getFitnessValue() > o2.getFitnessValue()){
			return 1;
		}
		else if(o1.getFitnessValue() < o2.getFitnessValue()){
			return -1;
		}
		
		return 0;
	}

}
