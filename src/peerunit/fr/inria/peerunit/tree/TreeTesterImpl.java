package fr.inria.peerunit.tree;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class TreeTesterImpl  implements TreeTester{
	
	public static void main(String args[]){
		TreeTester tt= new TreeTesterImpl();
		tt.startNet(tt);
		tt.run();		
	}
	
	public void startNet(TreeTester t){		
		Registry registry=null;
		try {
			registry = LocateRegistry.getRegistry("172.16.9.101");
			Bootstrapper boot = (Bootstrapper) registry.lookup("Bootstrapper");	
			boot.register(t);
			UnicastRemoteObject.exportObject(t);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public void run(){
		
	}
}
