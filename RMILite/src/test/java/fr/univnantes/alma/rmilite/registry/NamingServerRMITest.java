package fr.univnantes.alma.rmilite.registry;

import org.junit.Before;

import fr.univnantes.alma.rmilite.ConfigManagerRMI;

/**
 * This class use the {@link AbstractNamingServerTest} test and initialize it
 * with a RMI-based NamingServer
 */
public class NamingServerRMITest extends AbstractNamingServerTest {

    @Before
    public void testInit() {
	setConfigManagerStrategy(new ConfigManagerRMI());
	setPort(8080);
    }

}
