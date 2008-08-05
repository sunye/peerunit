package fr.inria.peerunit.tree;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class TreeTesterImpl  implements TreeTester,Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private int id;

	public static void main(String args[]) throws Exception{
		TreeTester tt= new TreeTesterImpl();
		tt.startNet();		
	}
	
	public  void startNet() throws RemoteException{		

		try {
						
			Registry registry = LocateRegistry.getRegistry("172.16.9.101");
			Bootstrapper boot = (Bootstrapper) registry.lookup("Bootstrapper");	
			id=boot.register(this);
			System.out.println("My ID is: "+id);
			UnicastRemoteObject.exportObject(this);			
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} 
	}	
}
