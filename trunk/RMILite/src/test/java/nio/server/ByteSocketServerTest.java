package nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import nio.server.ByteSocketServer;

import org.testng.annotations.Test;

public class ByteSocketServerTest {

	@Test
	public void startAndStopTest() {
		final int port = 1111;
		final int milliDelay = 800;// the time we give the server to obey us, in
									// ms

		ByteSocketServer bss = new ByteSocketServer(port) {

			@Override
			protected ByteArrayHandler createHandler(SocketChannel socket) {
				throw new UnsupportedOperationException();
			}
		};

		assert (!bss.isRunning());
		new Thread(bss).start();

		try {
			Thread.sleep(milliDelay);
			assert (bss.isRunning());
			bss.stop();
			Thread.sleep(milliDelay);
			assert (!bss.isRunning());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testMultiListenerOpened() {
		final int port = 1111;
		final int waintingmilli = 100;
		
		receivedConnexions=0;

		ByteSocketServer bss = new ByteSocketServer(port) {

			@Override
			protected ByteArrayHandler createHandler(SocketChannel socket) {
				
				receivedConnexions++;
				
				return new ByteArrayHandler() {
					
					@Override
					public void handle(byte[] array, int size) {
					}
					
					@Override
					public void flush() {
					}
				};
			}
		};
		new Thread(bss).start();
		try {
			Thread.sleep(waintingmilli);
			assert(bss.isRunning());
			SocketChannel sc1, sc2;
			sc1 = SocketChannel.open(new InetSocketAddress("localhost", port));
			sc1.close();
			sc1 = SocketChannel.open(new InetSocketAddress("localhost", port));
			sc1.close();
			Thread.sleep(waintingmilli);
			assert(receivedConnexions==2);
			sc1 = SocketChannel.open(new InetSocketAddress("localhost", port));
			sc2 = SocketChannel.open(new InetSocketAddress("localhost", port));
			Thread.sleep(waintingmilli);
			sc1.close();
			sc2.close();
			Thread.sleep(waintingmilli);
			assert(receivedConnexions==4);
			bss.stop();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	protected static int receivedConnexions = 0;

}
