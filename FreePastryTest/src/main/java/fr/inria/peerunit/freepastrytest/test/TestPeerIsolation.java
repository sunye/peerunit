package fr.inria.peerunit.freepastrytest.test;

import static fr.inria.peerunit.tester.Assert.fail;
import static fr.inria.peerunit.tester.Assert.inconclusive;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import rice.pastry.Id;
import rice.pastry.NodeHandle;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.freepastrytest.Network;

/**
 * Testing the recovery from peer isolation
 *
 * @author almeida
 *
 */
public class TestPeerIsolation extends AbstractFreePastryTest {

    private static final Logger log = Logger.getLogger(TestPeerIsolation.class.getName());
    private List<Id> volatiles = new ArrayList<Id>();

    @TestStep(range = "*", timeout = 10000, order = 1)
    public void startingNetwork() throws Exception {


        log.info("Joining in first");
        Network net = new Network();
        Thread.sleep(this.getPeerName() * 1000);

        if (!net.joinNetwork(peer, null, false, log)) {
            inconclusive("I couldn't join, sorry");
        }
        log.log(Level.INFO, "Getting bootstrapper {0}", net.getInetSocketAddress().toString());
        log.log(Level.INFO, "Running on port {0}", peer.getPort());
        log.info("Time to bootstrap");


    }

    @TestStep(order = 4, timeout = 100000, range = "0")
    public void listingTheNeighbours() throws RemoteException, InterruptedException {


        // Letting the system to stabilize
        while (peer.getRoutingTable().isEmpty()) {
            Thread.sleep(sleep);
        }


        this.put(1, peer.getRoutingTable());

        log.log(Level.INFO, "My ID {0}", peer.getId().toString());
        for (NodeHandle nd : peer.getRoutingTable()) {
            if (!peer.getId().toString().equalsIgnoreCase(nd.getNodeId().toString())) {
                volatiles.add(nd.getNodeId());
                log.log(Level.INFO, " Successor to leave {0}", nd.getNodeId());
            }
        }


    }

    @TestStep(order = 5, timeout = 100000, range = "*")
    public void testLeave() throws InterruptedException, RemoteException {

        // Waiting a while to get the global variable
        Thread.sleep(2000);

        if (this.getPeerName() != 0) {
            List<NodeHandle> actuals = (List<NodeHandle>) this.get(1);

            for (NodeHandle nd : actuals) {
                if (nd.getNodeId().toString().trim().equalsIgnoreCase(peer.getId().toString().trim())) {
                    log.info("Leaving early");
                    this.kill();
                }
            }
        }


    }

    @TestStep(range = "*", timeout = 10000, order = 6)
    public void searchingNeighbour() throws InterruptedException {

        if (this.getPeerName() == 0) {
            List<NodeHandle> actuals;

            //Iterations to find someone in the routing table
            int timeToClean = 0;
            Id obj = null;
            boolean tableUpdated = false;

            while (!tableUpdated && (timeToClean < defaults.getLoopToFail())) {
                log.info(" Let's verify the table" + timeToClean);

                Thread.sleep(1000);


                actuals = peer.getRoutingTable();

                for (NodeHandle nd : actuals) {
                    obj = nd.getNodeId();
                    log.log(Level.INFO, " Successor NodeId {0} is volatile {1}", new Object[]{obj, volatiles.contains(obj)});

                    if ((obj != peer.getId()) && (!volatiles.contains(obj))) {
                        log.info(" Table was updated, verdict may be PASS ");
                        tableUpdated = true;
                        timeToClean = defaults.getLoopToFail();
                    }
                }

                log.info("Demanding the routing table update");
                peer.pingNodes();
                timeToClean++;
            }
            if (!tableUpdated) {
                log.info(" Did not find a sucessor ");
                fail("Routing Table wasn't updated. Still finding all volatiles. Increase qty of loops.");
            }
        }
        log.info(" Waiting to receive a  verdict ");
        Thread.sleep(1000);
    }

    @AfterClass(timeout = 100000, range = "*")
    public void end() {
        log.info("[PastryTest] Peer bye bye");
    }
}
