package com.alma.rmilite;

import com.alma.rmilite.registry.NamingServer;
import com.alma.rmilite.registry.Registry;
import com.alma.rmilite.server.RemoteObjectProvider;

@Deprecated
public class FakeServer {
	private static ConfigManagerStrategy configManagerStrategy = null;

	private static NamingServer namingServer;
	private static RemoteObjectProvider remoteObjectProvider;

	private static Registry registry;

	private static RemoteObjectTest ro;
	
	public static void main(String args[]) {
		
		if (args.length == 1 && args[0].equals("RMI")) {
			configManagerStrategy = new ConfigManagerRMIStrategy();
			System.out.println("Starting FakeServer using RMI");
		} else {
			configManagerStrategy = new ConfigManagerSocketStrategy();
			System.out.println("Starting FakeServer using Socket");
		}
		
		namingServer = configManagerStrategy.getNamingServer();
		remoteObjectProvider = configManagerStrategy.getRemoteObjectProvider();
		
		createRegistry();
		
		createAndBindObject();
		
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		
	}

	private static void createRegistry() {	
		try {
			registry = namingServer.createRegistry(1099);
			System.out.println("Registry created on port 1099");
		} catch (Exception e) {
			try {
				registry = namingServer.createRegistry(1101);
				System.out.println("Registry created on port 1101");
			} catch (Exception e1) {
				// fail("Unable to create registry");
			}
		}
	}

	private static void createAndBindObject() {		
		try {
			ro = new RemoteObjectTestImpl();
			try {
				remoteObjectProvider.exportObject(ro,0);
				try {
					registry.bind("ro1", ro);
					System.out.println("Object bound");
				} catch (Exception e) {
					// fail("Unable to bind object");
				}
			} catch (Exception e) {
				// fail("Unable to export object");
			}
		} catch (Exception e) {
			// fail("Unable to create registry");
		}
	}
}
