package fr.inria.bacteria;

public interface IRanker {
	/**
	 * Ranks the solution set solutions by their fitness value and returns them ordered from
	 * the lowest to the highest value (when the problem is minimization)
	 * @param solutionSet
	 * @return an ordered solution set
	 */
	ISolutionSet rank(ISolutionSet solutionSet);

}
