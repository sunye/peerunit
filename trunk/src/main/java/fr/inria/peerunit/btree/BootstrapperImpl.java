package fr.inria.peerunit.btree;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.Bootstrapper;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.btreeStrategy.ConcreteBtreeStrategy;
import fr.inria.peerunit.btreeStrategy.ConcreteONSTreeStrategy;
import fr.inria.peerunit.btreeStrategy.TreeStrategy;
import fr.inria.peerunit.util.TesterUtil;

/**
 * 
 * @author Eduardo Almeida, Veronique PELLEAU
 * @version 1.0
 * @since 1.0
 */
public class BootstrapperImpl implements Bootstrapper, Serializable, Runnable {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(BootstrapperImpl.class.getName());
    private TreeStrategy context;
    private TesterUtil defaults;

    public BootstrapperImpl(TesterUtil tu) {
        defaults = tu;

        if (defaults.getTreeStrategy() == 2) {
            context = new ConcreteONSTreeStrategy();
        } else {
            context = new ConcreteBtreeStrategy(defaults);
        }
    }

    public void run() {
        log.entering("BootstrapperImpl", "run()");
        log.info("Starting Bootstrapper");
        
        try {
            
            context.waitForTesterRegistration();
            context.buildTree();
            context.setCommunication();
            context.startRoot();
            
            log.info("[Bootstrapper] Finished !");
            
        } catch (RemoteException ex) {
            log.log(Level.SEVERE, "Remote exception", ex);
        } catch (InterruptedException ex) {
            log.log(Level.SEVERE,"Wait interrupted" , ex);
        }
    }

    public synchronized int register(Tester t) throws RemoteException {
        log.entering("BootstrapperImpl", "register()");
        return context.register(t);
    }

    /**
     * Returns the current number of registered nodes
     * @return the current number of registered nodes
     */
    public int getRegistered() {
        log.entering("BootstrapperImpl", "getRegistered()");
        return context.getRegistered();
    }

    /**
     * Return true if id follow to Bootstrapper
     * @param id
     * @return
     * @throws RemoteException
     */
    public boolean isRoot(int id) throws RemoteException {
        log.entering("BootstrapperImpl", "isRoot(int)");

        return context.getNode(null).isRoot();
    }

}

