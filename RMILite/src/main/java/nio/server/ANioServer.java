package nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

import nio.Server;

/**
 * This implementation of {@link Server} requires to be started in a different
 * Thread.<br />
 * It relies on the java.nio socket implementation, wich aims at using few
 * threads for listening to a socket<br />
 * //TODO use a statemachine pattern
 * @author E06A193P
 */
public abstract class ANioServer implements Server {

	/** the map of the port we listen to and the socket opened for those ports */
	protected Map<Integer, ServerSocketChannel> portsListened = Collections
			.synchronizedMap( new HashMap<Integer, ServerSocketChannel>() );

	/** the lock to modify the ports listened */
	protected Object selectorLock = new Object();

	/** number of threads used to handle incoming data */
	protected int nbThreads = 5;

	@Override
	public void setNbThreads( int nbThreads ) {
		// TODO
		this.nbThreads = nbThreads;
		if( this.nbThreads < 1 ) {
			this.nbThreads = 1;
		}
	}

	@Override
	public int getNbThreads() {
		return nbThreads;
	}

	protected Selector sel;

	/** is the server supposed to stop as soon as possible ? */
	protected boolean hasToStop = false;

	@Override
	public void stop() {
		hasToStop = true;
		if( sel != null ) {
			sel.wakeup();
		}
		while( isRunning() ) {
			Thread.yield();
		}
	}

	/** is the server listening on his socket ? */
	protected boolean isRunning = false;

	@Override
	public boolean isRunning() {
		return this.isRunning;
	}

	/** start retrieving data from the socket */
	public void run() {
		if( isRunning() ) {
			return;
		}
		hasToStop = false;
		try {
			sel = Selector.open();
			isRunning = true;
			startHandlingDatas();
		} catch( IOException ioe ) {
			ioe.printStackTrace();
		}
		closeSockets();
		isRunning = false;
	}

	@Override
	public boolean start() {
		new Thread( this ).start();
		while( !isRunning() ) {
			Thread.yield();
		}
		return true;
	}

	/** close sockets and selector */
	protected void closeSockets() {
		try {
			sel.close();
		} catch( IOException e ) {
			e.printStackTrace();
		}
		for( Integer port : portsListened.keySet() ) {
			ServerSocketChannel ssc = portsListened.get( port );
			try {
				ssc.close();
			} catch( IOException e ) {
				e.printStackTrace();
			}
			portsListened.remove( port );
		}
	}

	protected void startHandlingDatas() {
		while( !hasToStop ) {
			try {
				synchronized( selectorLock ) {
					// we may need the selector to wait before we actually ask him to
					// select
				}
				sel.select();
				for( SelectionKey key : sel.selectedKeys() ) {
					if( key.isAcceptable() ) {
						handleIncommingConnection( key );
					} else if( key.isReadable() ) {
						handleIncommingReadable( key );
					}
				}
				sel.selectedKeys().clear();
				Thread.yield();
			} catch( ClosedSelectorException cse ) {} catch( IOException ioe ) {
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
	protected final ByteBuffer bytebuffer = ByteBuffer.wrap( bytearray );

	/** data has to be read from a socket */
	protected void handleIncommingReadable( SelectionKey key ) {
		SocketChannel sc = (SocketChannel) key.channel();
		ByteArrayHandler datahandler = handlers.get( sc );
		if( datahandler == null ) {
			return;
		}
		bytebuffer.clear();
		try {
			int res;
			do {
				bytebuffer.clear();
				res = sc.read( bytebuffer );
				if( res > 0 ) {
					datahandler.handle( bytearray, res );
				} else if( res < 0 ) {
					datahandler.flush();
					key.channel().close();
				}
			} while( res > 0 );
		} catch( IOException e ) {
			e.printStackTrace();
		}
	}

	/**
	 * each incoming connection must be associated with a {@link ByteArrayHandler}
	 * that will handle further incoming bytes
	 */
	protected Map<SocketChannel, ByteArrayHandler> handlers = Collections
			.synchronizedMap( ( new HashMap<SocketChannel, ByteArrayHandler>() ) );

	/** a connection event has occurred */
	protected void handleIncommingConnection( SelectionKey key ) {
		try {
			SocketChannel client = ( (ServerSocketChannel) key.channel() ).accept();
			ByteArrayHandler datahandler = createHandler( client );
			client.configureBlocking( false );
			client.register( sel, SelectionKey.OP_READ );
			handlers.put( client, datahandler );
		} catch( IOException e ) {
			e.printStackTrace();
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
	 * @return a {@link ByteArrayHandler} implementing decoding of bytes array
	 * @param socket
	 *          the socket providing the connection to handle
	 */
	protected abstract ByteArrayHandler createHandler( SocketChannel socket );

	@Override
	public void closePort( int port ) {
		ServerSocketChannel ssc = portsListened.get( port );
		if( ssc == null ) {
			return;
		}
		try {
			ssc.close();
		} catch( IOException e ) {}
		portsListened.remove( port );
	}

	@Override
	public Collection<Integer> getOpenedPort() {
		return portsListened.keySet();
	}

	@Override
	public boolean isPortOpened( int port ) {
		return portsListened.containsKey( port );
	}

	@Override
	public boolean openPort( int port ) {
		if( portsListened.containsKey( port ) || !isRunning ) {
			return true;
		}
		try {
			ServerSocketChannel channel = ServerSocketChannel.open();
			channel.configureBlocking( false );
			channel.socket().bind( new InetSocketAddress( port ) );
			// if the selector is selecting, meaning waiting for incoming data on
			// selected sockets, then sel.register() is blocked until something is
			// sent to a socket. Thus, we need to prevent sel from re-selecting, and
			// then wake up sel to stop its current selecting.
			synchronized( selectorLock ) {
				sel.wakeup();
				channel.register( sel, SelectionKey.OP_ACCEPT );
			}
			portsListened.put( port, channel );
			return true;
		} catch( IOException e ) {
			e.printStackTrace();
			return false;
		}
	}

}
