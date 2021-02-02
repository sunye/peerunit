package fr.inria.mdca.core.model;

import java.util.ArrayList;

public class BaseSerieInstance {
	
	private ArrayList<BaseInstance> instanceSet;

	public void setInstanceSet(ArrayList<BaseInstance> solutionSet) {
		this.instanceSet = solutionSet;
	}

	public ArrayList<BaseInstance> getInstanceSet() {
		if(this.instanceSet==null){
			this.instanceSet=new ArrayList<BaseInstance>();
		}
		return instanceSet;
	}
	
	public BaseSerieInstance clone(){
		BaseSerieInstance baseSeries=new BaseSerieInstance();
		for(int i=0;i<this.getInstanceSet().size();i++){
			baseSeries.getInstanceSet().add(this.getInstanceSet().get(i).clone());
		}
		return baseSeries;
	}
	
	public String toString(){
		StringBuffer buff=new StringBuffer();
		for(BaseInstance i:this.getInstanceSet()){
			buff.append(i.toString()+"\n");
		}
		return buff.toString();
	}
}
