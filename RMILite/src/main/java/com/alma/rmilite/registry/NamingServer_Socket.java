package com.alma.rmilite.registry;

import com.alma.rmilite.client.StubFactory;
import com.alma.rmilite.server.RemoteObjectProvider;

public class NamingServer_Socket implements NamingServer {
	
	private RemoteObjectProvider remoteObjectProvider;

	/* (non-Javadoc)
	 * @see com.alma.rmilite.registry.NamingServer#createRegistry(int)
	 */
	@Override
	public Registry createRegistry(int port) throws Exception {
		return (Registry) this.remoteObjectProvider.exportObject(new Registry_Socket(), port);
	}

	/* (non-Javadoc)
	 * @see com.alma.rmilite.registry.NamingServer#getRegistry(java.lang.String, int)
	 */
	@Override
	public Registry getRegistry(String host, int port) throws Exception {
		return (Registry) StubFactory.createStub(host, port, new Class<?>[]{ Registry.class });
	}

	/* (non-Javadoc)
	 * @see com.alma.rmilite.registry.NamingServer#getRemoteObjectProvider()
	 */
	@Override
	public RemoteObjectProvider getRemoteObjectProvider() {
		return this.remoteObjectProvider;
	}

	/* (non-Javadoc)
	 * @see com.alma.rmilite.registry.NamingServer#setRemoteObjectProvider(com.alma.rmilite.server.RemoteObjectProvider)
	 */
	@Override
	public void setRemoteObjectProvider(RemoteObjectProvider rop) {
		this.remoteObjectProvider = rop;	
	}
}
