/*
    This file is part of PeerUnit.

    Foobar is free software: you can redistribute it and/or modify
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
package fr.inria.peerunit.btree;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.Bootstrapper;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.btreeStrategy.ConcreteBtreeStrategy;
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
        context = new ConcreteBtreeStrategy(defaults);
    }

    public void run() {
        log.entering("BootstrapperImpl", "run()");
        log.info("Starting Bootstrapper");
        
        try {
            
            context.waitForTesterRegistration();
            context.buildTree();
            context.setCommunication();
            context.startRoot();
            //context.cleanUp();
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

}

