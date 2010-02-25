package com.alma.rmilite.server;

import java.io.IOException;
import java.rmi.Remote;
import java.util.HashMap;
import java.util.Map;

import com.alma.rmilite.RemoteMethod;
import com.alma.rmilite.io.IOManager;
import com.alma.rmilite.io.RemoteProxy;

public class RemoteObjectProvider_Socket implements RemoteObjectProvider,
		RemoteObjectManager {

	private Map<Integer, Remote> port2object;
	private Map<Remote, Integer> object2port;

	public RemoteObjectProvider_Socket() {
		this.port2object = new HashMap<Integer, Remote>();
		this.object2port = new HashMap<Remote, Integer>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alma.rmilite.server.RemoteObjectProvider#exportObject(java.rmi.Remote
	 * , int)
	 */
	@Override
	public Remote exportObject(Remote object, int port) throws IOException {
		int realPort = IOManager.instance.open(port);
		this.port2object.put(realPort, object);
		this.object2port.put(object, realPort);
		return object;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alma.rmilite.server.RemoteObjectProvider#unexportObject(java.rmi.
	 * Remote)
	 */
	@Override
	public boolean unexportObject(Remote object) throws IOException {
		int port = this.object2port.remove(object);
		IOManager.instance.close(port);
		this.port2object.remove(port);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alma.rmilite.server.RemoteObjectManager#getPort(java.rmi.Remote)
	 */
	@Override
	public int getPort(Remote object) {
		return this.object2port.get(object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alma.rmilite.server.RemoteObjectManager#isExported(java.rmi.Remote)
	 */
	@Override
	public boolean isExported(Remote object) {
		return this.object2port.containsKey(object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alma.rmilite.server.RemoteObjectManager#remoteProcedureCall(int,
	 * com.alma.rmilite.io.RemoteProxy)
	 */
	@Override
	public void remoteProcedureCall(int port, RemoteProxy remoteProxy) {
		try {
			Object result;
			try {
				/* Receive the distant call from the stub */
				RemoteMethod remoteMethod = (RemoteMethod) remoteProxy
						.recieveObject();

				/* Execute the call */
				result = this.execute(port, remoteMethod);
			} catch (ClassNotFoundException e) { // Unknown remote method
				e.printStackTrace();
				result = e;
			}

			/* Send the result of the method/call */
			remoteProxy.sendObject(result);

			remoteProxy.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	/**
	 * This method is {@code synchronized} for preventing concurrent accesses to
	 * the remote object.
	 * 
	 * @param port
	 *            - the port to export the remote object on
	 * @param remoteMethod
	 *            - the distant call, the invoked method
	 * @return the result of the method/call
	 */
	synchronized private Object execute(int port, RemoteMethod remoteMethod) {
		try {
			return remoteMethod.invoke(this.port2object.get(port));
		} catch (Exception e) { // an exception occurs during the
								// execution of the method
			e.printStackTrace();
			return e;
		}
	}
}
