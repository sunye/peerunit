package fr.univnantes.alma.rmilite.registry;

import org.junit.Before;

import fr.univnantes.alma.rmilite.ConfigManagerSocketStrategy;

/**
 * This class use the {@link AbstractNamingServerTest} test and initialize it
 * with a Socket-based NamingServer
 */
public class NamingServerSocketTest extends AbstractNamingServerTest {

    @Before
    public void testInit() {
	setConfigManagerStrategy(new ConfigManagerSocketStrategy());
	setPort(9090);
    }

}