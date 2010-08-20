package fr.univnantes.alma.nio.client;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.SocketChannel;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fr.univnantes.alma.nio.Server;
import fr.univnantes.alma.nio.Writer;
import fr.univnantes.alma.nio.server.ANioServer;
import fr.univnantes.alma.nio.server.ByteArrayDecoder;
import fr.univnantes.alma.nio.server.ByteArrayHandler;

public class NioByteArrayClientTest {

    protected Server server;
    protected static final int port = 2057;
    protected NioByteArrayClient client;

    @BeforeMethod
    public void setUp() throws IOException {
	server = new ANioServer() {

	    @Override
	    protected ByteArrayHandler createHandler(final SocketChannel socket) {
		try {
		    return new ByteArrayDecoder(socket, executor) {

			protected Writer writer = new NioByteArrayWriter(socket);

			@Override
			public void handleObject(Serializable o,
				SocketChannel sc) {
			    writer.send("ok");
			}
		    };
		} catch (IOException e) {
		    e.printStackTrace();
		    return null;
		}
	    }
	};
	server.start();
	server.openPort(port);
	client = new NioByteArrayClient("localhost", port);
    }

    @Test
    public void testSend() {
	assert (client.send(""));
    }

    @Test
    public void testReceive() {
	client.send("");
	assert (client.receive().equals("ok"));
    }

    @AfterMethod
    public void tearDown() {
	server.stop();
    }
}
