package com.alma.rmilite;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.alma.rmilite.registry.NamingServer;
import com.alma.rmilite.registry.Registry;
import com.alma.rmilite.server.RemoteObjectProvider;


public abstract class AbstractRemoteTest extends TestCase {

	private static ConfigManagerStrategy configManagerStrategy = null;

	private static RemoteObjectProvider remoteObjectProvider;
	private static NamingServer namingServer;
	private static Thread server;
	
	protected static void setConfigManagerStrategy (
			ConfigManagerStrategy aConfigManagerStrategy) {
		configManagerStrategy = aConfigManagerStrategy;
	}
	
	@Before
	public void setUp() {
		assertNotNull(configManagerStrategy);
		
		remoteObjectProvider = configManagerStrategy.getRemoteObjectProvider();
		namingServer = configManagerStrategy.getNamingServer();
		
		assertNotNull(remoteObjectProvider);
		assertNotNull(namingServer);
	}
	
	/*
	 * A server
	 */
	private class RunnableServer implements Runnable {
		
		private Registry registry;
		private RemoteObjectTest ro;
		
		public RunnableServer() {
			try {
				registry = namingServer.createRegistry(1099);
			} catch (Exception e) {
				try {
					registry = namingServer.createRegistry(1101);
				} catch (Exception e1) {
					fail("Unable to create registry");
				}
			}
		}
		
		public void run() {
			try {
				ro = new RemoteObjectTestImpl();
				try {
					remoteObjectProvider.exportObject(ro,0);
					try {
						registry.bind("ro1", ro);
					} catch (Exception e) {
						fail("Unable to bind object");
					}
				} catch (Exception e) {
					fail("Unable to export object");
				}
			} catch (Exception e) {
				fail("Unable to create registry");
			}
		}
	}
	
	@Test
	public void testExportObject() {
		server = new Thread(new RunnableServer());		
		server.start();
	}
	
	@Test
	public void testClient() {
		Registry registry = null;
		try {
			registry = namingServer.getRegistry("127.0.0.1", 1099);
		} catch (Exception e) {
			fail("Unable de get created registry");
		}
		
		try {
			RemoteObjectTest ro1;
			RemoteObjectTest ro2 = new RemoteObjectTestImpl();
			ro2.setNb(1);
	
			ro1 = (RemoteObjectTest) registry.lookup("ro1");
			
			// RemoteObjectProvider remoteObjectProvider = RemoteObjectProvider.instance;
			remoteObjectProvider.exportObject(ro2,0);
			
			System.out.println(ro1.getNb()); // -1
			
			ro1.setNb(0);
			System.out.println(ro1.getNb()); // 0
			
			ro1.setNb(ro2);
			System.out.println(ro1.getNb()); // 1
			
			ro1.incNb();
			System.out.println(ro1.getNb()); // 2
			
			RemoteObjectTest ro3 = ro1.add2Nb(ro2, 0); //3
			System.out.println(ro3.getNb());
	
			ro3.incNb();
			System.out.println(ro3.getNb()); // 4	
			
			/*
			try {
				remoteObjectProvider.unexportObject(ro2); // throw an exception, only indicates socket is closed
			} catch (Exception e) {
				 // ?
			}
			*/
		} catch (Exception e) {
			fail();
		}
	}
	
}
