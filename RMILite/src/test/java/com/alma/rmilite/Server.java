package com.alma.rmilite;

import com.alma.rmilite.registry.NamingServer;
import com.alma.rmilite.registry.Registry;

public class Server {

	public static void main(String[] args) throws Exception {	
		RemoteObject ro1 = new RemoteObjectImpl();
		
		NamingServer namingServer = NamingServer.instance;
		Registry registry = namingServer.createRegistry(1099);
		registry.bind("ro1", ro1);
	}
}
