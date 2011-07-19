package fr.inria.triskell.moga.jgap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jgap.BulkFitnessFunction;
import org.jgap.Chromosome;
import org.jgap.IChromosome;
import org.jgap.Population;

import fr.inria.triskell.moga.IObjective;
import fr.inria.triskell.moga.comparer.IDistanceMeasure;
import fr.inria.triskell.moga.comparer.IDominanceComparator;

public class SPEA2FitnessFunction extends BulkFitnessFunction {
	
	private IDominanceComparator comparator;
	
	private ArrayList<IObjective> objectives;

	public IDominanceComparator getComparator() {
		return comparator;
	}

	public void setComparator(IDominanceComparator comparator) {
		this.comparator = comparator;
	}

	public SPEA2Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(SPEA2Configuration configuration) {
		this.configuration = configuration;
	}

	public void setMeasure(IDistanceMeasure measure) {
		this.measure = measure;
	}

	public IDistanceMeasure getMeasure() {
		return measure;
	}
	
	public void setObjectives(ArrayList<IObjective> objectives) {

		this.objectives = objectives;
	}

	public ArrayList<IObjective> getObjectives() {
		if(this.objectives==null){
			this.objectives=new ArrayList<IObjective>();
		}
		return objectives;
	}

	private SPEA2Configuration configuration;
	
	private IDistanceMeasure measure;
	/**
	 * 
	 */
	private static final long serialVersionUID = 6737896225542771806L;

	@SuppressWarnings("unchecked")
	@Override
	public void evaluate(Population population) {
		List<IChromosome> chs=(List<IChromosome>)population.getChromosomes();
		
		// evaluate the objectives on the population
		for(IChromosome chr0:chs){
			ArrayList<Double> objectives=new ArrayList<Double>();
			Chromosome c=(Chromosome)chr0;
			
			for(IObjective objective:this.getObjectives()){
				double value=objective.evaluate(c);
				objectives.add(value);
			}
			assert(objectives.size()==this.objectives.size());
			if(c.getMultiObjectives()!=null) c.getMultiObjectives().clear();
			c.setMultiObjectives(objectives);
		}
		
		int[] a_strength=new int[chs.size()];
		ArrayList<ArrayList<Integer>> a_dominated=new ArrayList<ArrayList<Integer>>();
		
		for(int i=0;i<chs.size();i++){
			a_dominated.add(new ArrayList<Integer>());
		}
		
		for(int i=0;i<chs.size();i++){
			IChromosome chr0=chs.get(0);
			a_strength[i]=0;
			for(int j=0;j<chs.size();j++){
				if(i!=j){
					IChromosome chr1=chs.get(j);
					int compare=this.comparator.compare(chr0, chr1);
					
					if(compare > 0){
						a_strength[i]++;
						a_dominated.get(j).add(new Integer(i));
					}
				}
			}
		}
		
		int[] rawvalues=new int[chs.size()];

		for(int i=0;i<chs.size();i++){
			int rawFit=0;
			
			for(Integer index:a_dominated.get(i)){
				rawFit=+a_strength[index];
			}
			rawvalues[i]=rawFit;

			((Chromosome)chs.get(i)).getMultiObjectives().add(new Double(rawFit));
		}
		
		
		double[][] distance=this.measure.distanceObjectives(population);
		
	    // Add the distance to the k-th individual. In the reference paper of SPEA2, 
	    // k = sqrt(population.size()), but a value of k = 1 recommended. See
	    // http://www.tik.ee.ethz.ch/pisa/selectors/spea2/spea2_documentation.txt
		int k=(int) Math.sqrt(population.size());
		for (int i = 0; i < distance.length; i++) {
		      Arrays.sort(distance[i]);
			 double kDistance = 1.0 / (distance[i][k] + 2.0); 
			 IChromosome chrom=chs.get(i);
			 //assign fitness
			 int rawvalue=rawvalues[i];
			 double fitness=(double)rawvalue+kDistance;
			 System.out.println(fitness+" "+chrom);
			 chrom.setFitnessValue(fitness);
		}
		System.out.println("EFA");
	}

}
