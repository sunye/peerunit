package fr.univnantes.alma.nio;

import java.io.Serializable;
import java.nio.channels.Channel;

/**
 * A Reader provides a way to directly received {@link Serializable} data sent
 * through a socket, or a {@link Channel}
 * 
 * @author Guillaume Le Louët
 * 
 */
public interface Reader {

    /**
     * wait for objects to be received on the socket
     * 
     * @return the first {@link Serializable} to be received, or null if the
     *         connection is stopped (by the server, the network, or this)
     */
    public Serializable receive();

    /**
     * Same as {@link #receive()} , except after a timeout the method returns.
     * 
     * @param timeoutMilli
     *            the max time to wait, in milliseconds
     * @return same as {@link #receive()}
     */
    public Serializable receive(int timeoutMilli);

    /**
     * close the socket associated. No more data can then be sent/received
     */
    public void close();

}
