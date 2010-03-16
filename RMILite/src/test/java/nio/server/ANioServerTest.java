package nio.server;

import static org.testng.Assert.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

public class ANioServerTest {

	@Test
	public void startAndStopTest() {
		final int port = 1111;
		// the time we give the server to obey us, in ms
		final int milliDelay = 800;

		ANioServer bss = new ANioServer() {

			@Override
			protected ByteArrayHandler createHandler( SocketChannel socket ) {
				throw new UnsupportedOperationException();
			}
		};

		assertFalse( bss.isRunning() );
		new Thread( bss ).start();
		try {
			Thread.sleep( milliDelay );
		} catch( InterruptedException e ) {
			e.printStackTrace();
		}
		assertTrue( bss.isRunning() );
		assertTrue( bss.openPort( port ) );
		bss.stop();
		assertFalse( bss.isRunning() );

		bss.start();
		assertTrue( bss.isRunning() );
		bss.stop();
		while( bss.isRunning() ) {
			Thread.yield();
		}
	}

	@Test( dependsOnMethods = { "startAndStopTest" } )
	public void testMultiListenerOpened() throws InterruptedException,
			IOException {
		final int port = 1111;
		final int waintingmilli = 100;

		receivedConnexions = 0;

		ANioServer bss = new ANioServer() {

			@Override
			protected ByteArrayHandler createHandler( SocketChannel socket ) {

				receivedConnexions++ ;

				return new ByteArrayHandler() {

					@Override
					public void handle( byte[] array, int size ) {}

					@Override
					public void flush() {}
				};
			}
		};
		new Thread( bss ).start();
		Thread.sleep( waintingmilli );
		assertTrue( bss.isRunning() );
		bss.openPort( port );
		SocketChannel sc1, sc2;
		sc1 = SocketChannel.open( new InetSocketAddress( "localhost", port ) );
		sc1.close();
		sc1 = SocketChannel.open( new InetSocketAddress( "localhost", port ) );
		sc1.close();
		Thread.sleep( waintingmilli );
		assertEquals( receivedConnexions, 2 );
		sc1 = SocketChannel.open( new InetSocketAddress( "localhost", port ) );
		sc2 = SocketChannel.open( new InetSocketAddress( "localhost", port ) );
		Thread.sleep( waintingmilli );
		sc1.close();
		sc2.close();
		Thread.sleep( waintingmilli );
		assertEquals( receivedConnexions, 4 );
		bss.stop();

	}
	protected static int receivedConnexions = 0;

	@Test
	public void testSeveralSocketListened() throws IOException {
		final Map<Integer, Integer> connectionsOnPorts = new HashMap<Integer, Integer>();
		ANioServer bss = new ANioServer() {

			@Override
			protected ByteArrayHandler createHandler( final SocketChannel socket ) {

				int port = socket.socket().getLocalPort();
				// System.out.println( "connection on local port " + port );
				Integer mapped = connectionsOnPorts.get( port );
				int yetDone = ( mapped == null ? 0 : mapped );
				connectionsOnPorts.put( port, yetDone + 1 );

				return new ByteArrayHandler() {

					@Override
					public void handle( byte[] array, int size ) {}

					@Override
					public void flush() {}
				};
			}
		};
		bss.start();
		bss.openPort( 1050 );
		bss.openPort( 1051 );
		SocketChannel sc1, sc2;
		sc1 = SocketChannel.open( new InetSocketAddress( "localhost", 1050 ) );
		sc2 = SocketChannel.open( new InetSocketAddress( "localhost", 1051 ) );
		sc1.close();
		sc1 = SocketChannel.open( new InetSocketAddress( "localhost", 1050 ) );
		sc1.close();
		sc2.close();
		try {
			Thread.sleep( 200 );
		} catch( InterruptedException e ) {
			e.printStackTrace();
		}
		assertEquals( connectionsOnPorts.get( 1050 ), new Integer( 2 ) );
		assertEquals( connectionsOnPorts.get( 1051 ), new Integer( 1 ) );
	}

}
