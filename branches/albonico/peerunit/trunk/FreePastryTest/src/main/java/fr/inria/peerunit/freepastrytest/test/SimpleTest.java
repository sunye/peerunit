package fr.inria.peerunit.freepastrytest.test;

import fr.inria.peerunit.freepastrytest.PastryPeer;
import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.TestStep;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SimpleTest extends AbstractFreePastryTest {
    // logger from jdk

    private static final int PORT = 1200;
    private static InetAddress HOST;

    public SimpleTest() throws UnknownHostException {
        HOST = InetAddress.getLocalHost();
    }

    @TestStep(range = "0", timeout = 40000, order = 0)
    public void startBootstrap() throws UnknownHostException, IOException,
            InterruptedException {

        InetSocketAddress address =
                new InetSocketAddress(HOST, PORT);

        peer = new PastryPeer(address);
        peer.bootsrap();
        peer.createPast();
        this.put(0, address);

        //Thread.sleep(16000);
    }

    @TestStep(range = "1-*", timeout = 100000, order = 1)
    public void startingNetwork() throws InterruptedException,
            UnknownHostException, IOException {

        Thread.sleep(this.getPeerName() * 100);
        InetSocketAddress address = (InetSocketAddress) this.get(0);

        peer = new PastryPeer(address);
        peer.join();
        peer.createPast();
    }

    /**
     * Stabilize the network.
     */
    //@TestStep(range = "*", timeout = 100000, order = 2)
    public void stabilize() throws InterruptedException {
        assert peer != null;

        for (int i = 0; i < 4; i++) {
            // Force the routing table update
            peer.pingNodes();
            Thread.sleep(1600);
        }
    }

    /**
     * Put some data and store in this variables.
     */
    @TestStep(range = "1", timeout = 10000, order = 3)
    public void put() throws RemoteException, InterruptedException {
        Random random = new Random();
        Map<String, String> expecteds = new HashMap<String, String>();

        for (int i = 0; i < 2; i++) {
            String value = "test" + random.nextInt();
            peer.put(value, value);
            expecteds.put(value, value);
        }

        // Use a this variable to store the expected data
        this.put(1, expecteds);
    }

    /**
     * Get the data and the verdict.
     */
    @TestStep(range = "*", timeout = 10000, order = 4)
    public void get() throws RemoteException, InterruptedException {
        Map<String, String> expectedContent = (Map<String, String>) this.get(1);

        for (String key : expectedContent.keySet()) {
            String result = peer.get(key);
            assert expectedContent.get(key).equals(result) : "Wrong value.";

        }
    }
}
