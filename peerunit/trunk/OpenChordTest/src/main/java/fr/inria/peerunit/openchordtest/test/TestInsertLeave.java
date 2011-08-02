package fr.inria.peerunit.openchordtest.test;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import fr.inria.peerunit.openchordtest.StringKey;
import de.uniba.wiai.lspi.chord.service.Key;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.tester.Assert;
import fr.inria.peerunit.util.TesterUtil;

/**
 * Test Insert/Retrieve in an Expanding System
 * @author almeida
 *
 */
public class TestInsertLeave extends AbstractOpenChordTest {

    private static Logger log = Logger.getLogger(TestInsertLeave.class.getName());

    private static final long serialVersionUID = 1L;
    private StringKey key = null;
    private String data = "";
    private Collection<Key> insertedKeys = new ArrayList<Key>(OBJECTS);
    private Map<Integer, Object> objList = new HashMap<Integer, Object>();
    private List<String> expecteds = new ArrayList<String>();

    public TestInsertLeave() {
        super();
        callback.setCallback(OBJECTS, log);
    }

    @TestStep(range = "0", timeout = 10000, order = 2)
    public void chosingPeer() throws RemoteException {
        Random rand = new Random();
        List<Integer> generated = new ArrayList<Integer>();
        int chosePeer;
        int netSize = (TesterUtil.instance.getExpectedTesters() * TesterUtil.instance.getChurnPercentage()) / 100;
        log.info("It will join " + netSize + " peers");
        boolean peerChose;
        while (netSize > 0) {
            peerChose = false;
            while (!peerChose) {
                chosePeer = rand.nextInt(TesterUtil.instance.getExpectedTesters());
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
            this.put(intObj.intValue() * 100, intObj);
        }
    }

    @TestStep(range = "0", timeout = 10000, order = 3)
    public void testInsert() throws InterruptedException {

        Thread.sleep(sleep);


        for (int i = 0; i < OBJECTS; i++) {
            data = "" + i;
            log.info("[TestDbpartout] Inserting data " + data);
            key = new StringKey(data);
            getChord().insert(key, data, callback);
            insertedKeys.add(key);
        }

    }

    @TestStep(range = "*", timeout = 10000, order = 4)
    public void testRetrieve() throws InterruptedException, RemoteException {

        Thread.sleep(sleep);
        if (!chosenOne(this.getPeerName())) {
            for (int i = 0; i < OBJECTS; i++) {
                data = "" + i;
                key = new StringKey(data);
                getChord().retrieve(key, callback);
            }
            callback.retr++;
            Thread.sleep(sleep);
            for (String actual : callback.getResultSet()) {
                log.info("Retrieve before depart " + actual);
            }
        }

    }

    @TestStep(order = 5, timeout = 100000, range = "*")
    public void leaving() throws RemoteException {

        if (chosenOne(this.getPeerName())) {
            log.info("Leaving early ");
            this.kill();
        }

    }

    @TestStep(range = "*", timeout = 10000, order = 6)
    public void testInitialRetrieve() throws InterruptedException, RemoteException {

        if (!chosenOne(this.getPeerName())) {
            List<String> actuals = new ArrayList<String>();
            Thread.sleep(sleep);

            for (int i = 0; i < OBJECTS; i++) {
                data = "" + i;
                key = new StringKey(data);
                getChord().retrieve(key, callback);
            }
            callback.retr++;
            Thread.sleep(sleep);
            for (String actual : callback.getResultSet()) {
                log.info("Retrieve after depart " + actual);
                actuals.add(actual);
                this.put(this.getPeerName(), actuals);
            }
        }

    }

    @TestStep(range = "*", timeout = 10000, order = 7)
    public void buildExpecteds() throws RemoteException {
        Set<Integer> keySet = this.getCollection().keySet();
        List<String> cached = new ArrayList<String>();
        Object obj;
        for (Integer key : keySet) {
            obj = this.get(key);
            if (!(obj instanceof Integer)) {
                cached = (List<String>) obj;
            }

            for (String cachedObj : cached) {
                log.info("Cached " + cached);
                if (!expecteds.contains(cachedObj)) {
                    expecteds.add(cachedObj);
                }
            }
        }
        log.info("I may find " + expecteds.size() + " objects");
        for (String exp : expecteds) {
            log.info("I may find " + exp);
        }

    }

    @TestStep(range = "*", timeout = 10000, order = 8)
    public void testFinalRetrieve() throws InterruptedException, RemoteException {

        if (!chosenOne(this.getPeerName())) {
            Thread.sleep(sleep);
            List<String> actuals = new ArrayList<String>();
            int timeToFind = 0;
            while (timeToFind < TesterUtil.instance.getLoopToFail()) {
                for (int i = 0; i < OBJECTS; i++) {
                    data = "" + i;
                    key = new StringKey(data);
                    getChord().retrieve(key, callback);
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
                timeToFind++;
            }
            //List<String> expecteds=(List<String>)test.get(0);
            log.info("[Local verdict] Waiting a Verdict. Found " + actuals.size() + " of " + expecteds.size());
            Assert.assertListEquals("[Local verdict] Arrays ", expecteds, actuals);
        }

    }

    @AfterClass(timeout = 100000, range = "*")
    public void end() {
        log.info(" Peer bye bye");
    }

    private boolean chosenOne(int name) throws RemoteException {
        if (objList.isEmpty()) {
            objList = this.getCollection();
        }
        Set<Integer> keySet = objList.keySet();
        Object nameChose;
        for (Integer key : keySet) {
            nameChose = objList.get(key);
            if (nameChose instanceof Integer) {
                Integer new_name = (Integer) nameChose;
                if (new_name.intValue() == name) {
                    return true;
                }
            }
        }

        return false;
    }
}
