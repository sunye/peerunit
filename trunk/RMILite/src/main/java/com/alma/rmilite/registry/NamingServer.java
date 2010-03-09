package com.alma.rmilite.registry;

import com.alma.rmilite.server.RemoteObjectProvider;

/**
 * The NamingServer instance is used to obtain a reference to a bootstrap remote object
 * {@link Registry} on a particular host (including the local host), or to create a
 * remote object registry that accepts calls on a specific port.
 * 
 * @see Registry
 */
public interface NamingServer {

	/**
	 * Creates and exports a {@code Registry} on the local host that accepts requests on the specified port.
	 * @param port - the port on which the registry accepts requests
	 * @return the registry
	 * @throws Exception
	 */
	public Registry createRegistry(int port) throws Exception;

	/**
	 * Returns a reference to the remote object {@code Registry} on the specified {@code host} and {@code port}.
	 * @param host - host for the remote registry
	 * @param port - port on which the registry accepts requests
	 * @return reference (a stub) to the remote object registry
	 * @throws Exception
	 */
	public Registry getRegistry(String host, int port) throws Exception;
	
	/**
	 * Sets the {@link RemoteObjectProvider}.
	 * @param rop - the RemoteObjectProvider.
	 */
	public void setRemoteObjectProvider(RemoteObjectProvider rop);
	
	/**
	 * Gets the {@link RemoteObjectProvider}.
	 * @return the used RemoteObjectProvider
	 */
	public RemoteObjectProvider getRemoteObjectProvider();
}
