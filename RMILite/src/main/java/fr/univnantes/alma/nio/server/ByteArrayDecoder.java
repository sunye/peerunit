package fr.univnantes.alma.nio.server;

import java.io.Serializable;
import java.nio.channels.SocketChannel;
import java.util.List;

import fr.univnantes.alma.nio.objectToBytes.Decoder;


public abstract class ByteArrayDecoder extends Decoder implements
		ByteArrayHandler {

	protected SocketChannel sc;

	public ByteArrayDecoder( SocketChannel sc ) {
		this.sc = sc;
	}

	@Override
	public void flush() {
		requireHeader();
	}

	@Override
	public void handle( byte[] array, int size ) {
		List<Serializable> decoded = decode( array, size - 1 );
		for( Serializable o : decoded ) {
			handleObject( o, sc );
		}
	}

	public abstract void handleObject( Serializable o, SocketChannel sc );

}
