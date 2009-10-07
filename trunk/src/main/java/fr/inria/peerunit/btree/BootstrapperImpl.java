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
public class BootstrapperImpl implements Bootstrapper, Serializable {

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

    public void start() {
        try {
            context.buildTree();
            context.waitForTesterRegistration();
            this.setCommunication();
            
            log.info("[Bootstrapper] Finished !");
        } catch (InterruptedException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }

    public synchronized int register(Tester t) throws RemoteException {
        return context.register(t);
    }

    /**
     * Returns the current number of registered nodes
     * @return the current number of registered nodes
     */
    public int getRegistered() {
        return context.getRegistered();
    }

    /**
     * Return true if id follow to Bootstrapper
     * @param id
     * @return
     * @throws RemoteException
     */
    public boolean isRoot(int id) throws RemoteException {

        return context.getNode(null).isRoot();
    }

    private void setCommunication() {
        context.setCommunication();
    }

}

