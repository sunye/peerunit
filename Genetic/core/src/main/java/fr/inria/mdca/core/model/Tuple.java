package fr.inria.mdca.core.model;

public class Tuple {
	private int elements=0;
	
	private int[] indexes;
	
	private int[][] transitions;
	
	private boolean contained=false;
	
	
	public Tuple(int size,int torder){
		this.elements=size;
		this.transitions=new int[size][torder];
		this.indexes=new int[size];
	}
	public void setIndexes(int[] indexes) {
		this.indexes = indexes;
	}
	public int[] getIndexes() {
		return indexes;
	}
	public void setElements(int elements) {
		this.elements = elements;
	}
	public int getElements() {
		return elements;
	}

	public void setTransitions(int[][] transtisions) {
		this.transitions = transtisions;
	}

	public int[][] getTransitions() {
		return transitions;
	}
	
	public boolean equals(Object o){
		if(!(o instanceof Tuple)){
			return false;
		}
		Tuple t=(Tuple)o;
		if(this.indexes.length==t.indexes.length){
			for(int i=0;i<this.indexes.length;i++){
				if(this.indexes[i]!=t.indexes[i]){
					return false;
				}
			}
			for(int i=0;i<this.transitions.length;i++){
				for(int j=0;j<this.transitions[i].length;j++){
					if(this.transitions[i][j]!=t.transitions[i][j]){
						return false;
					}
				}
			}
		}
		else return false;
		return true;
	}
	
	public String toString(){
		StringBuffer buffer=new StringBuffer();
		for(int i=0;i<this.transitions.length;i++){
			buffer.append(""+this.indexes[i]+" : ");
			for(int j=0;j<this.transitions[i].length;j++){
				buffer.append(" "+this.transitions[i][j]);
			}
			buffer.append("\n");
		}
		buffer.append("---\n");
		return buffer.toString();
	}
	public void setContained(boolean contained) {
		this.contained = contained;
	}
	public boolean isContained() {
		return contained;
	}
	
	public boolean conflicts(Tuple t){
		for(int k=0;k<t.getIndexes().length;k++){
			for(int j=0;j<t.getIndexes().length;j++){
				if(this.getIndexes()[k]==t.getIndexes()[j]){
					return true;
				}
			}
		}
		return false;
	}
}
