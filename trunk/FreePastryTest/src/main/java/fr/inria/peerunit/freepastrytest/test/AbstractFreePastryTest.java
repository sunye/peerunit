/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.freepastrytest.test;

import static fr.inria.peerunit.test.assertion.Assert.inconclusive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.logging.Logger;

import fr.inria.peerunit.GlobalVariables;
import fr.inria.peerunit.freepastrytest.Network;
import fr.inria.peerunit.freepastrytest.Peer;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.SetGlobals;
import fr.inria.peerunit.parser.SetId;
import fr.inria.peerunit.util.TesterUtil;

/**
 * 
 * @author sunye
 */
public class AbstractFreePastryTest {

	private static Logger LOG = Logger.getLogger(AbstractFreePastryTest.class
			.getName());
	private int id;
	private static GlobalVariables globals;
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
    }

    /**
     * 
     * FIXME: Implement clear()
     */
    protected void clear() {
    }

	protected void bootstrap() throws UnknownHostException,
			InterruptedException, IOException {
		
		Network net = new Network();
		if (!net.joinNetwork(peer, null, true, LOG)) {
			inconclusive("Can't bootstrap");
		}
		this.put(-1, net.getInetSocketAddress());
		LOG.info(String
				.format("Net created at: %s", net.getInetSocketAddress()));

		while (!peer.isReady()) {
			Thread.sleep(1000);
		}
	}
	
	protected void join() throws InterruptedException, UnknownHostException, IOException {
        LOG.info("Joining network");
        Network net = new Network();
        Thread.sleep(this.getPeerName() * 1000);

        InetSocketAddress bootaddress = (InetSocketAddress) this.get(-1);

        if (!net.joinNetwork(peer, bootaddress, false, LOG)) {
            inconclusive("I couldn't join, sorry");
        }

	}
}
