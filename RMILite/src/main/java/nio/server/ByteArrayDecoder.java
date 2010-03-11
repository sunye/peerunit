package nio.server;

import java.nio.channels.SocketChannel;
import java.util.List;

import nio.objectToBytes.Decoder;


public abstract class ByteArrayDecoder extends Decoder implements ByteArrayHandler {
	
	protected SocketChannel sc;
	
	public ByteArrayDecoder(SocketChannel sc) {
		this.sc = sc;
	}

	@Override
	public void flush() {
	}

	@Override
	public void handle( byte[] array, int size ) {
		List<Object> decoded = decode( array , size-1);
		for(Object o : decoded) {
			handleObject(o, sc);
		}
	}
	
	public abstract  void handleObject(Object o, SocketChannel sc);
	
}
