package com.alma.rmilite.io;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.alma.rmilite.RemoteMethodFactory;
import com.alma.rmilite.server.RemoteObjectManager;

/**
 * Network Abstraction Layer<br/>
 * <br/>
 * Uses the {@link RemoteObjectManager} instance.
 * 
 * @see RemoteProxy
 * @see RemoteObjectManager
 */
public interface IOManager {

	/**
	 * Returns the specified RemoteProxy {@code reference}.
	 * 
	 * @param reference
	 *            - host and port on whiches the remote proxy accepts requests
	 * @return the remote proxy
	 * @throws IOException
	 */
	public RemoteProxy getRemoteProxy(InetSocketAddress reference)
			throws IOException;

	/**
	 * Opens the specified {@code port} on the local machine. A port of 0 opens
	 * a free port.<br/>
	 * <br/>
	 * When a new connection is accepted, the method {@code remoteProcedureCall}
	 * of the {@link RemoteObjectManager} instance is invoked.
	 * 
	 * @param port
	 *            - the specified port
	 * @return the open port, useful if the specified {@code port} was 0
	 * @throws IOException
	 */
	public int open(int port) throws IOException;

	/**
	 * Closes the specified {@code port};
	 * 
	 * @param port
	 *            - the specified port
	 * @throws IOException
	 */
	public void close(int port) throws IOException;

	/**
	 * Sets the {@link RemoteObjectManager}, it's the same instance as
	 * {@link RemoteMethodFactory};
	 * 
	 * @param rop
	 *            - the RemoteObjectManager
	 */
	public void setRemoteObjectManager(RemoteObjectManager rom);

	/**
	 * Gets the {@link RemoteObjectManager}.
	 * 
	 * @return the used RemoteObjectManager
	 */
	public RemoteObjectManager getRemoteObjectManager();
}
