package com.alma.rmilite.registry;

import java.rmi.RemoteException;

import com.alma.rmilite.client.StubFactory;

public class NamingServer_Socket implements NamingServer {

	@Override
	public Registry createRegistry(int port) throws RemoteException {
		return new Registry_Socket();
	}

	@Override
	public Registry getRegistry(String host, int port) throws RemoteException {
		return (Registry) StubFactory.createStub(host, port, Registry.class);
	}
}
