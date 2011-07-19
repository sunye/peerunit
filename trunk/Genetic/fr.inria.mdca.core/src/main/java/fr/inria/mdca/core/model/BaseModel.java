package fr.inria.mdca.core.model;

import java.util.ArrayList;

public class BaseModel {
	
	private ArrayList<BaseModelElement> elements;
	
	private int twise;
	private int order;
	
	public static final CharSequence ORDER = "ORDER";
	public static final CharSequence TWISE = "TWISE";

	public BaseModel(int twise, int order) {
		super();
		this.twise = twise;
		this.order = order;
	}
	
	public BaseModel() {
		super();
	}

	public void setElements(ArrayList<BaseModelElement> elements) {
		this.elements = elements;
	}

	public ArrayList<BaseModelElement> getElements() {
		if(this.elements==null){
			this.elements=new  ArrayList<BaseModelElement>();
		}
		return elements;
	}
	
	/**
	 * use this operation to add new elements and set the opposite reference
	 * @param element
	 */
	public void addElement(BaseModelElement element){
		this.getElements().add(element);
		element.setBase(this);
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getOrder() {
		return order;
	}

	public void setTwise(int twise) {
		this.twise = twise;
	}

	public int getTwise() {
		return twise;
	}
	public String toString(){
		StringBuffer buffer=new StringBuffer();
		buffer.append(ORDER+"="+this.order+"\n");
		buffer.append(TWISE+"="+this.twise+"\n");
		int j=0;
		int lim=elements.size();
		for(BaseModelElement element:this.elements){
			buffer.append(element.getName()+":"+element.getElementsNum());
			if(j<lim-1){
				buffer.append("\n");
			}
			j++;
		}
		return buffer.toString();
		
	}
}
