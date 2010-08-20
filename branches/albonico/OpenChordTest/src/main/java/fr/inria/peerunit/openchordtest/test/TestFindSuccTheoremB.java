package fr.inria.peerunit.openchordtest.test;

import static fr.inria.peerunit.tester.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.util.TesterUtil;

public class TestFindSuccTheoremB extends AbstractOpenChordTest {

    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(TestFindSuccTheoremB.class.getName());
    private static final int OBJECTS = TesterUtil.instance.getObjects();

    public TestFindSuccTheoremB() {
        super();
        callback.setCallback(OBJECTS, log);
    }

    @TestStep(order = 2,   timeout = 10000000, range = "*")
    public void find() throws Exception {

        chordPrint = (ChordImpl) chord;

        Thread.sleep(sleep);
        log.info("My ID is " + chord.getID());
        String[] succ = chordPrint.printSuccessorList().split("\n");
        for (String succList : succ) {
            log.info("Successor List " + succList + " size " + succ.length);
        }


    }

    @TestStep(order = 4, timeout = 10000000, range = "*")
    public void testLeave() throws Exception {

        Thread.sleep(sleep);


        if (this.getName() % 10 == 9) {
            log.info("Leaving early");

            chord.leave();
            String insertValue = chord.getID().toString().substring(0, 2) + " " + localURL.toString();
            this.put(this.getName(), insertValue);
            log.info("Cached " + insertValue);

        }
        int maxSize = (TesterUtil.instance.getExpectedTesters() / 2);
        log.info("MAX SIZE " + maxSize);
        int newSize = 0;

        while (this.getCollection().size() < maxSize) {
            log.info("Cached size " + this.getCollection().size());
            Thread.sleep(1000);
            newSize++;
            if (newSize == 15) {
                maxSize = this.getCollection().size();
            }
        }

    }

    @TestStep(order = 5, timeout = 10000000, range = "*")
    public void testRetrieve() throws Exception {
        Thread.sleep(sleep);

        if (this.getName() % 10 != 9) {
            String[] immediateSuccessor = chordPrint.printSuccessorList().split("\n");
            String successor = null;
            for (int i = 0; i < immediateSuccessor.length; i++) {
                if (i == 1) {
                    successor = immediateSuccessor[i].toString();
                }
            }

            log.info("Immediate successor " + successor);

            List<String> listQuitPeers = new ArrayList<String>();
            String quitPeer;
            for (int i = 0; i < TesterUtil.instance.getExpectedTesters(); i++) {
                if (i % 10 == 9) {
                    if (this.get(i) != null) {
                        quitPeer = this.get(i).toString().trim();
                        listQuitPeers.add(quitPeer);
                        log.info("Quit peer " + quitPeer);
                    }
                }
            }
            String[] succ = chordPrint.printSuccessorList().split("\n");
            for (String succList : succ) {
                log.info("Successor List " + succList + " size " + succ.length);
            }

            if (listQuitPeers.contains(successor.trim())) {
                log.info("Contains in GV " + successor);
                assertTrue("Successor wasn't updated correctly ", false);
            } else {
                log.info("Not Contains in GV " + successor);
            }

        }
    }

    @AfterClass(timeout = 100000, range = "*")
    public void end() throws Exception {
        if (this.getName() % 10 != 9) {
            Thread.sleep(sleep);
            chord.leave();
            log.info("Peer bye bye");
        }
    }
}

