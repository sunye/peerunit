package fr.inria.triskell.moga.example.qv;

import org.jgap.Gene;
import org.jgap.IChromosome;
import org.jgap.impl.DoubleGene;

import fr.inria.triskell.moga.IObjective;

public class F1 implements IObjective {

	public double evaluate(IChromosome chromosome) {
		Gene[] genes=chromosome.getGenes();
		double sum=0;
		for(Gene gene:genes){
			DoubleGene d=(DoubleGene)gene;
			sum=Math.pow(d.doubleValue(),2)-Math.cos(2*Math.PI*d.doubleValue())+10;
		}
		sum=sum/genes.length;
		sum=Math.sqrt(Math.sqrt(sum));
		return sum;
	}

}
