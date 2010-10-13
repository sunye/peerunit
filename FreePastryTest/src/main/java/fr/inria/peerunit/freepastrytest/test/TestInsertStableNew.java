package fr.inria.peerunit.freepastrytest.test;

import static fr.inria.peerunit.tester.Assert.inconclusive;

import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import rice.p2p.past.PastContent;
import rice.tutorial.past.MyPastContent;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.tester.Assert;
import fr.inria.peerunit.freepastrytest.Network;
import java.util.HashSet;
import java.util.Random;

/**
 * Test Insert and retrieve on a stable system
 * @author almeida
 *
 */
public class TestInsertStableNew extends AbstractFreePastryTest {

    private static Logger log = Logger.getLogger(TestInsertStableNew.class.getName());
    private List<String> expecteds = new ArrayList<String>();

    @TestStep(range = "0", timeout = 100000, order = 1)
    public void startNetwork() throws Exception {
//        this.bootstrap();
        Thread.sleep(sleep);
    }

    @TestStep(range = "*", timeout = 100000, order = 2)
    public void joiningNet() throws Exception {


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
     * Forces the routing table update
     *
     */
    @TestStep(range = "*", timeout = 100000, order = 3)
    public void stabilize() throws InterruptedException {
        for (int i = 0; i < 4; i++) {
            peer.pingNodes();
            Thread.sleep(sleep);
        }
    }

    @TestStep(range = "*", timeout = 100000, order = 4)
    public void testInsert() throws InterruptedException, RemoteException {

        Random random = new Random();
        Thread.sleep(sleep);
        if (this.getPeerName() == 0) {
            List<PastContent> resultSet = new ArrayList<PastContent>();

            // these variables are final so that the continuation can access them
            for (int i = 0; i < OBJECTS; i++) {
                final String s = "test" + random.nextInt();//peer.env.getRandomSource().nextInt();

                // build the past content
                //final PastContent myContent = new MyPastContent(peer.localFactory.buildId(s), s);
                //peer.insert(myContent);


                peer.put(s, s);
                //resultSet.add(myContent);

            }
            this.put(-1, resultSet);
        }

    }

    //@TestStep(range = "*", timeout = 30000, order = 6)
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

    @TestStep(range = "*", timeout = 30000, order = 5)
    public void testRetrieveBis() throws RemoteException, InterruptedException {

        List<PastContent> contents = new ArrayList<PastContent>(OBJECTS);
        contents.addAll((List<PastContent>) this.get(0));
        Set<PastContent> retrieved = new HashSet<PastContent>();

        int times = 0;
        while (times < defaults.getLoopToFail() && retrieved.size() < OBJECTS) {
            for (PastContent p : contents) {
                peer.lookup(p.getId());
            }
            Thread.sleep(sleep);
            for (Object actual : peer.getResultSet()) {
                if (actual != null) {
                    retrieved.add((PastContent) actual);
                }
            }
            peer.pingNodes();
            times++;
        }

        log.info("Waiting a Verdict. Found " + retrieved.size() + " of " + OBJECTS) ;

        List<String> expected, found;
        expected = new ArrayList<String>(OBJECTS);
        found = new ArrayList<String>(OBJECTS);

        for (PastContent each: contents) {
            expected.add(each.toString());
        }

        for (PastContent each: retrieved) {
            found.add(each.toString());
        }

        assert found.containsAll(expected);


    }

    @AfterClass(timeout = 100000, range = "*")
    public void end() {
        log.info("Peer bye bye");
    }
}
