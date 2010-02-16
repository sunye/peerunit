package com.alma.rmilite.registry.tests;

import java.rmi.Remote;

import junit.framework.TestCase;

import org.junit.Test;

import com.alma.rmilite.registry.NamingServer;
import com.alma.rmilite.registry.Registry;

/**
 * JUnit test to check registries methods. In this classes, all the
 * tests are written : two subclasses just initiates a RMI registry
 * and a Socket registry
 */
public abstract class AbstractNamingServerTest extends TestCase {

	/**
	 * A stub class, it will be instancieted while testing
	 */
	private class MyRemoteClass implements Remote {
		public int signature = 123456;
	};
	
	private static NamingServer namingServer;
	private static Registry registry;
	
	/**
	 * A arbitrary ID, used as an id to store a MyRemoteClass instance
	 * on the registry
	 */
	private static String id = "my_id_123";
	
	/**
	 * this will be used by subclasses to set something using RMI
	 * or Sockets before the test runs
	 * @param aNamingServer the naming server that will be used in the whole test
	 */
	protected static void setNamingServer(NamingServer aNamingServer) {
		namingServer = aNamingServer;
	}
	
	/**
	 * test {@link NamingServer#createRegistry(int)}
	 */
	@Test
	public void testRegistry() {
		try {
			registry = namingServer.createRegistry(8080);
			assertNotNull(registry);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	/**
	 * test {@link Registry#bind(String, Remote)}
	 */
	@Test
	public void testBind() {
		MyRemoteClass myObject = new MyRemoteClass();
		myObject.signature += 1;
		try {
			registry.bind(id, myObject);
			assertTrue(true);
		} catch (Exception e) {
			fail("Unwanted exception caught");
		}
	}
	
	/**
	 * test {@link Registry#lookup(String)}
	 */
	@Test
	public void testLookup() {
		try {
			MyRemoteClass myRemoteObject = (MyRemoteClass) registry.lookup(id);
			assertEquals(myRemoteObject.signature, 123457);
		} catch (Exception e) {
			fail("Unwanted exception caught");
		}
	}
	
	/**
	 * test {@link Registry#unbind(String)}
	 */
	@Test
	public void testUnbind() {
		try {
			registry.unbind(id);
		} catch (Exception e) {
			assertTrue(false);
		}
		
		// Let's try a look-up on the stuff we just removed,
		// this should throw an exception
		try {
			MyRemoteClass myRemoteObject = (MyRemoteClass) registry.lookup(id); // bad if it works
			// this point should not be reached
			assertNull("Object have not been deleted from register", myRemoteObject);
			fail("No exception have been thrown, but one should");
		} catch (Exception e) {
			// nice, an exception has been thrown
			assertTrue(true);
		}	
	}
}
