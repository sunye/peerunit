package fr.univnantes.alma.nio;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import fr.univnantes.alma.nio.client.NioByteArrayWriter;
import fr.univnantes.alma.nio.server.ANioServer;
import fr.univnantes.alma.nio.server.ByteArrayDecoder;
import fr.univnantes.alma.nio.server.ByteArrayHandler;

/**
 * Check if the server handles an incoming connection
 * 
 * @author E06A193P
 */
public class ClientAndServerConnection {

    protected Writer client;
    protected Server server;
    protected ByteArrayHandler handler;
    protected final static int listeningPort = 3128;
    protected List<Object> receivedObjects;
    protected int nb_received = 0;

    @BeforeMethod
    public void setUp() throws IOException {

	nb_received = 0;
	receivedObjects = new ArrayList<Object>();

	server = new ANioServer() {

	    @Override
	    protected ByteArrayHandler createHandler(SocketChannel socket) {
		return handler;
	    }
	};

	server.start();
	server.openPort(listeningPort);

	handler = new ByteArrayDecoder(null, Executors
		.newSingleThreadExecutor()) {

	    @Override
	    public void handleObject(Serializable o, SocketChannel sc) {
		receivedObjects.add(o);
		nb_received++;
	    }
	};
	client = new NioByteArrayWriter("localhost", listeningPort);

    }

    @AfterMethod
    public void tearDown() {
	server.stop();
    }

    @Test(dataProvider = "elementsToSend")
    public void testTransmition(Collection<Serializable> toSend) {

	nb_received = 0;

	for (Serializable s : toSend) {
	    client.send(s);
	}
	client.close();

	while (nb_received < toSend.size()) {
	    Thread.yield();
	}
	assertTrue(receivedObjects.containsAll(toSend));
	assertEquals(nb_received, toSend.size());
    }

    @DataProvider(name = "elementsToSend")
    public Object[][] elementsToSend() {
	// for each call, each argument, is an array of Serializable
	return new Object[][] { { Arrays.asList(new Integer(42)) },
		{ Arrays.asList("je", "tu", "il") },
		// check that sending three times the same object
		// is not considered as once by object
		{ Arrays.asList(singleton, singleton, singleton) } };
    }

    protected final String singleton = "single";

}
