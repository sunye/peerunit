package fr.inria.peerunit.tree;

import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import fr.inria.peerunit.Tester;

public class BootstrapperImpl implements Runnable, Remote, Bootstrapper{
	//private static int expectedTesters=TesterUtil.getExpectedPeers();
	private static int expectedTesters=4;

	private AtomicInteger registered = new AtomicInteger(0);
	
	private List<TreeTester> registeredTesters = Collections
	.synchronizedList(new ArrayList<TreeTester>());

	public static void main(String[] args) {	
		BootstrapperImpl boot=new BootstrapperImpl();

		try {
			boot.startNet(boot);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		}

		Thread updateThread = new Thread(boot, "StockInfoUpdate");
		updateThread.start();
	}

	private void startNet(BootstrapperImpl boot) throws RemoteException, AlreadyBoundException  {
		// Bind the remote object's stub in the registry
		Registry registry = LocateRegistry.createRegistry(1099);
		registry.bind("Bootstrapper", boot);
	}

	public void run() {
		waitForTesterRegistration() ;
	}

	private void waitForTesterRegistration() {
		while (registered.get() < expectedTesters) {
			try {
				synchronized (registeredTesters) {
					registeredTesters.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void register(TreeTester t)	throws RemoteException {	
		registeredTesters.add(t);		
		registeredTesters.notifyAll();		
	}
	
	public  int getNewId(TreeTester t) throws RemoteException {
		int id = runningTesters.getAndIncrement();
		System.out.println("New Registered Tester: " + id);
		return id;
	}
}
