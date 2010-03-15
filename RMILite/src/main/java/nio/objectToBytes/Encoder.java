package nio.objectToBytes;

import java.io.*;
import java.nio.channels.SocketChannel;

/**
 * Encode a {@link Serializable} in an array of bytes :
 * <ol>
 * <li>Serialize the Object into a bytes array</li>
 * <li>Encode the length of this array into a 4 bytes array</li>
 * <li>concatenate the length array and the object's array and return it</li>
 * </ol>
 * <p>
 * Such an encoded byte array can be decoded with {@link Decoder}
 * </p>
 * <p>
 * With the use of {@link Decoder}, this allows to send {@link Serializable} by
 * sockets handling ByteArrays and that may fragment data (such as the nio
 * {@link SocketChannel} )
 * </p>
 */
public class Encoder {

	public final static int headerBytesSize = 4;

	public byte[] encode( Serializable s ) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream( baos );
			oos.writeObject( s );
			int size = baos.size();
			byte[] ret = new byte[size + headerBytesSize];
			encodeInt( size, ret );
			byte[] buff = baos.toByteArray();
			for( int i = 0 ; i < size ; i++ ) {
				ret[ headerBytesSize + i ] = buff[ i ];
			}
			return ret;
		} catch( IOException e ) {
			e.printStackTrace();
		}
		return null;
	}

	protected void encodeInt( int toEncode, byte[] buffer ) {
		for( int i = headerBytesSize - 1 ; i >= 0 ; i-- ) {
			buffer[ headerBytesSize - i - 1 ] = ( new Integer( toEncode
					/ (int) Math.pow( 256, i ) ).byteValue() );
		}
	}

}
