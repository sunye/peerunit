/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.openchordtest.test;

import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.AsynChord;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import fr.inria.peerunit.remote.GlobalVariables;
import fr.inria.peerunit.openchordtest.DbCallback;
import fr.inria.peerunit.openchordtest.util.FreeLocalPort;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.SetGlobals;
import fr.inria.peerunit.parser.SetId;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.util.TesterUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author sunye
 */
public class AbstractOpenChordTest {

    private static Logger log = Logger.getLogger(AbstractOpenChordTest.class.getName());
    private int id;
    private GlobalVariables globals;
    protected TesterUtil defaults;
    protected int size;
    protected int sleep;
    protected AsynChord chord = null;
    protected URL localURL = null;
    protected DbCallback callback = new DbCallback();
    protected ChordImpl chordPrint = null;
    protected int OBJECTS;


    @SetId
    public void setId(int i) {
        id = i;
    }

    public int getId() {
        return id;
    }

    @SetGlobals
    public void setGlobals(GlobalVariables gv) {
        globals = gv;
    }

    public GlobalVariables getGlobals() {
        return globals;
    }

    protected void put(int key, Object value) throws RemoteException {
        globals.put(key, value);
    }

    protected Object get(int key) throws RemoteException {
        return globals.get(key);
    }

    protected int getPeerName() {
        return id;

    }

    protected int getName() {
        return id;
    }

    protected Map<Integer,Object> getCollection() throws RemoteException {
        return globals.getCollection();
    }

    protected void kill() {
        
    }

    protected void clear() {
        
    }

    @BeforeClass(range = "*", timeout = 1000000)
    public void bc() throws FileNotFoundException {
        if (new File("peerunit.properties").exists()) {
            String filename = "peerunit.properties";
            FileInputStream fs = new FileInputStream(filename);
            defaults = new TesterUtil(fs);
        } else {
            defaults = TesterUtil.instance;
        }
        size = defaults.getObjects();
        sleep = defaults.getSleep();
        OBJECTS =defaults.getObjects();
        log.info("Starting test DHT ");
    }

    @TestStep(order = 1, range = "*", timeout = 10000)
    public void init() throws Exception {
        log.info("Sleeping: " + sleep);
        Thread.sleep(sleep);
        log.info("wake up !");
        PropertiesLoader.loadPropertyFile();
        log.info("property filr loaded");
        String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
        log.info("Peer name " + this.getId());
        String address = InetAddress.getLocalHost().toString();
        address = address.substring(address.indexOf("/") + 1, address.length());
        FreeLocalPort port = new FreeLocalPort();
        log.info("Address: " + address + " on port " + port.getPort());
        localURL = new URL(protocol + "://" + address + ":" + port.getPort() + "/");
        URL bootstrapURL = new URL(protocol + "://" + defaults.getBootstrap() + ":" + defaults.getBootstrapPort() + "/");
        chord = new ChordImpl();
        Thread.sleep(100 * this.getId());
        log.info("LocalURL: " + localURL.toString());
        chord.join(localURL, bootstrapURL);
        log.info("Joining Chord DHT: " + chord.toString());
        log.info("Peer init");
    }


}
