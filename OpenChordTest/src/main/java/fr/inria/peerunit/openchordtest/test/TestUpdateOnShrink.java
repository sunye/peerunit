package fr.inria.peerunit.openchordtest.test;

import static fr.inria.peerunit.tester.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.util.TesterUtil;

public class TestUpdateOnShrink extends AbstractOpenChordTest {

    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(TestUpdateOnShrink.class.getName());

    public TestUpdateOnShrink() {
        super();
        callback.setCallback(OBJECTS, log);
        // TODO Auto-generated constructor stub
    }

    @TestStep(order = 2, timeout = 100000, range = "*")
    public void find() throws InterruptedException {

        chordPrint = (ChordImpl) getChord();

        Thread.sleep(sleep);
        log.info("My ID is " + getChord().getID());
        String[] succ = chordPrint.printSuccessorList().split("\n");
        for (String succList : succ) {
            log.info("Successor List " + succList + " size " + succ.length);
        }


    }

    @TestStep(order = 4, timeout = 100000, range = "*")
    public void testLeave() throws RemoteException, ServiceException, InterruptedException {


        if (this.getName() % 2 == 0) {
            log.info("Leaving early");
            getChord().leave();
            String insertValue = getChord().getID().toString().substring(0, 2) + " " + localURL.toString();
            this.put(this.getName(), insertValue);
            log.info("Cached " + insertValue);
        }

        // little time to cache the variables
        Thread.sleep(sleep);

    }

    @TestStep(order = 5, timeout = 100000, range = "*")
    public void testRetrieve() throws RemoteException {

        if (this.getName() % 2 != 0) {
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
                if (i % 2 == 0) {
                    if (this.get(i) != null) {
                        quitPeer = this.get(i).toString().trim();
                        listQuitPeers.add(quitPeer);
                        log.info("Quit peer " + quitPeer);
                    }
                }
            }
            int timeToClean = 0;
            boolean tableUpdated = false;
            while (timeToClean < TesterUtil.instance.getLoopToFail()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!listQuitPeers.contains(successor.trim())) {
                    tableUpdated = true;
                    break;
                }
                timeToClean++;
            }

            if (tableUpdated) {
                log.info("Contains in GV " + successor);
                assertTrue("Successor updated correctly ", true);
            } else {
                log.info("Not Contains in GV " + successor);
                assertTrue("Successor didn't updated correctly ", false);
            }
        }
    }

    @AfterClass(timeout = 100000, range = "*")
    public void end() throws InterruptedException, ServiceException {
        if (this.getName() % 2 != 0) {

            Thread.sleep(sleep);
            getChord().leave();


            log.info("Peer bye bye");
        }
    }
}

