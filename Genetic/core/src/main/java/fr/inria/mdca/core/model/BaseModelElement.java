package fr.inria.mdca.core.model;

public class BaseModelElement {
	
	public BaseModelElement(int elements, String name) {
		super();
		this.elements = elements;
		this.name = name;
	}
	public BaseModelElement(){
		
	}

	private int elements=0;
	private String name="";
	private BaseModel base;
	
	public int getElementsNum() {
		return elements;
	}
	
	public void setElementsNum(int elements) {
		this.elements = elements;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setBase(BaseModel base) {
		this.base = base;
	}

	public BaseModel getBase() {
		return base;
	}
	
}
