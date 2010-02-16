package com.alma.rmilite.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteObjectProvider_RMI implements RemoteObjectProvider {

	@Override
	public Remote exportObject(Remote object) throws RemoteException {
		return UnicastRemoteObject.exportObject(object);
	}

	@Override
	public Remote exportObject(Remote object, int port) throws RemoteException {
		return UnicastRemoteObject.exportObject(object, port);
	}

	@Override
	public boolean unexportObject(Remote object) throws RemoteException {
		return UnicastRemoteObject.unexportObject(object, true);
	}

}
