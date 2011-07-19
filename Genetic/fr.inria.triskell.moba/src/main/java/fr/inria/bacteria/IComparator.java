package fr.inria.bacteria;

import java.util.Comparator;

public interface IComparator extends Comparator<IBacterium> {

	int compare(ISolution sp, ISolution s);

}
