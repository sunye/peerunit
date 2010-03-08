package com.alma.rmilite.registry;

import org.junit.Before;

import com.alma.rmilite.registry.NamingServer_RMI;

/**
 * This class use the {@link AbstractNamingServerTest} test
 * and initialize it with a RMI-based NamingServer
 */
public class NamingServerRMITest extends AbstractNamingServerTest {
	
	@Before
	public void testInit() {
		setNamingServer(new NamingServer_RMI());
	}
	
}
