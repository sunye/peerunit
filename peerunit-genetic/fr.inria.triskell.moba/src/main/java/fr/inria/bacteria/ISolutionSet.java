package fr.inria.bacteria;

import java.util.ArrayList;

public interface ISolutionSet extends IMultiObjetive{

	public ArrayList<ISolution> getSolutionSet();
	
	public ISolutionSet clone();
		
	public void clean();
	
}
