package fr.inria.peerunit.openchordtest.test;

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import fr.inria.peerunit.openchordtest.DbCallback;
import fr.inria.peerunit.openchordtest.StringKey;
import fr.inria.peerunit.openchordtest.util.FreeLocalPort;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.AsynChord;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.tester.Assert;
import fr.inria.peerunit.util.TesterUtil;
import java.io.File;
import java.io.FileInputStream;

/**
 * Test E3 on experiments list
 * @author almeida
 *
 */
public class TestInsert extends AbstractOpenChordTest {

    private static Logger log = Logger.getLogger(TestInsert.class.getName());
    //private static final long serialVersionUID = 1L;
    private StringKey key = null;
    private String data = "joao";
    
    private static ChordImpl chordPrint = null;
    private static DbCallback callback = new DbCallback();
    

    public TestInsert() {
        callback.setCallback(size, log);
    }

    @TestStep(order = 2, timeout = 10000000, range = "*")
    public void find() throws InterruptedException {

        chordPrint = (ChordImpl) chord;

        Thread.sleep(sleep);
        log.info("My ID is " + chord.getID());
        String[] succ = chordPrint.printSuccessorList().split("\n");
        for (String succList : succ) {
            log.info("Successor List " + succList + " size " + succ.length);
        }
    }

    @TestStep(order = 3, range = "0", timeout = 10000)
    public void testInsert() throws InterruptedException, RemoteException {

        Thread.sleep(sleep);

        List<String> resultSet = new ArrayList<String>();
        for (int i = 1; i < size; i++) {
            data = "" + i;
            log.info("[TestDbpartout] Inserting data " + data);
            key = new StringKey(data);
            chord.insert(key, data, callback);
            resultSet.add(data);
        }

        
        while (!callback.isInserted()) {
            Thread.sleep(sleep);
        }

        this.put(0, resultSet);
    }

    @TestStep(order = 4, range = "*", timeout = 10000)
    public void testRetrieve() throws InterruptedException, RemoteException {
        List<String> actuals = new ArrayList<String>();


        int timeToFind = 0;
        while (timeToFind < defaults.getLoopToFail()) {
            for (int i = 0; i < size; i++) {
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
        List<String> expecteds = (List<String>) this.get(0);
        log.info("[Local verdict] Waiting a Verdict. Found " + actuals.size() + " of " + expecteds.size());
        Assert.assertListEquals("[Local verdict] Arrays ", expecteds, actuals);
    }

    @AfterClass(range = "*")
    public void end() {
        log.info(" Peer bye bye");
    }

}
