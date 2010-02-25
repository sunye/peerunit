package com.alma.rmilite.registry;

/**
 * NamingServer is used to obtain a reference to a bootstrap remote object
 * {@link Registry} on a particular host (including the local host), or to create a
 * remote object registry that accepts calls on a specific port.
 * 
 * @see Registry
 */
public interface NamingServer {

	/**
	 * We want the NamingServer to be instantiated only once, so it uses
	 * singleton pattern.
	 */
	public static final NamingServer instance = new NamingServer_Socket();

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
}
