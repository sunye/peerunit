package com.alma.rmilite.io;

import java.io.IOException;

import com.alma.rmilite.client.Stub;
import com.alma.rmilite.server.RemoteObjectManager;

/**
 * Allows to send and receive serializable objects between proxys. A proxy is a
 * {@link Stub} or skeleton.
 * 
 * @see Strub
 * @see RemoteObjectManager
 */
public interface RemoteProxy {

	/**
	 * Sends a serializable object to the remote proxy.
	 * 
	 * @param object
	 *            - serializable object
	 * @throws IOException
	 */
	public void sendObject(Object object) throws IOException;

	/**
	 * Receives a serializable object from the remote proxy.
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
