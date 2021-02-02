package fr.inria.bacteria.impl;

import java.util.ArrayList;

import fr.inria.bacteria.IBacteriaFactory;
import fr.inria.bacteria.IBacterium;
import fr.inria.bacteria.IComparator;
import fr.inria.bacteria.IEvolutionOperator;
import fr.inria.bacteria.IFitnessFunction;
import fr.inria.bacteria.IMedium;
import fr.inria.bacteria.IObjective;
import fr.inria.bacteria.IRanker;
import fr.inria.bacteria.ISelector;
import fr.inria.bacteria.ISolution;
import fr.inria.bacteria.ISolutionSet;
import fr.inria.bacteria.InvalidSetUpException;

public class MOBA {
	
	private ArrayList<IObjective> objectives;
	private IFitnessFunction fitnessFunction;
	private ISelector selector;
	private ISolutionSet solutionSet;
	private ISolutionSet tempSolutionSet;
	private IMedium medium;
	private IComparator comparator;
	private IBacteriaFactory factory;
	private IRanker ranker;
	
	private ArrayList<IEvolutionOperator> operators;
	
	private int solutionSetSize;
	
	private int mediumSize;
	
	public void evolve() throws InvalidSetUpException{
		this.check();
		this.fitnessFunction.bulkfitness(this.getTempSolutionSet());
		this.fitnessFunction.bulkfitness(this.getSolutionSet());
		

		ArrayList<ISolution> tremovable=new ArrayList<ISolution>();
		
		for(int i=0;i<this.getSolutionSet().getSolutionSet().size();i++){
		
			ISolution s=this.getSolutionSet().getSolutionSet().get(i);

			tremovable.clear();
			for(ISolution sp:this.getTempSolutionSet().getSolutionSet()){
				if(sp.getFitness() < s.getFitness() && !tremovable.contains(sp)){
					this.getSolutionSet().getSolutionSet().set(i, sp);
					tremovable.add(sp);
					break;
				}
			}
			
			for(ISolution sol:tremovable){
				this.getTempSolutionSet().getSolutionSet().remove(sol);
			}
		}
		
		// compare solutionSet and tempSolutionSet
		this.getTempSolutionSet().clean();
		
		while(!medium.isEmpty()){
			ISolution s=factory.createSolution();
			s.getSolution().add(medium.getNextBacterium());
			ArrayList<IBacterium> remove=new  ArrayList<IBacterium>();
			for(IBacterium b:medium.getBacteria()){
				ISolution sp=s.clone();
				sp.getSolution().addAll(s.getSolution());
				sp.getSolution().add(b);
				if(this.comparator.compare(sp, s)>0){
					remove.add(b);
					s.getSolution().add(b);
				}
			}
			for(IBacterium b:remove){
				this.medium.getBacteria().remove(b);
			}
			this.getTempSolutionSet().getSolutionSet().add(s);
		}
		
		if(this.tempSolutionSet.getSolutionSet().size() > this.solutionSetSize){
			int delete=this.tempSolutionSet.getSolutionSet().size()-this.solutionSetSize;
			this.tempSolutionSet=this.ranker.rank(this.tempSolutionSet);
			// truncate deleting the X for this means rank the solutions by their fitness value
			for(int i=0;i<delete;i++){
				this.tempSolutionSet.getSolutionSet().remove(this.tempSolutionSet.getSolutionSet().size()-1);
			}
		}
		ArrayList<IBacterium> mating=this.selector.select(this.tempSolutionSet,this.solutionSet);
		this.breed(mating);
	}
	private IMedium breed(ArrayList<IBacterium> mating) {
		for(IBacterium b:mating){
			for(IEvolutionOperator op:this.getOperators()){
				IBacterium  bc=op.evolve(b);
				this.medium.getBacteria().add(bc);
			}
		}
		return this.medium;
	}
	private void check() throws InvalidSetUpException{
		throw new InvalidSetUpException();
	}
	
	
	public ArrayList<IObjective> getObjectives() {
		return objectives;
	}
	public void setObjectives(ArrayList<IObjective> objectives) {
		this.objectives = objectives;
	}
	public IFitnessFunction getFitnessFunction() {
		return fitnessFunction;
	}
	public void setFitnessFunction(IFitnessFunction fitnessFunction) {
		this.fitnessFunction = fitnessFunction;
	}
	public ISelector getSelector() {
		return selector;
	}
	public void setSelector(ISelector selector) {
		this.selector = selector;
	}
	public ISolutionSet getSolutionSet() {
		return solutionSet;
	}
	public void setSolutionSet(ISolutionSet solutionSet) {
		this.solutionSet = solutionSet;
	}
	public ISolutionSet getTempSolutionSet() {
		return tempSolutionSet;
	}
	public void setTempSolutionSet(ISolutionSet tempSolutionSet) {
		this.tempSolutionSet = tempSolutionSet;
	}
	public IMedium getMedium() {
		return medium;
	}
	public void setMedium(IMedium medium) {
		this.medium = medium;
	}
	public void setFactory(IBacteriaFactory factory) {
		this.factory = factory;
	}
	public IBacteriaFactory getFactory() {
		return factory;
	}
	public void setSolutionSetSize(int solutionSetSize) {
		this.solutionSetSize = solutionSetSize;
	}
	public int getSolutionSetSize() {
		return solutionSetSize;
	}
	public void setMediumSize(int mediumSize) {
		this.mediumSize = mediumSize;
	}
	public int getMediumSize() {
		return mediumSize;
	}
	public void setOperators(ArrayList<IEvolutionOperator> operators) {
		if(this.operators==null)
			this.operators=new ArrayList<IEvolutionOperator>();
		this.operators = operators;
	}
	public ArrayList<IEvolutionOperator> getOperators() {
		return operators;
	}

}
