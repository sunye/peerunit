/**
 * 
 */
package fr.inria.peerunit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
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
    private GlobalVariablesImpl globals;
    private GlobalVariables globalsStub;
    private BootstrapperImpl bootstrapper;
    private TesterUtil defaults;

    /**
     * @param defaults
     */
    public CoordinatorRunner(TesterUtil tu) {
        defaults = tu;
        try {
            this.initializeLogger();
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }

    }

    /**
     * @param defaults
     * @throws IOException
     */
    private void initializeLogger() throws IOException {
        Level l = defaults.getLogLevel();
        FileHandler handler = new FileHandler("coordination.log");
        handler.setFormatter(new LogFormat());
        handler.setLevel(l);
        Logger.getLogger("").addHandler(handler);
        //Logger.getLogger("").setLevel(defaults.getLogLevel());
        Logger.getLogger("fr.inria").setLevel(l);
        Logger.getLogger("").getHandlers()[0].setLevel(l);
    }

    private void start() throws RemoteException, AlreadyBoundException, InterruptedException, NotBoundException {
        log.entering("CoordinatorRunner", "start()");

        // Bind the remote object's stub in the registry
        Registry registry = LocateRegistry.createRegistry(1099);
        globals = new GlobalVariablesImpl();
        globalsStub = (GlobalVariables) UnicastRemoteObject.exportObject(globals, 0);
        registry.bind("Globals", globalsStub);

        if (defaults.getCoordinationType() == 0) {
            log.info("Using the centralized architecture");
            cii = new CoordinatorImpl(defaults);
            stub = (Coordinator) UnicastRemoteObject.exportObject(cii, 0);
            log.info("New Coordinator address is : " + defaults.getServerAddr());
            registry.bind("Coordinator", stub);

            Thread coordination = new Thread(cii, "Coordinator");
            coordination.start();
            coordination.join();
            log.info("Coordination thread finished");
            registry.unbind("Coordinator");
        } else {
            log.info("Using the distributed architecture");
            bootstrapper = new BootstrapperImpl(defaults);
            Bootstrapper bootStub = (Bootstrapper) UnicastRemoteObject.exportObject(bootstrapper, 0);
            registry.bind("Bootstrapper", bootStub);
            Thread boot = new Thread(bootstrapper, "Bootstrapper");
            boot.start();
            boot.join();
            log.info("Bootstrap thread finished");

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

        } catch (RemoteException ex) {
            log.log(Level.SEVERE, null, ex);
        } catch (AlreadyBoundException ex) {
            log.log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            log.log(Level.SEVERE, null, ex);
        } catch (NotBoundException ex) {
            log.log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException e) {
            System.err.println("Error: Unable to open properties file");
            System.exit(1);
        }

    }
}
