package fr.univnantes.alma.nio.server;

import java.io.Serializable;
import java.nio.channels.SocketChannel;
import java.util.List;

import fr.univnantes.alma.nio.objectToBytes.Decoder;

/**
 * a {@link ByteArrayHandler} that will decode incomming Bytes using a
 * {@link Decoder}.<br />
 * It needs to be redefined its method
 * {@link #handleObject(Serializable, SocketChannel)} in order to become usable
 * 
 * @author Guillaume Le Louët
 * 
 */
public abstract class ByteArrayDecoder extends Decoder implements
	ByteArrayHandler {

    protected SocketChannel sc;

    /**
     * creates with a channel
     * 
     * @param sc
     *            a channel that is supposed to be the one to answer in case we
     *            need to. It can, too, be a way to retrieve the port of
     *            incoming data
     */
    public ByteArrayDecoder(SocketChannel sc) {
	this.sc = sc;
    }

    @Override
    public void flush() {
	requireHeader();
    }

    @Override
    public void handle(byte[] array, int size) {
	List<Serializable> decoded = decode(array, size - 1);
	for (Serializable o : decoded) {
	    handleObject(o, sc);
	}
    }

    public abstract void handleObject(Serializable o, SocketChannel sc);

}
