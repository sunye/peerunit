package fr.univnantes.alma.rmilite.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import fr.univnantes.alma.rmilite.io.Manager;

public class RemoteObjectProvider_RMI implements RemoteObjectProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.univnantes.alma.rmilite.server.RemoteObjectProvider#exportObject(java.rmi.Remote
	 * , int)
	 */
	@Override
	public Remote exportObject(Remote object, int port) throws RemoteException {
		return UnicastRemoteObject.exportObject(object, port);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.univnantes.alma.rmilite.server.RemoteObjectProvider#unexportObject(java.rmi.
	 * Remote)
	 */
	@Override
	public boolean unexportObject(Remote object) throws RemoteException {
		return UnicastRemoteObject.unexportObject(object, true);
	}

	/* (non-Javadoc)
	 * @see fr.univnantes.alma.rmilite.server.RemoteObjectProvider#setIOManager(fr.univnantes.alma.rmilite.io.IOManager)
	 */
	@Override
	public void setIOManager(Manager ioManager) {}

	/* (non-Javadoc)
	 * @see fr.univnantes.alma.rmilite.server.RemoteObjectProvider#getIOManager()
	 */
	@Override
	public Manager getIOManager() {
		return null;
	}
}
