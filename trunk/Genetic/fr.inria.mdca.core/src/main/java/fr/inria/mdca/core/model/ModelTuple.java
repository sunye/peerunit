package fr.inria.mdca.core.model;

import java.util.ArrayList;

public class ModelTuple {
	private int elements=0;
	private ArrayList<Tuple> values;
	public ModelTuple(int elements){
		this.elements=elements;
		this.values=new ArrayList<Tuple>(elements);
	}
	public void setElements(int elements) {
		this.elements = elements;
	}
	public int getElements() {
		return elements;
	}
	public void setValues(ArrayList<Tuple> values) {
		this.values = values;
	}
	public ArrayList<Tuple> getValues() {
		return values;
	}
	public void addTuple(Tuple t){
		this.values.add(t);

	}
}
