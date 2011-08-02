package fr.univnantes.alma.rmilite.registry;

import org.junit.Before;

import fr.univnantes.alma.rmilite.ConfigManager_Socket;

/**
 * This class use the {@link AbstractNamingServerTest} test and initialize it
 * with a Socket-based NamingServer
 */
public class NamingServerSocketTest extends AbstractNamingServerTest {

    @Before
    public void testInit() {
	setConfigManagerStrategy(new ConfigManager_Socket());
	setPort(9090);
    }

}
