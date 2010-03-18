package fr.univnantes.alma.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.Arrays;
import java.util.Collection;

/** default is this of a stopped server */
abstract class ServerState {

    public void stop(ANioServer server) {
    }

    /**
     * only one is called at the same time. There should not be a call to this
     * while the server is starting/stopping
     */
    public boolean start(ANioServer server) {
	return false;
    }

    public boolean isRunning(ANioServer server) {
	return false;
    }

    public boolean openPort(ANioServer server, int port) {
	return false;
    }

    public void closePort(ANioServer server, int port) {
    }

    public boolean isPortOpened(ANioServer server, int port) {
	return false;
    }

    public Collection<Integer> getOpenedPort(ANioServer server) {
	return Arrays.asList();
    }

    public void setNbThreads(ANioServer server, int nbThreads) {

    }

    public int getNbThreads(ANioServer server) {
	return server.nbThreads;
    }

    public static final ServerState stopped = new ServerState() {

	@Override
	public boolean start(ANioServer server) {
	    new Thread(server).start();
	    while (server.state != started) {
		Thread.yield();
	    }
	    return true;
	}

	@Override
	public void setNbThreads(ANioServer server, int nbThreads) {
	    server.nbThreads = nbThreads;
	}

    };

    public static final ServerState started = new ServerState() {

	@Override
	public void stop(ANioServer server) {
	    server.hasToStop = true;
	    if (server.sel != null) {
		server.sel.wakeup();
	    }
	    while (server.state != stopped) {
		Thread.yield();
	    }
	}

	@Override
	public boolean isRunning(ANioServer server) {
	    return true;
	}

	@Override
	public void closePort(ANioServer server, int port) {
	    ServerSocketChannel ssc = server.portsListened.get(port);
	    if (ssc == null) {
		return;
	    }
	    try {
		ssc.close();
	    } catch (IOException e) {
	    }
	    server.portsListened.remove(port);
	}

	@Override
	public Collection<Integer> getOpenedPort(ANioServer server) {
	    return server.portsListened.keySet();
	}

	@Override
	public boolean isPortOpened(ANioServer server, int port) {
	    return server.portsListened.containsKey(port);
	}

	@Override
	public boolean openPort(ANioServer server, int port) {
	    if (server.portsListened.containsKey(port)) {
		return true;
	    }
	    try {
		ServerSocketChannel channel = ServerSocketChannel.open();
		channel.configureBlocking(false);
		channel.socket().bind(new InetSocketAddress(port));
		// if the selector is selecting, meaning waiting for incoming
		// data on selected sockets, then sel.register() is blocked
		// until something is sent to a socket. Thus, we need to prevent
		// sel from re-selecting, and then wake up sel to stop its
		// current selecting.
		synchronized (server.selectorLock) {
		    server.sel.wakeup();
		    channel.register(server.sel, SelectionKey.OP_ACCEPT);
		}
		server.portsListened.put(port, channel);
		return true;
	    } catch (IOException e) {
		e.printStackTrace();
		return false;
	    }
	}
    };
}
