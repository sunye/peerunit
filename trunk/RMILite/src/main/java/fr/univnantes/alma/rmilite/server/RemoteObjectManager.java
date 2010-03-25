package fr.univnantes.alma.rmilite.server;

import java.rmi.Remote;

import fr.univnantes.alma.rmilite.RemoteMethodFactory;
import fr.univnantes.alma.rmilite.UnexportedException;
import fr.univnantes.alma.rmilite.io.IOManager;
import fr.univnantes.alma.rmilite.io.RemoteProxy;

/**
 * The RemoteObjectManager instance provides methods for
 * {@link RemoteMethodFactory} and {@link IOManager} objects.
 * 
 * @see IOManager
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
