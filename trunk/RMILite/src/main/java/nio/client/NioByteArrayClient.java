package nio.client;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

import nio.Reader;
import nio.objectToBytes.Decoder;

/**
 * Connect to a distant socket via nio, and then handle sending and receiving
 * data via a specific protocol handled by {@link Decoder} and {@link Encoder}<br />
 * <ul>
 * <li>The sending part is inherited from the {@link NioByteArrayWriter}</li>
 * <li>The receiving part is blocking and doesn't use a thread.</li>
 * </ul>
 * @author E06A193P
 */
public class NioByteArrayClient extends NioByteArrayWriter implements Reader {

	public NioByteArrayClient( String remoteAdress, int remotePort )
			throws IOException {
		super( remoteAdress, remotePort );
		this.sel = Selector.open();
		sc.register( sel, SelectionKey.OP_READ );
	}

	Selector sel;

	/** LinkedList of items, because we will remove them one at a time */
	protected List<Serializable> yetReceived = new LinkedList<Serializable>();

	protected Decoder decoder = new Decoder();

	@Override
	public Serializable receive() {
		return receive( -1 );
	}

	@Override
	public Serializable receive( int timeoutMilli ) {
		long maxTime = System.currentTimeMillis() + timeoutMilli;
		while( yetReceived.isEmpty()
				&& ( timeoutMilli < 0 || System.currentTimeMillis() < maxTime ) ) {
			try {
				if( timeoutMilli > -1 ) {
					sel.select( maxTime - System.currentTimeMillis() );
				} else {
					sel.select();
				}
				for( SelectionKey sk : sel.selectedKeys() ) {
					if( sk.isReadable() ) {
						handleIncommingReadable( sk );
					}
				}
			} catch( IOException e ) {
				e.printStackTrace();
				return null;
			}
		}
		Iterator<Serializable> it = yetReceived.iterator();
		Serializable ret = it.next();
		it.remove();
		return ret;
	}

	// XXX copied from ByteSocketServer with few modifications
	public static final int buff_size = 1024;

	/** the array of bytes backing {@link #bytebuffer} */
	protected final byte[] bytearray = new byte[buff_size];

	/**
	 * a buffer wrapped on {@link #bytearray}. Buffering incomming bytes on a
	 * socket
	 */
	protected final ByteBuffer bytebuffer = ByteBuffer.wrap( bytearray );

	protected void handleIncommingReadable( SelectionKey key ) {
		SocketChannel sc = (SocketChannel) key.channel();
		bytebuffer.clear();
		try {
			int res;
			do {
				bytebuffer.clear();
				res = sc.read( bytebuffer );
				if( res > 0 ) {
					yetReceived.addAll( decoder.decode( bytearray, res - 1 ) );
				} else if( res < 0 ) {
					key.channel().close();
				}
			} while( res > 0 && yetReceived.isEmpty() );
		} catch( IOException e ) {
			e.printStackTrace();
		}
	}

}
