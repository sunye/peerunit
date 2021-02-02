package fr.inria.peerunit.openchordtest.test;

import java.net.InetAddress;
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
import fr.inria.peerunit.openchordtest.util.FreeLocalPort;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.Key;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.tester.Assert;
import fr.inria.peerunit.util.TesterUtil;

/**
 * Test E4 on experiments list
 * @author almeida
 *
 */
public class TestInsertJoinB extends AbstractOpenChordTest {

    private static Logger log = Logger.getLogger(TestInsertJoinB.class.getName());
    private static final int OBJECTS = TesterUtil.instance.getObjects();
   
    boolean iAmBootsrapper = false;
    private static final long serialVersionUID = 1L;
    StringKey key = null;
    String data = "";
    
    int actualResults = 0;
    int expectedResults = 0;
    private Collection<Key> insertedKeys = new ArrayList<Key>(OBJECTS);
    
    Map<Integer, Object> objList = new HashMap<Integer, Object>();

    public TestInsertJoinB() {
        super();
        callback.setCallback(OBJECTS, log);
    }



    @TestStep(order = 2, range = "0", timeout = 10000)
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
            this.put(intObj.intValue() * 10, intObj);
        }
    }

    @TestStep(order = 3, timeout = 10000, range = "*")
    public void initInitHalf() throws Exception {

        if (!chosenOne(this.getPeerName()) && (this.getPeerName() != 0)) {

            Thread.sleep(sleep);
            log.info("Peer name " + this.getPeerName());


            de.uniba.wiai.lspi.chord.service.PropertiesLoader.loadPropertyFile();
            String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);


            String address = InetAddress.getLocalHost().toString();
            address = address.substring(address.indexOf("/") + 1, address.length());
            FreeLocalPort port = new FreeLocalPort();
            log.info("Address: " + address + " on port " + port.getPort());
            localURL = new URL(protocol + "://" + address + ":" + port.getPort() + "/");

            URL bootstrapURL = null;

            bootstrapURL = new URL(protocol + "://" + TesterUtil.instance.getBootstrap() + ":" + TesterUtil.instance.getBootstrapPort() + "/");


            chord = new de.uniba.wiai.lspi.chord.service.impl.ChordImpl();

            Thread.sleep(100 * this.getPeerName());
            log.info("LocalURL: " + localURL.toString());
            getChord().join(localURL, bootstrapURL);

            log.info("Joining Chord DHT: " + getChord().toString());



            log.info("Peer init");
        }

    }

    @TestStep(order = 4, range = "0", timeout = 1000000)
    public void testInsert() throws Exception {

        Thread.sleep(sleep);


        for (int i = 1; i < OBJECTS; i++) {
            data = "" + i;
            log.info("[TestDbpartout] Inserting data " + data);
            key = new StringKey(data);
            getChord().insert(key, data, callback);
            insertedKeys.add(key);
        }
    }

    @TestStep(order = 5, range = "*", timeout = 10000)
    public void testRetrieve() throws Exception {
        List<String> resultSet = new ArrayList<String>();

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
                log.info("[Local verdict] Expected " + actual);
                resultSet.add(actual);
            }
            if (this.getPeerName() == 0) {
                this.put(0, resultSet);
            }
        }

    }

    @TestStep(order = 6, timeout = 10000, range = "*")
    public void initOtherHalf() throws Exception {

        if (chosenOne(this.getPeerName())) {

            Thread.sleep(sleep);
            log.info("Peer name " + this.getPeerName());


            de.uniba.wiai.lspi.chord.service.PropertiesLoader.loadPropertyFile();
            String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);


            String address = InetAddress.getLocalHost().toString();
            address = address.substring(address.indexOf("/") + 1, address.length());
            FreeLocalPort port = new FreeLocalPort();
            log.info("Address: " + address + " on port " + port.getPort());
            localURL = new URL(protocol + "://" + address + ":" + port.getPort() + "/");

            URL bootstrapURL = null;

            bootstrapURL = new URL(protocol + "://" + TesterUtil.instance.getBootstrap() + ":" + TesterUtil.instance.getBootstrapPort() + "/");


            chord = new de.uniba.wiai.lspi.chord.service.impl.ChordImpl();

            Thread.sleep(100 * this.getPeerName());
            log.info("LocalURL: " + localURL.toString());
            getChord().join(localURL, bootstrapURL);

            log.info("Joining Chord DHT: " + getChord().toString());



            log.info("Peer init");
        }

    }

    @TestStep(order = 7, range = "*", timeout = 10000)
    public void testRetrieveByOthers() throws Exception {
        List<String> actuals = new ArrayList<String>();

        Thread.sleep(sleep);
        if (chosenOne(this.getPeerName())) {
            for (int i = 0; i < OBJECTS; i++) {
                data = "" + i;
                key = new StringKey(data);
                getChord().retrieve(key, callback);
            }
            callback.retr++;
            Thread.sleep(sleep);
            for (String actual : callback.getResultSet()) {
                log.info("[Local verdict] Actual " + actual);
                actuals.add(actual);
            }
            List<String> expecteds = (List<String>) this.get(0);
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
