package com.alma.rmilite;

import com.alma.rmilite.registry.Registry;

public class FakeServer {
    private static ConfigManagerStrategy configManagerStrategy = null;
    private static int port;

    private static Registry registry;

    private static RemoteObjectTest ro;

    public static void main(String args[]) {

	port = Integer.decode(args[0]);

	System.out.println("Using port " + port);

	if (args.length == 2 && args[1].equals("--rmi")) {
	    configManagerStrategy = new ConfigManagerRMIStrategy();
	    System.out.println("Starting FakeServer using RMI");
	} else {
	    configManagerStrategy = new ConfigManagerSocketStrategy();
	    System.out.println("Starting FakeServer using Socket");
	}

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
	    registry = configManagerStrategy.getNamingServer().createRegistry(
		    port);
	    System.out.println("Registry created on port " + port);
	} catch (Exception e) {
	    System.out.println("Unable to create registry");
	}
    }

    private static void createAndBindObject() {
	try {
	    ro = new RemoteObjectTestImpl();
	    try {
		configManagerStrategy.getRemoteObjectProvider().exportObject(
			ro, 0);
		try {
		    registry.bind("ro1", ro);
		    System.out.println("Object bound");
		} catch (Exception e) {
		    System.out.println("Unable to bind object");
		}
	    } catch (Exception e) {
		e.printStackTrace();
		System.out.println("Unable to export object");
	    }
	} catch (Exception e) {
	    System.out.println("Unable to create registry");
	}
    }
}
