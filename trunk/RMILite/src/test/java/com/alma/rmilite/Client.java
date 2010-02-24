package com.alma.rmilite;

import com.alma.rmilite.registry.NamingServer;
import com.alma.rmilite.registry.Registry;

public class Client {
	
	public static void main(String[] args) throws Exception {
		RemoteObject ro1;
		
		NamingServer namingServer = NamingServer.instance;
		Registry registry = namingServer.getRegistry("127.0.0.1", 1099);
		ro1 = (RemoteObject) registry.lookup("ro1");
		
		System.out.println(ro1.getNbCall());
		System.out.println(ro1.getNbCall());
		System.out.println(ro1.getNbCall());
	}
}
