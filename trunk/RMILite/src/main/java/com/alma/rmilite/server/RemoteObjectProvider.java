package com.alma.rmilite.server;

import java.rmi.Remote;

/**
 * The RemoteObjectProvider instance defines a non-replicated remote object whose
 * references are valid only while the server process is alive. The
 * RemoteObjectProvider instance provides support for point-to-point active object
 * references (invocations, parameters, and results) using TCP streams.<br/>
 * <br/>
 * The implementation class of a remote object must then assume the
 * responsibility for the correct semantics of the hashCode, equals, and
 * toString methods inherited from the Object class, so that they behave
 * appropriately for remote objects.
 */
public interface RemoteObjectProvider {

	/**
	 * We want the RemoteObjectProvider to be instantiated only once, so it uses
	 * singleton pattern.
	 */
	public static final RemoteObjectProvider instance = new RemoteObjectProvider_Socket();

	/**
	 * Exports the remote {@code object} to make it available to receive
	 * incoming calls, using the particular supplied {@code port}.
	 * 
	 * @param object
	 *            - the remote object to be exported
	 * @param port
	 *            - the port to export the object on, or 0 to use any free port.
	 * @return the remote object
	 * @throws Exception
	 */
	public Remote exportObject(Remote object, int port) throws Exception;

	/**
	 * Removes the remote {@code object}. If successful, the object can no
	 * longer accept incoming calls.<br/>
	 * <br/>
	 * The object is forcibly unexported even if there are pending calls to the
	 * remote object or the remote object still has calls in progress.
	 * 
	 * @param object
	 *            - the remote object to be unexported
	 * @return true if operation is successful, false otherwise
	 * @throws Exception
	 */
	public boolean unexportObject(Remote object) throws Exception;
}
