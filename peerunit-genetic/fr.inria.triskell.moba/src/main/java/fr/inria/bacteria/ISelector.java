package fr.inria.bacteria;

import java.util.ArrayList;

public interface ISelector {

	ArrayList<IBacterium> select(ISolutionSet tempSolutionSet, ISolutionSet solutionSet);

}
