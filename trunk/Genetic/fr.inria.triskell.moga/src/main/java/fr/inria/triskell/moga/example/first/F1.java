package fr.inria.triskell.moga.example.first;

import org.jgap.Gene;
import org.jgap.IChromosome;
import org.jgap.impl.DoubleGene;

import fr.inria.triskell.moga.IObjective;

public class F1 implements IObjective {

	public double evaluate(IChromosome chromosome) {
		Gene[] genes=chromosome.getGenes();
		DoubleGene d0=(DoubleGene)genes[0];
		DoubleGene d1=(DoubleGene)genes[1];
//		DoubleGene d2=(DoubleGene)genes[2];
//		DoubleGene d3=(DoubleGene)genes[3];
		
		double formulae=d0.doubleValue()+d1.doubleValue();//+d2.doubleValue()+d3.doubleValue();
		formulae=formulae/20;
		return formulae;
	}

}
