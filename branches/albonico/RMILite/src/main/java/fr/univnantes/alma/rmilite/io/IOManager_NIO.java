package fr.univnantes.alma.rmilite.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import fr.univnantes.alma.rmilite.server.RemoteObjectManager;

/**
 * A manager that handles incoming data on several ports with a
 * {@link NioByteArrayDispatcherServer}.<br />
 * I allows to send data and received data back with a {@link RemoteProxy_NIO}.
 * 
 * @author Guillaume Le Lou�t
 */
public class IOManager_NIO implements IOManager {

	protected NioByteArrayDispatcherServer server;

	{
		server = new NioByteArrayDispatcherServer();
		// we need to start the server as soon as the manager is
		// started, even if we don't listen to any port yet.
		server.start();
	}

	public void close(int port) throws IOException {
		server.closePort(port);
	}

	public RemoteObjectManager getRemoteObjectManager() {
		return server.getRemoteObjectManager();
	}

	public void setRemoteObjectManager(RemoteObjectManager rom) {
		server.setRemoteObjectManager(rom);
	}

	@Override
	public RemoteProxy getRemoteProxy(InetSocketAddress reference)
			throws IOException {
		return new RemoteProxy_NIO(reference);
	}

	@Override
	public int open(int port) throws IOException {
		if (port == 0) {
			port = detectAvailablePort();
		}
		if (port != 0) {
			server.openPort(port);
		}
		return port;
	}

	int lastAvailablePort = 1024;

	/**
	 * guess what port is available to listen at on local machine
	 * 
	 * @return 0 if no port can be opened, or an available port if exists
	 */
	protected int detectAvailablePort() {
		lastAvailablePort++;
		for (; lastAvailablePort < 10000; lastAvailablePort++) {
			try {
				SocketChannel sc = SocketChannel.open(new InetSocketAddress(
						"localhost", lastAvailablePort));
				sc.close();
			} catch (IOException ioe) {// nothing listens at this port
				return lastAvailablePort;
			}
		}
		return 0;
	}

}
