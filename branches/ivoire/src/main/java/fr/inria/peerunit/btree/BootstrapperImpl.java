package fr.inria.peerunit.btree;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.ArchitectureImpl;
import fr.inria.peerunit.btreeStrategy.ConcreteBtreeStrategy;
import fr.inria.peerunit.btreeStrategy.ConcreteONSTreeStrategy;
import fr.inria.peerunit.btreeStrategy.Context;
import fr.inria.peerunit.rmi.coord.CoordinatorImpl;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;

/**
 * 
 * @author Eduardo Almeida, Veronique PELLEAU
 * @version 1.0
 * @since 1.0
 */
public class BootstrapperImpl extends ArchitectureImpl implements  Bootstrapper, Serializable  {

	private static final long serialVersionUID = 1L;

	private  int expectedTesters=TesterUtil.instance.getExpectedTesters();
	
	private static final Logger log = Logger.getLogger(CoordinatorImpl.class
			.getName());
	
	private  Context context;
	
	private  Long time;
		
	
	public BootstrapperImpl(TesterUtil defaults) {
		
		time=System.currentTimeMillis();
		
		switch (defaults.getTreeStrategy()) {
		case 1:
			context = new Context(new ConcreteBtreeStrategy());
			log.info("[Bootstrapper] Strategy BTree !");
			break;

		case 2:
			context = new Context(new ConcreteONSTreeStrategy());
			log.info("[Bootstrapper] Strategy optimized station tree !");
			break;
			
		case 3:
			context = new Context(new ConcreteONSTreeStrategy());
			break;
			
		default:
			context = new Context(new ConcreteBtreeStrategy());
			break;
		}
		
		context.buildTree();
		
		time=System.currentTimeMillis()-time;		
		log.info("[Bootstrapper] Built tree in: "+time+" msec");		
		log.info("[Bootstrapper] Nodes expected :"+context.getNodesSize());
		while (this.getRegistered() < expectedTesters) {
			try {								
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		this.setCommunication();
		log.info("[Bootstrapper] Finished !");
	}
	
	public static void main(String[] args)  {	
		// Log creation
		FileHandler handler;
		try {
			handler = new FileHandler(TesterUtil.instance.getLogfile());
			handler.setFormatter(new LogFormat());
			log.addHandler(handler);
			log.setLevel(TesterUtil.instance.getLogLevel());

		} catch (SecurityException e) {
			log.log(Level.SEVERE, "SecurityException", e);
			e.printStackTrace();
		} catch (IOException e) {
			log.log(Level.SEVERE, "IOException", e);
			e.printStackTrace();
		}

		// Check tester.coordination property
		ckeckFileProperty();
		
		BootstrapperImpl boot=new BootstrapperImpl(TesterUtil.instance);
		boot.startNet(boot);	
		System.out.println("[Bootstrapper] Lets see the tree !");		


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
		}  catch (AlreadyBoundException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized int register(Node node)	throws RemoteException {
		return context.register(node);
	}
	
	/**
	 * Returns the current number of registered nodes
	 * @return the current number of registered nodes
	 */
	public int getRegistered(){
		return context.getRegistered(); 
	}
	
	/**
	 * Return true if id follow to Bootstrapper
	 * @param id
	 * @return
	 * @throws RemoteException
	 */
	public boolean isRoot(int id) throws RemoteException {

		return context.getNode(id).isRoot();
	}
	
	private void setCommunication(){
		context.setCommunication();
	}



	/**
	 * Check that the test.coordination property is correctly record in the configuration file tester.properties
	 * i.e is equals at 1 for the distributed coordination
	 */
	private static void ckeckFileProperty() {
		if (TesterUtil.instance.getCoordinationType() != 1) {
			log.log(Level.WARNING, "Running the distributed coordination but using the centralized coordination in the configuration file tester.properties. \n" +
					"Set property test.coordination=1 to use distributed coordination.");
		}
	}
	
}
