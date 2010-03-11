package com.alma.rmilite;

import com.alma.rmilite.registry.NamingServer;
import com.alma.rmilite.registry.NamingServer_RMI;
import com.alma.rmilite.server.RemoteObjectProvider;
import com.alma.rmilite.server.RemoteObjectProvider_RMI;

public class ConfigManagerRMIStrategy implements ConfigManagerStrategy {

	public RemoteObjectProvider remoteObjectProvider;
	public NamingServer namingServer;

	public ConfigManagerRMIStrategy() {
		namingServer = new NamingServer_RMI();
		remoteObjectProvider = new RemoteObjectProvider_RMI();
	}
	

	public RemoteObjectProvider getRemoteObjectProvider() {
		return remoteObjectProvider;
	}

	public NamingServer getNamingServer() {
		return namingServer;
	}
	

}
