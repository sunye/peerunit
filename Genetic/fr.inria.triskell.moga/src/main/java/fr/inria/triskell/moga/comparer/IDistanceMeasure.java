package fr.inria.triskell.moga.comparer;

import org.jgap.Population;

public interface IDistanceMeasure {
	public double[][] distanceObjectives(Population population);
}
