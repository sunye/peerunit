package fr.inria.mdca.ga;

import java.util.ArrayList;

public class StatisticTrace {
	
	private int iterator=0;
	public class InterationTrace{
		private int intertionNum;
		private float fitness;
		private int length;
		private int type;
		
		public InterationTrace(int intertionNum, float fitness, int length, int type) {
			super();
			this.intertionNum = intertionNum;
			this.fitness = fitness;
			this.length = length;
			this.type=type;
		}
		
		public int getIntertionNum() {
			return intertionNum;
		}
		public void setIntertionNum(int intertionNum) {
			this.intertionNum = intertionNum;
		}
		public float getFitness() {
			return fitness;
		}
		public void setFitness(float fitness) {
			this.fitness = fitness;
		}
		public int getLength() {
			return length;
		}
		public void setLength(int length) {
			this.length = length;
		}
		public String toString(){
			return new String(intertionNum+","+fitness+","+length+","+type);
		}
	}
	
	private ArrayList<InterationTrace> traces=new ArrayList<InterationTrace>();

	public void setTraces(ArrayList<InterationTrace> traces) {
		this.traces = traces;
	}

	public ArrayList<InterationTrace> getTraces() {
		return traces;
	}
	
	public void addTrace(int iter,float fx,int length, int type){
		this.traces.add(new InterationTrace(iter+1,fx,length,type));
	}
	
	public void addTrace(float fx,int length, int type){
		this.iterator++;
		if(traces.size()>0){
			InterationTrace lt=traces.get(traces.size()-1);
			if(lt.getFitness()==fx&&lt.getLength()==length){
				return;
			}
		}
		this.traces.add(new InterationTrace(this.iterator,fx,length,type));
	}
	

}
