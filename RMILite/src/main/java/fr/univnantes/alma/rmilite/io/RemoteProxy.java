package fr.univnantes.alma.rmilite.io;

import java.io.IOException;
import java.io.Serializable;

import fr.univnantes.alma.rmilite.client.Stub;
import fr.univnantes.alma.rmilite.server.RemoteObjectManager;

/**
 * Allows to send and receive {@link Serializable} objects between proxys. A
 * proxy is a {@link Stub} or skeleton.
 * 
 * @see Stub
 * @see RemoteObjectManager
 */
public interface RemoteProxy {

    /**
     * Sends a {@link Serializable} object to an other remote proxy.
     * 
     * @param object
     *            - serializable object
     * @throws IOException
     */
    public void sendObject(Object object) throws IOException;

    /**
     * Receives a serializable object from an other remote proxy.
     * 
     * @return serializable object
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Object receiveObject() throws IOException, ClassNotFoundException;

    /**
     * Closes the connection with the remote proxy.
     * 
     * @throws IOException
     */
    public void close() throws IOException;

    /**
     * Returns the local port to which this remote proxy is bound.
     * 
     * @return the port
     */
    public int getLocalPort();
}
