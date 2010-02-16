package fr.inria.peerunit.openchordtest.test;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import fr.inria.peerunit.openchordtest.StringKey;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.test.assertion.Assert;
import fr.inria.peerunit.util.TesterUtil;

/**
 * Test E3 on experiments list
 * @author almeida
 *
 */
public class TestInsertMultiple extends AbstractOpenChordTest {

    private static Logger log = Logger.getLogger(TestInsertMultiple.class.getName());
    private static final long serialVersionUID = 1L;
    private StringKey key = null;
    private String data = "";

    public TestInsertMultiple() {
        super();
        callback.setCallback(OBJECTS, log);
        // TODO Auto-generated constructor stub
    }

    @TestStep(order = 2, timeout = 100000, range = "*")
    public void find() throws InterruptedException {

        chordPrint = (ChordImpl) chord;

        Thread.sleep(sleep);
        log.info("My ID is " + chord.getID());
        String[] succ = chordPrint.printSuccessorList().split("\n");
        for (String succList : succ) {
            log.info("Successor List " + succList + " size " + succ.length);
        }


    }

    @TestStep(order = 3, timeout = 100000, range = "0-9")
    public void testInsert() throws InterruptedException, RemoteException {

        Thread.sleep(sleep);
        data = "" + this.getPeerName();


        log.info("[TestDbpartout] Inserting data " + data);
        key = new StringKey(data);
        chord.insert(key, data, callback);

        log.info("[TestDbpartout] Will cache ");
        log.info("[TestDbpartout] Caching data " + data);


        this.put(this.getPeerName(), data);

    }

    @TestStep(range = "*", timeout = 10000, order = 4)
    public void testRetrieve() throws RemoteException, InterruptedException {
        List<String> actuals = new ArrayList<String>();


        int timeToFind = 0;
        while (timeToFind < TesterUtil.instance.getLoopToFail()) {
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
            timeToFind++;
        }

        // Loading expecteds
        List<String> expecteds = new ArrayList<String>();
        Set<Integer> keySet = this.getCollection().keySet();
        Object data;
        for (Integer globalKey : keySet) {
            data = this.get(globalKey);
            //if (data instanceof String) {
            log.info("Expected " + (String) data);
            expecteds.add((String) data);
            //}
        }

        log.info("[Local verdict] Waiting a Verdict. Found " + actuals.size() + " of " + expecteds.size());
        Assert.assertListEquals("[Local verdict] Arrays ", expecteds, actuals);

    }

    @AfterClass(timeout = 100000, range = "*")
    public void end() {
        log.info(" Peer bye bye");
    }
}
