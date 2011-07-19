package fr.inria.bacteria;

public interface IObjective {
	public IObjectiveValue evaluate(IBacterium bacterium);
}
