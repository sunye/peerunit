package fr.inria.peerunit.freepastrytest.test;

import static fr.inria.peerunit.test.assertion.Assert.fail;
import static fr.inria.peerunit.test.assertion.Assert.inconclusive;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import rice.pastry.Id;
import rice.pastry.NodeHandle;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.freepastrytest.Network;

/**
 * Test routing table update in an expanding system
 * @author almeida
 *
 */
public class TestNewJoin extends AbstractFreePastryTest {

    private static Logger log = Logger.getLogger(TestNewJoin.class.getName());
    private List<Id> firstSuccessors = new ArrayList<Id>();

    @TestStep(range = "*", timeout = 10000, order = 2)
    public void startingHalfNet() throws InterruptedException {


        if (this.getPeerName() % 2 != 0) {
            log.info("Joining in first");
            Network net = new Network();
            Thread.sleep(this.getPeerName() * 1000);

            if (!net.joinNetwork(peer, null, false, log)) {
                inconclusive("I couldn't join, sorry");
            }
            log.info("Getting cached boot " + net.getInetSocketAddress().toString());
            log.info("Running on port " + peer.getPort());
            log.info("Time to bootstrap");

        }

    }

    @TestStep(range = "*", timeout = 10000, order = 4)
    public void testFind() throws InterruptedException {

        Thread.sleep(sleep);
        if (this.getPeerName() % 2 != 0) {
            log.info("My ID " + peer.getId());
            for (NodeHandle nd : peer.getRoutingTable()) {
                log.info("Successor NodeId " + nd.getId());
                firstSuccessors.add(nd.getNodeId());
            }
        }


    }

    @TestStep(range = "*", timeout = 10000, order = 5)
    public void startingOtherHalfNet() throws InterruptedException {



        if (this.getPeerName() % 2 == 0) {
            log.info("Joining in first");
            Network net = new Network();
            Thread.sleep(this.getPeerName() * 1000);

            if (!net.joinNetwork(peer, null, false, log)) {
                inconclusive("I couldn't join, sorry");
            }
            log.info("Getting cached boot " + net.getInetSocketAddress().toString());
            log.info("Running on port " + peer.getPort());
            log.info("Time to bootstrap");

        }

    }

    @TestStep(range = "*", timeout = 10000, order = 7)
    public void testFindAgain() throws InterruptedException {

        if ((this.getPeerName() % 2 != 0)) {
            List<NodeHandle> actuals;

            //Iterations to clean the volatiles from the routing table
            int timeToUpdate = 0;
            Id obj = null;
            boolean tableUpdated = false;
            while (!tableUpdated && timeToUpdate < defaults.getLoopToFail()) {
                log.info("Verifying the " + timeToUpdate + " time ");

                Thread.sleep(1000);

                actuals = peer.getRoutingTable();

                for (NodeHandle nd : actuals) {
                    obj = nd.getNodeId();
                    log.info(" Successor NodeId " + obj);
                    if (!firstSuccessors.contains(obj)) {
                        log.info("List updated, the verdict may be PASS ");
                        tableUpdated = true;
                    }
                }

                //Demanding the routing table update
                peer.pingNodes();

                timeToUpdate++;
            }
            if (!tableUpdated) {
                fail("Routing Table wasn't updated. Still finding all volatiles. Increase qty of loops.");
            } else {
                log.info("List updated, the verdict may be PASS. Table updated " + timeToUpdate + " times.");
            }

        }

    }

    @AfterClass(timeout = 100000, range = "*")
    public void end() {
        log.info("[PastryTest] Peer bye bye");
    }
}
