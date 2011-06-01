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
package fr.inria.peerunit;

import fr.inria.peerunit.coordinator.CoordinationStrategy;
import fr.inria.peerunit.coordinator.TesterSet;
import fr.inria.peerunit.remote.GlobalVariables;
import fr.inria.peerunit.util.TesterUtil;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sunye
 */
public class TestSimpleStrategy implements CoordinationStrategy {

    private static final Logger LOG =
            Logger.getLogger(TestSimpleStrategy.class.getName());
    private static final int PORT = 8282;
    private TesterSet testers;
    private TesterUtil defaults = TesterUtil.instance;
    private Registry registry;
    private GlobalVariables globals;

    public void init(TesterSet ts) {
        testers = ts;

        try {
            registry = LocateRegistry.getRegistry(defaults.getRegistryPort());
            globals = (GlobalVariables) registry.lookup("Globals");
        } catch (NotBoundException ex) {
            LOG.log(Level.SEVERE, null, ex);
            System.err.println(ex.getMessage());
        } catch (AccessException ex) {
            LOG.log(Level.SEVERE, null, ex);
            System.err.println(ex.getMessage());
        } catch (RemoteException ex) {
            LOG.log(Level.SEVERE, null, ex);
            System.err.println(ex.getMessage());
        }
    }

    /**
     * 
     * @throws InterruptedException
     */
    public void testcaseExecution() throws InterruptedException {

        LOG.entering("TestSimpleStrategy", "testCaseExecution()");

        testers.execute("firstStep");

        for (int i=0;i<10;i++) {
            testers.execute("incrementCount");
        }

        testers.execute("lastStep");
    }

}
