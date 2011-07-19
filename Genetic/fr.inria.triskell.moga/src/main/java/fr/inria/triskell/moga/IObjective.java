package fr.inria.triskell.moga;

import org.jgap.IChromosome;

public interface IObjective {
	public double evaluate(IChromosome chromosome);
}
