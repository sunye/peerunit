package com.alma.rmilite;

public class RemoteObjectImpl implements RemoteObject {
	
	private int nbCall = 0;

	public int getNbCall() {
		return ++nbCall;
	}
}
