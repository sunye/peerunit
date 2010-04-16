package fr.inria.peerunit.openchordtest.test;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import fr.inria.peerunit.openchordtest.StringKey;
import de.uniba.wiai.lspi.chord.service.Key;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.tester.Assert;
import fr.inria.peerunit.util.TesterUtil;

/**
 * Test E3 on experiments list
 * @author almeida
 *
 */
public class TestQueryTheoremB extends AbstractOpenChordTest {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private StringKey key = null;
    private String data = "";
    private static Logger log = Logger.getLogger(TestQueryTheoremB.class.getName());
    private Collection<Key> insertedKeys = new ArrayList<Key>(OBJECTS);

    public TestQueryTheoremB() {
        super();
        callback.setCallback(OBJECTS, log);
        // TODO Auto-generated constructor stub
    }

    @TestStep(order = 2, timeout = 10000000, range = "*")
    public void find() throws InterruptedException {

        chordPrint = (ChordImpl) chord;

        Thread.sleep(sleep);
        log.info("[TestDbpartout] My ID is " + chord.getID());
        String[] succ = chordPrint.printSuccessorList().split("\n");
        for (String succList : succ) {
            log.info("[TestDbpartout] Successor List " + succList + " size " + succ.length);
        }
        String[] finger = chordPrint.printFingerTable().split("\n");
        for (String fingList : finger) {
            log.info("[TestDbpartout] FingerTable " + fingList);
        }

        String[] ref = chordPrint.printReferences().split("\n");
        for (String refList : ref) {
            log.info("[TestDbpartout] ReferenceTable " + refList);
        }

    }

    @TestStep(order = 3, timeout = 10000000, range = "0-7")
    public void testInsert() throws RemoteException, InterruptedException {

        Thread.sleep(sleep);

        data = "" + this.getName();
        log.info("[TestDbpartout] Inserting data " + data);
        key = new StringKey(data);
        chord.insert(key, data, callback);
        insertedKeys.add(key);

        log.info("[TestDbpartout] Will cache ");
        log.info("[TestDbpartout] Caching data " + data);

        this.put(this.getName(), data);
    }

    @TestStep(order = 4, timeout = 10000000, range = "*")
    public void testRetrieve() throws RemoteException, InterruptedException {
        List<String> expecteds = new ArrayList<String>(OBJECTS);
        String data;

        Thread.sleep(sleep);

        for (int i = 0; i < OBJECTS; i++) {
            data = (String) this.get((Integer) i);
            if (data != null) {
                expecteds.add(data);
                log.info("[Local verdict] Populating expected " + data);
            }
        }
        log.info("[Local verdict] Expected has " + expecteds.size());
        int timeToFind = 0;
        while (timeToFind < TesterUtil.instance.getLoopToFail()) {
            for (int i = 0; i < OBJECTS; i++) {
                data = "" + i;
                key = new StringKey(data);
                chord.retrieve(key, callback);
            }
            callback.retr++;
            String[] succ = chordPrint.printSuccessorList().split("\n");
            for (String succList : succ) {
                log.info("[TestDbpartout] Successor List " + succList + " size " + succ.length);
            }
            String[] finger = chordPrint.printFingerTable().split("\n");
            for (String fingList : finger) {
                log.info("[TestDbpartout] FingerTable " + fingList);
            }

            String[] ref = chordPrint.printReferences().split("\n");
            for (String refList : ref) {
                log.info("[TestDbpartout] ReferenceTable " + refList);
            }

            log.info("[TestDbpartout] EntriesTable ");
            String[] ent = chordPrint.printEntries().split("\n");
            for (String entList : ent) {
                log.info("[TestDbpartout] EntriesTable " + entList);
            }


            Thread.sleep(sleep);


            log.info("[Local verdict] Verifying Expected ");

            /**
             * Here v represents the first insert
             */
            for (String expected : expecteds) {
                log.info("[Local verdict] Expected " + expected);
            }
            for (String actual : callback.getResultSet()) {
                log.info("[Local verdict] Actual " + actual);
            }

            timeToFind++;
            log.info("New Retrieval " + timeToFind + " will start " + expecteds.size() + " " + callback.getSizeExpected());
        }
        log.info("Retrieval " + timeToFind + " found " + expecteds.size() + " of " + callback.getSizeExpected());

        Assert.assertListEquals("[Local verdict] Arrays ", expecteds, callback.getResultSet());

        log.info("[TestDbpartout] Inserted data size " + insertedKeys.size());
    }

    @AfterClass(timeout = 100000, range = "*")
    public void end() throws InterruptedException, ServiceException {

        Thread.sleep(sleep);
        chord.leave();

        log.info("[TestDbpartout] Peer bye bye");
    }
}
