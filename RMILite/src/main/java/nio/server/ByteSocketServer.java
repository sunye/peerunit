package nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import nio.Server;


public abstract class ByteSocketServer implements Server {

	protected int portToListen;

	/**
	 * create a server. This may be started by calling the {@link #run()} method
	 * @param port
	 *          the port this server should listen to.
	 */
	public ByteSocketServer( int port ) {
		this.portToListen = port;
	}

	/** number of threads used to handle incoming data */
	protected int nbThreads = 5;
	
	@Override
	public void setNbThreads( int nbThreads ) {
		this.nbThreads = nbThreads;
		if( this.nbThreads < 1 ) {
			this.nbThreads = 1;
		}
	}
	
	@Override
	public int getNbThreads() {
		return nbThreads;
	}

	protected ServerSocketChannel channel;

	protected Selector sel;

	/** is the server supposed to stop as soon as possible ? */
	protected boolean hasToStop = false;

	@Override
	public void stop() {
		hasToStop = true;
		if(sel!=null) {
			sel.wakeup();
//			try {
//				sel.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
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
		hasToStop = false;
		try {
			openSocket();
		} catch( IOException ioe ) {
			ioe.printStackTrace();
			return;
		}
		isRunning = true;
		startHandlingDatas();
		closeSocket();
		isRunning = false;
	}

	/** open socket and selector */
	protected void openSocket() throws IOException {
		channel = ServerSocketChannel.open();
		channel.configureBlocking( false );
		channel.socket().bind( new InetSocketAddress( portToListen ) );
		sel = Selector.open();
		channel.register( sel, SelectionKey.OP_ACCEPT);
	}

	/** close sockets and selector */
	protected void closeSocket() {
		try {
			channel.close();
			sel.close();
		} catch( IOException e ) {
			e.printStackTrace();
		}
	}

	protected void startHandlingDatas() {
		while( !hasToStop ) {
			try {
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
			} catch(ClosedSelectorException cse) { 
			} catch( IOException ioe ) {
				ioe.printStackTrace();
			}
		}
	}

	public static final int buff_size = 1024;

	/** the array of bytes backing {@link #bytebuffer} */
	protected final byte[] bytearray = new byte[buff_size];

	/** a buffer wrapped on {@link #bytearray} */
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
					datahandler.handle( bytearray , res);
				} else if(res<0){
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
			ByteArrayHandler datahandler = createHandler(client);
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
	 */
	protected abstract ByteArrayHandler createHandler(SocketChannel socket);

}
