package com.alma.rmilite.registry;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class NamingServer_RMI implements NamingServer {

	@Override
	public Registry createRegistry(int port) throws RemoteException {
		return new Registry_RMI(LocateRegistry.createRegistry(port));
	}

	@Override
	public Registry getRegistry(String host, int port) throws RemoteException {
		return new Registry_RMI(LocateRegistry.getRegistry(host, port));
	}
}
