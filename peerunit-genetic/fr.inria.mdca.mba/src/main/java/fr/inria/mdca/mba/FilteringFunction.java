package fr.inria.mdca.mba;

import java.util.ArrayList;
import java.util.HashMap;

import fr.inria.mdca.core.model.BaseInstance;


public class FilteringFunction {
	
	/**
	 * The minimum size of the medium 
	 */
	private int minMedium;
	
	/**
	 * The maximum number of turns a bacterium can stand in the medium
	 */
	private int maxTurnNumber;
	
	private ArrayList<BaseInstance> arrayBestBact;
	
	private float bactRelFitMin;

	public void run() {
		
		this.getBactereologicAlgorithm().updateSolutionFitness();
		
		HashMap<BaseInstance,Float>  cash = new HashMap<BaseInstance,Float> ();
		arrayBestBact=new ArrayList<BaseInstance>();
		ArrayList<BaseInstance> remove=new ArrayList<BaseInstance>();
		
		for(BaseInstance b:bactereologicAlgorithm.getMedium()){
			float relativef=this.bactereologicAlgorithm.getFitnessFunction().relativeFitness(b);
			if ( relativef <= bactRelFitMin && b.getAge() > maxTurnNumber){
				remove.add(b);
			}
			else{
				cash.put(b,relativef);
			}
			if(cash.size() < minMedium) {
				computeAdd(b);
			}
			
			b.setAge(b.getAge()+1);
		}
		
		for(BaseInstance b:remove){
			bactereologicAlgorithm.getMedium().remove(b);
		}
		
		if(cash.size()<minMedium) {
			this.bactereologicAlgorithm.getMedium().clear();
			 cash = new HashMap<BaseInstance,Float> ();
			 for(BaseInstance b:this.arrayBestBact){
				 float relativef=this.bactereologicAlgorithm.getFitnessFunction().relativeFitness(b);
				 cash.put(b,relativef);
				 bactereologicAlgorithm.getMedium().add(b);
			 }
		}
		
		this.getBactereologicAlgorithm().setRelFitnessCash(cash);
		
	}
	
	private void computeAdd(BaseInstance b){
		if(arrayBestBact.size()<minMedium){
			arrayBestBact.add(b);
		}
		else{
			float minFitnessVect=-11000;
			BaseInstance minFitnessBact=null;
			for(BaseInstance ba:this.arrayBestBact){
				float currentFitness  = bactereologicAlgorithm.getFitnessFunction().relativeFitness(ba);
				if(currentFitness < minFitnessVect) {
					minFitnessVect = currentFitness;
					minFitnessBact = ba;
				}
			}
			float relFitBact = bactereologicAlgorithm.getFitnessFunction().relativeFitness(b);
			if(relFitBact<minFitnessVect) {
				arrayBestBact.remove(minFitnessBact);
				arrayBestBact.add(b);
			}
		}
	}
	
	private BactereologicAlgorithm bactereologicAlgorithm;

	public void setBactereologicAlgorithm(BactereologicAlgorithm bactereologicAlgorithm) {
		this.bactereologicAlgorithm = bactereologicAlgorithm;
	}
	public BactereologicAlgorithm getBactereologicAlgorithm() {
		return bactereologicAlgorithm;
	}

	
	public FilteringFunction (int theMinMedium, int theMaxTurn, float theBactRelFitMin){
		minMedium = theMinMedium;
		maxTurnNumber = theMaxTurn;
		bactRelFitMin = theBactRelFitMin;
	}
}
