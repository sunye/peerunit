package com.alma.rmilite;

import java.rmi.RemoteException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.alma.rmilite.registry.NamingServer;
import com.alma.rmilite.registry.Registry;
import com.alma.rmilite.server.RemoteObjectProvider;


public abstract class AbstractRemoteTest extends TestCase {

	private static RemoteObjectProvider remoteObjectProvider;
	private static NamingServer namingServer;
	
	protected static void setRemoteObjectProvider(
			RemoteObjectProvider remoteObjectProvider) {
		AbstractRemoteTest.remoteObjectProvider = remoteObjectProvider;
	}

	protected static void setNamingServer(NamingServer namingServer) {
		AbstractRemoteTest.namingServer = namingServer;
	}
	
	@Before
	public void setUp() {
		assertNotNull(remoteObjectProvider);
		assertNotNull(namingServer);
	}
	
	@Test
	public void testExportObject() {
		Registry registry = null;
		try {
			registry = namingServer.createRegistry(1099);
		} catch (Exception e) {
			try {
				registry = namingServer.createRegistry(1101);
			} catch (Exception e1) {
				fail("Unable to create registry");
			}
		}
			
		try {
			RemoteObjectTest ro1 = new RemoteObjectTestImpl();
			try {
				remoteObjectProvider.exportObject(ro1,0);
				try {
					registry.bind("ro1", ro1);					
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
			
			RemoteObjectProvider remoteObjectProvider = RemoteObjectProvider.instance;
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
