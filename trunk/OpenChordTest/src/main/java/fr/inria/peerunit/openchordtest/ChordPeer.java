/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.openchordtest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import fr.inria.peerunit.dhtmodel.FreeLocalPort;
import fr.inria.peerunit.dhtmodel.Peer;

/**
 * 
 * @author sunye
 */
public class ChordPeer implements Peer {

    private static final Logger LOG = Logger.getLogger(ChordPeer.class.getName());
    private ChordImpl node;
    private URL bootstrapURL;
    private URL localURL;
    private final String protocol;

    public ChordPeer(InetSocketAddress address) throws IOException {
        PropertiesLoader.loadPropertyFile();
        protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
        bootstrapURL = new URL(protocol + "://" + address.getHostName() + ":" + address.getPort() + "/");
        node = new ChordImpl();

        LOG.info("property file loaded");
    }

    public boolean bootsrap() throws Exception {
        node = new ChordImpl();
        node.create(bootstrapURL);
        return true;
    }

    public String get(String key) throws InterruptedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void put(String key, String value) throws InterruptedException {
        StringKey chordKey = new StringKey(key);
        node.insert(chordKey, value, null);
    }

    public String getId() {
        String result = node.getID().toHexString(4).trim();
        LOG.info("Id size = " + result.length());

        return result;
    }

    public int getPort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Set<String> getRoutingTable() {
        Set<String> result = new HashSet<String>();

        String[] successors = node.printSuccessorList().split("\n");

        for(int i = 1 ; i < successors.length ; i++) {
            String entry = successors[i].split(",")[0].trim();
            result.add(entry);
            LOG.fine(entry);
        }

        return result;
    }

    public boolean join() throws Exception {
        FreeLocalPort port = new FreeLocalPort();
        int freePort = port.getPort();
        String address = InetAddress.getLocalHost().toString();
        address = address.substring(address.indexOf("/") + 1, address.length());

        localURL = new URL(protocol + "://" + address + ":" + freePort + "/");

        node.join(localURL, bootstrapURL);
        return true;
    }

    public void leave() {
        node.leave();
    }

    public void print() {
        LOG.log(Level.INFO, "My ID is {0}", this.getId());

        for (String each : this.getRoutingTable()) {
            LOG.info("Successor entry: " + each + each.length());
        }

        for (String each : node.printFingerTable().split("\n")) {
            LOG.info("Finger table entry:"+ each);
        }


        LOG.info("FINGER TABLE:");
        LOG.info("===========================================");
        LOG.info(node.printFingerTable());
        LOG.info("===========================================");

        LOG.info("===========================================");
        LOG.info("SUCCESSOR TABLE:");
        LOG.info(node.printSuccessorList());
        LOG.info("===========================================");
    }
}
