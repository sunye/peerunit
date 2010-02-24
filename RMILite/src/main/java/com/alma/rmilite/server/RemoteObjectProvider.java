package com.alma.rmilite.server;

import java.rmi.Remote;

public interface  RemoteObjectProvider {

	public static final RemoteObjectProvider instance = new RemoteObjectProvider_Socket();
	
	public Remote exportObject(Remote object) throws Exception;
	
	public Remote exportObject(Remote object, int port) throws Exception;
	
	public boolean unexportObject(Remote object) throws Exception;
}
