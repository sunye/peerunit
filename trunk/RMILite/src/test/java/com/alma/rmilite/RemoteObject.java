package com.alma.rmilite;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteObject extends Remote {
	
	public int getNbCall() throws RemoteException;

}
