package com.alma.rmilite.io;

import java.io.IOException;

public interface RemoteProxy {

	public void sendObject(Object object) throws IOException;

	public Object recieveObject() throws IOException, ClassNotFoundException;
	
	public void close() throws IOException;
}
