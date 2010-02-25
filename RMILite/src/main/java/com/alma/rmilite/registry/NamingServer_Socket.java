package com.alma.rmilite.registry;

import com.alma.rmilite.client.StubFactory;
import com.alma.rmilite.server.RemoteObjectProvider;

public class NamingServer_Socket implements NamingServer {

	/* (non-Javadoc)
	 * @see com.alma.rmilite.registry.NamingServer#createRegistry(int)
	 */
	@Override
	public Registry createRegistry(int port) throws Exception {
		return (Registry) RemoteObjectProvider.instance.exportObject(new Registry_Socket(), port);
	}

	/* (non-Javadoc)
	 * @see com.alma.rmilite.registry.NamingServer#getRegistry(java.lang.String, int)
	 */
	@Override
	public Registry getRegistry(String host, int port) throws Exception {
		return (Registry) StubFactory.createRegistryStub(host, port);
	}
}
