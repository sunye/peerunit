package fr.inria.peerunit.freepastrytest.test;

import static fr.inria.peerunit.tester.Assert.fail;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import rice.pastry.Id;
import rice.pastry.NodeHandle;
import fr.inria.peerunit.parser.TestStep;
import java.net.UnknownHostException;

/**
 * Testing the recovery from peer isolation
 *
 * @author almeida
 *
 */
public class TestPeerIsolation extends AbstractFreePastryTest {

    private static final Logger LOG = Logger.getLogger(TestPeerIsolation.class.getName());
    private List<Id> volatiles = new ArrayList<Id>();



    public TestPeerIsolation() throws UnknownHostException {
        super();
    }


    @TestStep(range = "0", timeout = 40000, order = 0)
    @Override
    public void startBootstrap() throws Exception {

            super.startBootstrap();
    }

    @TestStep(range = "1-*", timeout = 100000, order = 1)
    @Override
    public void startingNetwork() throws Exception {

        super.startingNetwork();
    }


    @TestStep(order = 4, timeout = 100000, range = "0")
    public void listingTheNeighbours() throws RemoteException, InterruptedException {


        // Letting the system to stabilize
        while (peer.oldGetRoutingTable().isEmpty()) {
            Thread.sleep(sleep);
        }

        LOG.info("My Id: " + peer.getId());
        LOG.info("Neighbors: " + peer.getRoutingTable().toString());

        this.put(1, peer.oldGetRoutingTable());

        LOG.log(Level.INFO, "My ID {0}", peer.oldGetId().toString());
        for (NodeHandle nd : peer.oldGetRoutingTable()) {
            if (!peer.oldGetId().toString().equalsIgnoreCase(nd.getNodeId().toString())) {
                volatiles.add(nd.getNodeId());
                LOG.log(Level.INFO, " Successor to leave {0}", nd.getNodeId());
            }
        }


    }

    @TestStep(order = 5, timeout = 200000, range = "*")
    public void testLeave() throws InterruptedException, RemoteException {

        // Waiting a while to get the global variable
        //Thread.sleep(2000);

        if (this.getPeerName() != 0) {
            List<NodeHandle> actuals = (List<NodeHandle>) this.get(1);

            for (NodeHandle nd : actuals) {
                if (nd.getNodeId().toString().trim().equalsIgnoreCase(peer.oldGetId().toString().trim())) {
                    LOG.info("Leaving early");
                    this.kill();
                }
            }
        }


    }

    @TestStep(order=6, range = "*", timeout = 100000)
    public void searchingNeighbour() throws InterruptedException {
        
        int loopToFail = defaults.getLoopToFail();

        if (this.getPeerName() == 0) {
            List<NodeHandle> actuals;

            //Iterations to find someone in the routing table
            int timeToClean = 0;
            Id obj = null;
            boolean tableUpdated = false;

            while (!tableUpdated && (timeToClean < loopToFail )) {
                LOG.info(" Let's verify the table" + timeToClean);

                Thread.sleep(1000);


                actuals = peer.oldGetRoutingTable();

                for (NodeHandle nd : actuals) {
                    obj = nd.getNodeId();
                    LOG.log(Level.INFO, " Successor NodeId {0} is volatile {1}",
                            new Object[]{obj, volatiles.contains(obj)});

                    if ((obj != peer.oldGetId()) && (!volatiles.contains(obj))) {
                        LOG.info(" Table was updated, verdict may be PASS ");
                        tableUpdated = true;
                        timeToClean = defaults.getLoopToFail();
                    }
                }

                LOG.info("Demanding the routing table update");
                peer.pingNodes();
                timeToClean++;
            }

            assert tableUpdated : " Did not find a sucessor ";
            if (!tableUpdated) {
                LOG.info(" Did not find a sucessor ");
                fail("Routing Table wasn't updated. Still finding all volatiles. Increase qty of loops.");
            }
        }
        LOG.info(" Waiting to receive a  verdict ");
        Thread.sleep(1000);
    }
}
