package fr.univnantes.alma.rmilite.registry;

import java.rmi.Remote;

/**
 * Registry is a remote interface to a simple remote object registry that
 * provides methods for storing and retrieving remote object references bound
 * with arbitrary string names. The {@code bind} and {@code unbind} methods are used to
 * alter the name bindings in the registry, and the {@code lookup} method is
 * used to query the current name bindings.<br/>
 * <br/>
 * In its typical usage, a Registry enables client bootstrapping: it
 * provides a simple means for a client to obtain an initial reference to a
 * remote object.
 * 
 * @see NamingServer
 */
public interface Registry extends Remote {

	/**
	 * Replaces the binding for the specified {@code name} in this registry with the
	 * supplied remote {@code object}. If there is an existing binding for
	 * the specified name, it is discarded.
	 * 
	 * @param name
	 *            - the name to associate with the remote object
	 * @param object
	 *            - a remote object
	 * @throws Exception
	 */
	public void bind(String name, Remote object)
			throws Exception;

	/**
	 * Removes the binding for the specified {@code name} in this registry.
	 * 
	 * @param name
	 *            - the name of the binding to remove
	 * @throws Exception
	 */
	public void unbind(String name) throws Exception;

	/**
	 * Returns the remote object bound to the specified {@code name} in this registry.
	 * 
	 * @param name
	 *            - the name for the remote object to look up
	 * @return a reference to a remote object
	 * @throws Exception
	 */
	public Remote lookup(String name) throws Exception;
}
