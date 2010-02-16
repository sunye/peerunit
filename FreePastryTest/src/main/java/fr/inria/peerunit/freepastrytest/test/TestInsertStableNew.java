package fr.inria.peerunit.freepastrytest.test;

import static fr.inria.peerunit.test.assertion.Assert.inconclusive;

import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import rice.p2p.past.PastContent;
import rice.tutorial.past.MyPastContent;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.test.assertion.Assert;
import fr.inria.peerunit.freepastrytest.Network;

/**
 * Test Insert and retrieve on a stable system
 * @author almeida
 *
 */
public class TestInsertStableNew extends AbstractFreePastryTest {

    private static Logger log = Logger.getLogger(TestInsertStableNew.class.getName());
    private List<String> expecteds = new ArrayList<String>();

    @TestStep(range = "*", timeout = 100000, order = 1)
    public void startingNetwork() throws RemoteException, InterruptedException {

        if (this.getPeerName() == 0) {
            Network net = new Network();
            if (!net.joinNetwork(peer, null, true, log)) {
                inconclusive("I couldn't become a boostrapper, sorry");
            }

            this.put(-1, net.getInetSocketAddress());
            log.info("Net created");

            while (!peer.isReady()) {
                Thread.sleep(16000);
            }
        }
        Thread.sleep(sleep);

    }

    @TestStep(range = "*", timeout = 100000, order = 2)
    public void joiningNet() throws RemoteException, InterruptedException {


        // Wait a while due to the bootstrapper performance
        Thread.sleep(16000);
        if (this.getPeerName() != 0) {
            log.info("Joining in first");
            Network net = new Network();
            Thread.sleep(this.getPeerName() * 1000);

            InetSocketAddress bootaddress = (InetSocketAddress) this.get(-1);
            log.info("Getting cached boot " + bootaddress.toString());

            if (!net.joinNetwork(peer, bootaddress, false, log)) {
                inconclusive("I couldn't join, sorry");
            }

            log.info("Running on port " + peer.getPort());
            log.info("Time to bootstrap");

        }

    }

    /**
     * Stabilize the network.
     */
    @TestStep(range = "*", timeout = 10000, order = 3)
    public void stabilize() throws InterruptedException {
        for (int i = 0; i < 4; i++) {

            // Force the routing table update
            peer.pingNodes();
            Thread.sleep(16000);

        }
    }

    @TestStep(range = "*", timeout = 100000, order = 4)
    public void testInsert() throws RemoteException, InterruptedException {

        if (this.getPeerName() == 0) {
            log.info("I will insert");
            List<PastContent> resultSet = new ArrayList<PastContent>();
            // these variables are final so that the continuation can access them
            for (int i = 0; i < defaults.getObjects(); i++) {
                final String s = "test" + peer.env.getRandomSource().nextInt();
                // build the past content
                final PastContent myContent = new MyPastContent(peer.localFactory.buildId(s), s);

                peer.insert(myContent);
                resultSet.add(myContent);
                Thread.sleep(150);
            }
            this.put(0, resultSet);

            log.info("Inserted " + resultSet.size());
        }

        Thread.sleep(sleep * 3);

    }

    @TestStep(range = "*", timeout = 10000, order = 5)
    public void testRetrieve() throws RemoteException, InterruptedException {
        // Get inserted data
        List<PastContent> cached = (List<PastContent>) this.get(0);

        // build the expecteds
        for (PastContent cachedObj : cached) {
            if (!expecteds.contains(cachedObj.toString())) {
                expecteds.add(cachedObj.toString());
            }
        }
        log.info("I may find " + expecteds.size() + " objects");

        // Lookup for data

        Thread.sleep(16000);
        String content;
        List<String> actuals = new ArrayList<String>();

        int timeToFind = 0;
        while (timeToFind < defaults.getLoopToFail()) {
            for (PastContent p : cached) {
                peer.lookup(p.getId());
            }

            Thread.sleep(sleep);

            log.info("Retrieval " + timeToFind);
            for (Object actual : peer.getResultSet()) {
                if (actual != null) {
                    if (!actuals.contains(actual.toString())) {
                        actuals.add(actual.toString());
                    }
                }
            }
            peer.pingNodes();
            Thread.sleep(sleep);
            timeToFind++;
        }
        log.info("Waiting a Verdict. Found " + actuals.size() + " of " + expecteds.size());
        Assert.assertListEquals("[Local verdict] ", expecteds, actuals);

    }

    @AfterClass(timeout = 100000, range = "*")
    public void end() {
        log.info("Peer bye bye");
    }
}
