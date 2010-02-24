package com.alma.rmilite;

public class SerializableObjectImpl implements SerializableObject {

	private static final long serialVersionUID = -1181121422978605062L;
	
	private String name;
	
	public SerializableObjectImpl(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
}
