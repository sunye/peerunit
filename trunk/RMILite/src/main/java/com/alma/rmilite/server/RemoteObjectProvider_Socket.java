package com.alma.rmilite.server;

import java.io.IOException;
import java.rmi.Remote;
import java.util.HashMap;
import java.util.Map;

import com.alma.rmilite.RemoteMethod;
import com.alma.rmilite.io.IOManager;
import com.alma.rmilite.io.RemoteProxy;

public class RemoteObjectProvider_Socket implements RemoteObjectProvider, RemoteObjectManager {
	
	private Map<Integer, Remote> port2object;
	private Map<Remote, Integer> object2port;

	public RemoteObjectProvider_Socket() {
		this.port2object = new HashMap<Integer, Remote>();
		this.object2port = new HashMap<Remote, Integer>();
	}
	
	@Override
	public Remote exportObject(Remote object, int port) throws IOException {
		int realPort = IOManager.instance.open(port);
		this.port2object.put(realPort, object);
		this.object2port.put(object, realPort);
		return object;
	}

	@Override
	public boolean unexportObject(Remote object) throws IOException {
		int port = this.object2port.remove(object);
		IOManager.instance.close(port);
		this.port2object.remove(port);
		return true;
	}

	@Override
	public int getPort(Remote object) {
		return this.object2port.get(object);
	}

	@Override
	public boolean isExported(Remote object) {
		return this.object2port.containsKey(object);
	}

	@Override
	public void remoteProcedureCall(int port, RemoteProxy remoteProxy) {
		try {
			Object result;
			try {
				RemoteMethod remoteMethod = (RemoteMethod) remoteProxy.recieveObject();
				result = this.execute(port, remoteMethod);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				result = e;
			}
			
			remoteProxy.sendObject(result);

			remoteProxy.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}
	
	
	synchronized private Object execute(int port, RemoteMethod remoteMethod) {
		try {
			return remoteMethod.invoke(this.port2object.get(port));
		} catch (Exception e) {
			e.printStackTrace();
			return e;
		}
	}
}
