/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.freepastrytest.test;


import fr.inria.peerunit.parser.AfterClass;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.remote.GlobalVariables;
import fr.inria.peerunit.freepastrytest.PastryPeer;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.SetGlobals;
import fr.inria.peerunit.parser.SetId;
import fr.inria.peerunit.util.TesterUtil;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * 
 * @author sunye
 */
public class AbstractFreePastryTest {

    private static Logger LOG = Logger.getLogger(AbstractFreePastryTest.class.getName());
    private int id;
    private static GlobalVariables globals;
    protected TesterUtil defaults;
    protected int size;
    protected int sleep;
    protected int churnPercentage;
    protected int OBJECTS;

    protected PastryPeer peer;


    private static final int PORT = 1200;
    private static InetAddress HOST;

    public AbstractFreePastryTest() {
        try {
            HOST = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
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
        churnPercentage = defaults.getChurnPercentage();
        OBJECTS = defaults.getObjects();
        LOG.info("Starting test DHT ");
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

    /**
     * 
     * FIXME: Implement kill()
     */
    protected void kill() {
        this.end();
    }

    /**
     * 
     * FIXME: Implement clear()
     */
    protected void clear() {
    }

    /**
     * The peer leaves the system.
     *
     */
    @AfterClass(timeout = 1000, range = "*")
    public void end() {
        peer.leave();
    }

    public void startBootstrap() throws UnknownHostException, IOException,
            InterruptedException {

        InetSocketAddress address =
                new InetSocketAddress(HOST, PORT);

        peer = new PastryPeer(address);
        peer.bootsrap();
        peer.createPast();
        this.put(0, address);

        //Thread.sleep(16000);
    }

    public void startingNetwork() throws InterruptedException,
            UnknownHostException, IOException {

        Thread.sleep(this.getPeerName() * 100);
        InetSocketAddress address = (InetSocketAddress) this.get(0);

        peer = new PastryPeer(address);
        peer.join();
        peer.createPast();
    }

}
