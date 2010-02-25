package com.alma.rmilite.server;

import java.rmi.Remote;

import com.alma.rmilite.io.RemoteProxy;

/**
 * A RemoteObjectManager provides methods for {@link RemoteMethod} and
 * {@link RemoteMethodResult} objects.
 * 
 * @see RemoteMethod
 * @see RemoteMethodResult
 */
public interface RemoteObjectManager {

	/**
	 * Determines if the remote {@code object} is exported.
	 * 
	 * @param object
	 *            - the remote object
	 * @return true if the remote object is exported, false otherwise
	 */
	public boolean isExported(Remote object);

	/**
	 * Returns the port on which the remote {@code object} receives incoming
	 * calls.
	 * 
	 * @param object
	 *            - the remote object
	 * @return the port
	 */
	public int getPort(Remote object);

	/**
	 * Executes a call {@code remoteProxy} on the specified {@code port}
	 * 
	 * @param port
	 *            - the port to export the remote object on
	 * @param remoteProxy
	 *            - the call
	 */
	public void remoteProcedureCall(int port, RemoteProxy remoteProxy);
}
