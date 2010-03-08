package com.alma.rmilite;

import org.junit.Before;

import com.alma.rmilite.registry.NamingServer_RMI;
import com.alma.rmilite.server.RemoteObjectProvider_RMI;

public class RemoteTestRMI extends AbstractRemoteTest {
	
	@Before
	public void setUp() {
		setRemoteObjectProvider(new RemoteObjectProvider_RMI());
		setNamingServer(new NamingServer_RMI());
	}
	
}
