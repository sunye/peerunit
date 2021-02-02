package fr.inria.bacteria;


public interface IBacterium extends IMultiObjetive {
	/**
	 * 
	 * @return the bacterium fitness value
	 */
	public double getFitness();
	/**
	 * 
	 * @param fitness the bacterium fitness value
	 */
	public void setFitness(double fitness);
	/**
	 * clone this bacterium
	 * @return a clone of this bacterium
	 */
	public IBacterium clone();
	/**
	 * 
	 * @param degree how much the bacterium should mutate
	 * @param direction value between -1 and 1 where the mutation should go (increment or decrement). 
	 */
	public void applyMutation(double degree,double direction);
}
