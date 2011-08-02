package fr.univnantes.alma.nio;

import java.io.Serializable;
import java.nio.channels.SocketChannel;

/**
 * A writer can send {@link Serializable} data to a distant server, through a
 * mean as a {@link SocketChannel}
 * 
 * @author Guillaume Le Louët
 */
public interface Writer {

    /**
     * Send an object to the server
     * 
     * @param s
     *            the {@link Serializable} to send
     * @return true if s was sent correctly
     */
    public boolean send(Serializable s);

    /**
     * close the socket associated. No more data can then be sent/received
     */
    public void close();

}
