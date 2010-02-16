/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.freepastrytest.test;

import fr.inria.peerunit.GlobalVariables;
import fr.inria.peerunit.freepastrytest.Peer;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.SetGlobals;
import fr.inria.peerunit.parser.SetId;
import fr.inria.peerunit.util.TesterUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author sunye
 */
public class AbstractFreePastryTest {

    private static Logger log = Logger.getLogger(AbstractFreePastryTest.class.getName());

    private int id;
    private GlobalVariables globals;
    protected TesterUtil defaults;
    protected int size;
    protected int sleep;
    protected int churnPercentage;
    protected int OBJECTS;

    protected Peer peer = new Peer();

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
        log.info("Starting test DHT ");
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

    protected Map<Integer,Object> getCollection() throws RemoteException {
        return globals.getCollection();
    }

    protected void kill() {
        
    }

    protected void clear() {
        
    }

}
