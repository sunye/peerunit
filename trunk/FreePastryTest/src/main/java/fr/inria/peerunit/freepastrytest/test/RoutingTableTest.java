/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.freepastrytest.test;

import fr.inria.peerunit.freepastrytest.model.RemoteModel;
import fr.inria.peerunit.freepastrytest.model.RemoteModelImpl;
import fr.inria.peerunit.freepastrytest.model.Model;
import fr.inria.peerunit.parser.TestStep;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author sunye
 */
public class RoutingTableTest extends AbstractFreePastryTest {

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

    @TestStep(range = "*", order = 2, timeout = 2000)
    public void lookupModel() throws RemoteException, NotBoundException {

        String hostName = (String) this.get(1);
        Registry registry = LocateRegistry.getRegistry(hostName, PORT);
        remoteModel = (RemoteModel) registry.lookup("Model");

        assert remoteModel != null;
    }

    @TestStep(range = "0", timeout = 40000, order = 3)
    @Override
    public void startBootstrap() throws UnknownHostException, IOException,
            InterruptedException {

        super.startBootstrap();
    }

    @TestStep(range = "1-*", timeout = 100000, order = 4)
    @Override
    public void startingNetwork() throws InterruptedException,
            UnknownHostException, IOException {

        super.startingNetwork();
    }

    @TestStep(range = "1-*", order = 5)
    public void nodeCreation() throws RemoteException {

        remoteModel.newNode(peer.getId());
    }

    @TestStep(range = "1-*", order = 6)
    public void updateModel() {
    }

    @TestStep(range = "6", order = 20)
    public void print() {
        model.print();
        model.stop();
    }
}
