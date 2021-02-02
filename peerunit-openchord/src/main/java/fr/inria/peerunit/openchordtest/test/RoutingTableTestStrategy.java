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
package fr.inria.peerunit.openchordtest.test;

import fr.inria.peerunit.common.MethodDescription;
import fr.inria.peerunit.coordinator.CoordinationStrategy;
import fr.inria.peerunit.coordinator.TesterSet;
import fr.inria.peerunit.remote.GlobalVariables;
import fr.inria.peerunit.util.TesterUtil;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import fr.inria.peerunit.dhtmodel.RemoteModel;
import fr.inria.peerunit.dhtmodel.RemoteModelImpl;
import fr.inria.peerunit.dhtmodel.Model;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.UnicastRemoteObject;


import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sunye
 */
public class RoutingTableTestStrategy implements CoordinationStrategy {

    private static final Logger LOG =
            Logger.getLogger(RoutingTableTestStrategy.class.getName());
    private static final int PORT = 8282;
    private TesterSet testers;
    private TesterUtil defaults = TesterUtil.instance;
    private Registry registry;
    private GlobalVariables globals;
    private RemoteModelImpl rmi = new RemoteModelImpl();
    private Model model;

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
        short tries = 0;
        int groups;
        LOG.entering("RoutingTableTestStrategy", "testCaseExecution()");

        testers.execute("initialize");
        startModel();
        testers.execute("lookupModel");
        testers.execute("startBootstrap");
        testers.execute("startingNetwork");
        testers.execute("nodeCreation");

        do {
            Thread.sleep(10000);
            testers.execute("updateModel");
            tries++;
            groups = model.groups();
            LOG.log(Level.SEVERE, "Try: {0} Groups: {1}",
                    new Object[]{tries, groups});
        } while (!unicity() && tries < 100);

        LOG.log(Level.SEVERE, "Tries: {0}", tries);

        testers.execute("updateModel");
        LOG.log(Level.SEVERE, "Max Distance: {0}", distance());
        print();
        testers.execute("print");
        testers.execute("quit");
    }

    private void startModel() {
        Registry testRegistry;
        try {
            RemoteModel stub = (RemoteModel) UnicastRemoteObject.exportObject(rmi, 0);
            testRegistry = LocateRegistry.createRegistry(PORT);
            testRegistry.rebind("Model", stub);
            globals.put(1, InetAddress.getLocalHost().getHostName());

        } catch (UnknownHostException ex) {
            LOG.log(Level.SEVERE, null, ex);
            System.err.println(ex.getMessage());
        } catch (RemoteException ex) {
            LOG.log(Level.SEVERE, null, ex);
            System.err.println(ex.getMessage());
        }

        model = new Model(rmi);
        model.start();

    }

    public boolean unicity() {
        LOG.log(Level.INFO, "Unicity Test");

        return model.unicity(); // : "Unicity";
    }

    public int distance() {
        LOG.log(Level.INFO, "Unicity Test");
        return model.distance();
    }


    public void print() {
        model.print();
        model.stop();
    }
}
