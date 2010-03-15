package com.alma.rmilite;

import com.alma.rmilite.client.StubFactory;
import com.alma.rmilite.io.IOManager_IO;
import com.alma.rmilite.registry.NamingServer;
import com.alma.rmilite.registry.NamingServer_Socket;
import com.alma.rmilite.server.RemoteObjectProvider;
import com.alma.rmilite.server.RemoteObjectProvider_Socket;

/**
 * Initializes the system.
 */
public class ConfigManagerSocketStrategy implements ConfigManagerStrategy {

	public RemoteObjectProvider remoteObjectProvider;
	public NamingServer namingServer;

	public ConfigManagerSocketStrategy() {
		/* Initialization */
		IOManager_IO io = new IOManager_IO();
		NamingServer_Socket ns = new NamingServer_Socket();
		RemoteObjectProvider_Socket rop = new RemoteObjectProvider_Socket();
		
		rop.setIOManager(io);
		io.setRemoteObjectManager(rop);
		ns.setRemoteObjectProvider(rop);		
		RemoteMethodFactory.remoteObjectManager = rop;
		StubFactory.ioManager = io;
		
		remoteObjectProvider = rop;
		namingServer = ns;
	}
	

	public RemoteObjectProvider getRemoteObjectProvider() {
		return remoteObjectProvider;
	}

	public NamingServer getNamingServer() {
		return namingServer;
	}
	
}