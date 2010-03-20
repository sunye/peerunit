package fr.univnantes.alma.nio.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.univnantes.alma.nio.Server;

/**
 * This implementation of {@link Server} requires to be started in a different
 * Thread.<br />
 * It relies on the java.nio socket implementation, wich aims at using few
 * threads for listening to a socket<br />
 * //TODO use a state machine pattern
 * 
 * @author Guillaume Le Louët
 */
public abstract class ANioServer implements Server {

    protected final Object StateLock = new Object();

    protected ServerState state = ServerState.stopped;

    @Override
    public void setNbThreads(int nbThreads) {
	synchronized (StateLock) {
	    state.setNbThreads(this, nbThreads);
	}
    }

    @Override
    public int getNbThreads() {
	synchronized (StateLock) {
	    return state.getNbThreads(this);
	}
    }

    @Override
    public void stop() {
	synchronized (StateLock) {
	    state.stop(this);
	}
    }

    @Override
    public boolean start() {
	synchronized (StateLock) {
	    return state.start(this);
	}
    }

    @Override
    public boolean isRunning() {
	synchronized (StateLock) {
	    return state.isRunning(this);
	}
    }

    @Override
    public void closePort(int port) {
	synchronized (StateLock) {
	    state.closePort(this, port);
	}
    }

    @Override
    public Collection<Integer> getOpenedPort() {
	synchronized (StateLock) {
	    return state.getOpenedPort(this);
	}
    }

    @Override
    public boolean isPortOpened(int port) {
	synchronized (StateLock) {
	    return state.isPortOpened(this, port);
	}
    }

    @Override
    public boolean openPort(int port) {
	synchronized (StateLock) {
	    return state.openPort(this, port);
	}
    }

    /** the map of the port we listen to and the socket opened for those ports */
    protected Map<Integer, ServerSocketChannel> portsListened = Collections
	    .synchronizedMap(new HashMap<Integer, ServerSocketChannel>());

    /** number of threads used to handle incoming data */
    protected int nbThreads = 5;

    protected ExecutorService executor;

    /** the lock to modify the ports listened */
    protected final Object selectorLock = new Object();

    protected Selector sel;

    /** is the server supposed to stop as soon as possible ? */
    protected boolean hasToStop = false;

    /** start retrieving data from the socket */
    @Override
    public void run() {
	hasToStop = false;
	executor = Executors.newFixedThreadPool(nbThreads);
	try {
	    sel = Selector.open();
	    state = ServerState.started;
	    startHandlingData();
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	}
	executor.shutdownNow();
	closeSockets();
	state = ServerState.stopped;
    }

    protected void startHandlingData() {
	while (!hasToStop) {
	    try {
		// we may need the selector to wait before we actually ask
		// him to select, in case we wanted to listen to another thread.
		synchronized (selectorLock) {
		}
		sel.select();
		for (SelectionKey key : sel.selectedKeys()) {
		    if (key.isAcceptable()) {
			handleIncommingConnection(key);
		    } else if (key.isReadable()) {
			handleIncommingReadable(key);
		    }
		}
		sel.selectedKeys().clear();
		Thread.yield();
	    } catch (ClosedSelectorException cse) {
	    } catch (IOException ioe) {
		ioe.printStackTrace();
	    }
	}
    }

    public static final int buff_size = 1024;

    /** the array of bytes backing {@link #bytebuffer} */
    protected final byte[] bytearray = new byte[buff_size];

    /**
     * a buffer wrapped on {@link #bytearray}. Buffering incomming bytes on a
     * socket
     */
    protected final ByteBuffer bytebuffer = ByteBuffer.wrap(bytearray);

    /** data has to be read from a socket */
    protected void handleIncommingReadable(SelectionKey key) {
	SocketChannel sc = (SocketChannel) key.channel();
	ByteArrayHandler datahandler = handlers.get(sc);
	if (datahandler == null) {
	    return;
	}
	bytebuffer.clear();
	try {
	    int res;
	    do {
		bytebuffer.clear();
		res = sc.read(bytebuffer);
		if (res > 0) {
		    datahandler.handle(bytearray, res);
		} else if (res < 0) {
		    datahandler.flush();
		    key.channel().close();
		}
	    } while (res > 0);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * each incoming connection must be associated with a
     * {@link ByteArrayHandler} that will handle further incoming bytes
     */
    protected Map<SocketChannel, ByteArrayHandler> handlers = Collections
	    .synchronizedMap((new HashMap<SocketChannel, ByteArrayHandler>()));

    /** a connection event has occurred */
    protected void handleIncommingConnection(SelectionKey key) {
	try {
	    SocketChannel client = ((ServerSocketChannel) key.channel())
		    .accept();
	    ByteArrayHandler datahandler = createHandler(client);
	    client.configureBlocking(false);
	    client.register(sel, SelectionKey.OP_READ);
	    handlers.put(client, datahandler);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /** close sockets and selector */
    protected void closeSockets() {
	try {
	    sel.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	for (Integer port : portsListened.keySet()) {
	    ServerSocketChannel ssc = portsListened.get(port);
	    try {
		ssc.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	    portsListened.remove(port);
	}
	for (SocketChannel sc : handlers.keySet()) {
	    try {
		sc.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    /**
     * create appropriate decoder from bytes arrays.
     * <p>
     * the server will invoke that method every time it will receive incoming
     * connection ; further data sent on that connection will be provided to the
     * handler generated.
     * </p>
     * <p>
     * this method is supposed to return object that can handle bytes arrays and
     * translate them according to the protocol.
     * </p>
     * 
     * @return a {@link ByteArrayHandler} implementing decoding of bytes array
     * @param socket
     *            the socket providing the connection to handle
     */
    protected abstract ByteArrayHandler createHandler(SocketChannel socket);

}
