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
import fr.inria.peerunit.coordinator.CoordinatorImpl;
import fr.inria.peerunit.coordinator.CoordinationStrategy;
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


import java.util.HashMap;
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
    private CoordinatorImpl coordinator;
    private Map<String, MethodDescription> methods;
    private TesterUtil defaults = TesterUtil.instance;
    private Registry registry;
    private GlobalVariables globals;
    private RemoteModelImpl rmi = new RemoteModelImpl();
    private Model model;

    public void init(CoordinatorImpl coord) {
        coordinator = coord;

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
     * Sequencial execution of test steps.
     *
     * @param schedule
     * @throws InterruptedException
     */
    public void testcaseExecution() throws InterruptedException {
        boolean unicity;
        short tries = 0;
        LOG.entering("RoutingTableTestStrategy", "testCaseExecution()");


        this.execute("initialize");
        //this.execute("startModel");
        startModel();

        this.execute("lookupModel");
        this.execute("startBootstrap");
        this.execute("startingNetwork");
        this.execute("nodeCreation");
        //this.execute("stabilize");


        do {
            Thread.sleep(10000);
            this.execute("updateModel");
            tries++;
        } while (!unicity() && tries < 100);

        System.out.println("Tries: " + tries);
        //this.execute("unicity");
        //this.execute("again");
        //this.execute("reunicity");
        //unicity();
        //this.execute("distance");
        distance();
        print();
        //this.execute("printPeer");
        //this.execute("print");
        //this.execute("quit");

    }

    private void execute(String str) throws InterruptedException {
        if (methods == null) {
            // Lazy initialization of methods Map.
            LOG.log(Level.FINE, "Method map initialization.");
            methods = new HashMap<String, MethodDescription>();
            for (MethodDescription each : coordinator.getSchedule().methods()) {
                methods.put(each.getName(), each);
            }
        }

        if (methods.containsKey(str)) {
            coordinator.execute(methods.get(str));
        } else {
            LOG.log(Level.WARNING, "Method not found: {0}", str);
        }
    }

    public void startModel() {
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

    public void distance() {
        LOG.log(Level.INFO, "Unicity Test");

        assert model.distance() : "Unicity";
    }

    public void printPeer() {
        //peer.print();
    }

    public void print() {
        model.print();
        model.stop();
        //registry.unbind("Model");
        //UnicastRemoteObject.unexportObject(registry, true);
        //UnicastRemoteObject.unexportObject(rmi, true);
    }
}
