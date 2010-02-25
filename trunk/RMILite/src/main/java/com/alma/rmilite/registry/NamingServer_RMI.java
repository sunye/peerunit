package com.alma.rmilite.registry;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class NamingServer_RMI implements NamingServer {

	/* (non-Javadoc)
	 * @see com.alma.rmilite.registry.NamingServer#createRegistry(int)
	 */
	@Override
	public Registry createRegistry(int port) throws RemoteException {
		return new Registry_RMI(LocateRegistry.createRegistry(port));
	}

	/* (non-Javadoc)
	 * @see com.alma.rmilite.registry.NamingServer#getRegistry(java.lang.String, int)
	 */
	@Override
	public Registry getRegistry(String host, int port) throws RemoteException {
		return new Registry_RMI(LocateRegistry.getRegistry(host, port));
	}
}
