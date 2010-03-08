package com.alma.rmilite.registry;

import org.junit.Before;

import com.alma.rmilite.registry.NamingServer_Socket;

/**
 * This class use the {@link AbstractNamingServerTest} test
 * and initialize it with a Socket-based NamingServer
 */
public class NamingServerSocketTest extends AbstractNamingServerTest {
	
	@Before
	public void testInit() {
		setNamingServer(new NamingServer_Socket());
	}	
	
}
