package nio;

import static org.testng.Assert.*;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.SocketChannel;
import java.util.*;

import nio.client.NioByteArrayWriter;
import nio.server.*;

import org.testng.annotations.*;

/**
 * Check if the server handles an incoming connection
 * @author E06A193P
 */
public class ClientAndServerConnection {

	protected Writer client;
	protected Server server;
	protected ByteArrayHandler handler;
	protected final static int listeningPort = 1112;
	protected List<Object> receivedObjects;
	protected int nb_received = 0;

	@BeforeMethod
	public void setUp() throws IOException {

		nb_received = 0;
		receivedObjects = new ArrayList<Object>();

		server = new ANioServer() {

			@Override
			protected ByteArrayHandler createHandler( SocketChannel socket ) {
				return handler;
			}
		};

		new Thread( server ).start();
		server.openPort( listeningPort );

		handler = new ByteArrayDecoder( null ) {

			@Override
			public void handleObject( Serializable o, SocketChannel sc ) {
				receivedObjects.add( o );
				nb_received++ ;
			}
		};
		while( !server.isRunning() ) {
			Thread.yield();
		}
		client = new NioByteArrayWriter( "localhost", listeningPort );
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

		nb_received = 0;

		for( Serializable s : toSend ) {
			client.send( s );
		}
		client.close();

		while( nb_received < toSend.size() ) {
			Thread.yield();
		}
		assertTrue( receivedObjects.containsAll( toSend ) );
		assertEquals( nb_received, toSend.size() );
	}

	@DataProvider( name = "elementsToSend" )
	public Object[][] elementsToSend() {
		// for each call, each argument, is an array of Serializable
		return new Object[][] { { Arrays.asList( new Integer( 42 ) ) },
				{ Arrays.asList( "je", "tu", "il" ) },
				// check that sending three times the same object
				// is not considered as once by object
				{ Arrays.asList( singleton, singleton, singleton ) } };
	}

	protected final String singleton = "single";

}
