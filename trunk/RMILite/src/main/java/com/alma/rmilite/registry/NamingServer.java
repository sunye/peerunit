package com.alma.rmilite.registry;

public interface NamingServer {
	
	/**
	 * We want the NamingServer to be instancieted only once, so we use
	 * singleton pattern
	 */
	public static final NamingServer instance = new NamingServer_Socket();
	
	public Registry createRegistry(int port) throws Exception;
	
	public Registry getRegistry(String host, int port) throws Exception;
}
