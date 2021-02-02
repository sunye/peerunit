package fr.inria.mockdht;

import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
	public static void main(String args[]){
		try{
			Registry registry=LocateRegistry.getRegistry(InetAddress.getLocalHost().getHostAddress());
			RemoteDHT mock = (RemoteDHT) registry.lookup("DHTService") ;
			mock.put("a", "toto");
			mock.put("b", "halo");
			mock.put("a", "tota");
			System.out.println("-----> " + mock.get("a"));
			System.out.println("-----> " + mock.get("b"));

		}
		catch (Exception e){
			System.out.println(e);
			
		}
	}

}