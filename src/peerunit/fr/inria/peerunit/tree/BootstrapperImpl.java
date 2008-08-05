package fr.inria.peerunit.tree;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BootstrapperImpl  implements  Bootstrapper,Serializable {
	
	protected BootstrapperImpl() throws RemoteException {
		super();		
	}

	private static final long serialVersionUID = 1L;

	//private static int expectedTesters=TesterUtil.getExpectedPeers();
	private static int expectedTesters=4;

	private AtomicInteger registered = new AtomicInteger(0);
	
	private List<TreeTester> registeredTesters = Collections
	.synchronizedList(new ArrayList<TreeTester>());

	public static void main(String[] args) throws Exception{	
		BootstrapperImpl boot=new BootstrapperImpl();
		Bootstrapper stub = (Bootstrapper) UnicastRemoteObject.exportObject(
				boot, 0);
		Registry registry = LocateRegistry.createRegistry(1099);

		registry.bind("Bootstrapper", stub);
				
		//boot.waitForTesterRegistration() ;
		/*Thread updateThread = new Thread(boot, "Bootstrapper");
		updateThread.start();*/
	}	

	public void run() {
		waitForTesterRegistration() ;
	}

	private synchronized void waitForTesterRegistration() {		
		while (registeredTesters.size() < expectedTesters) {
			try {
				
				//synchronized (registeredTesters) {
					System.out.println("Waiting registrations "+registeredTesters.size() );
				//}
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized int register(TreeTester t)	throws RemoteException {		
		int id = registered.getAndIncrement();
		registeredTesters.add(t);		
		//registeredTesters.notifyAll();		
		System.out.println("New Registered Tester: " + id);
		return id;
	}
}
