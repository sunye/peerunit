/**
 *
 */
package fr.inria.peerunit;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import fr.inria.peerunit.rmi.tester.TesterImpl;
import fr.inria.peerunit.tree.Bootstrapper;
import fr.inria.peerunit.tree.TreeTesterImpl;
import fr.inria.peerunit.util.TesterUtil;

/**
 * @author sunye
 *
 * Main class. Should be used to execute all test cases.
 *
 */
public class TestRunner {

	/**
	 * The test case that will be excuted and those name was
	 * passed at the command line.
	 */
	private Class <? extends TestCaseImpl> testcase;

	public TestRunner(Class<? extends TestCaseImpl> klass) {
		try {
			Registry registry = LocateRegistry.getRegistry(TesterUtil
					.getServerAddr());
			testcase = klass;
			
			switch (TesterUtil.getCoordinationType()) {
			case 0:				
				System.out.println("Using the centralized coordination.");
				bootCentralized(registry);
				break;
			case 1:	
				System.out.println("Using the distributed coordination.");
				bootBTree(registry);
				break;

			default:
				System.out.println("Error: Cannot know where to boot.");
				break;
			}
		} catch (RemoteException e) {
			System.out.println("Error: Unable to communicate.");
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.out.println("Error: Unable to bind.");
			e.printStackTrace();
		}
	}

	/**
	 * @param args The only argument should be a class name.
	 */

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: java TestRunner <Test Case Class>");
		} else {
			String name = args[0];
			try {
				Class<?> klass = Class.forName(name);
				Class<? extends TestCaseImpl> tklass = klass.asSubclass(TestCaseImpl.class);
				new TestRunner(tklass);
			} catch (ClassCastException e) {
				System.out.println("Error: Class "+name+ " does not implement TestCase interface.");
			} catch (ClassNotFoundException e) {
				System.out.println("Error: Class "+name+ " not found.");
			}
		}
	}

	/**
	 * The tester, which should communicate with the Coordinator and
	 * control the test case execution.
	 */	
	private void bootCentralized(Registry registry)throws RemoteException, NotBoundException{
		Coordinator coord = (Coordinator) registry.lookup("Coordinator");		
		TesterImpl tester = new TesterImpl(coord);
		UnicastRemoteObject.exportObject(tester);
		tester.export(testcase);
		tester.run();	
	}
	
	private void bootBTree(Registry registry)throws RemoteException, NotBoundException{		
		Bootstrapper boot = (Bootstrapper) registry.lookup("Bootstrapper");
		TreeTesterImpl tester= new TreeTesterImpl(boot);
		tester.run();
	}	
}
