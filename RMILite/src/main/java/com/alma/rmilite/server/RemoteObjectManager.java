package com.alma.rmilite.server;

import java.rmi.Remote;

import com.alma.rmilite.io.RemoteProxy;

public interface RemoteObjectManager {

	public boolean isExported(Remote object);
	
	public int getPort(Remote object);

	public void remoteProcedureCall(int port, RemoteProxy remoteProxy);
}
