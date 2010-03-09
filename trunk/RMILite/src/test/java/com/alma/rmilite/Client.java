package com.alma.rmilite;

import com.alma.rmilite.registry.NamingServer;
import com.alma.rmilite.registry.Registry;
import com.alma.rmilite.server.RemoteObjectProvider;

public class Client {
	
	public static void main(String[] args) throws Exception {
		RemoteObjectTest ro1;
		RemoteObjectTest ro2 = new RemoteObjectTestImpl();
		ro2.setNb(1);
		
		NamingServer namingServer = ClientManager.namingServer;
		Registry registry = namingServer.getRegistry("127.0.0.1", 1099);
		ro1 = (RemoteObjectTest) registry.lookup("ro1");
		
		RemoteObjectProvider remoteObjectProvider = ClientManager.remoteObjectProvider;
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
		
		remoteObjectProvider.unexportObject(ro2); // throw an exception, only indicates socket is closed 
	}
}
