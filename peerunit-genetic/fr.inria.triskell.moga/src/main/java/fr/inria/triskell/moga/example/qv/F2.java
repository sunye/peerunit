package fr.inria.triskell.moga.example.qv;

import org.jgap.Gene;
import org.jgap.IChromosome;
import org.jgap.impl.DoubleGene;

import fr.inria.triskell.moga.IObjective;

public class F2 implements IObjective {

	public double evaluate(IChromosome chromosome) {
		Gene[] genes=chromosome.getGenes();
		double sum=0;
		for(Gene gene:genes){
			DoubleGene d=(DoubleGene)gene;
			double p1=Math.pow((d.doubleValue()-1.5),2);
			double p2=10*Math.cos(2*Math.PI*(d.doubleValue()-1.5));
			sum=p1-p2+10;
		}
		sum=sum/genes.length;
		sum=Math.sqrt(Math.sqrt(sum));
		return sum;
	}

}
