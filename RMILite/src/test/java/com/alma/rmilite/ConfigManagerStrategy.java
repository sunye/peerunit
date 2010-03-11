package com.alma.rmilite;

import com.alma.rmilite.registry.NamingServer;
import com.alma.rmilite.server.RemoteObjectProvider;

public interface ConfigManagerStrategy {
	
	public RemoteObjectProvider getRemoteObjectProvider();

	public NamingServer getNamingServer();
	
}
