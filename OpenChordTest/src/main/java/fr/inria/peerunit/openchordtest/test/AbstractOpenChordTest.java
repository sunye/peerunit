/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.openchordtest.test;

import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.AsynChord;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import fr.inria.peerunit.openchordtest.ChordPeer;
import fr.inria.peerunit.remote.GlobalVariables;
import fr.inria.peerunit.openchordtest.DbCallback;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.SetGlobals;
import fr.inria.peerunit.parser.SetId;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.util.TesterUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author sunye
 */
public class AbstractOpenChordTest {

    private static final Logger log = Logger.getLogger(AbstractOpenChordTest.class.getName());
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
    // new
    protected ChordPeer peer;
    private static final int PORT = 1200;
    private static InetAddress HOST;

    public AbstractOpenChordTest() {
    }

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

    protected Map<Integer, Object> getCollection() throws RemoteException {
        return globals.getCollection();
    }

    protected void kill() {
    }

    protected void clear() {
    }

    @BeforeClass(range = "*", timeout = 1000000)
    public void initialize() throws FileNotFoundException {
        if (new File("peerunit.properties").exists()) {
            String filename = "peerunit.properties";
            FileInputStream fs = new FileInputStream(filename);
            defaults = new TesterUtil(fs);
        } else {
            defaults = TesterUtil.instance;
        }
        size = defaults.getObjects();
        sleep = defaults.getSleep();
        OBJECTS = defaults.getObjects();
        log.info("Starting test DHT ");
    }

    //@TestStep(order = 1, range = "*", timeout = 10000)
    public void init() throws Exception {
        peer.join();
    }


    /**
     * @return the chord
     */
    public AsynChord getChord() {
        return chord;
    }

    public void startBootstrap() throws IOException, InterruptedException {

        InetSocketAddress address =
                new InetSocketAddress(HOST, PORT);

        peer = new ChordPeer(address);
        peer.bootsrap();
        this.put(0, address);

        //Thread.sleep(16000);
    }

        public void startingNetwork() throws Exception {

        Thread.sleep(this.getPeerName() * 100);
        InetSocketAddress address = (InetSocketAddress) this.get(0);

        peer = new ChordPeer(address);
        peer.join();
    }
}
