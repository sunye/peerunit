package com.alma.rmilite.registry;

import java.rmi.RemoteException;

public interface NamingServer {
	
	/**
	 * We want the NamingServer to be instancieted only once, so we use
	 * singleton pattern
	 */
	public static final NamingServer instance = new NamingServer_RMI();
	
	public Registry createRegistry(int port) throws RemoteException;
	
	public Registry getRegistry(String host, int port) throws RemoteException;
}
