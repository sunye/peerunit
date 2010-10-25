/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.openchordtest.test;

import fr.inria.peerunit.dhtmodel.RemoteModel;
import fr.inria.peerunit.dhtmodel.RemoteModelImpl;
import fr.inria.peerunit.dhtmodel.Model;
import fr.inria.peerunit.parser.TestStep;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sunye
 */
public class RoutingTableTest extends AbstractOpenChordTest {

    private static final Logger LOG = Logger.getLogger(RoutingTableTest.class.getName());
    private static final int PORT = 8282;
    private Model model;
    private RemoteModel remoteModel;
    private Set<String> neighbors = new HashSet<String>();

    @TestStep(range = "6", order = 1)
    public void startModel() throws RemoteException, UnknownHostException {
        RemoteModelImpl rmi = new RemoteModelImpl();
        RemoteModel stub = (RemoteModel) UnicastRemoteObject.exportObject(rmi, 0);
        Registry registry = LocateRegistry.createRegistry(PORT);
        registry.rebind("Model", stub);
        this.put(1, InetAddress.getLocalHost().getHostName());

        model = new Model(rmi);
        model.start();

    }

    @TestStep(range = "*", order = 2, timeout = 40000)
    public void lookupModel() throws RemoteException, NotBoundException {

        String hostName = (String) this.get(1);
        Registry registry = LocateRegistry.getRegistry(hostName, PORT);
        remoteModel = (RemoteModel) registry.lookup("Model");

        assert remoteModel != null;
    }

    @TestStep(range = "0", timeout = 40000, order = 3)
    @Override
    public void startBootstrap() throws Exception {

        super.startBootstrap();
    }

    @TestStep(range = "1-*", timeout = 100000, order = 4)
    @Override
    public void startingNetwork() throws Exception {

        super.startingNetwork();
    }

    @TestStep(range = "*", order = 5)
    public void nodeCreation() throws RemoteException {
        LOG.log(Level.INFO, "My id: {0}", peer.getId());
        for (String each : peer.getRoutingTable()) {
            LOG.log(Level.INFO, "Neighbor: {0}", each);
        }


        remoteModel.newNode(peer.getId());
    }

    @TestStep(range = "*", order = 6, timeout = 11000)
    public void stabilize() throws InterruptedException {
        Thread.sleep(10000);
    }


    @TestStep(range = "*", order = 7)
    public void updateModel() throws RemoteException {

        LOG.log(Level.INFO, "Neighbors size: {0}", peer.getRoutingTable().size());
        remoteModel.updateNode(peer.getId(), peer.getRoutingTable());
    }

    @TestStep(range = "6", order = 9)
    public void unicity() {
        LOG.log(Level.INFO, "Unicity Test");

        assert model.unicity() : "Unicity";
    }


    @TestStep(range = "*", order = 10, timeout = 110000)
    public void again() throws Exception {
        Thread.sleep(100000);
        remoteModel.updateNode(peer.getId(), peer.getRoutingTable());
    }

    @TestStep(range = "6", order = 11)
    public void reUnicity() {
        LOG.log(Level.INFO, "Unicity Test");

        assert model.unicity() : "Unicity";
    }



    @TestStep(range = "6", order = 14)
    public void distance() {
        LOG.log(Level.INFO, "Unicity Test");

        assert model.distance() : "Unicity";
    }

    @TestStep(range = "6", order = 18)
    public void printPeer() {
        peer.print();
    }

    @TestStep(range = "6", order = 20)
    public void print() {
        model.print();
        model.stop();
    }
}
