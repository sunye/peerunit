package fr.inria.peerunit.freepastrytest.test;

import static fr.inria.peerunit.tester.Assert.inconclusive;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import rice.environment.Environment;
import rice.p2p.past.PastContent;
import rice.tutorial.past.MyPastContent;
import fr.inria.peerunit.freepastrytest.util.FreeLocalPort;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.tester.Assert;
import java.util.Random;

/**
 * Test Insert and retrieve on a stable system
 * @author almeida
 *
 */
public class TestInsertStable extends AbstractFreePastryTest {

    private static Logger log = Logger.getLogger(TestInsertStable.class.getName());

    @TestStep(range = "0", timeout = 10000, order = 1)
    public void startingNetwork() throws InterruptedException, RemoteException, UnknownHostException, IOException {


        log.info("I am " + this.getPeerName());
        //	Loads pastry settings
        Environment env = new Environment();

        // the port to use locally
        FreeLocalPort port = new FreeLocalPort();
        int bindport = port.getPort();
        log.info("LocalPort:" + bindport);

        // build the bootaddress from the command line args
        InetAddress bootaddr = InetAddress.getByName(defaults.getBootstrap());
        Integer bootport = new Integer(defaults.getBootstrapPort());
        InetSocketAddress bootaddress;

        bootaddress = new InetSocketAddress(bootaddr, bootport.intValue());
//        if (!peer.join(bindport, bootaddress, env, log, true)) {
//            inconclusive("I couldn't become a boostrapper, sorry");
//        }

//        this.put(-1, peer.getInetSocketAddress(bootaddr));
        //log.info("Cached boot address: "+bootaddress.toString());
        //test.put(-1,bootaddress);
        log.info("Net created");

        while (!peer.isReady()) {
            Thread.sleep(sleep);
        }


    }

    @TestStep(range = "*", timeout = 10000, order = 1)
    public void startingInitNet() throws RemoteException, InterruptedException, IOException {

        // Wait a while due to the bootstrapper performance
        Thread.sleep(sleep);
        if (this.getPeerName() != 0) {
            log.info("Joining in first");
            //	Loads pastry settings
            Environment env = new Environment();

            // the port to use locally
            FreeLocalPort port = new FreeLocalPort();
            int bindport = port.getPort();
            log.info("LocalPort:" + bindport);

            Thread.sleep(this.getPeerName() * 1000);
            InetSocketAddress bootaddress = (InetSocketAddress) this.get(-1);
            log.info("Getting cached boot " + bootaddress.toString());
//            if (!peer.join(bindport, bootaddress, env, log)) {
//                inconclusive("Couldn't boostrap, sorry");
//                this.put(this.getPeerName(), "INCONCLUSIVE");
//            }
            log.info("Running on port " + peer.getPort());
            log.info("Time to bootstrap");

        }

    }

    /**
     * Stabilize the network.
     */
    @TestStep(range = "*", timeout = 10000, order = 2)
    public void stabilize() {
        for (int i = 0; i < 4; i++) {
            try {
                // Force the routing table update
                peer.pingNodes();
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @TestStep(range = "*", timeout = 10000, order = 3)
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

    @TestStep(range = "*", timeout = 10000, order = 4)
    public void testRetrieve() throws InterruptedException, RemoteException {

        Thread.sleep(sleep);
        String content;
        List<String> actuals = new ArrayList<String>();
        List<String> expecteds = new ArrayList<String>();
        int timeToFind = 0;
        while (timeToFind < defaults.getLoopToFail()) {
            for (int i = 0; i < defaults.getObjects(); i++) {
                log.info("lookup for " + i);
                // Build the content
                content = "" + i;


                if (bootstrapped(i)) {
                    if (expecteds.size() < defaults.getObjects()) {
//                        expecteds.add(new MyPastContent(peer.localFactory.buildId(content), content).toString());
                    }
                   // peer.lookup(peer.localFactory.buildId(content));
                }
            }

            Thread.sleep(sleep);

            log.info("Retrieval " + timeToFind);
            for (Object actual : peer.getResultSet()) {
                if (actual != null) {
                    log.info("Actual " + actual.toString());

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

    /**
     * Returns true if the peer bootstrapped properly
     * @param i
     * @return
     */
    private boolean bootstrapped(int i) throws RemoteException {
        for (Integer peer : this.getCollection().keySet()) {
            if (peer.intValue() == i) {
                return false;
            }

        }


        return true;
    }
}
