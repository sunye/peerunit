package com.alma.rmilite.io;

import java.io.IOException;
import java.net.InetSocketAddress;

public interface IOManager {
	
	public static IOManager instance = new IOManager_IO();

	public RemoteProxy getRemoteProxy(InetSocketAddress reference) throws IOException;
	
	public int open(int port) throws IOException;
	
	public void close(int port) throws IOException;
}
