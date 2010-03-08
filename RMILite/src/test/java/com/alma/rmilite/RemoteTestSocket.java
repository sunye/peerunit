package com.alma.rmilite;

import org.junit.Before;

import com.alma.rmilite.registry.NamingServer_Socket;
import com.alma.rmilite.server.RemoteObjectProvider_Socket;

public class RemoteTestSocket extends AbstractRemoteTest {
	
	@Before
	public void setUp() {
		setRemoteObjectProvider(new RemoteObjectProvider_Socket());
		setNamingServer(new NamingServer_Socket());
	}
	
}
