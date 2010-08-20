package fr.inria.peerunit.openchordtest.test;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import fr.inria.peerunit.openchordtest.StringKey;
import de.uniba.wiai.lspi.chord.service.Key;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.tester.Assert;

/**
 * Test E5B on experiments list
 * @author almeida
 *
 */
public class TestInsertLeaveB extends AbstractOpenChordTest {

    private static Logger log = Logger.getLogger(TestInsertLeaveB.class.getName());
    static TestInsertLeaveB test;
    boolean iAmBootsrapper = false;
    private static final long serialVersionUID = 1L;
    Key key;
    String data = "";
    int actualResults = 0;
    int expectedResults = 0;
    Map<Integer, Object> objList = new HashMap<Integer, Object>();

    public TestInsertLeaveB() {
        super();
        callback.setCallback(OBJECTS, log);
    }

    @TestStep(order = 2, range = "0", timeout = 10000)
    public void chosingPeer() throws RemoteException {
        Random rand = new Random();
        List<Integer> generated = new ArrayList<Integer>();
        int chosePeer;
        int netSize = (defaults.getExpectedTesters() * defaults.getChurnPercentage()) / 100;
        log.info("It will join " + netSize + " peers");
        boolean peerChose;
        while (netSize > 0) {
            peerChose = false;
            while (!peerChose) {
                chosePeer = rand.nextInt(defaults.getExpectedTesters());
                if (chosePeer != 0) {
                    Integer genInt = new Integer(chosePeer);
                    if (!generated.contains(genInt)) {
                        generated.add(genInt);
                        peerChose = true;
                        log.info("Chose peer " + genInt);
                    }
                }
            }
            netSize--;
        }
        for (Integer intObj : generated) {
            test.put(intObj.intValue() * 100, intObj);
        }
    }

    @TestStep(order = 3, range = "*", timeout = 10000)
    public void stabilize() throws InterruptedException {
        int timeToFind = 0;
        while (timeToFind < defaults.getLoopToFail()) {

            Thread.sleep(sleep);
            timeToFind++;
        }

    }

    @TestStep(order = 4, range = "0", timeout = 10000)
    public void testInsert() throws Exception {

            Thread.sleep(sleep);

        Map<Key, String> map = new HashMap<Key, String>();
        for (int i = 0; i < OBJECTS; i++) {
            data = "" + i;
            log.info("[TestDbpartout] Inserting data " + data);
            key = new StringKey(data);
            chord.insert(key, data, callback);
            map.put(key, data);
        }

        while (!callback.isInserted()) {

                Thread.sleep(sleep);

        }
        List<String> expecteds = new ArrayList<String>();
        for (Key expectedKey : callback.getInsertedKeys()) {
            expecteds.add(map.get(expectedKey));
        }
        test.put(0, expecteds);
    }

    @TestStep(order = 5, range = "*", timeout = 10000)
    public void testRetrieve() throws InterruptedException {

            Thread.sleep(sleep);

            int timeToFind = 0;
            while (timeToFind < defaults.getLoopToFail()) {
                for (int i = 0; i < OBJECTS; i++) {
                    data = "" + i;
                    key = new StringKey(data);
                    chord.retrieve(key, callback);
                }
                callback.retr++;
                Thread.sleep(sleep);
                for (String actual : callback.getResultSet()) {
                    log.info("Retrieve before leave " + timeToFind + " got " + actual);
                }
                callback.clearResultSet();
                timeToFind++;
            }

    }

    @TestStep(order = 6, timeout = 100000, range = "*")
    public void printSuccList() throws InterruptedException {

            Thread.sleep(sleep);
            String[] succ;
            //storing my table
            String successor = null;
            chordPrint = (ChordImpl) chord;
            succ = chordPrint.printSuccessorList().split("\n");
            for (int i = 0; i < succ.length; i++) {
                if (i > 0) {
                    successor = succ[i].toString().trim();
                    log.info("Successor List before " + successor);
                }
            }
            String[] entr = chordPrint.printEntries().split("\n");
            String entry = null;
            for (int i = 0; i < entr.length; i++) {
                if (i > 0) {
                    entry = entr[i].toString().trim();
                    log.info("Entries before " + entry);
                }
            }
        
    }

    @TestStep(order = 7, range = "*", timeout = 100000)
    public void leaving() throws InterruptedException, ServiceException {

            Thread.sleep(sleep);
            if (chosenOne(test.getPeerName())) {
                log.info("Leaving early ");
                chord.leave();
            }
            String[] succ;
            //storing my table
            String successor = null;
            Thread.sleep(sleep);
            chordPrint = (ChordImpl) chord;
            succ = chordPrint.printSuccessorList().split("\n");
            for (int i = 0; i < succ.length; i++) {
                if (i > 0) {
                    successor = succ[i].toString().trim();
                    log.info("Successor List after " + successor);
                }
            }
    }

    @TestStep(order = 8, range = "*", timeout = 10000)
    public void testFinalRetrieve() throws InterruptedException, RemoteException {

            if (!chosenOne(test.getPeerName())) {
                Thread.sleep(sleep);
                List<String> actuals = new ArrayList<String>();
                int timeToFind = 0;
                while (timeToFind < defaults.getLoopToFail()) {
                    for (int i = 0; i < OBJECTS; i++) {
                        data = "" + i;
                        key = new StringKey(data);
                        chord.retrieve(key, callback);
                    }
                    callback.retr++;
                    Thread.sleep(sleep);
                    for (String actual : callback.getResultSet()) {
                        log.info("Final retrieve " + timeToFind + " got " + actual);
                        if (!actuals.contains(actual.toString())) {
                            actuals.add(actual);
                        } else {
                            log.info("Already have " + actual);
                        }
                    }
                    callback.clearResultSet();
                    timeToFind++;
                }

                String[] entr = chordPrint.printEntries().split("\n");
                String entry = null;
                for (int i = 0; i < entr.length; i++) {
                    if (i > 0) {
                        entry = entr[i].toString().trim();
                        log.info("Entries after " + entry);
                    }
                }

                List<String> expecteds = (List<String>) test.get(0);
                log.info("[Local verdict] Waiting a Verdict. Found " + actuals.size() + " of " + expecteds.size());
                Assert.assertListEquals("[Local verdict] Arrays ", expecteds, actuals);
            }
        
    }

    @AfterClass(timeout = 100000, range = "*")
    public void end() {
        log.info(" Peer bye bye");
    }

    private boolean chosenOne(int name) {
        try {
            if (objList.isEmpty()) {
                objList = test.getCollection();
            }
            Set<Integer> keySet = objList.keySet();
            Object nameChose;
            for (Integer key : keySet) {
                nameChose = objList.get(key);
                if ((nameChose instanceof Integer) && (key.intValue() >= 100)) {
                    Integer new_name = (Integer) nameChose;
                    if (new_name.intValue() == name) {
                        return true;
                    }
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }
}
