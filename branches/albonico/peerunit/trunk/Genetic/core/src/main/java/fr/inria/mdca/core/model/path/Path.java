package fr.inria.mdca.core.model.path;

import java.util.ArrayList;

public class Path {

	private int[] indexes;
	private ArrayList<ArrayList<Integer>> values;
	public Path(int twise){
		this.indexes=new int[twise];
		values=new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<twise;i++){
			values.add(new ArrayList<Integer>());
		}
	}
	
	public int[] getIndexes() {
		return indexes;
	}
	public void setIndexes(int[] indexes) {
		this.indexes = indexes;
	}
	public ArrayList<ArrayList<Integer>> getValues() {
		return values;
	}
	public void setValues(ArrayList<ArrayList<Integer>> values) {
		this.values = values;
	}
	public String toString(){
		int j=0;
		StringBuffer buffer=new StringBuffer();
		int lim=indexes.length;
		for(int i:indexes){
			buffer.append(""+i+": ");
			int lim_=values.get(j).size();
			int k=0;
			for(Integer t:values.get(j)){
				buffer.append(t);
				if(k<lim_-1){
					buffer.append(" ");
				}
				k++;
			}
			if(j<lim-1){
				buffer.append("\n");
			}
			j++;
		}
		return buffer.toString();
	}
}
