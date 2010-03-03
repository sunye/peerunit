package com.alma.rmilite;

import com.alma.rmilite.registry.NamingServer;
import com.alma.rmilite.registry.Registry;
import com.alma.rmilite.server.RemoteObjectProvider;

public class Server {

	public static void main(String[] args) throws Exception {	
		RemoteObjectTest ro1 = new RemoteObjectTestImpl();
		RemoteObjectProvider remoteObjectProvider = RemoteObjectProvider.instance;
		remoteObjectProvider.exportObject(ro1,0);
		
		NamingServer namingServer = NamingServer.instance;
		Registry registry = namingServer.createRegistry(1099);
		registry.bind("ro1", ro1);
	}
}
