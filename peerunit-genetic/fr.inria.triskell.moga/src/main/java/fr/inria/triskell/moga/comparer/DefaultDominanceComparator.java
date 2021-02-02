package fr.inria.triskell.moga.comparer;

import org.jgap.Chromosome;
import org.jgap.IChromosome;
/**
 * given two solutions x, y, if for all the objective values i in x and j in y i >= i, then
 * x dominates y
 * 
 * @author freddy
 */
public class DefaultDominanceComparator implements IDominanceComparator{

	public int compare(IChromosome o1, IChromosome o2) {
		
		Chromosome chr0=(Chromosome)o1;
		Chromosome chr1=(Chromosome)o2;
		
		int count0=0;
		int count1=0;
		
		for (int nObj = 0; nObj < chr0.getMultiObjectives().size();nObj++){
			
			double v0=(Double) chr0.getMultiObjectives().get(nObj);
			double v1=(Double) chr1.getMultiObjectives().get(nObj);
			
			if(v0 > v1){
				count0++;
			}
			else if(v0 < v1){
				count1++;
			}
		}
		if(count0 > 0 && count1 > 0){
			return 0;
		}
		else{
			if(count0 > 0){
				return 1;
			}
			else if(count1 > 0) {
				return -1;
			}
			return 0; 
		}
	}

}
