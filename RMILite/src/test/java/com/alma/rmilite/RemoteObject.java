package com.alma.rmilite;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteObject extends Remote {
	
	public int getNb() throws RemoteException;
	
	public void setNb(RemoteObject nb) throws RemoteException;
	
	public void setNb(int nb) throws RemoteException;
	
	public void incNb()  throws RemoteException;
	
	public RemoteObject add2Nb(RemoteObject nb1, int nb2);
}
