package com.alma.rmilite.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteObjectProvider_RMI implements RemoteObjectProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alma.rmilite.server.RemoteObjectProvider#exportObject(java.rmi.Remote
	 * , int)
	 */
	@Override
	public Remote exportObject(Remote object, int port) throws RemoteException {
		return UnicastRemoteObject.exportObject(object, port);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alma.rmilite.server.RemoteObjectProvider#unexportObject(java.rmi.
	 * Remote)
	 */
	@Override
	public boolean unexportObject(Remote object) throws RemoteException {
		return UnicastRemoteObject.unexportObject(object, true);
	}

}
