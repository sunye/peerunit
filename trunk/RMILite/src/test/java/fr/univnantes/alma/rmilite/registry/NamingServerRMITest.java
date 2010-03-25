package fr.univnantes.alma.rmilite.registry;

import org.junit.Before;

import fr.univnantes.alma.rmilite.ConfigManager_RMI;

/**
 * This class use the {@link AbstractNamingServerTest} test and initialize it
 * with a RMI-based NamingServer
 */
public class NamingServerRMITest extends AbstractNamingServerTest {

    @Before
    public void testInit() {
	setConfigManagerStrategy(new ConfigManager_RMI());
	setPort(8080);
    }

}
