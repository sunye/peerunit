package fr.inria.triskell.moga.jgap;

import java.util.ArrayList;

import org.jgap.IChromosome;
import org.jgap.INaturalSelector;
import org.jgap.NaturalSelector;
import org.jgap.Population;

public class BinaryTournamentSelectionWR extends NaturalSelector {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8286532473530505245L;
	
	private SPEA2Configuration configuration;

	public BinaryTournamentSelectionWR(SPEA2Configuration configuration) {
		super();
		this.configuration = configuration;
	}

	private ArrayList<IChromosome> selected=new ArrayList<IChromosome>();
	
	public void empty() {
		this.selected.clear();
	}

	public boolean returnsUniqueChromosomes() {
		return false;
	}

	/**
	 * int num
	 * arg1 from
	 * arg2 to
	 */
	public void select(int arg0, Population from, Population to) {
		for(int i=0;i<arg0;i++){
			int p0=(int)(this.configuration.getRandomGenerator().nextDouble()*from.size());
			int p1=(int)(this.configuration.getRandomGenerator().nextDouble()*from.size());
			
			IChromosome ch0=from.getChromosome(p0);
			IChromosome ch1=from.getChromosome(p1);
			if(this.selected.contains(ch0)||this.selected.contains(ch1)){
				if(this.selected.contains(ch0)){
					this.selected.add(ch1);
				}
				else{
					this.selected.add(ch0);
				}
			}
			else{
				if(ch0.getFitnessValue()==0){	
					this.selected.add(ch0);
				}
				else if(ch1.getFitnessValue()==0){
					this.selected.add(ch1);
				}
				else if(ch0.getFitnessValue() > ch1.getFitnessValue()){
					this.selected.add(ch1);
				}
				else if(ch0.getFitnessValue() < ch1.getFitnessValue()){
					this.selected.add(ch0);
				}
			}
		}
		for(IChromosome chrom:this.selected){
			
			to.addChromosome((IChromosome)chrom.clone());
		}
	}

	@Override
	protected void add(IChromosome arg0) {
		this.selected.add(arg0);
	}

}
