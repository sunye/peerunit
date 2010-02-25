package com.alma.rmilite;

import java.rmi.RemoteException;

public class RemoteObjectImpl implements RemoteObject {
		
	private int nbCall = 0;

	public int getNbCall() throws RemoteException {
		return ++nbCall;
	}
}
