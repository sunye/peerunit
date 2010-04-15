/*
This file is part of PeerUnit.

PeerUnit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

PeerUnit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with PeerUnit.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.inria.peerunit.bootstrapper;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.remote.Bootstrapper;
import fr.inria.peerunit.remote.DistributedTester;
import fr.inria.peerunit.util.TesterUtil;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 * @author Eduardo Almeida, Veronique PELLEAU
 * @version 1.0
 * @since 1.0
 */
public class BootstrapperImpl implements Bootstrapper, Serializable, Runnable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(BootstrapperImpl.class.getName());
    private TreeStrategy context;
    private TesterUtil defaults;
    private final AtomicBoolean shouldILeave = new AtomicBoolean(false);

    public BootstrapperImpl(TesterUtil tu) {
        defaults = tu;
        if (defaults.getCoordinationType() == 1) {
            LOG.fine("Using the HTree strategy");
            context = new ConcreteBtreeStrategy(defaults);
        } else {
            LOG.fine("Using the Grid strategy");
            context = new GridStrategy(defaults);
        }
    }

    public void run() {
        LOG.entering("BootstrapperImpl", "run()");
        LOG.info("Starting Bootstrapper");

        try {

            context.waitForTesterRegistration();
            context.buildTree();
            context.setCommunication();
            context.startRoot();

            LOG.fine("Waiting fot testers to terminate");
            this.waitForTesterTermination();
            context.cleanUp();
            LOG.info("[Bootstrapper] Finished !");
        } catch (RemoteException ex) {
            LOG.log(Level.SEVERE, "Remote exception", ex);
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, "Wait interrupted", ex);
        }
    }

    /**
     * 
     * @param t
     * @return
     * @throws RemoteException
     */
    public synchronized int register(DistributedTester t) throws RemoteException {
        LOG.entering("BootstrapperImpl", "register()");
        return context.register(t);
    }

    /**
     * Returns the current number of registered nodes
     * @return the current number of registered nodes
     */
    public int getRegistered() {
        LOG.entering("BootstrapperImpl", "getRegistered()");
        return context.getRegistered();
    }

    private void waitForTesterTermination() throws InterruptedException {
        LOG.entering("BootstrapperImpl", "waitForTesterTermination()");
        while (!shouldILeave.get()) {
            synchronized (shouldILeave) {
                shouldILeave.wait();
            }
        }
        LOG.exiting("BootstrapperImpl", "waitForTesterTermination()");
    }

    public void quit() throws RemoteException {
        LOG.entering("BootstrapperImpl", "quit()");
        synchronized (shouldILeave) {
            shouldILeave.set(true);
            shouldILeave.notifyAll();
        }
        LOG.exiting("BootstrapperImpl", "quit()");
    }
}

