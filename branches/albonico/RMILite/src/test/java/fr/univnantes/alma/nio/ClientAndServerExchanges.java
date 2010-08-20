package fr.univnantes.alma.nio;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fr.univnantes.alma.nio.client.NioByteArrayClient;
import fr.univnantes.alma.nio.client.NioByteArrayWriter;
import fr.univnantes.alma.nio.server.ByteArrayDecoder;
import fr.univnantes.alma.nio.server.ByteArrayHandler;

/**
 * Test if a couple client/server works well:
 * <ol>
 * <li>start the server</li>
 * <li>send Integer to the server</li>
 * <li>receive answer</li>
 * <li>check if answer is correct</li>
 * </ol>
 * The server will received and send back Integers.
 * 
 * @author E06A193P
 */
public class ClientAndServerExchanges {

    protected Server serverToTest;

    protected int port_server = 1111;

    @BeforeMethod
    public void setUp() {

	/**
	 * this server handles incoming Integer by sending back the number of
	 * elements it already received from that connection+1
	 */
	serverToTest = new fr.univnantes.alma.nio.server.ANioServer() {

	    @Override
	    protected ByteArrayHandler createHandler(SocketChannel socket) {
		return new ByteArrayDecoder(socket, Executors
			.newSingleThreadExecutor()) {

		    protected int received = 0;

		    @Override
		    public void handleObject(Serializable o, SocketChannel sc) {
			received++;
			try {
			    new NioByteArrayWriter(sc).send((Integer) o
				    + received);
			} catch (IOException e) {
			    e.printStackTrace();
			}
		    }
		};
	    }

	};

	serverToTest.start();
	serverToTest.openPort(port_server);

    }

    @Test
    public void sendAndReceive() throws IOException {
	Client client = new NioByteArrayClient("localhost", port_server);
	Integer is = new Integer(1);
	client.send(is);
	Integer ir = (Integer) client.receive();
	Assert.assertEquals((Integer) (is + 1), ir);
	client.close();
    }

    @Test(dependsOnMethods = { "sendAndReceive" })
    public void mutlipleclients() throws IOException {
	Client cl1 = new NioByteArrayClient("localhost", port_server), cl2 = new NioByteArrayClient(
		"localhost", port_server), cl3 = new NioByteArrayClient(
		"localhost", port_server), cl4 = new NioByteArrayClient(
		"localhost", port_server);

	Assert.assertEquals(sendOnClient(1, cl1), 2);
	Assert.assertEquals(sendOnClient(2, cl2), 3);
	Assert.assertEquals(sendOnClient(4, cl3), 5);
	Assert.assertEquals(sendOnClient(2, cl2), 4);// sent+2 because we sent 2
	// objects
	cl2.close();
	Assert.assertEquals(sendOnClient(-1, cl4), 0);
	cl3.close();
	cl4.close();
	Assert.assertEquals(sendOnClient(5, cl1), 7);
	Assert.assertEquals(sendOnClient(5, cl1), 8);
	cl1.close();
    }

    public int sendOnClient(int data, Client client) {
	client.send(data);
	return (Integer) client.receive();
    }

    @AfterMethod
    public void tearDown() {
	serverToTest.stop();
    }
}
