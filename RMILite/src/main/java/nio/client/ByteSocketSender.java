package nio.client;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import nio.Client;
import nio.objectToBytes.Encoder;

/**
 * A class designed to send objects through a {@link SocketChannel}.
 * <ol>
 * <li>Serialize the Object into a bytes array</li>
 * <li>Encode the length into a 4 bytes array</li>
 * <li>Send the length bytes</li>
 * <li>Send the object's bytes array in the socket</li>
 * </ol>
 */
public class ByteSocketSender implements Client {

	/** the channel with which we send data to */
	final SocketChannel sc;

	public ByteSocketSender( SocketChannel socketToSend) {
		this.sc = socketToSend;
	}

	protected Encoder encoder = new Encoder();

	@Override
	public boolean send( Serializable s ) {
		try {
			sc.write( ByteBuffer.wrap( encoder.encode( s ) ) );
			return true;
		} catch( IOException ioe ) {
			ioe.printStackTrace();
			return false;
		}
	}

	@Override
	public void close() {
		try {
			sc.close();
		} catch( IOException ioe ) {

		}
	}

}
