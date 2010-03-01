package com.alma.rmilite;

import com.alma.rmilite.registry.NamingServer;
import com.alma.rmilite.registry.Registry;
import com.alma.rmilite.server.RemoteObjectProvider;

public class Client {
	
	public static void main(String[] args) throws Exception {
		RemoteObject ro1;
		RemoteObject ro2 = new RemoteObjectImpl();
		ro2.setNb(1);
		
		NamingServer namingServer = NamingServer.instance;
		Registry registry = namingServer.getRegistry("127.0.0.1", 1099);
		ro1 = (RemoteObject) registry.lookup("ro1");
		
		RemoteObjectProvider remoteObjectProvider = RemoteObjectProvider.instance;
		remoteObjectProvider.exportObject(ro2,0);
		
		System.out.println(ro1.getNb()); // -1
		
		ro1.setNb(0);
		System.out.println(ro1.getNb()); // 0
		
		ro1.setNb(ro2);
		System.out.println(ro1.getNb()); // 1
		
		ro1.incNb();
		System.out.println(ro1.getNb()); // 2
		
		RemoteObject ro3 = ro1.add2Nb(ro2, 0); //3
		System.out.println(ro3.getNb());

		ro3.incNb();
		System.out.println(ro3.getNb()); // 4	
		
		remoteObjectProvider.unexportObject(ro2); // throw an exception, only indicates socket is closed 
	}
}
