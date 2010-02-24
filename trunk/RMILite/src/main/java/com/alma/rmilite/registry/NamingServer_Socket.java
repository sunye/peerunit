package com.alma.rmilite.registry;

import com.alma.rmilite.client.StubFactory;
import com.alma.rmilite.server.RemoteObjectProvider;

public class NamingServer_Socket implements NamingServer {

	@Override
	public Registry createRegistry(int port) throws Exception {
		return (Registry) RemoteObjectProvider.instance.exportObject(new Registry_Socket());
	}

	@Override
	public Registry getRegistry(String host, int port) throws Exception {
		return (Registry) StubFactory.createStub(host, port, Registry.class);
	}
}
