package nio;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import nio.client.ByteSocketSender;
import nio.server.ByteArrayDecoder;
import nio.server.ByteArrayHandler;
import nio.server.ByteSocketServer;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ClientAndServerTest {

	protected Client client;
	protected Server server;
	protected ByteArrayHandler handler;
	protected final static int listeningPort = 1112;
	protected List<Object> receivedObjects;
	protected int nb_received = 0;

	@BeforeClass
	public void setUpClass() {

	}

	@BeforeMethod
	public void setUp() throws IOException {

		nb_received = 0;
		receivedObjects = new ArrayList<Object>();

		server = new ByteSocketServer( listeningPort ) {

			@Override
			protected ByteArrayHandler createHandler( SocketChannel socket ) {
				return handler;
			}
		};

		new Thread( server ).start();

		handler = new ByteArrayDecoder( null ) {

			@Override
			public void handleObject( Object o, SocketChannel sc ) {
				receivedObjects.add( o );
				nb_received++ ;
			}
		};
		while( !server.isRunning() ) {
			Thread.yield();
		}
		SocketChannel sc = SocketChannel.open();
		sc.connect( new InetSocketAddress( "localhost", listeningPort ) );
		client = new ByteSocketSender( sc );
	}

	@AfterMethod
	public void tearDown() {
		server.stop();
		while( server.isRunning() ) {
			Thread.yield();
		}
	}

	@Test( dataProvider = "elementsToSend" )
	public void testTransmition( Collection<Serializable> toSend ) {

		for( Serializable s : toSend ) {
			client.send( s );
		}
		client.close();
		while( nb_received < toSend.size() ) {
			Thread.yield();
		}
		assert ( receivedObjects.containsAll( toSend ) );
		assert ( nb_received == toSend.size() );
	}

	@DataProvider( name = "elementsToSend" )
	public Object[][] elementsToSend() {
		// for each call, each argument, is an array of Serializable
		return new Object[][] {
				{ Arrays.asList( new Integer( 42 ) ) },
				{ Arrays.asList( "je", "tu", "il" ) },
				// check that sending three times the same object
				//is not considered as once by object
				{ Arrays.asList( singleton, singleton, singleton ) } };
	}

	protected final String singleton = "single";

}
