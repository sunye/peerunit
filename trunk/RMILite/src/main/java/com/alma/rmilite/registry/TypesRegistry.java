package com.alma.rmilite.registry;

import java.rmi.Remote;

/**
 * Allows to obtain a type of a remote object.
 * 
 * @see RegistryStub
 */
public interface TypesRegistry extends Remote {

	/**
	 * Returns the type of the specified remote object.
	 * @param name
	 *            - the name for the remote object to look up
	 * @return the interface implemented by the remote object
	 * @throws Exception
	 */
	public Class<? extends Remote> getType(String object) throws Exception;
}
