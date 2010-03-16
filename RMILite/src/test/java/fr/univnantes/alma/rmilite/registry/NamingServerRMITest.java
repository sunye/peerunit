package fr.univnantes.alma.rmilite.registry;

import org.junit.Before;

import fr.univnantes.alma.rmilite.ConfigManagerRMIStrategy;

/**
 * This class use the {@link AbstractNamingServerTest} test and initialize it
 * with a RMI-based NamingServer
 */
public class NamingServerRMITest extends AbstractNamingServerTest {

    @Before
    public void testInit() {
	setConfigManagerStrategy(new ConfigManagerRMIStrategy());
	setPort(8080);
    }

}
