/**
 *
 */
package fr.inria.peerunit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import fr.inria.peerunit.btree.Bootstrapper;
import fr.inria.peerunit.btree.NodeImpl;
import fr.inria.peerunit.rmi.tester.TesterImpl;
import fr.inria.peerunit.util.TesterUtil;

/**
 * A <i>test</i> on a peer is launched by a instance of the <tt>TestRunner</tt> class. Its role consists
 * to launch the right implementation of <i>Tester</i> interface, i.e centralized or distributed (the type
 * is set in the framework property file ), passing it as argument the <tt>Class</tt> instance corresponding
 * to the <i>test case</i> to perform.
 * 
 * @author sunye
 * @author Aboubakar Koïta
 * @version 1.0
 * @since 1.0
 */
public class TestRunner {

	/**
	 * The test case that will be excuted and those name was
	 * passed at the command line.
	 */
	private Class <? extends TestCaseImpl> testcase;
	
	private TesterUtil defaults;

	/**
	 * Launch the right implementation of <i>tester</i> passing it as argument the <tt>Class</tt>
	 * instance corresponding to the <i>test case</i> to execute. 
	 * 
	 * @param klass the<tt>Class</tt> instance corresponding to the <i>test case</i> to execute
	 */	
	public TestRunner(Class<? extends TestCaseImpl> klass) {
		try {
			Registry registry = LocateRegistry.getRegistry(TesterUtil.instance
					.getServerAddr());
			testcase = klass;
			String filename = "peerunit.properties";
			
			if (new File(filename).exists()) {
				FileInputStream fs = new FileInputStream(filename);
				defaults = new TesterUtil(fs);
			} else {
				defaults = TesterUtil.instance;
			}
			
			switch (defaults.getCoordinationType()) {
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
		} catch (FileNotFoundException e) {
			System.out.println("Error: File not found.");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * In the main method, we get the only argument corresponding to class name of 
	 * <i>test case</i> to perform. We load the <tt>Class</tt> corresponding and we 
	 * create a <code>TestRunner</tt> instance.
	 *   
	 * @param args The only argument should be a class name of <i>test case</i> to execute
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
	 * To create a <i>tester</i> in the centralized architecture.
	 * 
	 * @param registry a instance of RMI Registry used to retrieve the </i>coordinator</i> of the centralized 
	 *        architecture.
	 * @throws RemoteException because the method is distant
	 * @throws NotBoundException if the <i>coordinator</i> is not bound in the RMI Registry
	 */	
	private void bootCentralized(Registry registry) throws RemoteException, NotBoundException {
		Coordinator coord = (Coordinator) registry.lookup("Coordinator");		
		TesterImpl tester = new TesterImpl(coord,defaults);
		tester.export(testcase);
		tester.run();	
	}
	
	/**
	 * To create a <i>tester</i> in  the distributed architecture.
	 * 
	 * @param registry a instance of RMI Registry used to retrieve the </i>bootstrapper</i> of the distributed 
	 *        architecture.
	 * @throws RemoteException because the method is distant
	 * @throws NotBoundException if the <i>boostrapper</i> is not bound in the RMI Registry
	 */			
	private void bootBTree(Registry registry) throws RemoteException, NotBoundException{		
		Bootstrapper boot = (Bootstrapper) registry.lookup("Bootstrapper");
		NodeImpl node= new NodeImpl(boot);
		node.export(testcase);
		node.run();
	}	
}
