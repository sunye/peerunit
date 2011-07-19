package fr.inria.triskell.moga.comparer;

import java.util.List;

import fr.inria.triskell.moga.jgap.SPEA2Population;
import org.jgap.*;

public interface IDominanceSelector {
	public List<IChromosome> selectNonDominated(SPEA2Population population);
	public List<IChromosome> selectDominated(SPEA2Population population);
}
