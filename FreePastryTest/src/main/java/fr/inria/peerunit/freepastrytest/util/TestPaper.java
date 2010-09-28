package fr.inria.peerunit.freepastrytest.util;

import static fr.inria.peerunit.tester.Assert.assertEquals;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.TestStep;

public class TestPaper {

    static TestPaper test;
    // Data to exercise a DHT
    String expected = "fourteen";
    int expectedKey = 14;
    String actual;
    Peer peer;

    @BeforeClass(range = "*", timeout = 100)
    public void start() {
        // Pseudocode to instantiate a peer
        //peer=new Peer();
    }

    @TestStep(range = "0-2", timeout = 100, order = 1)
    public void join() {
        // Let's join the system
        peer.join();
    }

    @TestStep(range = "2", timeout = 100, order = 2)
    public void put() {
        // Put data
        peer.put(expectedKey, expected);
    }

    @TestStep(range = "3-4", timeout = 100, order = 3)
    public void joinOthers() {
        // The rest of the peers join the system
        peer.join();
    }

    @TestStep(range = "3-4", timeout = 100, order = 4)
    public void retrieve() {
        // Retrieving the inserted data
        actual = peer.get(expectedKey);
    }

    @TestStep(range = "3-4", timeout = 100, order = 5)
    public void assertRetrieve() {
        // Let's see if we got the expected data
        assertEquals(expected, actual);
    }

    @AfterClass(range = "*", timeout = 100)
    public void stop() {
        peer.leave();
    }
}
