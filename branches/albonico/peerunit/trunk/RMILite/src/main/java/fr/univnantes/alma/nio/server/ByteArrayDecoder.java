package fr.univnantes.alma.nio.server;

import java.io.Serializable;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

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
	ByteArrayHandler, Runnable {

    protected SocketChannel sc;
    protected Executor executor;

    /**
     * creates with a channel
     * 
     * @param sc
     *            a channel that is supposed to be the one to answer in case we
     *            need to. It can, too, be a way to retrieve the port of
     *            incoming data
     */
    public ByteArrayDecoder(SocketChannel sc, Executor executor) {
	this.sc = sc;
	this.executor = executor;
    }

    @Override
    public void flush() {
	requireHeader();
    }

    /** List of Objects decoded and to handle in a Thread ASAP */
    List<Serializable> decoded = new ArrayList<Serializable>();

    /** List of Objects that are being handled by a thread */
    List<Serializable> decodingBackBuffer = new ArrayList<Serializable>();

    /**
     * the lock to prevent swapping {@link #decodingBackBuffer} and
     * {@link #decoded} while one of them is in use.
     */
    Object decodingListsLock = new Object();

    /** done by only one thread at a time (by the server's main thread) */
    @Override
    public void handle(byte[] array, int size) {
	synchronized (decoded) {
	    boolean empty = decoded.isEmpty();
	    decoded.addAll(decode(array, size - 1));
	    if (empty) {
		/**
		 * if the list is not empty, it means this is yet in the
		 * executor's queue of runnable
		 */
		executor.execute(this);
	    }
	}
    }

    @Override
    public void run() {
	synchronized (decodingListsLock) {

	    synchronized (decoded) {
		List<Serializable> ltmp = decoded;
		decoded = decodingBackBuffer;
		decodingBackBuffer = ltmp;
		decoded.clear();
	    }

	    for (Serializable s : decodingBackBuffer) {
		handleObject(s, sc);
	    }
	}
    }

    public abstract void handleObject(Serializable o, SocketChannel sc);

}
