package com.alma.rmilite.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface  RemoteObjectProvider {

	public static final RemoteObjectProvider instance = new RemoteObjectProvider_RMI();
	
	public Remote exportObject(Remote object) throws RemoteException;
	
	public Remote exportObject(Remote object, int port) throws RemoteException;
	
	public boolean unexportObject(Remote object) throws RemoteException;
}
