package com.alma.rmilite;

import com.alma.rmilite.registry.NamingServer;
import com.alma.rmilite.registry.NamingServer_RMI;
import com.alma.rmilite.server.RemoteObjectProvider;
import com.alma.rmilite.server.RemoteObjectProvider_RMI;

public class ConfigManagerRMIStrategy implements ConfigManagerStrategy {

    private static RemoteObjectProvider remoteObjectProvider = new RemoteObjectProvider_RMI();
    private static NamingServer namingServer = new NamingServer_RMI();

    public ConfigManagerRMIStrategy() {
    }

    public RemoteObjectProvider getRemoteObjectProvider() {
	return remoteObjectProvider;
    }

    public NamingServer getNamingServer() {
	return namingServer;
    }

}
