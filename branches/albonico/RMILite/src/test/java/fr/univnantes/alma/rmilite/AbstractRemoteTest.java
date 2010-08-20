package fr.univnantes.alma.rmilite;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.alma.rmilite.registry.Registry;

public abstract class AbstractRemoteTest extends TestCase {

    private static ConfigManager configManagerStrategy = null;
    private static int port;

    protected static void setConfigManagerStrategy(
	    ConfigManager aConfigManagerStrategy) {
	configManagerStrategy = aConfigManagerStrategy;
    }

    protected static void setPort(int aPort) {
	port = aPort;
    }

    @Before
    public void setUp() {
	assertNotNull(configManagerStrategy);
	assertNotNull(configManagerStrategy.getRemoteObjectProvider());
	assertNotNull(configManagerStrategy.getNamingServer());
    }

    @Test
    public void testClient() {
	Registry registry = null;
	try {
	    System.out.println("Looking for registry on port " + port);
	    registry = configManagerStrategy.getNamingServer().getRegistry(
		    "127.0.0.1", port);
	} catch (Exception e) {
	    fail("Unable de get created registry");
	}

	try {
	    RemoteObjectTest ro1;
	    RemoteObjectTest ro2 = new RemoteObjectTestImpl();
	    ro2.setNb(1);
	    ro1 = (RemoteObjectTest) registry.lookup("ro1");

	    // RemoteObjectProvider remoteObjectProvider =
	    // RemoteObjectProvider.instance;
	    configManagerStrategy.getRemoteObjectProvider()
		    .exportObject(ro2, 0);

	    assertEquals(ro1.getNb(), -1);

	    ro1.setNb(0);
	    assertEquals(ro1.getNb(), 0);

	    ro1.setNb(ro2);
	    assertEquals(ro1.getNb(), 1);

	    ro1.incNb();
	    assertEquals(ro1.getNb(), 2);

	    /*
	     * RemoteObjectTest ro3 = ro1.add2Nb(ro2, 0); //3
	     * System.out.println(ro3.getNb()); ro3.incNb();
	     * System.out.println(ro3.getNb()); // 4 try {
	     * remoteObjectProvider.unexportObject(ro2); // throw an exception,
	     * only indicates socket is closed } catch (Exception e) { // ? }
	     */
	} catch (Exception e) {
	    fail();
	}
    }

}
