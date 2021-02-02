/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.openchordtest.test;

import fr.inria.peerunit.dhtmodel.RemoteModel;
import fr.inria.peerunit.dhtmodel.Model;
import fr.inria.peerunit.parser.TestStep;
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
public class RoutingTableTest extends AbstractOpenChordTest {

    /**
     * Logger
     */
    private static final Logger LOG = Logger.getLogger(RoutingTableTest.class.getName());
    
    /**
     * Port for RMI Registry
     */
    private static final int PORT = 8282;
    
    private Model model;
    private RemoteModel remoteModel;
    private Registry registry;




    @TestStep(range = "*", order = 2, timeout = 40000)
    public void lookupModel() throws RemoteException, NotBoundException {

        String hostName = (String) this.get(1);
        LOG.info(hostName);
        
        registry = LocateRegistry.getRegistry(hostName, PORT);
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

    @TestStep(range = "*", order = 6, timeout = 110000)
    public void stabilize() throws InterruptedException {
        Thread.sleep(100000);
    }


    @TestStep(range = "*", order = 7)
    public void updateModel() throws RemoteException {

        LOG.log(Level.INFO, "Neighbors size: {0}", peer.getRoutingTable().size());
        remoteModel.updateNode(peer.getId(), peer.getRoutingTable());
    }



    /**
    @TestStep(range = "*", order = 10, timeout = 110000)
    public void again() throws Exception {
        Thread.sleep(100000);
        remoteModel.updateNode(peer.getId(), peer.getRoutingTable());
    }
     */


    /**
    @TestStep(range = "*", order = 12, timeout = 200000)
    public void unicityLoop() throws InterruptedException, RemoteException {
        for(int i = 0; i < 10; i++) {
            Thread.sleep(5000);
            remoteModel.updateNode(peer.getId(), peer.getRoutingTable());
            if (this.getId() == 6) {
                model.unicity();
            }
             Thread.sleep(5000);

        }
    }
*/
    
    @TestStep(range = "*", order = 14, timeout = 200000)
    public void print() throws InterruptedException, RemoteException {
        peer.print();

    }


    @TestStep(range = "*", order = 22)
    public void quit() throws RemoteException, NotBoundException {
        peer.leave();
        //UnicastRemoteObject.unexportObject(remoteModel, true);
        

    }
}
