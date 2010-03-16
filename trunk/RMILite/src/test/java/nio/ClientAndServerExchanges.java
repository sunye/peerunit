package nio;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.SocketChannel;

import nio.client.NioByteArrayClient;
import nio.client.NioByteArrayWriter;
import nio.server.ByteArrayDecoder;
import nio.server.ByteArrayHandler;

import org.testng.Assert;
import org.testng.annotations.*;

/**
 * Test if a couple client/server works well:
 * <ol>
 * <li>start the server</li>
 * <li></li>
 * <li></li>
 * <li></li>
 * </ol>
 * @author E06A193P
 */
public class ClientAndServerExchanges {

	protected Server serverToTest;

	protected int port_server = 1111;

	@BeforeMethod
	public void setUp() {

		serverToTest = new nio.server.ANioServer() {

			@Override
			protected ByteArrayHandler createHandler( SocketChannel socket ) {
				return new ByteArrayDecoder( socket ) {

					@Override
					public void handleObject( Serializable o, SocketChannel sc ) {
						Integer i = (Integer) o;
						try {
							new NioByteArrayWriter( sc ).send( i + 1 );
						} catch( IOException e ) {
							e.printStackTrace();
						}
					}
				};
			}

		};

		serverToTest.start();
		serverToTest.openPort( port_server );

	}

	@Test
	public void sendAndReceive() throws IOException {
		Client client = new NioByteArrayClient( "localhost", port_server );
		Integer is = new Integer( 1 );
		client.send( is );
		Integer ir = (Integer) client.receive();
		Assert.assertEquals( (Integer) ( is + 1 ), ir );
		client.close();
	}

	@Test( dependsOnMethods = { "sendAndReceive" } )
	public void mutlipleclients() throws IOException {
		Client cl1 = new NioByteArrayClient( "localhost", port_server ), cl2 = new NioByteArrayClient(
				"localhost", port_server ), cl3 = new NioByteArrayClient( "localhost",
				port_server ), cl4 = new NioByteArrayClient( "localhost", port_server );

		Assert.assertEquals( sendOnClient( 1, cl1 ), 2 );
		cl1.close();
		Assert.assertEquals( sendOnClient( 2, cl2 ), 3 );
		Assert.assertEquals( sendOnClient( 4, cl3 ), 5 );
		cl2.close();
		Assert.assertEquals( sendOnClient( -1, cl4 ), 0 );
		cl3.close();
		cl4.close();
	}

	public int sendOnClient( int data, Client client ) {
		client.send( data );
		return (Integer) client.receive();
	}

	@AfterMethod
	public void tearDown() {
		serverToTest.stop();
	}
}
