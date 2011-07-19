package fr.inria.bacteria;

import java.util.ArrayList;

public interface ISolution  extends IMultiObjetive{

	public ArrayList<IBacterium> getSolution();
	
	public ISolution clone();
	
	public double getFitness();
	
	public void setFitness(double fitness);
	
	public void clean();

}
