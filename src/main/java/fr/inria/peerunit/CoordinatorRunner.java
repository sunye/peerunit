/**
 * 
 */
package fr.inria.peerunit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.btree.BootstrapperImpl;
import fr.inria.peerunit.rmi.coord.CoordinatorImpl;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;

/**
 * @author sunye
 * 
 */
public class CoordinatorRunner {

    private static final Logger log = Logger.getLogger(CoordinatorImpl.class.getName());
    private Coordinator stub;
    private CoordinatorImpl cii;
    private GlobalVariables globals;
    private BootstrapperImpl bootstrapper;
    private TesterUtil defaults;



    /**
     * @param defaults
     */
    public CoordinatorRunner(TesterUtil tu) {
    	defaults = tu;
    	
    	
        try {
            initializeLogger();
            initializeGlobals();
            if (defaults.getCoordinationType() == 1) {
                this.initializeCoordinator();
            } else {
            	this.initializeBootstrapper();
            }           
            

        } catch (Exception e) {
            log.log(Level.SEVERE, "Problem when running the coordinator", e);
        }
        

        
    }

	/**
	 * @throws RemoteException
	 */
	private void initializeCoordinator() throws RemoteException {
		cii = new CoordinatorImpl(defaults);
		stub = (Coordinator) UnicastRemoteObject.exportObject(cii, 0);
		log.info("New Coordinator address is : " + defaults.getServerAddr());
	}

	
	private void initializeBootstrapper() throws RemoteException {
        bootstrapper  = new BootstrapperImpl(defaults);
        //BootstrapperImpl.startNet(boot);
        bootstrapper.start();
	}
	
	/**
	 * @throws RemoteException
	 */
	private void initializeGlobals() throws RemoteException {
		globals = (GlobalVariables) UnicastRemoteObject.exportObject(new GlobalVariablesImpl(), 0);
	}

	/**
	 * @param defaults
	 * @throws IOException
	 */
	private void initializeLogger() throws IOException {
		FileHandler handler = new FileHandler(defaults.getLogfile());
		handler.setFormatter(new LogFormat());
		log.addHandler(handler);
		log.setLevel(defaults.getLogLevel());
	}

    private void start() {

        try {
            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("Coordinator", stub);
            registry.bind("Globals", globals);
            Thread coordination = new Thread(cii, "Coordinator");
            coordination.start();
            coordination.join();
            log.info("Coordination thread finished");
            registry.unbind("Coordinator");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Problem when running the coordinator", e);
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        TesterUtil defaults;

        try {
            String filename;
            if (args.length == 1) {
                filename = args[0];
                FileInputStream fs = new FileInputStream(filename);
                defaults = new TesterUtil(fs);
            } else if (new File("peerunit.properties").exists()) {
                filename = "peerunit.properties";
                FileInputStream fs = new FileInputStream(filename);
                defaults = new TesterUtil(fs);
            } else {
                defaults = TesterUtil.instance;
            }
            CoordinatorRunner cr = new CoordinatorRunner(defaults);
            cr.start();

        } catch (FileNotFoundException e) {
            System.err.println("Error: Unable to open properties file");
            System.exit(1);
        }

    }
}
