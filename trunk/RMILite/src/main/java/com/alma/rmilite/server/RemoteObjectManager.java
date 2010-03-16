package com.alma.rmilite.server;

import java.rmi.Remote;

import com.alma.rmilite.RemoteMethodFactory;
import com.alma.rmilite.UnexportedException;
import com.alma.rmilite.ioLayer.Manager;
import com.alma.rmilite.ioLayer.RemoteProxy;

/**
 * The RemoteObjectManager instance provides methods for
 * {@link RemoteMethodFactory} and {@link Manager} objects.
 * 
 * @see Manager
 * @see RemoteMethodFactory
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
	public int getPort(Remote object) throws UnexportedException;

	/**
	 * Links a remote stub with a remote object (identified the port of the
	 * {@code remoteProxy}).
	 * @param remoteProxy
	 *            - Calls from the remote stub
	 * 
	 * @see RemoteProxy
	 */
	public void remoteProcedureCall(RemoteProxy remoteProxy);
}
