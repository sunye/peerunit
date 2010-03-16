package fr.univnantes.alma.rmilite.registry;

import fr.univnantes.alma.rmilite.client.StubFactory;
import fr.univnantes.alma.rmilite.server.RemoteObjectProvider;

public class NamingServer_Socket implements NamingServer {
	
	private RemoteObjectProvider remoteObjectProvider;

	/* (non-Javadoc)
	 * @see fr.univnantes.alma.rmilite.registry.NamingServer#createRegistry(int)
	 */
	@Override
	public Registry createRegistry(int port) throws Exception {
		return (Registry) this.remoteObjectProvider.exportObject(new Registry_Socket(), port);
	}

	/* (non-Javadoc)
	 * @see fr.univnantes.alma.rmilite.registry.NamingServer#getRegistry(java.lang.String, int)
	 */
	@Override
	public Registry getRegistry(String host, int port) throws Exception {
		return (Registry) StubFactory.createStub(host, port, new Class<?>[]{ Registry.class });
	}

	/* (non-Javadoc)
	 * @see fr.univnantes.alma.rmilite.registry.NamingServer#getRemoteObjectProvider()
	 */
	@Override
	public RemoteObjectProvider getRemoteObjectProvider() {
		return this.remoteObjectProvider;
	}

	/* (non-Javadoc)
	 * @see fr.univnantes.alma.rmilite.registry.NamingServer#setRemoteObjectProvider(fr.univnantes.alma.rmilite.server.RemoteObjectProvider)
	 */
	@Override
	public void setRemoteObjectProvider(RemoteObjectProvider rop) {
		this.remoteObjectProvider = rop;	
	}
}
