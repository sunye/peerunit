package com.alma.rmilite;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteObjectTest extends Remote {
	
	public int getNb() throws RemoteException;
	
	public void setNb(RemoteObjectTest nb) throws RemoteException;
	
	public void setNb(int nb) throws RemoteException;
	
	public void incNb()  throws RemoteException;
	
	public RemoteObjectTest add2Nb(RemoteObjectTest nb1, int nb2) throws RemoteException;
}
