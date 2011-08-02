package fr.univnantes.alma.nio.server;

/**
 * interface of items that are supposed to handles byte arrays incoming by a
 * socket
 * 
 * @author Guillaume Le Louët
 * 
 */
public interface ByteArrayHandler {

    /**
     * Handles arrays of bytes incoming<br />
     * This may memorize it for further use, decode it now or later, delegate
     * it, etc.<br />
     * this method is the heart of the decoding procedure<br />
     * 
     * @param array
     *            the array that has been received by the socket. It may be
     *            modified later, other data may be received later
     * @param size
     *            the number of bytes that have meaning in the array
     */
    public void handle(byte[] array, int size);

    /**
     * declares to this that no more data will be sent. the socket will be close
     * after this method returns, if it is not yet
     */
    public void flush();

}
