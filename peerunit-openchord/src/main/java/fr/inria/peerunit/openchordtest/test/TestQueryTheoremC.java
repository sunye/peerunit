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

public class TestQueryTheoremC extends AbstractOpenChordTest {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private StringKey key = null;
    private String data = "";
    private static Logger log = Logger.getLogger(TestQueryTheorem.class.getName());
    private Collection<Key> insertedKeys = new ArrayList<Key>(OBJECTS);

    public TestQueryTheoremC() {
        super();
        callback.setCallback(OBJECTS, log);
    }

    @TestStep(order = 2, timeout = 100000, range = "*")
    public void find() throws InterruptedException {

        chordPrint = (ChordImpl) getChord();

        Thread.sleep(sleep);
        log.info("[TestDbpartout] My ID is " + getChord().getID());
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

    @TestStep(order = 3, timeout = 100000, range = "1")
    public void testInsert() throws RemoteException, InterruptedException {
        List<String> resultSet = new ArrayList<String>();

        Thread.sleep(sleep);


        for (int i = 1; i < OBJECTS; i++) {
            data = "" + i;
            log.info("[TestDbpartout] Inserting data " + data);
            key = new StringKey(data);
            getChord().insert(key, data, callback);
            insertedKeys.add(key);
            resultSet.add(data);
        }

        log.info("[TestDbpartout] Will cache ");
        for (String cacheData : resultSet) {
            log.info("[TestDbpartout] Caching data " + cacheData);
        }
        this.put(2, resultSet);
    }

    @TestStep(order = 4, timeout = 100000, range = "*")
    public void testLeave() throws InterruptedException {

        Thread.sleep(sleep);

        log.info("[TestDbpartout] EntriesTable ");
        String[] ent = chordPrint.printEntries().split("\n");
        for (String entList : ent) {
            log.info("[TestDbpartout] EntriesTable " + entList);
        }

        if (this.getName() % 2 == 0) {
            log.info("[TestDbpartout] Leaving early");
            try {
                getChord().leave();
            } catch (ServiceException e) {
                e.printStackTrace();
            }
        }
    }

    @TestStep(order = 5, timeout = 100000, range = "*")
    public void testRetrieve() throws RemoteException, InterruptedException {
        if (this.getName() % 2 != 0) {
            List<String> expecteds = null;
            while (expecteds == null) {
                expecteds = (List<String>) this.get(2);
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            for (int retrieveQty = 0; retrieveQty < 2; retrieveQty++) {
                for (int i = 0; i < OBJECTS; i++) {
                    data = "" + i;
                    key = new StringKey(data);
                    getChord().retrieve(key, callback);
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
                log.info("[Local verdict] Retrieval " + callback.retr + " " + expecteds.size() + " " + callback.getSizeExpected());

                Assert.assertListEquals("[Local verdict] Arrays ", expecteds, callback.getResultSet());

                callback.clearResultSet();
                log.info("[Local verdict] New Retrieval will start " + expecteds.size() + " " + callback.getSizeExpected());
            }
            log.info("[TestDbpartout] Inserted data size " + insertedKeys.size());
        }
    }

    @AfterClass(timeout = 100000, range = "*")
    public void end() throws InterruptedException, ServiceException {
        if (this.getName() % 2 != 0) {

            Thread.sleep(sleep);
            getChord().leave();
        }

        log.info("[TestDbpartout] Peer bye bye");

    }
}
