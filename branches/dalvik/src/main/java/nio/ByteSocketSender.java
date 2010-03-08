package nio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * A class designed to send objects through a {@link SocketChannel}.
 * <ol>
 * <li>Serialize the Object into a bytes array</li>
 * <li>Encode the length into a 4 bytes array</li>
 * <li>Send the length bytes</li>
 * <li>Send the object's bytes array in the socket</li>
 * </ol>
 */
public class ByteSocketSender {

	/** the channel with which we send data to */
	SocketChannel sc;

	/** default number of bytes we use to encode the size of the data to send */
	public static int defaultBytesOfSize = 4;

	/** encoder used to encode the size */
	protected final ByteBuffer sizeEncoder;

	/** size of the encoder */
	protected int bytesOfSize;

	public ByteSocketSender( SocketChannel socketToSend, int bytesOfSize ) {
		this.sc = socketToSend;
		this.bytesOfSize = bytesOfSize;
		this.sizeEncoder = ByteBuffer.allocateDirect( bytesOfSize );
	}

	public ByteSocketSender( SocketChannel socketToSend ) {
		this( socketToSend, defaultBytesOfSize );
	}

	/**
	 * send an array of bytes encoded with the protocol.
	 * @param toSend
	 *          the data to send
	 * @return is the buffer fully sent ? If false, then there may have been an
	 *         error
	 */
	protected synchronized boolean sendByteBuffer( ByteBuffer toSend ) {
		int size = toSend.remaining();
		if( size > Math.pow( 256, bytesOfSize ) ) {
			throw new OutOfCapacityException();
		}
		encodeInt( size, sizeEncoder );
		try {
			sc.write( sizeEncoder );
			sc.write( toSend );
		} catch( IOException e ) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static class OutOfCapacityException extends RuntimeException {

		private static final long serialVersionUID = 1L;

	}

	/**
	 * encode an int into a byte[] and put it in a {@link ByteBuffer}
	 * @param toEncode
	 *          the int that will be encoded
	 * @param buffer
	 *          the buffer to place encoded data into. It MUST be of capactity at
	 *          least {@link #bytesOfSize}. It is cleared on entering this method
	 */
	protected void encodeInt( int toEncode, ByteBuffer buffer ) {
		buffer.clear();
		for( int i = bytesOfSize - 1 ; i >= 0 ; i-- ) {
			buffer.put( new Integer( toEncode / (int) Math.pow( 256, i ) )
					.byteValue() );
		}
		buffer.flip();
	}

	/** auto-sized buffer that will contain serialized data */
	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	/** {@link ObjectOutputStream} that translates objects to {@link #baos} */
	protected ObjectOutputStream oos;

	{
		try {
			oos = new ObjectOutputStream( baos );
		} catch( IOException e ) {
			e.printStackTrace();
		}
	}

	public boolean send( Serializable s ) throws IOException {
		oos.reset();
		baos.reset();
		oos.writeObject( s );
		oos.flush();
		return sendByteBuffer( ByteBuffer.wrap( baos.toByteArray() ) );
	}

}
