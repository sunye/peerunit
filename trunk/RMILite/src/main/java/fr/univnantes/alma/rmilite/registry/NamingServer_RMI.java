package fr.univnantes.alma.rmilite.registry;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import fr.univnantes.alma.rmilite.server.RemoteObjectProvider;

public class NamingServer_RMI implements NamingServer {

	/* (non-Javadoc)
	 * @see fr.univnantes.alma.rmilite.registry.NamingServer#createRegistry(int)
	 */
	@Override
	public Registry createRegistry(int port) throws RemoteException {
		return new Registry_RMI(LocateRegistry.createRegistry(port));
	}

	/* (non-Javadoc)
	 * @see fr.univnantes.alma.rmilite.registry.NamingServer#getRegistry(java.lang.String, int)
	 */
	@Override
	public Registry getRegistry(String host, int port) throws RemoteException {
		return new Registry_RMI(LocateRegistry.getRegistry(host, port));
	}

	/* (non-Javadoc)
	 * @see fr.univnantes.alma.rmilite.registry.NamingServer#getRemoteObjectProvider()
	 */
	@Override
	public RemoteObjectProvider getRemoteObjectProvider() {
		return null;
	}

	/* (non-Javadoc)
	 * @see fr.univnantes.alma.rmilite.registry.NamingServer#setRemoteObjectProvider(fr.univnantes.alma.rmilite.server.RemoteObjectProvider)
	 */
	@Override
	public void setRemoteObjectProvider(RemoteObjectProvider rop) {}
}
