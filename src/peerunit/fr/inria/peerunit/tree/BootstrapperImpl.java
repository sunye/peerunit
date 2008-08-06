package fr.inria.peerunit.tree;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import fr.inria.peerunit.util.TesterUtil;

public class BootstrapperImpl  implements  Bootstrapper,Serializable {
	private static final long serialVersionUID = 1L;

	private AtomicInteger registered = new AtomicInteger(0);
	
	private static int expectedTesters=TesterUtil.getExpectedPeers();
	
	private List<TreeTester> testers = Collections.synchronizedList(new ArrayList<TreeTester>());
	
	protected BootstrapperImpl() throws RemoteException {
		super();		
	}
	
	public static void main(String[] args) throws RemoteException {	
		BootstrapperImpl boot=new BootstrapperImpl();
		boot.startNet(boot);	
		
		while (boot.getRegistered() < expectedTesters) {
			try {								
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("[Bootstrapper] I'm done !");
	}	

	private void startNet(BootstrapperImpl boot) {
		Bootstrapper stub=null;
		try {
			stub = (Bootstrapper) UnicastRemoteObject.exportObject(
					boot, 0);
			Registry registry = LocateRegistry.createRegistry(1099);

			registry.bind("Bootstrapper", stub);			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized int register(TreeTester t)	throws RemoteException {		
		int id = registered.getAndIncrement();					
		testers.add(t);
		System.out.println("New Registered ID: " + id);
		return id;
	}
	
	public int getRegistered(){
		return registered.get();
	}
}
