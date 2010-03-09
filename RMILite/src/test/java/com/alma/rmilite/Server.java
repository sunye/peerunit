package com.alma.rmilite;

import com.alma.rmilite.registry.NamingServer;
import com.alma.rmilite.registry.Registry;
import com.alma.rmilite.server.RemoteObjectProvider;

public class Server {

	public static void main(String[] args) throws Exception {	
		RemoteObjectTest ro1 = new RemoteObjectTestImpl();
		RemoteObjectProvider remoteObjectProvider = ServerManager.remoteObjectProvider;;
		remoteObjectProvider.exportObject(ro1,0);
		
		NamingServer namingServer = ServerManager.namingServer;
		Registry registry = namingServer.createRegistry(1099);
		registry.bind("ro1", ro1);
	}
}
