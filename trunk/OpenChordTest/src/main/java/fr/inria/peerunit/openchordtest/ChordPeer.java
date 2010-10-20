/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.openchordtest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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
	private static final Logger LOG = Logger.getLogger(ChordPeer.class
			.getName());
	private ChordImpl node;
	private URL bootstrapURL;
	private URL localURL;

	public ChordPeer(InetSocketAddress address) throws IOException {
		PropertiesLoader.loadPropertyFile();
		String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
		bootstrapURL = new URL(protocol + "://" + address.getHostName() + ":" + address.getPort() + "/");
		node = new ChordImpl();
		
		LOG.info("property file loaded");
	}

	public boolean bootsrap() throws InterruptedException, IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String get(String key) throws InterruptedException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void put(String key, String value) throws InterruptedException {
		StringKey chordKey = new StringKey(key);
		node.insert(chordKey, value, null);
	}

	public String getId() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public int getPort() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Set<String> getRoutingTable() {
		 String[] successors = node.printSuccessorList().split("\n");
		 return new HashSet<String>(Arrays.asList(successors));
	}

	public boolean join() throws Exception {
		FreeLocalPort port = new FreeLocalPort();
		int freePort = port.getPort();
		String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
		String address = InetAddress.getLocalHost().toString();
		address = address.substring(address.indexOf("/") + 1, address.length());

		localURL = new URL(protocol + "://" + address + ":" + freePort + "/");

		node.join(localURL, bootstrapURL);
		return true;
	}

	public void leave() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	// public void join(AbstractOpenChordTest abstractOpenChordTest) throws
	// ServiceException, UnknownHostException, MalformedURLException,
	// InterruptedException {
	//
	//
	// String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
	// LOG.info("Peer name " + abstractOpenChordTest.getId());
	// String address = InetAddress.getLocalHost().toString();
	// address = address.substring(address.indexOf("/") + 1, address.length());
	// FreeLocalPort port = new FreeLocalPort();
	// LOG.info("Address: " + address + " on port " + port.getPort());
	// abstractOpenChordTest.localURL = new URL(protocol + "://" + address + ":"
	// + port.getPort() + "/");
	// URL bootstrapURL = new URL(protocol + "://" +
	// abstractOpenChordTest.defaults.getBootstrap() + ":" +
	// abstractOpenChordTest.defaults.getBootstrapPort() + "/");
	// abstractOpenChordTest.chord = new ChordImpl();
	// Thread.sleep(100 * abstractOpenChordTest.getId());
	// LOG.info("LocalURL: " + abstractOpenChordTest.localURL.toString());
	// abstractOpenChordTest.getChord().join(abstractOpenChordTest.localURL,
	// bootstrapURL);
	// LOG.info("Joining Chord DHT: " +
	// abstractOpenChordTest.getChord().toString());
	// LOG.info("Peer init");
	// }

	public void print() {
		LOG.info("My ID is " + this.getId());
		String[] succ = node.printSuccessorList().split("\n");
		for (String succList : succ) {
			LOG.info("Successor List " + succList + " size " + succ.length);
		}

	}
}
